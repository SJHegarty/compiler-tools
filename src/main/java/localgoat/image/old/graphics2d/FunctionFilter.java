 package localgoat.image.old.graphics2d;

 import localgoat.image.images.GridImage;

 public class FunctionFilter
   extends Filter
 {
   private final Function2 f;
   private final GridImage i;

   public FunctionFilter(PixelGrid6 pg, GridImage i, Function2 f) {
     super(pg);
     this.f = f;
     this.i = i;
   }


   public void apply(int x, int y) { this.pg.applyFunctionAt(x, y, this.f, this.i); }


   public void applyCentre(int x, int y) { this.pg.applyFunctionAt(x - (this.i.width >> 1), y - (this.i.height >> 1), this.f, this.i); }
 }

