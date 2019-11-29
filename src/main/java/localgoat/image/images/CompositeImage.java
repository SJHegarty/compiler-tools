package localgoat.image.images;

import localgoat.image.Image;

import java.util.ArrayList;
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

	private final ImageTree tree;

	private CompositeImage(List<OffsetImage> images, int width, int height){
		this.tree = new ImageTree(0, 0, width, height);
		for(var i: images){
			tree.addImage(i);
		}
	}

	@Override
	public int width(){
		return tree.width();
	}

	@Override
	public int height(){
		return tree.height();
	}

	@Override
	public int colourAt(int x, int y){
		final var images = tree.images(x, y);
		//System.err.println(new Exception().getStackTrace()[0] + " - Warning! partial implementation.");
		return images.size() == 0 ? 0 : images.get(0).colourAt(x, y);
	}
}
