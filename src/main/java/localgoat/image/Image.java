package localgoat.image;

import java.awt.image.BufferedImage;
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
