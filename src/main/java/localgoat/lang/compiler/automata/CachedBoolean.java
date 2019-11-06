package localgoat.lang.compiler.automata;

enum CachedBoolean{
	UNCACHED{
		@Override
		boolean asBoolean(){
			throw new UnsupportedOperationException();
		}
	},
	TRUE{
		@Override
		boolean asBoolean(){
			return true;
		}
	},
	FALSE{
		@Override
		boolean asBoolean(){
			return false;
		}
	};

	abstract boolean asBoolean();
	static CachedBoolean of(boolean value){
		return value ? TRUE : FALSE;
	}
}
