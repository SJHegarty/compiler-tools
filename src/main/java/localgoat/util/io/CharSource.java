package localgoat.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class CharSource implements AutoCloseable{

	public static final int STREAM_END = 0xffff;
	private final BufferedReader reader;

	public CharSource(String s){
		this.reader = new BufferedReader(new StringReader(s));
	}

	private void mark(int charCount){
		try{
			reader.mark(charCount);
		}
		catch(IOException e){
			throw new IllegalStateException(e);
		}
	}

	private void reset(){
		try{
			reader.reset();
		}
		catch(IOException e){
			throw new IllegalStateException(e);
		}
	}

	public char peek(){
		mark(1);
		final char rv = read();
		reset();
		return rv;
	}

	public char read(){
		try{
			return (char) reader.read();
		}
		catch(IOException e){
			throw new IllegalStateException(e);
		}
	}

	public char[] peek(int charCount){
		mark(charCount);
		final char[] rv = read(charCount);
		reset();
		return rv;
	}


	public void skip(int charCount){
		read(charCount);
	}

	public char[] read(int charCount){
		try{
			final char[] result = new char[charCount];
			final int read = reader.read(result);
			final char[] rv;

			if(read == charCount){
				rv = result;
			}
			else if(read == -1){
				rv = new char[0];
			}
			else{
				rv = new char[read];
				System.arraycopy(result, 0, rv, 0, read);
			}
			return rv;
		}
		catch(IOException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close(){
		try{
			reader.close();
		}
		catch(IOException e){
			throw new IllegalStateException();
		}
	}

}
