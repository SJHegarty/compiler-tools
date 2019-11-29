package localgoat.image.old.graphics2d.filter;


import localgoat.image.old.graphics2d.Filter;
import localgoat.image.old.graphics2d.PixelGrid6;
import localgoat.image.old.graphics2d.image.Image;
import localgoat.image.old.graphics2d.image.ImageGenerator;


public class PaintBrush
  extends Filter
{
  private final Image image;

  public PaintBrush(PixelGrid6 pg, Image image) {
    super(pg);
    this.image = image;
  }


  public PaintBrush(PixelGrid6 pg, int co, int ct, int radius) { this(pg, ImageGenerator.getCircle(co, ct, radius)); }


  public PaintBrush(localgoat.image.old.graphics2d.PixelGrid6 pg, int c, int radius) { this(pg, c, c, radius); }


  public PaintBrush(PixelGrid6 pg, int co, int ct, int ol, int outline, int radius) { this(pg, ImageGenerator.getCircle(co, ct, ol, outline, radius)); }



  public void apply(int x, int y) { this.pg.mergeImageAt(x, y, this.image); }


  public void applyCentre(int x, int y) { this.pg.mergeImageAtCentre(x, y, this.image); }
}





