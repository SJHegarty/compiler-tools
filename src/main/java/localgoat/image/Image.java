package localgoat.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.InputStream;

public interface Image{
	int width();
	int height();
	int colourAt(int x, int y);

	default BufferedImage toBufferedImage(){
		final var rv = new BufferedImage(
			width(),
			height(),
			BufferedImage.TYPE_INT_RGB
		);
		drawTo(rv, 0, 0);
		return rv;
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
					if((xw|yw) < 0 || xw >= width || yw >= height){
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

}
