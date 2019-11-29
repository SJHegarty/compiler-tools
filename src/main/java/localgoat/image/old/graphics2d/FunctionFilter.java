 package localgoat.image.old.graphics2d;

 import localgoat.image.old.graphics2d.image.Image;

 public class FunctionFilter
   extends Filter
 {
   private final Function f;
   private final Image i;

   public FunctionFilter(PixelGrid6 pg, Image i, Function f) {
     super(pg);
     this.f = f;
     this.i = i;
   }


   public void apply(int x, int y) { this.pg.applyFunctionAt(x, y, this.f, this.i); }


   public void applyCentre(int x, int y) { this.pg.applyFunctionAt(x - (this.i.width >> 1), y - (this.i.height >> 1), this.f, this.i); }
 }

