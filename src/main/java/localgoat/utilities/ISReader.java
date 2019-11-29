package localgoat.utilities;

import java.io.IOException;
import java.io.InputStream;

public class ISReader{
	private final InputStream inputstream;

	public ISReader(InputStream is){
		this.inputstream = is;
	}


	public String readline() throws IOException{
		return read('\n');
	}

	public String read(char delimiter) throws IOException{
		StringBuilder builder = new StringBuilder();
		while(true){
			char c = (char) this.inputstream.read();
			if(c == delimiter){
				return builder.toString();
			}
			builder.append(c);
		}
	}

	public String read(String delimiter) throws IOException{
		int length = delimiter.length();
		char[] chars = delimiter.toCharArray();
		int index = 0;
		StringBuilder builder = new StringBuilder();


		while(true){
			int i = this.inputstream.read();
			if((i & 0xFFFFFF00) != 0){
				throw new IOException("connection closed");
			}
			char c = (char) i;

			if(chars[index] == c){
				if(++index == length){
					return builder.toString();
				}
				continue;
			}

			for(int u = 0; u < index; u++){
				builder.append(chars[u]);
			}
			index = 0;
			builder.append(c);
		}
	}

	public String read(int length){
		StringBuilder builder = new StringBuilder();
		try{
			for(int i = 0; i < length; i++){
				int val = this.inputstream.read();
				if((val & 0xFFFFFF00) != 0){
					break;
				}
				builder.append((char) val);
			}

		}
		catch(IOException iOException){
		}
		return builder.toString();
	}

	public String readuntil(int remain){
		StringBuilder builder = new StringBuilder();
		try{
			while(this.inputstream.available() != remain){
				int val = this.inputstream.read();
				if((val & 0xFFFFFF00) != 0){
					break;
				}
				builder.append((char) val);
			}

		}
		catch(IOException iOException){
		}
		return builder.toString();
	}
}





