package localgoat.image.old.graphics2d.image;


import localgoat.image.Colour;
import localgoat.image.old.graphics2d.AbstractColour;
import localgoat.image.old.graphics2d.PixelGrid6;
import localgoat.image.old.graphics2d.filter.MonoFunction;

public class Image{
	public final int width;
	public final int height;
	public final int[][] colours;

	public Image(int width, int height){
		this.width = width;
		this.height = height;
		this.colours = new int[width][height];
	}


	public Image(Image i, MonoFunction m){
		this.colours = new int[i.width][i.height];
		this.width = i.width;
		this.height = i.height;
		for(int x = 0; x < this.height; ){
			for(int y = 0; y < this.height; y++){
				this.colours[x][y] = m.apply(i.colours[x][y]);
			}
			x++;
		}

	}

	public Image(int[][] d, int width, int height){
		this.width = width;
		this.height = height;
		this.colours = (int[][]) d.clone();
	}


	public static int[][] merge(int[][] imageu, int[][] imagel, int width, int height){
		int[][] rv = new int[width][height];
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				rv[x][y] = Colour.merge(imageu[x][y], imagel[x][y]);
			}
			x++;
		}

		return rv;
	}


	public static void merge(int[][] imageu, int[][] imagel, int[][] rv, int width, int height){
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				rv[x][y] = Colour.merge(imageu[x][y], imagel[x][y]);
			}
			x++;
		}
	}


	public Image scale(double xscale, double yscale){
		return resize((int) Math.round(xscale * this.width), (int) Math.round(yscale * this.height));
	}


	public Image resize(int wnew, int hnew){
		double xscale = wnew / this.width;
		double yscale = hnew / this.height;
		Image rv = new Image(wnew, hnew);
		for(int x = 0; x < wnew; ){
			for(int y = 0; y < hnew; y++){
				rv.colours[x][y] = getColourSub(x / xscale, y / yscale, (x + 1) / xscale, (y + 1) / yscale);
			}
			x++;
		}

		return rv;
	}


	private int getColourSub(double x0, double y0, double x1, double y1){
		boolean xo = (x0 > x1);
		boolean yo = (y0 > y1);
		if(xo){
			double xb = x0;
			x0 = x1;
			x1 = xb;
		}
		if(yo){
			double yb = y0;
			y0 = y1;
			y1 = yb;
		}
		if(x0 < 0.0D){
			x0 = 0.0D;
		}
		if(y0 < 0.0D){
			y0 = 0.0D;
		}
		if(x1 < 0.0D || y1 < 0.0D || x0 >= this.width || y0 >= this.height){
			return 0;
		}
		AbstractColour ac = new AbstractColour();
		double xm0 = Math.ceil(x0) - x0;
		double ym0 = Math.ceil(y0) - y0;
		double xm1 = x1 - Math.floor(x1);
		double ym1 = y1 - Math.floor(y1);
		ac.add(this.colours[(int) x0][(int) y0], xm0 * ym0);
		if(x1 < this.width){
			ac.add(this.colours[(int) x1][(int) y0], xm1 * ym0);
		}
		if(y1 < this.height){
			ac.add(this.colours[(int) x0][(int) y1], xm0 * ym1);
		}
		if(x1 < this.width && y1 < this.height){
			ac.add(this.colours[(int) x1][(int) y1], xm1 * ym1);
		}
		for(int x = (int) Math.ceil(x0); x < (int) x1; x++){
			if(x < this.width){
				ac.add(this.colours[x][(int) y0], ym0);
			}
		}
		for(int y = (int) Math.ceil(y0); y < (int) y1; y++){
			if(y < this.height){
				ac.add(this.colours[(int) x0][y], xm0);
			}
		}
		if(y1 < this.height){
			for(int x = (int) Math.ceil(x0); x < (int) x1; x++){
				if(x < this.width){
					ac.add(this.colours[x][(int) y1], ym1);
				}
			}
		}
		if(x1 < this.width){
			for(int y = (int) Math.ceil(y0); y < (int) y1; y++){
				if(y < this.height){
					ac.add(this.colours[(int) x1][y], xm1);
				}
			}
		}
		for(int x = (int) Math.ceil(x0); x < (int) x1; ){
			for(int y = (int) Math.ceil(y0); y < (int) y1; y++){
				ac.add(this.colours[x][y]);
			}
			x++;
		}

		return ac.toInt();
	}


	public Image scale(double factor){
		return scale(factor, factor);
	}


	public static int[][] merge(int[][] foreground, int[][] background, PixelGrid6.BackGroundCalculator bg, int width, int height){
		int[][] rv = new int[width][height];
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				rv[x][y] = Colour.merge(foreground[x][y], Colour.merge(background[x][y], bg.getColour(x, y)));
			}
			x++;
		}

		return rv;
	}


	public static int[][] bg(int[][] image, PixelGrid6.BackGroundCalculator bg, int width, int height){
		int[][] rv = new int[width][height];
		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				rv[x][y] = bg.getColour(image[x][y], x, y);
			}
			x++;
		}

		return rv;
	}
}
