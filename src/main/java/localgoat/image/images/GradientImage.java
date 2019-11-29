package localgoat.image.images;

import localgoat.image.Colour;
import localgoat.image.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.io.InputStream;

public class GradientImage implements Image{
	public static void main(String...args){
		final var frame = new JFrame();

		final var panel = new JPanel(){
			@Override
			public void paint(Graphics g){
				final int width = getWidth();
				final int height = getHeight();
				final var g0 = new GradientImage(
					width * 2/3, height * 2/3,
					0xff0080ff,
					0xff000000,
					0xffff8000,
					0xffffffff
				);
				final var g1 = new GradientImage(
					width * 2/3, height * 2/3,
					0x80ffff00,
					0xff000000,
					0xffff0000,
					0xffffffff
				);
				final var gc = new CompositeImage.Builder(width, height)
					.addImage(g0, 0, 0)
					.addImage(g1, width * 1/3, height * 1/3)
					.build();

				g.drawImage(gc.toBufferedImage(), 0, 0, this);
			}
		};
		frame.getContentPane().add(panel);
		frame.setSize(100, 100);
		frame.setVisible(true);
	}
	private static final float ALPHA_MUL = 1/255f;
	private final int width;
	private final int height;

	private final int[] c00;
	private final int[] c01;
	private final int[] c11;
	private final int[] c10;

	public GradientImage(int width, int height, int c00, int c01, int c11, int c10){
		this.width = width;
		this.height = height;
		this.c00 = Colour.split(c00);
		this.c01 = Colour.split(c01);
		this.c11 = Colour.split(c11);
		this.c10 = Colour.split(c10);
	}

	@Override
	public int width(){
		return width;
	}

	@Override
	public int height(){
		return height;
	}

	public int c00(){
		return Colour.composite(c00);
	}

	public int c01(){
		return Colour.composite(c01);
	}

	public int c11(){
		return Colour.composite(c11);
	}

	public int c10(){
		return Colour.composite(c10);
	}

	@Override
	public int colourAt(int x, int y){
		if((c00[0]|c01[0]|c11[0]|c10[0]) == 0){
			return 0;
		}
		final float mulx1 = x/(float)(width - 1);
		final float muly1 = y/(float)(height - 1);
		final float mulx0 = 1f - mulx1;
		final float muly0 = 1f - muly1;


		final float mul00 = mulx0 * muly0 * c00[0] * ALPHA_MUL;
		final float mul01 = mulx0 * muly1 * c01[0] * ALPHA_MUL;
		final float mul11 = mulx1 * muly1 * c11[0] * ALPHA_MUL;
		final float mul10 = mulx1 * muly0 * c10[0] * ALPHA_MUL;


		final int[] colour = new int[4];
		final float sumAlpha = mul00 + mul01 + mul11 + mul10;
		colour[0] = Math.round(255f * sumAlpha);
		final float alphaMul = 1f/sumAlpha;
		for(int i = 1; i < 4; i++){
			colour[i] = Math.round(alphaMul * (c00[i] * mul00 + c01[i] * mul01 + c11[i] * mul11 + c10[i] * mul10));
		}
		return Colour.composite(colour);
	}

}
