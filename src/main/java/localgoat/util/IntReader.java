package localgoat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.IntSupplier;

public final class IntReader implements IntSupplier{
	private final InputStream stream;
	private final int readLength;

	public IntReader(InputStream stream, int readLength){
		if(readLength == 0 || readLength > 4){
			throw new IllegalArgumentException("Bytes read must fit into i32.");
		}
		this.stream = stream;
		this.readLength = readLength;
	}

	@Override
	public int getAsInt(){
		try{
			int rv = 0;
			for(int i = 0; i < readLength; i++){
				final int value = stream.read();
				if(value == -1){
					throw new IOException("End of stream.");
				}
				rv = (rv << 8) | value;
			}
			return rv;
		}
		catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}
}
