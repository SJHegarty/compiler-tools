package localgoat.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class ImageCodecs{
	private static final ApplicationContext CONTEXT = new AnnotationConfigApplicationContext(ImageCodecs.class);
	public static final ImageCodecs INSTANCE = CONTEXT.getBean(ImageCodecs.class);

	private final Map<Integer, ImageCodec> codecs = new TreeMap<>();

	@Autowired
	private synchronized void registerCodec(ImageCodec codec){
		final int type = codec.type();
		final var current = codecs.get(codec.type());
		if(current != null){
			throw new IllegalStateException(
				String.format(
					"Conflicting codecs for type %s:\n\t%s\n\t",
					type,
					codec,
					current
				)
			);
		}
		codecs.put(codec.type(), codec);
	}

	public Image decode(InputStream stream){
		return decode(new ImageData(stream));
	}

	public Image decode(ImageData data){
		return codecs.get(data.type()).decode(data);
	}

	public static void main(String...args){
		for(var v: INSTANCE.codecs.values()){
			System.err.println(v);
		}
	}
}
