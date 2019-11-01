package localgoat.util;

import java.util.Arrays;

public class StringUtils{

	public static String repeating(char c, int count){
		final char chars[] = new char[count];
		Arrays.fill(chars, c);
		return new String(chars);
	}
}
