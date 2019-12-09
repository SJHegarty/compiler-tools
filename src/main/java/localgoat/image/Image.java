package localgoat.image;

import localgoat.image.images.ScaledImage;

import java.awt.image.BufferedImage;

public interface Image{
	int width();
	int height();
	int colourAt(int x, int y);

	default BufferedImage toBufferedImage(){
		if((width() | height()) < 0){
			throw new IllegalStateException();
		}
		final var rv = new BufferedImage(
			width(),
			height(),
			BufferedImage.TYPE_INT_RGB
		);
		drawTo(rv, 0, 0);
		return rv;
	}

	default boolean isBlank(){
		final int width = width();
		final int height = height();
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				if((colourAt(x, y) & 0xff000000) != 0){
					return false;
				}
			}
		}
		return true;
	}
	default Image withBackground(BackGroundCalculator calculator){
		return new Image(){
			@Override
			public int width(){
				return Image.this.width();
			}

			@Override
			public int height(){
				return Image.this.height();
			}

			@Override
			public int colourAt(int x, int y){
				return calculator.getColour(Image.this.colourAt(x, y), x, y);
			}
		};
	}

	default Image subImage(int width, int height){
		return subImage(0, 0, width, height);
	}

	default Image subImage(int x0, int y0, int width, int height){
		final int xmax = x0 + width;
		final int ymax = y0 + height;
		if((x0|y0) < 0 || xmax > width() || ymax > height()){
			return new Image(){
				@Override
				public int width(){
					return width;
				}

				@Override
				public int height(){
					return height;
				}

				@Override
				public int colourAt(int x, int y){
					final int xw = x + x0;
					final int yw = y + y0;
					if((xw|yw) < 0 || xw >= Image.this.width() || yw >= Image.this.height()){
						return 0;
					}
					return Image.this.colourAt(xw, yw);
				}
			};
		}
		else return new Image(){
			@Override
			public int width(){
				return width;
			}

			@Override
			public int height(){
				return height;
			}

			@Override
			public int colourAt(int x, int y){
				return Image.this.colourAt(x + x0, y + y0);
			}
		};
	}

	default void drawTo(BufferedImage destination, int offsetx, int offsety){
		final int xmax = Math.min(width(), destination.getWidth() - offsetx);
		final int ymax = Math.min(height(), destination.getHeight() - offsety);
		for(int x = 0; x < xmax; x++){
			for(int y = 0; y < ymax; y++){
				destination.setRGB(
					x + offsetx,
					y + offsety,
					0xff000000 | colourAt(x, y)
				);
			}
		}
	}

	default Image scale(int factor){
		return scale(factor, factor);
	}

	default Image scale(int xfactor,  int yfactor){
		return new ScaledImage(this, xfactor, yfactor);
	}
}
