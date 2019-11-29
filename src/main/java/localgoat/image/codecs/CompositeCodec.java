package localgoat.image.codecs;

import localgoat.image.Image;
import localgoat.image.ImageCodec;
import localgoat.image.ImageData;
import localgoat.image.images.CompositeImage;
import org.springframework.context.annotation.ComponentScan;

import java.io.InputStream;


public class CompositeCodec implements ImageCodec<CompositeImage>{


	@Override
	public int type(){
		return 0;
	}

	@Override
	public CompositeImage decode(ImageData data){
		return null;
	}

	@Override
	public CompositeImage convert(Image image){
		return null;
	}

	@Override
	public ImageData encode(CompositeImage image){
		return null;
	}
}
