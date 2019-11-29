package localgoat.image.old.graphics2d.image;


import localgoat.image.Colour;
import localgoat.image.old.graphics2d.AbstractColour;
import localgoat.image.old.graphics2d.Gradient;

public class ImageGenerator{
	public static Image getCircle(int c, int radius){
		return getCircle(c, c, radius);
	}


	public static Image getCircle(int c0, int c1, int radius){
		return getCircle(Gradient.getGradient(c0, c1, radius));
	}


	public static Image getCircle(int c0, int c1, int ol, int outline, int radius){
		int[] c = new int[radius];
		int rn = radius - outline;
		if(rn < 0){
			rn = 0;
		}
		for(int i = 0; i < rn; i++){
			int m = rn - i;
			AbstractColour ac = new AbstractColour();
			ac.add(c0, m);
			ac.add(c1, i);
			c[i] = ac.toInt();
		}
		for(int i = rn; i < radius; i++){
			c[i] = ol;
		}

		return getCircle(c);
	}


	public static Image getRectangle(int c, int width, int height){
		Image rv = new Image(width, height);
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				rv.colours[x][y] = c;
			}
			x++;
		}

		return rv;
	}

	public static Image getRectangle(int c0, int c1, int c2, int c3, int width, int height){
		Image rv = new Image(width, height);
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


	public static Image getCircle(int[] c){
		int radius = c.length, diameter = radius << 1;
		Image rv = new Image(diameter, diameter);

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

					if((c0 & 0xFF000000) == 0){
						c0 = 0;
					}
					if(c0 != c1){

						col = Gradient.getColour(c0, c1, d0, d1);
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


	public static Image getSquare(int c, int d){
		return getRectangle(c, d, d);
	}


	public static Image foo(int radius, int div){
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


	public static Image getSquare(int c, int ol, int dim){
		return getRectangle(c, ol, dim, dim);
	}


	public static Image getRectangle(int c, int ol, int width, int height){
		Image rv = new Image(width, height);
		int ymax = height - 1;
		int xmax = width - 1;
		for(int x = 0; x < width;
			rv.colours[x][0] =
				ol, x++){
			rv.colours[x][ymax] = ol;
		}

		for(int y = 1; y < ymax; y++){

			rv.colours[xmax][y] = ol;
			rv.colours[0][y] = ol;
			for(int x = 1; x < xmax; x++){
				rv.colours[x][y] = c;
			}
		}
		return rv;
	}
}