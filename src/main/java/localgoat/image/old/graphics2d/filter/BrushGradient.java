/*    */
package localgoat.image.old.graphics2d.filter;


public class BrushGradient{
	public static int[] getGradient(int c0, int c1, int radius){
		int[] c = new int[radius];
		for(int i = 0; i < radius; i++){
			int m = radius - i;
			BrushAbstractColour ac = new BrushAbstractColour();
			ac.add(c0, m);
			ac.add(c1, i);
			c[i] = ac.toInt();
		}
		return c;
	}
}





