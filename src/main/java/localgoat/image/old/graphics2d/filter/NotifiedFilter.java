package localgoat.image.old.graphics2d.filter;


import localgoat.image.old.graphics2d.Filter;
import localgoat.image.old.graphics2d.PixelGrid6;

public abstract class NotifiedFilter extends Filter{
	public static class FilterWrapper
		extends NotifiedFilter{
		private final Filter f;

		public FilterWrapper(Filter f){
			super(f.pg);
			this.f = f;
		}


		public void apply(int x, int y){
			this.f.apply(x, y);
		}

		public void applied(){
		}

		public void applyCentre(int x, int y){
			this.f.applyCentre(x, y);
		}
	}


	public NotifiedFilter(PixelGrid6 pg){
		super(pg);
	}

	public abstract void apply(int paramInt1, int paramInt2);

	public abstract void applied();
}





