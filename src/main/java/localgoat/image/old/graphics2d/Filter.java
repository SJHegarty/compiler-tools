package localgoat.image.old.graphics2d;


public abstract class Filter{
	public static final Filter NUL = new Filter(null){
		public void apply(int x, int y){
		}

		public void applyCentre(int x, int y){
		}
	};

	public Filter(localgoat.image.old.graphics2d.PixelGrid6 pg){
		this.pg = pg;
	}

	public final PixelGrid6 pg;

	public abstract void apply(int paramInt1, int paramInt2);

	public abstract void applyCentre(int paramInt1, int paramInt2);
}
