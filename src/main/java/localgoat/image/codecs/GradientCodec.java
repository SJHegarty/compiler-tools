package localgoat.image.codecs;

import localgoat.image.Colour;
import localgoat.image.Image;
import localgoat.image.ImageCodec;
import localgoat.image.ImageData;
import localgoat.image.images.GradientImage;
import localgoat.util.IntReader;
import localgoat.util.IntWriter;
import localgoat.util.NYI;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Component
public class GradientCodec implements ImageCodec<GradientImage>{
	@Override
	public int type(){
		return 1;
	}

	@Override
	public GradientImage decode(ImageData data){
		try(InputStream in = data.dataStream()){
			final var reader = new IntReader(in, 4);
			return new GradientImage(
				data.width(),
				data.height(),
				reader.getAsInt(),
				reader.getAsInt(),
				reader.getAsInt(),
				reader.getAsInt()
			);
		}
		catch(IOException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public GradientImage convert(Image image){
		throw new NYI();
	}

	@Override
	public ImageData encode(GradientImage image){
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()){
			final var writer = new IntWriter(out, 4);
			writer.accept(image.c00());
			writer.accept(image.c01());
			writer.accept(image.c11());
			writer.accept(image.c10());
			return new ImageData(
				type(),
				image.width(),
				image.height(),
				out.toByteArray()
			);
		}
		catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}
}
