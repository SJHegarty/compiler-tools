package localgoat.image.images;

import localgoat.image.Colour;
import localgoat.image.Image;

import java.io.InputStream;

public class GradientImage implements Image{
	private final int width;
	private final int height;

	private final int[] c00;
	private final int[] c01;
	private final int[] c11;
	private final int[] c10;

	public GradientImage(int width, int height, int c00, int c01, int c11, int c10){
		this.width = width;
		this.height = height;
		this.c00 = Colour.split(c00);
		this.c01 = Colour.split(c01);
		this.c11 = Colour.split(c11);
		this.c10 = Colour.split(c10);
	}

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
		
		return 0;
	}

}
