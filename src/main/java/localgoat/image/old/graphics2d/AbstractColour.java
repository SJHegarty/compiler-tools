/*     */ package localgoat.image.old.graphics2d;

import localgoat.image.Colour;

/*     */
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AbstractColour
/*     */ {
/*     */   private double red;
/*     */   private double green;
/*     */   private double blue;
/*     */   private double alpha;
/*     */   private double rgbcount;
/*     */   private double alphacount;
/*     */   
/*     */   public AbstractColour(int... colours) {
/*  25 */     this.red = 0.0D;
/*  26 */     this.green = 0.0D;
/*  27 */     this.blue = 0.0D;
/*  28 */     this.alpha = 0.0D;
/*  29 */     this.rgbcount = 0.0D;
/*  30 */     this.alphacount = 0.0D;
/*  31 */     for (int i = 0; i < colours.length; i++) {
/*  32 */       add(colours[i], 1.0D);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public AbstractColour() {
/*  40 */     this.red = 0.0D;
/*  41 */     this.green = 0.0D;
/*  42 */     this.blue = 0.0D;
/*  43 */     this.alpha = 0.0D;
/*  44 */     this.rgbcount = 0.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getRed() {
/*  51 */     if (this.rgbcount == 0.0D) return 0; 
/*  52 */     int r = (int)Math.round(this.red / this.rgbcount);
/*  53 */     return (r < 0) ? 0 : ((r >= 256) ? 255 : r);
/*     */   }
/*     */ 
/*     */   
/*  57 */   public double getRedD() { return this.red / this.rgbcount; }
/*     */ 
/*     */   
/*  60 */   public double getGreenD() { return this.green / this.rgbcount; }
/*     */ 
/*     */   
/*  63 */   public double getBlueD() { return this.blue / this.rgbcount; }
/*     */ 
/*     */   
/*  66 */   public double getAlphaD() { return this.alpha / this.alphacount; }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getGreen() {
/*  73 */     if (this.rgbcount == 0.0D) return 0; 
/*  74 */     int g = (int)Math.round(this.green / this.rgbcount);
/*  75 */     return (g < 0) ? 0 : ((g >= 256) ? 255 : g);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getBlue() {
/*  82 */     if (this.rgbcount == 0.0D) return 0; 
/*  83 */     int b = (int)Math.round(this.blue / this.rgbcount);
/*  84 */     return (b < 0) ? 0 : ((b >= 256) ? 255 : b);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getAlpha() {
/*  91 */     if (this.rgbcount == 0.0D) return 0; 
/*  92 */     int a = (int)Math.round(this.alpha / this.alphacount);
/*  93 */     return (a < 0) ? 0 : ((a >= 256) ? 255 : a);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 100 */   public double getCount() { return this.rgbcount; }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int toInt() {
/* 107 */     int alpha = getAlpha();
/* 108 */     if (alpha == 0) return 0; 
/* 109 */     return Colour.toInt(getRed(), getGreen(), getBlue(), alpha);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void add(int c, double e) {
/* 119 */     double rgbe = Colour.getAlpha(c) * e;
/* 120 */     this.red += Colour.getRed(c) * rgbe;
/* 121 */     this.green += Colour.getGreen(c) * rgbe;
/* 122 */     this.blue += Colour.getBlue(c) * rgbe;
/* 123 */     this.alpha += Colour.getAlpha(c) * e;
/* 124 */     this.rgbcount += rgbe;
/* 125 */     this.alphacount += e;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 133 */   public void add(int c) { add(c, 1.0D); }
/*     */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar!\localgoat.image.old.graphics2d\AbstractColour.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */