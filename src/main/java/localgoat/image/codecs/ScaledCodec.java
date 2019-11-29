package localgoat.image.codecs;

import localgoat.image.Image;
import localgoat.image.ImageCodec;
import localgoat.image.ImageCodecs;
import localgoat.image.ImageData;
import localgoat.image.images.ScaledImage;
import localgoat.util.NYI;

public class ScaledCodec implements ImageCodec<ScaledImage>{
	@Override
	public int type(){
		return 2;
	}

	@Override
	public ScaledImage decode(ImageData data){
		if(data.type() != type()){
			throw new IllegalArgumentException();
		}
		final int width = data.width();
		final int height = data.height();
		final Image wrapped = ImageCodecs.INSTANCE.decode(data.dataStream());
		if(((width % wrapped.width()) | (height % wrapped.height())) != 0){
			throw new UnsupportedOperationException();
		}
		return new ScaledImage(wrapped, width/wrapped.width(), height/wrapped.height());
	}

	@Override
	public ScaledImage convert(Image image){
		throw new NYI();
	}

	@Override
	public ImageData encode(ScaledImage image){
		return null;
	}
}
