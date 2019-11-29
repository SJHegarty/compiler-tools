/*     */ package localgoat.image.old.graphics2d;
/*     */ 
/*     */

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
/*     */ public interface Function
/*     */ {
/*  15 */   public static final Function ERASE = new Function() {
/*     */       public int apply(int co, int ct) {
/*  17 */         if ((ct & 0xFF000000) == 255) return 0; 
/*  18 */         int a = Colour.getA(co) * (255 - Colour.getA(ct)) + 255 >> 8;
/*  19 */         if (a == 0) return 0; 
/*  20 */         int r = Colour.getR(co) * (255 - Colour.getR(ct)) + 255 >> 8;
/*  21 */         int g = Colour.getG(co) * (255 - Colour.getG(ct)) + 255 >> 8;
/*  22 */         int b = Colour.getB(co) * (255 - Colour.getB(ct)) + 255 >> 8;
/*  23 */         return Colour.toInt(r, g, b, a);
/*     */       }
/*     */     };
/*     */ 
/*     */ 
/*     */   
/*  29 */   public static final Function DRAW = new Function()
/*     */     {
/*  31 */       public int apply(int co, int ct) { return co; }
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  37 */   public static final Function MERGE = new Function()
/*     */     {
/*  39 */       public int apply(int co, int ct) { return Colour.merge(co, ct); }
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  46 */   public static final Function INVERT = new Function() {
/*     */       public int apply(int co, int ct) {
/*  48 */         int at = Colour.getA(ct);
/*  49 */         int rt = Colour.getR(ct);
/*  50 */         int gt = Colour.getG(ct);
/*  51 */         int bt = Colour.getB(ct);
/*  52 */         int ao = Colour.getA(co);
/*  53 */         int ro = Colour.getR(co);
/*  54 */         int go = Colour.getG(co);
/*  55 */         int bo = Colour.getB(co);
/*  56 */         int a = ao + at - ((ao * at << 1) + 255 >> 8);
/*  57 */         int r = ro + rt - ((ro * rt << 1) + 255 >> 8);
/*  58 */         int g = go + gt - ((go * gt << 1) + 255 >> 8);
/*  59 */         int b = bo + bt - ((bo * bt << 1) + 255 >> 8);
/*  60 */         return Colour.toInt(r, g, b, a);
/*     */       }
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  67 */   public static final Function COLOUR_REDUCE = new Function() {
/*     */       public int apply(int co, int ct) {
/*  69 */         int at = Colour.getA(ct);
/*  70 */         int ro = Colour.getR(co);
/*  71 */         int go = Colour.getG(co);
/*  72 */         int bo = Colour.getB(co);
/*  73 */         int sc = ro + go + bo;
/*  74 */         int gs = sc + (sc << 2);
/*     */         
/*  76 */         gs = (gs << 6) + (gs << 2) + sc + 512 >> 10;
/*     */         
/*  78 */         int r = at * (gs - ro) + (ro << 8) - ro + 255 >> 8;
/*  79 */         int g = at * (gs - go) + (go << 8) - go + 255 >> 8;
/*  80 */         int b = at * (gs - bo) + (bo << 8) - bo + 255 >> 8;
/*  81 */         int a = Colour.getA(co);
/*  82 */         return Colour.toInt(r, g, b, a);
/*     */       }
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  90 */   public static final Function DIFFERENCE = new Function() {
/*     */       public int apply(int co, int ct) {
/*  92 */         return co & 0xFF000000 | 
/*  93 */           Math.abs(Colour.getR(co) - Colour.getR(ct)) << 16 |
/*  94 */           Math.abs(Colour.getG(co) - Colour.getG(ct)) << 8 |
/*  95 */           Math.abs(Colour.getB(co) - Colour.getB(ct));
/*     */       }
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 103 */   public static final Function COLOUR_INCREASE = new Function() {
/*     */       public int apply(int co, int ct) {
/* 105 */         int at = Colour.getA(ct);
/* 106 */         int ro = Colour.getR(co);
/* 107 */         int go = Colour.getG(co);
/* 108 */         int bo = Colour.getB(co);
/* 109 */         if (at == 255) {
/* 110 */           int rv = co & 0xFF000000;
/* 111 */           if ((ro & 0x80) != 0) rv |= 0xFF0000; 
/* 112 */           if ((go & 0x80) != 0) rv |= 0xFF00; 
/* 113 */           if ((bo & 0x80) != 0) rv |= 0xFF; 
/* 114 */           return rv;
/*     */         } 
/* 116 */         int sc = ro + go + bo;
/* 117 */         int gsa = sc + (sc << 2);
/*     */         
/* 119 */         gsa = at * ((gsa << 6) + (gsa << 2) + sc) + 512 >> 10;
/*     */         
/* 121 */         int ai = 255 - at;
/* 122 */         int r = ((ro << 8) - ro - gsa) / ai;
/* 123 */         int g = ((go << 8) - go - gsa) / ai;
/* 124 */         int b = ((bo << 8) - bo - gsa) / ai;
/* 125 */         if (r > 255) r = 255; 
/* 126 */         if (r < 0) r = 0; 
/* 127 */         if (g > 255) g = 255; 
/* 128 */         if (g < 0) g = 0; 
/* 129 */         if (b > 255) b = 255; 
/* 130 */         if (b < 0) b = 0; 
/* 131 */         return Colour.toInt(r, g, b, Colour.getA(co));
/*     */       }
/*     */     };
/*     */   
/*     */   int apply(int paramInt1, int paramInt2);
/*     */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar!\localgoat.image.old.graphics2d\filter\Function.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */