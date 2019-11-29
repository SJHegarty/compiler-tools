package localgoat.image;

import localgoat.util.NYI;

import java.util.stream.IntStream;

public class Colour{
	public static int[] split(int argb){
		return IntStream.range(0, 4)
			.map(i -> 0xff & (argb >> (0x18 - (i << 3))))
			.toArray();
	}

	public static int composite(int...colour){
		final int alpha = switch(colour.length){
			case 3 -> 0xff;
			case 4 -> colour[0];
			default -> throw new UnsupportedOperationException("Supports RGB and ARGB only.");
		};
		int rv = alpha << 0x18;
		for(int channel = 0; channel < 3; channel++){
			final int value = colour[colour.length - 1 - channel];
			final int shift = channel << 3;
			rv |= value << shift;
		}
		return rv;
	}
}
