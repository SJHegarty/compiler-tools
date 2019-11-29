package localgoat.image.images;

import localgoat.image.Colour;
import localgoat.image.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeImage implements Image{

	public static class Builder{

		private final List<OffsetImage> images = new ArrayList<>();
		private final int width;
		private final int height;

		public Builder(int width, int height){
			this.width = width;
			this.height = height;
		}

		public Builder addImage(Image i, int offsetx, int offsety){
			images.add(new OffsetImage(i, images.size(), offsetx, offsety));
			return this;
		}

		public CompositeImage build(){
			return new CompositeImage(this.images, width, height);
		}
	}

	//private final ImageTree tree;
	private final List<OffsetImage> images;
	private final int width;
	private final int height;

	private CompositeImage(List<OffsetImage> images, int width, int height){
		this.images = Collections.unmodifiableList(images);
		this.width = width;
		this.height = height;
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
		return images.stream()
			.filter(i -> i.region().contains(x, y))
			.mapToInt(i -> i.colourAt(x, y))
			.reduce(0, (i0, i1) -> Colour.merge(i1, i0));
	}
}
