package localgoat.image.old.graphics2d;


import localgoat.image.Colour;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

public class ColourChooser
	extends Container{
	private static final long serialVersionUID = -7153664465968697788L;

	private class ColourChooserS
		extends JPanel{
		private static final long serialVersionUID = -8650853505229075762L;

		public ColourChooserS(){
			int c = 64;
			int[] cc = {0, 85, 170, 255};
			for(int i = 0; i < c; ){
				ColourChooser.this.colours[i >> 2][i & 0x3] = Colour.toInt(cc[i >> 4], cc[i >> 2 & 0x3], cc[i & 0x3]);
				i++;
			}
			setPreferredSize(new Dimension(328, 65));
			setMaximumSize(getPreferredSize());
			ColourChooser.this.addColourChangeListener(new ColourChangeListener(){
				public void ColourChanged(){
					ColourChooser.ColourChooserS.this.repaint();
				}
			});
			addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent arg0){
					int x = arg0.getX();
					int y = arg0.getY();
					if(x < 256 && y < 64){
						ColourChooser.this.colourChanged(ColourChooser.this.colours[x >> 4][y >> 4]);
					}
				}

				public void mouseEntered(MouseEvent arg0){
				}

				public void mouseExited(MouseEvent arg0){
				}

				public void mousePressed(MouseEvent arg0){
				}

				public void mouseReleased(MouseEvent arg0){
				}
			});
		}

		public void paint(Graphics g){
			g.clearRect(0, 0, 330, 2);
			for(int x = 0; x < 16; ){
				for(int y = 0; y < 4; y++){
					g.setColor(new Color(ColourChooser.this.colours[x][y]));
					g.fillRect(x << 4, y << 4, 16, 16);
					g.setColor(Color.BLACK);
					g.drawRect(x << 4, y << 4, 16, 16);
				}
				x++;
			}
			g.drawRect(256, 0, 64, 64);
			g.setColor(new Color(ColourChooser.this.colour));
			g.fillRect(257, 1, 63, 63);
		}
	}

	private class RGBAColourSlider extends Container{
		private static final long serialVersionUID = -7877871352292960592L;
		private final ColourSlider r_slider = new ColourSlider(-16777216, -65536);
		private final ColourSlider g_slider = new ColourSlider(-16777216, -16711936);
		private final ColourSlider b_slider = new ColourSlider(-16777216, -16776961);
		private final ColourSlider a_slider = new ColourSlider(-1118482, -16777216);

		RGBAColourSlider(){
			setLayout(new BoxLayout(this, 1));
			add(this.r_slider);
			add(this.g_slider);
			add(this.b_slider);
			add(this.a_slider);
			ChangeListener cl = new ChangeListener(){
				public void stateChanged(ChangeEvent arg0){
					ColourChooser.this.colourChanged(
						Colour.toInt(
							ColourChooser.RGBAColourSlider.this.r_slider.getValue(),
							ColourChooser.RGBAColourSlider.this.g_slider.getValue(),
							ColourChooser.RGBAColourSlider.this.b_slider.getValue(),
							ColourChooser.RGBAColourSlider.this.a_slider.getValue()
						)
					);
				}
			};
			this.r_slider.addChangeListener(cl);
			this.g_slider.addChangeListener(cl);
			this.b_slider.addChangeListener(cl);
			this.a_slider.addChangeListener(cl);
			this.a_slider.setValue(255);
		}

		public void update(){
			this.r_slider.setValue(Colour.getR(ColourChooser.this.colour));
			this.g_slider.setValue(Colour.getG(ColourChooser.this.colour));
			this.b_slider.setValue(Colour.getB(ColourChooser.this.colour));
			this.a_slider.colour1 = 0xFF000000 | ColourChooser.this.colour;
			this.a_slider.repaint();
		}

		private class ColourSlider extends JSlider{
			private static final long serialVersionUID = 3712279458145542908L;
			private int colour0;
			private int colour1;

			ColourSlider(int colour0, int colour1){
				super(0, 0, 255, 0);
				setUI(new ColourSliderUI(this));
				setMaximumSize(new Dimension(64, 16));
				setPreferredSize(getMaximumSize());
				this.colour0 = colour0;
				this.colour1 = colour1;
			}

			class ColourSliderUI extends BasicSliderUI{
				int c0;
				int c1;
				private int[] gradient;

				public ColourSliderUI(JSlider slider){
					super(slider);
				}

				public void paintThumb(Graphics g){
					ICON.paintIcon(this.slider, g, this.thumbRect.x, this.thumbRect.y);
				}

				public void paintTrack(Graphics g){
					int trackY = this.trackRect.y + (this.trackRect.height - UIManager.getInt("Slider.trackWidth") >> 1);
					int trackW = this.trackRect.width;
					if(this.gradient == null || this.gradient.length != trackW || this.c0 != ColourChooser.RGBAColourSlider.ColourSlider.this.colour0 || this.c1 != ColourChooser.RGBAColourSlider.ColourSlider.this.colour1){
						this.gradient = Gradient.getGradient(ColourChooser.RGBAColourSlider.ColourSlider.this.colour0, ColourChooser.RGBAColourSlider.ColourSlider.this.colour1, trackW);
						this.c0 = ColourChooser.RGBAColourSlider.ColourSlider.this.colour0;
						this.c1 = ColourChooser.RGBAColourSlider.ColourSlider.this.colour1;
					}
					g.setColor(Color.BLACK);
					g.drawRect(5, trackY, trackW + 1, 5);
					for(int xn = 0; xn < trackW; xn++){
						g.setColor(new Color(this.gradient[xn]));
						g.drawLine(xn + 6, trackY + 1, xn + 6, trackY + 4);
					}
				}

				protected Dimension getThumbSize(){
					return ICON_DIM;
				}
			}
		}
	}

	private final ArrayList<ColourChangeListener> listeners = new ArrayList<ColourChangeListener>();
	private static final int H_COLOURS = 16;
	private static final int V_COLOURS = 4;
	private static final int D_BITS = 4;
	private static final int DIM = 16;
	private static final int WIDTH = 256;
	private static final int HEIGHT = 64;

	public void setColourAt(int c, int x, int y){
		if(x < 16 && y < 4){
			this.colours[x][y] = c | 0xFF000000;
		}
	}

	private static final int FULL_WIDTH = 320;
	private static final Icon ICON = UIManager.getIcon("Slider.horizontalThumbIcon");
	private static final Dimension ICON_DIM = new Dimension(ICON.getIconWidth(), ICON.getIconHeight());
	private final int[][] colours = new int[16][4];
	private int colour = -16744193;
	private boolean changing;

	public ColourChooser(){
		setLayout(new BoxLayout(this, 0));
		add(Box.createHorizontalStrut(4));
		add(new ColourChooserS());
		final RGBAColourSlider slider = new RGBAColourSlider();
		addColourChangeListener(new ColourChangeListener(){
			public void ColourChanged(){
				slider.update();
			}
		});
		add(slider);
		setPreferredSize(new Dimension(320, 72));
		setMinimumSize(getPreferredSize());
		trigger();
	}

	public void trigger(){
		colourChanged(this.colour);
	}

	private void colourChanged(int colour){
		synchronized(this){
			if(this.changing){
				return;
			}
			this.changing = true;
		}
		this.colour = colour;
		for(ColourChangeListener ccl : this.listeners){
			ccl.ColourChanged();
		}
		synchronized(this){
			this.changing = false;
		}
	}

	public int getSelectedColour(){
		return this.colour;
	}


	public void addColourChangeListener(ColourChangeListener ccl){
		this.listeners.add(ccl);
	}
}





