package localgoat.lang.compiler.automata.utility;

public enum CachedBoolean{
	UNCACHED{
		@Override
		public boolean asBoolean(){
			throw new UnsupportedOperationException();
		}
	},
	TRUE{
		@Override
		public boolean asBoolean(){
			return true;
		}
	},
	FALSE{
		@Override
		public boolean asBoolean(){
			return false;
		}
	};

	public abstract boolean asBoolean();
	public static CachedBoolean of(boolean value){
		return value ? TRUE : FALSE;
	}
}
