package localgoat.image.codecs;

import localgoat.image.Image;
import localgoat.image.ImageCodec;
import localgoat.image.ImageData;
import org.springframework.stereotype.Component;

@Component
public class GradientCodec implements ImageCodec{
	@Override
	public int type(){
		return 1;
	}

	@Override
	public Image decode(ImageData data){
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public ImageData encode(Image image){
		throw new UnsupportedOperationException("Not yet implemented.");
	}
}
