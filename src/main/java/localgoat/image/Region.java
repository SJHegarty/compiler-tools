package localgoat.image;

public class Region{
	public final int x;
	public final int y;
	public final int width;
	public final int height;

	public Region(int x, int y, int width, int height){
		if((width|height) < 0){
			throw new IllegalArgumentException();
		}
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Region(int width, int height){
		this(0, 0, width, height);
	}

	public Region intersect(Region r){
		final int x0 = Math.max(x, r.x);
		final int y0 = Math.max(y, r.y);
		final int x1 = Math.min(x + width, r.x + r.width);
		final int y1 = Math.min(y + height, r.y + r.height);
		return new Region(x0, y0, (x0 > x1) ? 0: (x1 - x0), (y0 > y1) ? 0 : (y1 - y0));
	}

	public boolean isEmpty(){
		return width == 0 || height == 0;
	}

	public boolean equals(Region r){
		return x == r.x && y == r.y && width == r.width && height == r.height;
	}

	public Region[][] split(){
		final int hwidth = (width == 1) ? 1 : width >> 1;
		final int hheight = (height == 1) ? 1 : height >> 1;
		return new Region[][]{
			new Region[]{
				new Region(x, y, hwidth, hheight),
				new Region(x + hwidth, y, width - hwidth, hheight)
			},
			new Region[]{
				new Region(x, y + hheight, width, height - hheight),
				new Region(x + hwidth, y + hheight, width - hwidth, height - hheight)
			}
		};
	}

	public boolean contains(int x, int y){
		return x >= this.x && y >= this.y && x < (this.x + width) && y < (this.y + height);
	}
}
