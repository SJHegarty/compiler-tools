package localgoat.util.functional;

@FunctionalInterface
public interface CharSupplier{
	public static void main(String...args){
		System.err.println((int)(char)-1);
	}
	char getAsChar();
}
