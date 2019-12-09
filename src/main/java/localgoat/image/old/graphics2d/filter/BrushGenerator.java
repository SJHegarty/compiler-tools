package localgoat.image.old.graphics2d.filter;


import localgoat.image.Colour;
import localgoat.image.images.GridImage;


public class BrushGenerator{
	public static GridImage getCircle(int c, int radius){
		return getCircle(c, c, radius);
	}


	public static GridImage getCircle(int c0, int c1, int radius){
		return getCircle(BrushGradient.getGradient(c0, c1, radius));
	}


	public static GridImage getCircle(int c0, int c1, int ol, int outline, int radius){
		int[] c = new int[radius];
		int rn = radius - outline;
		if(rn < 0){
			rn = 0;
		}
		for(int i = 0; i < rn; i++){
			int m = rn - i;
			BrushAbstractColour ac = new BrushAbstractColour();
			ac.add(c0, m);
			ac.add(c1, i);
			c[i] = ac.toInt();
		}
		for(int i = rn; i < radius; i++){
			c[i] = ol;
		}

		return getCircle(c);
	}


	public static GridImage getRectangle(int width, int height, int c){
		GridImage rv = new GridImage(width, height);
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				rv.colours[x][y] = c;
			}
			x++;
		}

		return rv;
	}

	public static GridImage getRectangle(int c0, int c1, int c2, int c3, int width, int height){
		GridImage rv = new GridImage(width, height);
		int divisor = width * height;
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				int alpha = (Colour.getAlpha(c0) * (width - x) * (height - y) +
					Colour.getAlpha(c1) * x * (height - y) +
					Colour.getAlpha(c2) * (width - x) * y +
					Colour.getAlpha(c3) * x * y) / divisor;
				if(alpha == 0){
					rv.colours[x][y] = 0;
				}
				else{
					int red = (Colour.getRed(c0) * (width - x) * (height - y) +
						Colour.getRed(c1) * x * (height - y) +
						Colour.getRed(c2) * (width - x) * y +
						Colour.getRed(c3) * x * y) / divisor;
					int green = (Colour.getGreen(c0) * (width - x) * (height - y) +
						Colour.getGreen(c1) * x * (height - y) +
						Colour.getGreen(c2) * (width - x) * y +
						Colour.getGreen(c3) * x * y) / divisor;
					int blue = (Colour.getBlue(c0) * (width - x) * (height - y) +
						Colour.getBlue(c1) * x * (height - y) +
						Colour.getBlue(c2) * (width - x) * y +
						Colour.getBlue(c3) * x * y) / divisor;
					rv.colours[x][y] = Colour.toInt(red, green, blue, alpha);
				}
			}
			x++;
		}
		return rv;
	}


	public static GridImage getCircle(int[] c){
		int radius = c.length, diameter = radius << 1;
		GridImage rv = new GridImage(diameter, diameter);

		int bg = 0;

		for(int x = 0; x < radius; ){
			for(int y = 0; y < radius; y++){
				int xd = radius - x, yd = radius - y;
				double distance = Math.pow((xd * xd + yd * yd), 0.5D);
				if(distance < radius){
					int col;
					double d1 = distance - Math.floor(distance);
					double d0 = Math.ceil(distance) - distance;
					int d = (int) Math.ceil(distance);
					int c0 = c[(int) Math.floor(distance)];
					int c1 = (d < radius) ? c[d] : 0;

					if(c0 != c1){
						BrushAbstractColour ac = new BrushAbstractColour();
						ac.add(c0, d0);
						ac.add(c1, d1);
						col = ac.toInt();
					}
					else{
						col = c0;
					}


					rv.colours[diameter - x - 1][diameter - y - 1] = col;
					rv.colours[x][diameter - y - 1] = col;
					rv.colours[diameter - x - 1][y] = col;
					rv.colours[x][y] = col;

				}
				else{

					rv.colours[diameter - x - 1][diameter - y - 1] = 0;
					rv.colours[x][diameter - y - 1] = 0;
					rv.colours[diameter - x - 1][y] = 0;
					rv.colours[x][y] = 0;
				}
			}
			x++;
		}
		return rv;
	}

	public static GridImage getSquare(int c, int d){
		return getRectangle(c, d, d);
	}

	public static GridImage foo(int radius, int div){
		int[] c = new int[radius];
		double area = (radius * radius / div--);
		double r1 = radius;
		for(int i = 0; i < div; i++){
			double r2 = Math.sqrt(r1 * r1 - area);
			int colour = Colour.random();
			for(int u = (int) r2; u < r1; u++){
				c[u] = colour;
			}
			r1 = r2;
		}
		int colour = Colour.random();
		for(int u = 0; u < r1; u++){
			c[u] = colour;
		}
		return getCircle(c);
	}
}





