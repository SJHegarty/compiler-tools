package localgoat.image;

import localgoat.util.NYI;

import java.util.Random;
import java.util.stream.IntStream;

public class Colour{

	public static final int C_SCOPE = -256;
	public static final int C_BITS = 8;
	public static final int C_MAX = 255;
	public static final int A_REMOVE = 16777215;
	public static final int R_REMOVE = -16711681;
	public static final int G_REMOVE = -65281;
	public static final int B_REMOVE = -256;
	public static final int A_RETAIN = -16777216;
	public static final int R_RETAIN = 16711680;
	public static final int G_RETAIN = 65280;
	public static final int B_RETAIN = 255;
	public static final int C_RETAIN = 255;
	public static final int R_SHIFT = 16;
	public static final int G_SHIFT = 8;
	public static final int A_SHIFT = 24;
	public static final int R_ROUND = 16777215;
	public static final int G_ROUND = 65535;
	public static final int B_ROUND = 255;
	public static final int C_ROUND = 255;
	public static final int M_ROUND = 8388608;
	public static final int BLACK = -16777216;
	public static final int DARKGREY = -12566464;
	public static final int GREY = -8355712;
	public static final int LIGHTGREY = -4144960;
	public static final int WHITE = -1;
	public static final int ALPHA = 0;
	public static final int RED = -65536;
	public static final int GREEN = -16711936;
	public static final int BLUE = -16776961;
	public static final int YELLOW = -256;
	public static final int PURPLE = -65281;
	public static final int AQUA = -16711681;
	public static final int ORANGE = -32768;

	public static int[] split(int argb){
		return IntStream.range(0, 4)
			.map(i -> 0xff & (argb >> (0x18 - (i << 3))))
			.toArray();
	}

	public static int composite(int... colour){
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

	private static final Random random = new Random();


	public static int toInt(int r, int g, int b, int a){
		return a << 24 |
			r << 16 |
			g << 8 |
			b;
	}


	public static int toInt(int r, int g, int b){
		return toInt(r, g, b, 255);
	}


	public static int withAlpha(int colour, int a){
		return colour & 0xFFFFFF | a << 24;
	}


	public static int withRed(int colour, int r){
		return colour & 0xFF00FFFF | r << 16;
	}


	public static int withGreen(int colour, int g){
		return colour & 0xFFFF00FF | g << 8;
	}


	public static int withBlue(int colour, int b){
		return colour & 0xFFFFFF00 | b;
	}


	public static int contrastInvert(int c){
		return c & 0xFF000000 | (c & 0xFF00FF) + 8388736 & 0xFF00FF | (c & 0xFF00) + 32768 & 0xFF00;
	}


	public static int invert(int colour){
		return (colour ^ 0xFFFFFFFF) & 0xFFFFFF | colour & 0xFF000000;
	}


	public static final long time(){
		return System.currentTimeMillis();
	}


	public static void time(long time){
		System.out.println(time() - time);
	}

	public static void print(int... colours){
		byte b;
		int i;
		int[] arrayOfInt;
		for(i = (arrayOfInt = colours).length, b = 0; b < i; ){
			int c = arrayOfInt[b];
			System.out.println(String.valueOf(Integer.toHexString(c)) + "\t");
			b++;
		}
		System.out.println();
	}


	public static int merge(int cu, int cm, int cl){
		return merge(cu, merge(cm, cl));
	}

	public static int merge(int... colours){
		int depth = colours.length - 1;
		while(depth < colours.length - 1 && (colours[depth] & 0xFF000000) != -16777216){
			depth++;
		}
		int rv = 0;
		while(depth >= 0){
			rv = merge(colours[depth--], rv);
		}
		return rv;
	}


	public static int merge(int f, int b){
		int af = f >>> 24;
		if(af == 0){
			return b;
		}
		if(af == 255){
			return f;
		}
		int ab = b >>> 24;
		if(ab == 0){
			return f;
		}
		if(ab == 255){
			int rf = f & 0xFF0000;
			int rb = b & 0xFF0000;
			int gf = f & 0xFF00;
			int gb = b & 0xFF00;
			int bf = f & 0xFF;
			int bb = b & 0xFF;
			int ai = 255 - af;
			int rc = rf * af + rb * ai + 16777215 >> 8 & 0xFF0000;
			int gc = gf * af + gb * ai + 65535 >> 8 & 0xFF00;
			int bc = bf * af + bb * ai + 255 >> 8 & 0xFF;
			return 0xFF000000 | rc | gc | bc;
		}

		int fm = f & 0xFFFFFF;
		if(fm == (b & 0xFFFFFF)){
			int abm = (255 - af) * ab + 255 >> 8;
			return abm + af << 24 | fm;
		}

		int rT = f >> 16 & 0xFF;
		int rB = b >> 16 & 0xFF;
		int gT = f >> 8 & 0xFF;
		int gB = b >> 8 & 0xFF;
		int bT = f & 0xFF;
		int bB = b & 0xFF;
		int dT = (af << 8) - af;
		int dB = (255 - af) * ab;
		int dA = dT + dB;
		int alpha = (dA >> 8) + 1;

		int offset = dA >> 1;
		int dM = (16777216 + offset) / dA;
		int red = (rT * dT + rB * dB) * dM + 8388608 >>> 24;
		int green = (gT * dT + gB * dB) * dM + 8388608 >>> 24;
		int blue = (bT * dT + bB * dB) * dM + 8388608 >>> 24;
		return alpha << 24 | red << 16 | green << 8 | blue;
	}


	public static int random(){
		int rv = random.nextInt();
		if((rv & 0xFF000000) == 0){
			return 0;
		}
		return rv;
	}


	public static int random(int alpha){
		if(alpha == 0){
			return 0;
		}
		return alpha << 24 & random.nextInt() & 0xFFFFFF;
	}


	public static int getAlpha(int colour){
		return colour >>> 24;
	}


	public static int getRed(int colour){
		return colour >> 16 & 0xFF;
	}


	public static int getGreen(int colour){
		return colour >> 8 & 0xFF;
	}


	public static int getBlue(int colour){
		return colour & 0xFF;
	}


	public static int getR(int colour){
		return colour >> 16 & 0xFF;
	}


	public static int getG(int colour){
		return colour >> 8 & 0xFF;
	}


	public static int getB(int colour){
		return colour & 0xFF;
	}


	public static int getA(int colour){
		return colour >>> 24;
	}


}
