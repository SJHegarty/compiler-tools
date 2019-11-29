package localgoat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.IntConsumer;

public class IntWriter implements IntConsumer{
	private final OutputStream stream;
	private final int writeLength;

	public IntWriter(OutputStream stream, int writeLength){
		if(writeLength == 0 || writeLength > 4){
			throw new IllegalArgumentException("Bytes written must fit into i32.");
		}
		this.stream = stream;
		this.writeLength = writeLength;
	}

	@Override
	public void accept(int value){
		try{
			for(int shift = (writeLength - 1) << 3; shift >= 0; shift -= 8){
				stream.write(0xff & (value >> shift));
			}
		}
		catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}
}
