package localgoat.image.images;

import localgoat.image.Image;
import localgoat.image.Region;

public class OffsetImage implements Comparable<OffsetImage>{

	final Image image;
	final int index;
	final int offsetx;
	final int offsety;

	OffsetImage(Image image, int index, int offsetx, int offsety){
		this.image = image;
		this.index = index;
		this.offsetx = offsetx;
		this.offsety = offsety;
	}

	Region region(){
		return new Region(offsetx, offsety, image.width(), image.height());
	}

	int colourAt(int x, int y){
		return image.colourAt(x - offsetx, y - offsety);
	}

	@Override
	public int compareTo(OffsetImage o){
		return index - o.index;
	}
}
