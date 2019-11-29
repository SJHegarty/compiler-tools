package localgoat.image.old.graphics2d.filter;


import localgoat.image.old.graphics2d.image.Image;

public class NCGradient{
	public static void main(String[] args){
		int s = convert(new int[]{128, 8, 1024, 12, 3432, 12});
		System.out.println(String.valueOf(s >>> 24) + " " + (s >> 12 & 0xFFF) + " " + (s & 0xFFF));
	}

	public static int convert(int... vals){
		int rv = 0;
		int shift = 0;
		for(int index = vals.length - 2; index >= 0; index -= 2){
			rv |= vals[index] << shift;
			shift += vals[index + 1];
		}
		return rv;
	}

	public static Image getCircle(int[] c, int... bd){
		int radius = c.length, diameter = radius << 1;
		Image rv = new Image(diameter, diameter);

		int bg = 0;

		for(int x = 0; x < radius; ){
			for(int y = 0; y < radius; y++){
				int xd = radius - x, yd = radius - y;
				double distance = Math.pow((xd * xd + yd * yd), 0.5D);
				final int col;
				if(distance < radius){
					double d1 = distance - Math.floor(distance);
					double d0 = Math.ceil(distance) - distance;
					int d = (int) Math.ceil(distance);
					int c0 = c[(int) Math.floor(distance)];
					int c1 = (d < radius) ? c[d] : 0;

					if(c0 != c1){
						col = getInt(c0, c1, d0, d1, bd);
					}
					else{
						col = c0;
					}
				}
				else{
					col = 0;
				}
				rv.colours[diameter - x - 1][diameter - y - 1] = col;
				rv.colours[x][diameter - y - 1] = col;
				rv.colours[diameter - x - 1][y] = col;
				rv.colours[x][y] = col;
			}
			x++;
		}
		return rv;
	}

	public static int getEqualInt(int co, int ct, int... bd){
		int rv = 0;
		int shift = 0;
		for(int index = bd.length - 1; index >= 0; index--){
			int shiftn = shift + bd[index];
			int mask = (shiftn == 32) ? -1 : ((1 << shiftn) - 1);
			int vo = (co & mask) >>> shift;
			int vt = (ct & mask) >>> shift;
			rv |= vo + vt >> 1 << shift;
			shift = shiftn;
		}
		return rv;
	}

	public static int getInt(int co, int ct, int eo, int et, int... bd){
		int rv = 0;
		int shift = 0;
		int div = eo + et;
		int hdiv = div >> 1;
		for(int index = bd.length - 1; index >= 0; index--){
			int shiftn = shift + bd[index];
			int mask = (shiftn == 32) ? -1 : ((1 << shiftn) - 1);
			int vo = (co & mask) >>> shift;
			int vt = (ct & mask) >>> shift;
			rv |= (vo * eo + vt * et + hdiv) / div << shift;
			shift = shiftn;
		}
		return rv;
	}

	public static int getInt(int co, int ct, double eo, double et, int... bd){
		int rv = 0;
		int shift = 0;
		double div = eo + et;
		for(int index = bd.length - 1; index >= 0; index--){
			int shiftn = shift + bd[index];
			int mask = (shiftn == 32) ? -1 : ((1 << shiftn) - 1);
			int vo = (co & mask) >>> shift;
			int vt = (ct & mask) >>> shift;
			rv = (int) (rv | Math.round((vo * eo + vt * et) / div) << shift);
			shift = shiftn;
		}
		return rv;
	}

	public static int[] getGrad(int co, int ct, int l, int... bd){
		int[] rv = new int[l];
		int shift = 0;
		int div = l - 1;
		int hdiv = div >> 1;
		for(int index = bd.length - 1; index >= 0; index--){
			int shiftn = shift + bd[index];
			int mask = (shiftn == 32) ? -1 : ((1 << shiftn) - 1);
			int vo = (co & mask) >>> shift;
			int vt = (ct & mask) >>> shift;
			for(int i = 0; i < l; i++){
				rv[i] = rv[i] | (vo * (div - i) + vt * i + hdiv) / div << shift;
			}
			shift = shiftn;
		}
		return rv;
	}

	public static void print(int... vals){
		byte b;
		int i;
		int[] arrayOfInt;
		for(i = (arrayOfInt = vals).length, b = 0; b < i; ){
			int v = arrayOfInt[b];
			System.out.print(String.valueOf(Integer.toHexString(v)) + "\t");
			b++;
		}
		System.out.println();
	}

}
