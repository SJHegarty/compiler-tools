package localgoat.utilities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class BitWriter{
	private int writeBits;
	private int writeBuffer;
	private final OutputStream os;

	public BitWriter(OutputStream os){
		this.os = os;
	}


	public BitWriter(String file){
		try{
			this.os = new FileOutputStream(file);
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}


	public void close(){
		push();
		try{
			this.os.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}


	public void write(int data, int bits){
		if(bits + this.writeBits > 32){
			int rbits = bits + this.writeBits - 32;
			write(data >>> rbits, bits - rbits);
			data &= (1 << rbits) - 1;
			bits = rbits;
		}
		if(bits == 32){
			this.writeBuffer = data;
			this.writeBits = bits;
			writePurge();
			return;
		}
		this.writeBuffer <<= bits;
		this.writeBuffer |= data & (1 << bits) - 1;
		this.writeBits += bits;
		writePurge();
	}


	public void write(int data){
		write(data, 32);
	}


	public void write(boolean b){
		if(b){
			write(1, 1);
			return;
		}
		write(0, 1);
	}


	private void writePurge(){
		while(this.writeBits >= 8){
			int write = this.writeBuffer >>> this.writeBits - 8;
			try{
				this.os.write(write);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			this.writeBits -= 8;
		}
	}


	public void push(){
		writePurge();
		if(this.writeBits == 0){
			return;
		}
		int write = this.writeBuffer << 8 - this.writeBits;
		try{
			this.os.write(write);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		this.writeBits = 0;
	}


	public void write(String s){
		for(int i = 0; i < s.length(); ){
			write(s.charAt(i), 8);
			i++;
		}
		write(0, 8);
	}


	public void writeJava(String s){
		for(int i = 0; i < s.length(); ){
			write(s.charAt(i), 16);
			i++;
		}
		write(0, 16);
	}


	public void writeConst(String s){
		for(int i = 0; i < s.length(); ){
			write(s.charAt(i), 8);
			i++;
		}
	}


	public void writeConstJava(String s){
		for(int i = 0; i < s.length(); ){
			write(s.charAt(i), 16);
			i++;
		}
	}

	public void write(int[] vals, int bits){
		write(vals.length, 16);
		write(bits, 8);
		byte b;
		int j, arrayOfInt[];
		for(j = (arrayOfInt = vals).length, b = 0; b < j; ){
			int i = arrayOfInt[b];
			write(i, bits);
			b++;
		}

	}
}





