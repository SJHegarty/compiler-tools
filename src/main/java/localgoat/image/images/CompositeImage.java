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
			if(i == null){
				throw new IllegalArgumentException();
			}
			images.add(new OffsetImage(i, images.size(), offsetx, offsety));
			return this;
		}

		public CompositeImage build(){
			return new CompositeImage(width, height, this.images);
		}

		public int size(){
			return images.size();
		}
	}

	//private final ImageTree tree;
	private final List<OffsetImage> images;
	private final int width;
	private final int height;

	private CompositeImage(int width, int height, List<OffsetImage> images){
		this.images = Collections.unmodifiableList(images);
		if((width|height) < 0){
			this.width = images.stream()
				.mapToInt(i -> i.offsetx + i.image.width())
				.max()
				.orElseThrow(() -> new IllegalArgumentException());

			this.height = images.stream()
				.mapToInt(i -> i.offsety + i.image.height())
				.max()
				.orElseThrow(() -> new IllegalArgumentException());
		}
		else{
			this.width = width;
			this.height = height;
		}
		if((width|height) < 0){
			throw new IllegalStateException();
		}
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
		final int mask = -(1 & ((x ^ y) >> 3));
		return images.stream()
			.filter(i -> i.region().contains(x, y))
			.mapToInt(i -> i.colourAt(x, y))
			.reduce(0, (i0, i1) -> Colour.merge(i1, i0));
	}
}
