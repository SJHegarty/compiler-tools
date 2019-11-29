package localgoat.image.images;

import localgoat.image.Image;

public class ScaledImage implements Image{

	private final Image wrapped;
	private final int xfactor;
	private final int yfactor;
	private final int width;
	private final int height;

	public ScaledImage(Image wrapped, int factor){
		this(wrapped, factor, factor);
	}

	public ScaledImage(Image wrapped, int xfactor, int yfactor){
		this.wrapped = wrapped;
		this.xfactor = xfactor;
		this.yfactor = yfactor;
		this.width = wrapped.width() * xfactor;
		this.height = wrapped.height() * yfactor;
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
		return wrapped.colourAt(x/xfactor, y/yfactor);
	}
}
