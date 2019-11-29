package localgoat.image.images;

import localgoat.image.Image;
import localgoat.image.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ImageTree{
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final ImageTree[][] children;
	private final List<OffsetImage> images = new ArrayList<>();

	ImageTree(Region r){
		this(r.x, r.y, r.width, r.height);
	}

	ImageTree(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		if((width|height) == 1){
			this.width = this.height = 1;
			this.children = null;
		}
		else{
			if(width == 1){
				this.width = 1;
				this.height = height;
				this.children = new ImageTree[2][2];
			}
			else{
				if(height == 1){
					this.height = 1;
					this.width = width;
					this.children = new ImageTree[2][2];
				}
				else{
					this.width = width;
					this.height = height;
					this.children = new ImageTree[2][2];
				}
			}
		}
	}

	public int width(){
		return width;
	}

	public int height(){
		return height;
	}

	public Region region(){
		return new Region(x, y, width, height);
	}

	public void addImage(OffsetImage i){
		addImage(i, i.region());
	}

	public void onTrees(Region region, Consumer<ImageTree> operation){
		final var intersect = region.intersect(region());
		if(!intersect.isEmpty()){
			onTreesUnchecked(intersect, operation);
		}
	}

	private void onTreesUnchecked(Region region, Consumer<ImageTree> operation){
		if(region.equals(region())){
			operation.accept(this);
		}
		else{
			if(children == null){
				throw new IllegalStateException();
			}
			final Region[][] split = region().split();
			for(int x = 0; x < split.length; x++){
				for(int y = 0; y < split[0].length; y++){
					final var intersect = split[x][y].intersect(region);
					if(!intersect.isEmpty()){
						try{
							if(children[x][y] == null){
								children[x][y] = new ImageTree(split[x][y]);
							}
						}
						catch(ArrayIndexOutOfBoundsException e){
							throw e;
						}
						children[x][y].onTreesUnchecked(intersect, operation);
					}
				}
			}
		}
	}

	private void addImage(OffsetImage image, Region region){
		onTrees(region, tree -> tree.images.add(image));
	}

	public List<OffsetImage> images(int x, int y){
		return images(new Region(x, y, 1, 1));
	}
	public List<OffsetImage> images(Region region){
		final List<OffsetImage> rv = new ArrayList<>();
		onTrees(region, tree -> rv.addAll(tree.images));
		Collections.sort(rv);
		return Collections.unmodifiableList(rv);
	}
}
