package localgoat.lang.ui;


import java.awt.Color;
import java.util.*;
import java.util.stream.IntStream;

public class ColourMap<T>{
	private final List<T> values = new ArrayList<>();

	public void add(T value){
		values.add(value);
	}

	public Map<T, Color> build(){
		final Map<T, Color> rv = new HashMap<>();

		final double twopi = 2 * Math.PI;
		final double offsetr = 0d;
		final double offsetg = twopi/3;
		final double offsetb = -offsetg;

		final int valueCount = values.size();
		for(int i = 0; i < valueCount; i++){
			final double delta = twopi * i / values.size();
			final int r = (int)Math.round(127.5 * (1 + Math.sin(offsetr + delta)));
			final int g = (int)Math.round(127.5 * (1 + Math.sin(offsetg + delta)));
			final int b = (int)Math.round(127.5 * (1 + Math.sin(offsetb + delta)));

			rv.put(values.get(i), new Color(r, g, b));
		}

		return Collections.unmodifiableMap(rv);
	}
}
