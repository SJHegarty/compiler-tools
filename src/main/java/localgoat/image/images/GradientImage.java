package localgoat.image.images;

import localgoat.image.Image;
import localgoat.image.codec.codecs.BrushstrokesCodec;
import localgoat.image.old.graphics2d.Gradient;
import localgoat.neural.data.*;
import localgoat.neural.learning.BackpropagationLearningAlgorithm;
import localgoat.neural.learning.LearningUtils;
import localgoat.neural.learning.MirroredLearningAlgorithm;
import localgoat.neural.mnist.*;
import localgoat.neural.structure.api.ConnectionSet;
import localgoat.neural.structure.api.Network;
import localgoat.neural.structure.api.ReversibleNetwork;
import localgoat.neural.ui.ActivityPanel;
import localgoat.neural.ui.DataPanel;
import localgoat.neural.ui.MirroredPanel;
import localgoat.util.*;
import localgoat.util.collections.MaskingList;
import localgoat.util.functional.LambdaUtils;
import localgoat.util.streaming.ESupplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import java.awt.Container;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import static localgoat.util.MathUtils.*;
public class GradientImage implements Image{

	public static void main(String...args){

		final var network = new MnistNetwork();

		final int maxBits = 0x0c;
		class TrainingSeries{
			private List<NodesData<?>> data = new ArrayList<>();

			public TrainingSeries(ImageData data){
				this.data.add(network.build(data.decoded()));
			}

			public void expand(){
				final int index = data.size() - 1;
				var nw = network.layer(index).asNetwork();
				var processed = ObjectUtils.lastFrom(nw.processForLearning(data.get(index)));
				data.add(processed);
			}

			public NodesData<?> data(int layer){
				return data.get(layer);
			}
		}
		final List<ImageData> training = LambdaUtils.get(
			() -> {
				final var loaded = LambdaUtils.get(
					() -> {
						var list = new MnistLoader().data();
						return list.subList(
							0,
							Math.min(
								1 << maxBits,
								MathUtils.lastPowerOfTwo(list.size())
							)
						);
					}
				);
				final int generatedBits = MathUtils.bitCount(loaded.size() - 1);
				final var generated = new MnistGenerator(loaded.size()).data();
				return ObjectUtils.interleave(loaded, generated);
			}
		);
		final int sampleLimitBits = Math.max(maxBits, bitCount(training.size() - 1));
		final var built = training.stream().map(t -> new TrainingSeries(t)).collect(Collectors.toList());
		final NodesData<?> meanSample = LambdaUtils.get(
			() -> {
				var virtual = new MaskingList<>(built, 4096)
					.stream()
					.map(b -> (NodesData<Value>)b.data(0))
					.reduce((data0, data1) -> NodesData.combine(data0, data1, MathUtils::mean))
					.orElseThrow(IllegalStateException::new);

				var rv = new MutableNodesData(virtual.nodes());
				NodesData.copy(virtual, rv);
				return rv;
			}
		);

		final var codec = new BrushstrokesCodec();

		final float learningRateDecay = .001f;
		final float decayRate = 0.3f;
		final int layerCount = network.layerCount();
		final var frame = new JFrame();
		final var activity = new MirroredPanel(network);
		final var nulldata = new ActivityPanel(network);
		nulldata.add(Box.createHorizontalGlue());
		final var container = new Container();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(activity);
		container.add(nulldata);
		final Consumer<ReversibleNetwork<?>> expectedPopulator = nw -> {
			nulldata.setActivity(nw.processForLearning(meanSample));
		};
		new Thread(
			() -> {
				final int lb0 = 2;
				//final float baseTarget = .005f;
				final float maxError = .05f;

				final float baseRate = .005f;
				float lastMiss = 1f;
				float learningRate = baseRate;

				NodesData.copy(meanSample.filter(f -> -f), network.inputBiases());
				final IntConsumer initialiser = index -> {
					var subnetwork = network.subNetwork(0, index + 1);
					var layer = network.layer(index);
					for(var weight : layer.connections().map(c -> c.weight())){
						weight.randomise();
					}
					var data = ObjectUtils.lastFrom(
						subnetwork.processForLearning(meanSample)
					);
					NodesData.copy(
						data.filter(f -> -f),
						layer.biases()
					);
				};
				initialiser.accept(0);
				for(int layerIndex = 0;;){
					{
						float target = 0f;

						final var layerData = built.get(layerIndex);
						float lr = learningRate;// * MathUtils.exp(1f - decayRate, layer);
						final var learning = new MirroredLearningAlgorithm<>(new BackpropagationLearningAlgorithm(lr));

						final var renderingNetwork = (layerIndex == 0) ? null : network.subNetwork(0, layerIndex);
						final var reversedRendering = (renderingNetwork == null) ? null : renderingNetwork.reverse();
						final var subnetwork = network.subNetwork(layerIndex, layerIndex + 1);
						int renderIndex = 0;

						final int trainingBits = bitCount(training.size() - 1);
						limitLoop:
						for(int limitBits = lb0; limitBits <= trainingBits; limitBits++){
							learning.setAlgorithm(new BackpropagationLearningAlgorithm(lr));
							int index = 0;
							final int maxEpochs = 1 << (sampleLimitBits - limitBits);
							final int limit = 1 << limitBits;
							var data = new MaskingList<>(training, limit);
							final var rolling = new RollingMean(limit);
							float meanError = Float.NaN;
							epochLoop:
							for(int epoch = 0; ; ){
								for(int i = 0; i < limit; i++){
									final int fi = i;

									var imageData = data.get(fi);
									var image = imageData.decoded();
									var input = built.get(fi);

									final List<NodesData<?>> processed;
									final float error;
									{
										final var expected = imageData.encoded();
										processed = learning.learn(subnetwork, input.data(layerIndex));
										error = LearningUtils.sumSquaredError(
											NodesData.difference(
												processed.get(0),
												processed.get(processed.size() - 1)
											)
										);
										rolling.add(error);
									}
									if(epoch != 0){
										float meanErrorn = rolling.value();
										if(meanErrorn < target || isCloseEnough(meanErrorn, target, .00001f)){
											System.err.println(
												String.format(
													"[learning rate = %s, layer = %s, limit = %s] - Target error level (%s) reached in epoch %s.",
													lr,
													layerIndex,
													StringUtils.toHex(limit),
													StringUtils.percentage(target),
													epoch
												)
											);
											target = meanErrorn;
											break epochLoop;
										}
									}
									if((renderIndex++ % 547) == 0){
										frame.setTitle(
											String.format(
												"Learning rate: %s, Limit: %s, Epoch: %s/%s, Target: %s Error: %s, Rolling: %s, Log: %s",
												lr,
												limit,
												epoch, maxEpochs,
												target,
												error,
												rolling,
												Math.log(rolling.value() / meanError)
											)
										);
										final int size = processed.size();
										final int hsize = size >> 1;
										activity.setActivity(
											ObjectUtils.concatenate(
												Optional.ofNullable(renderingNetwork)
													.map(nw -> nw.processForLearning(input.data(0)))
													.orElseGet(() -> Collections.emptyList()),
												processed,
												ESupplier.from(reversedRendering)
													.flatMap(
														nw -> ESupplier.from(
															nw.processForLearning(
																ObjectUtils.lastFrom(processed)
															)
														)
													)
													.collect(Collectors.toList())
											)
										);
										expectedPopulator.accept(network.subNetwork(0, layerIndex + 1));
									}
									index++;
								}

								final float meanErrorn = rolling.value();

								if(!Float.isNaN(meanError) && !isCloseEnough(meanError, meanErrorn, .0000015f)){
									float delta = (meanError / meanErrorn) - 1f;
									if(delta > 1){
										delta *= .7f;
									}
									lr *= 1f + delta;
									learning.setAlgorithm(new BackpropagationLearningAlgorithm(lr));
								}
								meanError = meanErrorn;

								epoch++;
								if(epoch == maxEpochs){
									if(meanErrorn < lastMiss && !isCloseEnough(meanError, lastMiss, 0.001f) && meanErrorn > maxError){
										learningRate *= .9f;
										var l = network.layer(layerIndex);
										System.err.println(
											String.format(
												"[learning rate = %s, layer = %s, limit = %s] - Max error(%s) missed (%s), last = %s; retraining %s @ LR = %s",
												lr,
												layerIndex,
												StringUtils.toHex(limit),
												StringUtils.percentage(maxError),
												StringUtils.percentage(meanErrorn),
												StringUtils.percentage(lastMiss),
												network.layer(layerIndex),
												learningRate)
										);
										lr = learningRate;
										lastMiss = meanErrorn;
										//initialiser.accept(layer);
										limitBits--;
										continue limitLoop;
									}
									lastMiss = 1f;
									System.err.println(
										String.format(
											"[learning rate = %s, layer = %s, limit = %s] - Target error level (%s) missed (%s) - epoch limit (%s) reached.",
											lr,
											layerIndex,
											StringUtils.toHex(limit),
											StringUtils.percentage(target),
											StringUtils.percentage(rolling.value()),
											epoch
										)
									);
									target = Math.min(meanErrorn, maxError);
									break;
								}
							}
						}
						built.stream().parallel().forEach(series -> series.expand());
						learningRate = baseRate;
						lastMiss = 1f;
					}
					if(++layerIndex == layerCount){
						break;
					}
					initialiser.accept(layerIndex);
				}
				frame.setTitle("Render mode.");
				try{
					int index = 0;
					final var reverse = network.reverse();
					while(true){
						final List<NodesData<?>> forwardData = network.processForLearning(built.get(index).data(0));
						final List<NodesData<?>> reverseData = reverse.processForLearning(ObjectUtils.lastFrom(forwardData));

						activity.setActivity(ObjectUtils.concatenate(forwardData, reverseData));

						Thread.sleep(500);
						if(++index == training.size()){
							index = 0;
						}
					}
				}
				catch(InterruptedException e){
					throw new IllegalStateException(e);
				}
			}
		).start();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(container);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}
	private static final float ALPHA_MUL = 1/255f;
	private final int width;
	private final int height;

	private final int[][] colours;

	public GradientImage(int width, int height, int c00, int cw0, int cwh, int c0h){
		if((width|height) < 0){
			throw new IllegalStateException();
		}
		this.width = width;
		this.height = height;
		this.colours = Gradient.getGradient(c00, cw0, cwh, c0h, width, height);
	}

	@Override
	public int width(){
		return width;
	}

	@Override
	public int height(){
		return height;
	}

	public int c00(){
		return colours[0][0];
	}

	public int c01(){
		return colours[width - 1][0];
	}

	public int c11(){
		return colours[width - 1][height - 1];
	}

	public int c10(){
		return colours[0][height - 1];
	}

	@Override
	public int colourAt(int x, int y){
		return colours[x][y];
	}

}
