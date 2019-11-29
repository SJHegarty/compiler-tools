/*    */
package localgoat.image.old.graphics2d.filter;


import localgoat.image.old.graphics2d.Function;

public interface MonoFunction{
	public static final MonoFunction NUL = new MonoFunction(){
		public int apply(int v){
			return v;
		}
	};


	public static final MonoFunction FULL_INVERT = new MonoFunction(){
		public int apply(int v){
			return v ^ 0xFFFFFFFF;
		}
	};


	int apply(int paramInt);

	public static class WrappedFunction
		implements MonoFunction{
		private final int v;
		private final Function f;

		public WrappedFunction(Function function, int value){
			this.v = value;
			this.f = function;
		}

		public int apply(int co){
			return this.f.apply(co, this.v);
		}
	}
}





