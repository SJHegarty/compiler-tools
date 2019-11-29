package localgoat.image;

import localgoat.util.IntReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.IntSupplier;

public class ImageData{
	private final int type;
	private final int width;
	private final int height;
	private final byte[] data;

	public ImageData(InputStream stream) throws IOException{
		final var reader = new IntReader(stream, 2);
		try{
			this.type = reader.getAsInt();
			this.width = reader.getAsInt();
			this.height = reader.getAsInt();
		}
		catch(UncheckedIOException e){
			throw e.getCause();
		}
		this.data = stream.readAllBytes();
	}

	public final int type(){
		return type;
	}


}
