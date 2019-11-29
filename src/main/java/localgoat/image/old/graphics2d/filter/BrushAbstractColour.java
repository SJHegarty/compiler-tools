package localgoat.image.old.graphics2d.filter;

import localgoat.image.Colour;

public class BrushAbstractColour{
	private double red;
	private double green;
	private double blue;
	private double alpha;
	private double count;

	public BrushAbstractColour(int... colours){
		this.red = 0.0D;
		this.green = 0.0D;
		this.blue = 0.0D;
		this.alpha = 0.0D;
		this.count = 0.0D;
		byte b;
		int j, arrayOfInt[];
		for(j = (arrayOfInt = colours).length, b = 0; b < j; ){
			int i = arrayOfInt[b];
			add(i, 1.0D);
			b++;
		}

	}


	public BrushAbstractColour(){
		this.red = 0.0D;
		this.green = 0.0D;
		this.blue = 0.0D;
		this.alpha = 0.0D;
		this.count = 0.0D;
	}


	public int getRed(){
		if(this.count == 0.0D){
			return 0;
		}
		int r = (int) Math.round(this.red / this.count);
		return (r < 0) ? 0 : ((r >= 256) ? 255 : r);
	}


	public double getRedD(){
		return this.red / this.count;
	}


	public double getGreenD(){
		return this.green / this.count;
	}


	public double getBlueD(){
		return this.blue / this.count;
	}


	public double getAlphaD(){
		return this.alpha / this.count;
	}


	public int getGreen(){
		if(this.count == 0.0D){
			return 0;
		}
		int g = (int) Math.round(this.green / this.count);
		return (g < 0) ? 0 : ((g >= 256) ? 255 : g);
	}


	public int getBlue(){
		if(this.count == 0.0D){
			return 0;
		}
		int b = (int) Math.round(this.blue / this.count);
		return (b < 0) ? 0 : ((b >= 256) ? 255 : b);
	}


	public int getAlpha(){
		if(this.count == 0.0D){
			return 0;
		}
		int a = (int) Math.round(this.alpha / this.count);
		return (a < 0) ? 0 : ((a >= 256) ? 255 : a);
	}


	public double getCount(){
		return this.count;
	}


	public int toInt(){
		int alpha = getAlpha();
		return Colour.toInt(getRed(), getGreen(), getBlue(), alpha);
	}


	public void add(int c, double e){
		this.red += Colour.getRed(c) * e;
		this.green += Colour.getGreen(c) * e;
		this.blue += Colour.getBlue(c) * e;
		this.alpha += Colour.getAlpha(c) * e;
		this.count += e;
	}


	public void add(int c){
		add(c, 1.0D);
	}
}





