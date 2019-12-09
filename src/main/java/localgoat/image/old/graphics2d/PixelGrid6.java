/*     */
package localgoat.image.old.graphics2d;
/*     */

import localgoat.image.BackGroundCalculator;
import localgoat.image.Colour;
import localgoat.image.images.GridImage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


public class PixelGrid6{
	private int[][][][] layers;
	private int[][] hlayer;
	private int layer;
	private int[][] llayer;
	private final BufferedImage clayer;
	private BufferedImage[] thumbs;
	private final int thumbbits;
	private final int thumbwidth;
	private final int thumbheight;
	private int[] tlayers;
	private final int width;
	private final int height;
	private final JPanel thumbpanel;
	private final JPanel imagepanel;
	private static final int THUMB_OFFSET = 5;
	private int current_button = -1;
	private Filter lfilter = null;
	private Filter rfilter = null;
	private final BackGroundCalculator bg;
	private static final int CHECK_ONE = -986896;
	private static final int CHECK_TWO = -16744193;
	public static final int L_BUTTON = 1;
	public static final int R_BUTTON = 3;


	public PixelGrid6(int width, int height){
		this(width, height, new BackGroundCalculator(){
			public final int getColour(int x, int y){
				return ((x & 0x8) == (y & 0x8)) ? -986896 : -16744193;
			}
		});
	}


	public PixelGrid6(int width, int height, int c){
		this(width, height, (x, y) -> c);
	}


	public PixelGrid6(int width, int height, BackGroundCalculator bg){
		this.clayer = new BufferedImage(width, height, 1);
		this.clayer.setAccelerationPriority(1.0F);
		this.layers = new int[1][1][width][height];
		this.bg = bg;

		int w = width;
		int h = height;
		int s = 1;
		int b = 0;
		while(w > 128){
			w >>= 1;
			h >>= 1;
			s <<= 1;
			b++;
		}
		this.thumbheight = h;
		this.thumbwidth = w;
		this.thumbbits = b;


		this.thumbs = new BufferedImage[]{get_thumb()};
		this.tlayers = new int[]{1};
		this.width = width;
		this.height = height;
		setLayerp(0);

		for(int x = 0; x < width; ){
			for(int y = 0; y < height; y++){
				this.clayer.setRGB(x, y, bg.getColour(x, y));
			}
			x++;
		}

		this.thumbpanel = new JPanel(){
			private static final long serialVersionUID = -810178821954285375L;

			public void paint(Graphics g){
				BufferedImage[] thumbs = PixelGrid6.this.getThumbs();
				int y = 5;
				g.clearRect(0, 0, PixelGrid6.this.thumbwidth + 10, (PixelGrid6.this.thumbheight + 10) * PixelGrid6.this.tlayers[0]);
				for(int i = thumbs.length - 1; i >= 0; i--){
					g.drawImage(thumbs[i], 6, y + 1, this);
					g.setColor(Color.GRAY);
					g.drawRect(5, y, PixelGrid6.this.thumbwidth + 1, PixelGrid6.this.thumbheight + 1);
					g.drawRect(4, y - 1, PixelGrid6.this.thumbwidth + 3, PixelGrid6.this.thumbheight + 3);
					y += 5 + PixelGrid6.this.thumbheight;
				}
				g.setColor(Color.BLACK);
				g.drawRect(5, (PixelGrid6.this.tlayers[0] - 1 - PixelGrid6.this.layer) * (5 + PixelGrid6.this.thumbheight) + 5, PixelGrid6.this.thumbwidth + 1, PixelGrid6.this.thumbheight + 1);
				g.drawRect(4, (PixelGrid6.this.tlayers[0] - 1 - PixelGrid6.this.layer) * (5 + PixelGrid6.this.thumbheight) + 5 - 1, PixelGrid6.this.thumbwidth + 3, PixelGrid6.this.thumbheight + 3);
			}
		}
		;

		this.thumbpanel.addMouseListener(
			new MouseListener(){
				public void mouseEntered(MouseEvent arg0){
				}

				public void mouseExited(MouseEvent arg0){
				}

				public void mouseClicked(MouseEvent arg0){
					PixelGrid6.this.setLayer(PixelGrid6.this.tlayers[0] - 1 - arg0.getY() / (PixelGrid6.this.thumbheight + 5));
					PixelGrid6.this.thumbpanel.repaint();
				}


				public void mousePressed(MouseEvent arg0){
				}

				public void mouseReleased(MouseEvent arg0){
				}
			}
		);
		this.imagepanel = new JPanel(){
			private static final long serialVersionUID = -4931596860472402165L;

			public void paint(Graphics g){
				g.drawImage(PixelGrid6.this.clayer, 0, 0, this);
			}

		};
		this.imagepanel.setPreferredSize(new Dimension(width, height));
		this.imagepanel.setMaximumSize(this.imagepanel.getPreferredSize());
		final MouseListener ml = new MouseListener(){
			public void mouseClicked(MouseEvent arg0){
			}

			public void mouseEntered(MouseEvent arg0){
			}

			public void mouseExited(MouseEvent arg0){
			}

			public void mousePressed(MouseEvent arg0){
				int button;
				if(PixelGrid6.this.current_button == -1){
					PixelGrid6.this.current_button = arg0.getButton();
					button = arg0.getButton();
				}
				else{
					button = PixelGrid6.this.current_button;
				}
				Filter f = (button == 1) ? PixelGrid6.this.lfilter : PixelGrid6.this.rfilter;
				if(f != null){
					f.applyCentre(arg0.getX(), arg0.getY());
					PixelGrid6.this.imagepanel.repaint();
				}
			}

			public void mouseReleased(MouseEvent arg0){
				PixelGrid6.this.current_button = -1;
			}
		};
		this.imagepanel.addMouseListener(ml);
		this.imagepanel.addMouseMotionListener(new MouseMotionListener(){
			public void mouseDragged(MouseEvent arg0){
				ml.mousePressed(arg0);
			}


			public void mouseMoved(MouseEvent arg0){
			}
		});
		settpsize();
	}


	private BufferedImage get_thumb(){
		BufferedImage rv = new BufferedImage(this.thumbwidth, this.thumbheight, 1);
		for(int x = 0; x < this.thumbwidth; ){
			for(int y = 0; y < this.thumbheight; y++){
				rv.setRGB(x, y, this.bg.getColour(x << 1, y << 1));
			}
			x++;
		}

		return rv;
	}


	public BufferedImage[] getThumbs(){
		BufferedImage[] rv = new BufferedImage[this.tlayers[0]];
		for(int i = 0; i < rv.length; i++){
			rv[i] = this.thumbs[i];
		}
		return rv;
	}


	public void addLayer(){
		if(this.tlayers[0] == 1 << this.tlayers.length - 1){
			int[][][][] layersnew = new int[this.layers.length + 1][][][];
			int[] tlayersnew = new int[this.tlayers.length + 1];
			for(int i = 0; i < this.layers.length; i++){
				layersnew[i] = new int[(this.layers[i]).length << 1][][];
				tlayersnew[i] = this.tlayers[i];
				for(int u = 0; u < (this.layers[i]).length; u++){
					layersnew[i][u] = this.layers[i][u];
				}
			}
			layersnew[this.layers.length] = new int[1][][];
			this.layers = layersnew;
			this.tlayers = tlayersnew;
			BufferedImage[] thumbsnew = new BufferedImage[(this.layers[0]).length];
			for(int i = 0; i < this.tlayers[0]; i++){
				thumbsnew[i] = this.thumbs[i];
			}
			this.thumbs = thumbsnew;
		}
		this.thumbs[this.tlayers[0]] = get_thumb();
		this.tlayers[0] = this.tlayers[0] + 1;
		this.layers[0][this.tlayers[0]] = new int[this.width][this.height];
		for(int i = 1; i < this.tlayers.length &&
			this.tlayers[i] != this.tlayers[i - 1] - (this.tlayers[i - 1] >> 1); i++){
			this.tlayers[i] = this.tlayers[i] + 1;
			this.layers[i][this.tlayers[i]] = new int[this.width][this.height];
		}
		settpsize();
	}

	private void settpsize(){
		this.thumbpanel.setPreferredSize(new Dimension(this.thumbwidth + 10 + 4, (5 + this.thumbheight) * this.tlayers[0] + 5));
		this.thumbpanel.repaint();
		this.thumbpanel.revalidate();
	}


	public void setLayer(int layer){
		if(this.layer == layer){
			return;
		}
		setLayerp(layer);
	}


	private void setLayerp(int layer){
		int l = this.layer >> 1;
		for(int i = 1; i < this.layers.length; i++){
			int lt = l << 1;
			int lc = lt + 1;
			if(lc != this.tlayers[i - 1]){
				for(int x = 0; x < this.width; ){
					for(int y = 0; y < this.height; y++){
						int cf = this.layers[i - 1][lc][x][y];
						int cb = this.layers[i - 1][lt][x][y];
						if(cf == 0){
							this.layers[i][l][x][y] = cb;
						}

						else if(cb == 0){
							this.layers[i][l][x][y] = cf;
						}
						else{
							this.layers[i][l][x][y] = Colour.merge(cf, cb);
						}
					}
					x++;
				}
			}
			else{
				for(int x = 0; x < this.width; ){
					for(int y = 0; y < this.height; y++){
						this.layers[i][l][x][y] = this.layers[i - 1][lt][x][y];
					}
					x++;
				}
			}

			l >>= 1;
		}

		this.layer = layer;

		this.hlayer = compile(layer + 1, this.tlayers[0]);
		this.llayer = compile(0, layer);
		System.gc();
	}


	private int[][] compile(int layerz, int layero){
		return compile(layerz, layero, this.tlayers.length - 1);
	}


	private int[][] compile(int layerz, int layero, int tlayer){
		if(layerz == this.tlayers[0] || layero == 0){
			return null;
		}
		if(tlayer == 0){
			if(layerz == 0){
				return this.layers[tlayer][layerz >> tlayer];
			}
			return this.layers[0][layerz];
		}

		int and = -1 << tlayer;
		int val = 1 << tlayer;
		if((layerz & and) == layerz && (layero >= this.tlayers[0] || layero == layerz + val)){
			if(layerz == 0){
				return this.layers[tlayer][layerz >> tlayer];
			}
			return this.layers[tlayer][layerz >> tlayer];
		}

		tlayer--;
		int split = (layerz & -1 << tlayer + 1) + (1 << tlayer);
		if(layerz >= split || layero <= split){
			return compile(layerz, layero, tlayer);
		}
		return GridImage.merge(compile(split, layero, tlayer), compile(layerz, split, tlayer), this.width, this.height);
	}


	public void mergeImageAtCentre(int x, int y, GridImage i){
		mergeImageAt(x - (i.width >> 1), y - (i.height >> 1), i);
	}


	public void mergeImageAt(int x, int y, GridImage i){
		mergeImageAtp(x, y, i);
		update_thumb(x, y, i);
	}


	private void update_thumb(int x, int y, GridImage i){
		int d = 1 << this.thumbbits;
		int xmax = Math.min(x + i.width, this.width);
		int ymax = Math.min(y + i.height, this.height);
		for(int xn = Math.max(0, x); xn < xmax; xn += d){
			for(int yn = Math.max(0, y); yn < ymax; yn++){
				int xl = xn >> this.thumbbits;
				int yl = yn >> this.thumbbits;
				int c = Colour.merge(this.layers[0][this.layer][xn][yn], this.bg.getColour(xl << 1, yl << 1));
				this.thumbs[this.layer].setRGB(xl, yl, c);
			}
		}
		this.thumbpanel.repaint();
	}


	private void mergeImageAtp(int x, int y, GridImage i){
		int xmax = Math.min(this.width, x + i.width);
		int ymax = Math.min(this.height, y + i.height);
		if(this.hlayer != null){
			if(this.llayer != null){
				for(int xn = Math.max(0, x); xn < xmax; ){
					for(int yn = Math.max(0, y); yn < ymax; yn++){
						int ci = i.colours[xn - x][yn - y];
						if(ci != 0){
							int cl = this.layers[0][this.layer][xn][yn];
							int c = (cl == 0) ? ci : Colour.merge(ci, cl);
							this.layers[0][this.layer][xn][yn] = c;
							if((this.hlayer[xn][yn] & 0xFF000000) != -16777216){

								this.clayer.setRGB(xn, yn, Colour.merge(new int[]{this.hlayer[xn][yn], c, this.llayer[xn][yn], this.bg.getColour(xn, yn)}));
							}
						}
					}
					xn++;
				}

				return;
			}
			for(int xn = Math.max(0, x); xn < xmax; ){
				for(int yn = Math.max(0, y); yn < ymax; yn++){
					int ci = i.colours[xn - x][yn - y];
					if(ci != 0){
						int cl = this.layers[0][this.layer][xn][yn];
						int c = (cl == 0) ? ci : Colour.merge(ci, cl);
						this.layers[0][this.layer][xn][yn] = c;
						if((this.hlayer[xn][yn] & 0xFF000000) != -16777216){

							this.clayer.setRGB(xn, yn, Colour.merge(this.hlayer[xn][yn], c, this.bg.getColour(xn, yn)));
						}
					}
				}
				xn++;
			}
			return;
		}
		if(this.llayer != null){
			for(int xn = Math.max(0, x); xn < xmax; xn++){
				for(int yn = Math.max(0, y); yn < ymax; yn++){
					int ci = i.colours[xn - x][yn - y];
					if(ci != 0){
						int cl = this.layers[0][this.layer][xn][yn];
						int c = (cl == 0) ? ci : Colour.merge(ci, cl);
						this.layers[0][this.layer][xn][yn] = c;
						if((c & 0xFF000000) == -16777216){
							this.clayer.setRGB(xn, yn, c);
						}
						else{

							this.clayer.setRGB(xn, yn, Colour.merge(c, this.llayer[xn][yn], this.bg.getColour(xn, yn)));
						}
					}
				}
			}
			return;
		}
		for(int xn = Math.max(0, x); xn < xmax; xn++){
			for(int yn = Math.max(0, y); yn < ymax; yn++){
				int ci = i.colours[xn - x][yn - y];
				if(ci != 0){
					int cl = this.layers[0][this.layer][xn][yn];
					int c = (cl == 0) ? ci : Colour.merge(ci, cl);
					this.layers[0][this.layer][xn][yn] = c;
					this.clayer.setRGB(xn, yn, Colour.merge(c, this.bg.getColour(xn, yn)));
				}
			}
		}
	}


	public void applyFunctionAt(int x, int y, Function2 f, GridImage i){
		applyFunctionAtp(x, y, f, i);
		update_thumb(x, y, i);
	}


	private void applyFunctionAtp(int x, int y, Function2 f, GridImage i){
		int xmax = Math.min(this.width, x + i.width);
		int ymax = Math.min(this.height, y + i.height);
		if(this.hlayer != null){
			if(this.llayer != null){
				for(int xn = Math.max(0, x); xn < xmax; ){
					for(int yn = Math.max(0, y); yn < ymax; yn++){
						int ci = i.colours[xn - x][yn - y];
						int cl = this.layers[0][this.layer][xn][yn];
						int c = f.apply(cl, ci);
						this.layers[0][this.layer][xn][yn] = c;
						if((this.hlayer[xn][yn] & 0xFF000000) != -16777216){

							this.clayer.setRGB(xn, yn, Colour.merge(new int[]{this.hlayer[xn][yn], c, this.llayer[xn][yn], this.bg.getColour(xn, yn)}));
						}
					}
					xn++;
				}

				return;
			}
			for(int xn = Math.max(0, x); xn < xmax; ){
				for(int yn = Math.max(0, y); yn < ymax; yn++){
					int ci = i.colours[xn - x][yn - y];
					int cl = this.layers[0][this.layer][xn][yn];
					int c = f.apply(cl, ci);
					this.layers[0][this.layer][xn][yn] = c;
					if((this.hlayer[xn][yn] & 0xFF000000) != -16777216){

						this.clayer.setRGB(xn, yn, Colour.merge(this.hlayer[xn][yn], c, this.bg.getColour(xn, yn)));
					}
				}
				xn++;
			}
			return;
		}
		if(this.llayer != null){
			for(int xn = Math.max(0, x); xn < xmax; xn++){
				for(int yn = Math.max(0, y); yn < ymax; yn++){
					int ci = i.colours[xn - x][yn - y];
					int cl = this.layers[0][this.layer][xn][yn];
					int c = f.apply(cl, ci);
					this.layers[0][this.layer][xn][yn] = c;
					this.clayer.setRGB(xn, yn, Colour.merge(c, this.llayer[xn][yn], this.bg.getColour(xn, yn)));
				}
			}
			return;
		}
		for(int xn = Math.max(0, x); xn < xmax; xn++){
			for(int yn = Math.max(0, y); yn < ymax; yn++){
				int ci = i.colours[xn - x][yn - y];
				int cl = this.layers[0][this.layer][xn][yn];
				int c = f.apply(cl, ci);
				this.layers[0][this.layer][xn][yn] = c;
				this.clayer.setRGB(xn, yn, Colour.merge(c, this.bg.getColour(xn, yn)));
			}
		}
	}


	public GridImage getImage(int x, int y, int width, int height){
		GridImage rv = new GridImage(width, height);
		int xs = (x < 0) ? -x : 0;
		int ys = (y < 0) ? -y : 0;
		int xf = x + width;
		xf = (xf > this.width) ? (this.width - x) : width;
		int yf = y + height;
		yf = (yf > this.height) ? (this.height - y) : height;
		for(int xn = xs; xn < xf; ){
			for(int yn = ys; yn < yf; yn++){
				rv.colours[xn][yn] = this.layers[0][this.layer][x + xn][y + yn];
			}
			xn++;
		}

		return rv;
	}


	public int getLayerCount(){
		return this.tlayers[0];
	}


	public int getCurrentLayer(){
		return this.layer;
	}


	public int getWidth(){
		return this.width;
	}


	public int getHeight(){
		return this.height;
	}


	public int getThumbHeight(){
		return this.thumbheight;
	}


	public int getThumbWidth(){
		return this.thumbwidth;
	}


	public Component getThumbPanel(){
		return this.thumbpanel;
	}


	public Component getImagePanel(){
		return this.imagepanel;
	}

	public void setFilter(Filter filter, int button){
		switch(button){
			case 1:
				this.lfilter = filter;
				break;

			case 3:
				this.rfilter = filter;
				break;
		}
	}
}





