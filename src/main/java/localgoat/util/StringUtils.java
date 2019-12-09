package localgoat.util;


import localgoat.util.streaming.ESupplier;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class StringUtils{

	public static String repeating(char c, int count){
		final char chars[] = new char[count];
		Arrays.fill(chars, c);
		return new String(chars);
	}

	public static void main(String...args){
		for(int i = 0; i < 256; i++){
			byte b = (byte)i;
			System.err.println(toHex(b) + " " + toBinary(b));
		}
	}

	public static String percentage(float value){
		return .01f * Math.round(10000 * value) + "%";
	}

	private static final class Formatter{
		private final String head;
		private final Function<Long, String> function;
		private final IntUnaryOperator lengths;

		Formatter(String head, Function<Long, String> function, IntUnaryOperator lengths){
			this.head = head;
			this.function = function;
			this.lengths = lengths;
		}
	}

	private static final Formatter HEX_STRING = new Formatter(
		"0x",
		l -> Long.toHexString(l),
		size -> size
	);

	private static final Formatter BINARY_STRING = new Formatter(
		"0b",
		l -> Long.toBinaryString(l),
		size -> size << 2
	);


	private static String format(Formatter formatter, long l, int length){
		final var result = formatter.function.apply(l).toUpperCase();
		final int remaining = formatter.lengths.applyAsInt(length) - result.length();
		return formatter.head + repeating('0', remaining) + result;
	}

	private static String formatByte(Formatter formatter, byte b){
		return format(formatter, b & 0xffL, 2);
	}

	public static String toHex(byte b){
		return formatByte(HEX_STRING, b);
	}

	public static String toBinary(byte b){
		return formatByte(BINARY_STRING, b);
	}

	private static String formatInt(Formatter formatter, int i){
		return format(formatter, i & 0xffffffffL, 8);
	}

	public static String toHex(int i){
		return formatInt(HEX_STRING, i);
	}

	public static String toBinary(int i){
		return formatInt(BINARY_STRING, i);
	}

	private static String formatLong(Formatter formatter, long l){
		return format(formatter, l, 16);
	}

	public static String toHex(long l){
		return formatLong(HEX_STRING, l);
	}

	public static String toBinary(long l){
		return formatLong(BINARY_STRING, l);
	}

	public static String toString(IntFunction<String> function, int...values){
		return "[" + ESupplier.from(values).map(i -> function.apply(i)).interleave(", ").concatenate() + "]";
}
	public static String toString(int...values){
		return toString(Integer::toString, values);
	}
}
