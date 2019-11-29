package localgoat.image.old.graphics2d;


import localgoat.image.Colour;

public class Gradient{
	public static int[] getGradient(int c0, int c1, int length){
		int[] c = new int[length];
		int rm = length - 1;
		for(int i = 0; i < length; i++){
			c[i] = getColour(c0, c1, rm - i, i);
		}
		return c;
	}


	public static int getColour(int c0, int c1){
		return getColour(c0, c1, 1, 1);
	}


	public static int getColour(int colour_one, int colour_two, int split_one, int split_two){
		int return_value = 0;

		int r_one = Colour.getR(colour_one);
		int g_one = Colour.getG(colour_one);
		int b_one = Colour.getB(colour_one);
		int a_one = Colour.getA(colour_one);
		int r_two = Colour.getR(colour_two);
		int g_two = Colour.getG(colour_two);
		int b_two = Colour.getB(colour_two);
		int a_two = Colour.getA(colour_two);

		int mult_one = split_one * a_one;
		int mult_two = split_two * a_two;
		int divi_rgb = mult_one + mult_two;
		int hdiv_rgb = divi_rgb >> 1;

		if(a_two == a_one){
			if(a_two == 0){
				return 0;
			}
			return_value |= a_two << 24;
			if(split_one == split_two){
				return return_value |
					r_one + r_two << 15 & 0xFF0000 |
					g_one + g_two << 7 & 0xFF00 |
					b_one + b_two >> 1;
			}
		}
		else{

			int divi_a = split_one + split_two;
			return_value |= (divi_rgb + (divi_a >> 1)) / divi_a << 24;
		}
		if(return_value == 0){
			return 0;
		}
		if(r_two == r_one){
			return_value |= r_two << 16;
		}
		else{
			return_value |= (r_one * mult_one + r_two * mult_two + hdiv_rgb) / divi_rgb << 16;
		}
		if(g_two == g_one){
			return_value |= g_two << 8;
		}
		else{
			return_value |= (g_one * mult_one + g_two * mult_two + hdiv_rgb) / divi_rgb << 8;
		}
		if(b_two == b_one){
			return_value |= b_two;
		}
		else{
			return_value |= (b_one * mult_one + b_two * mult_two + hdiv_rgb) / divi_rgb;
		}
		return return_value;
	}


	public static int getColour(int colour_one, int colour_two, double split_one, double split_two){
		int return_value = 0;

		int r_one = Colour.getR(colour_one);
		int g_one = Colour.getG(colour_one);
		int b_one = Colour.getB(colour_one);
		int a_one = Colour.getA(colour_one);
		int r_two = Colour.getR(colour_two);
		int g_two = Colour.getG(colour_two);
		int b_two = Colour.getB(colour_two);
		int a_two = Colour.getA(colour_two);

		double mult_one = split_one * a_one;
		double mult_two = split_two * a_two;
		double divi_rgb = mult_one + mult_two;

		if(a_two == a_one){
			if(a_two == 0){
				return 0;
			}
			return_value |= a_two << 24;
			if(split_one == split_two){
				return return_value |
					r_one + r_two << 15 & 0xFF0000 |
					g_one + g_two << 7 & 0xFF00 |
					b_one + b_two >> 1;
			}
		}
		else{

			double divi_a = split_one + split_two;
			return_value = (int) (return_value | Math.round(divi_rgb / divi_a) << 24L);
		}
		if(return_value == 0){
			return 0;
		}
		if(r_two == r_one){
			return_value |= r_two << 16;
		}
		else{
			return_value = (int) (return_value | Math.round((r_one * mult_one + r_two * mult_two) / divi_rgb) << 16L);
		}
		if(g_two == g_one){
			return_value |= g_two << 8;
		}
		else{
			return_value = (int) (return_value | Math.round((g_one * mult_one + g_two * mult_two) / divi_rgb) << 8L);
		}
		if(b_two == b_one){
			return_value |= b_two;
		}
		else{
			return_value = (int) (return_value | Math.round((b_one * mult_one + b_two * mult_two) / divi_rgb));
		}
		return return_value;
	}
}
