package localgoat.image;

import localgoat.util.IntReader;
import localgoat.util.IntWriter;

import java.io.*;
import java.util.function.IntSupplier;

public class ImageData{

	private final int type;
	private final int width;
	private final int height;
	private final byte[] data;

	public ImageData(int type, int width, int height, byte[] data){
		this.type = type;
		this.width = width;
		this.height = height;
		this.data = data.clone();
	}

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

	public void writeTo(OutputStream stream) throws IOException{
		final var writer = new IntWriter(stream, 2);
		try{
			writer.accept(type);
			writer.accept(width);
			writer.accept(height);
		}
		catch(UncheckedIOException e){
			throw e.getCause();
		}
		dataStream().transferTo(stream);
	}

	public final int type(){
		return type;
	}

	public final int width(){
		return width;
	}

	public final int height(){
		return height;
	}

	public byte[] data(){
		return data.clone();
	}

	public InputStream dataStream(){
		return new ByteArrayInputStream(data);
	}
}
