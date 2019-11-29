/*     */ package localgoat.utilities;
/*     */ 
/*     */ 
/*     */ public class Queue<T>
/*     */ {
/*     */   private static final int dbits = 4;
/*     */   private int zidx;
/*   8 */   private T[] data = (T[])new Object[16];
/*   9 */   private int and = this.data.length - 1;
/*     */   private int size;
/*     */   
/*     */   public void append(T t) {
/*  13 */     if (this.data.length == this.size) {
/*  14 */       Object[] datanew = new Object[this.size << 1];
/*  15 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  16 */        this.data = (T[])datanew;
/*  17 */       this.and = this.data.length - 1;
/*  18 */       this.zidx = 0;
/*     */     } 
/*  20 */     this.data[this.size++] = t;
/*     */   }
/*     */   public void append(Object... ts) {
/*  23 */     int snew = this.size + ts.length;
/*  24 */     if (this.data.length < snew) {
/*  25 */       snew |= snew >> 1;
/*  26 */       snew |= snew >> 2;
/*  27 */       snew |= snew >> 4;
/*  28 */       snew |= snew >> 8;
/*  29 */       snew |= snew >> 16;
/*  30 */       snew++;
/*  31 */       Object[] datanew = new Object[snew];
/*  32 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  33 */        this.data = (T[])datanew;
/*  34 */       this.and = this.data.length - 1;
/*  35 */       this.zidx = 0;
/*     */     }  byte b; int j; Object[] arrayOfObject;
/*  37 */     for (j = (arrayOfObject = ts).length, b = 0; b < j; ) { T t = (T)arrayOfObject[b]; this.data[this.size++] = t; b++; }
/*     */   
/*     */   } public T head() {
/*  40 */     T rv = this.data[this.zidx];
/*  41 */     this.zidx = this.zidx + 1 & this.and;
/*  42 */     this.size--;
/*  43 */     if (this.size < this.data.length >> 2) {
/*  44 */       Object[] datanew = new Object[this.data.length >> 1];
/*  45 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  46 */        this.data = (T[])datanew;
/*  47 */       this.and = this.data.length - 1;
/*  48 */       this.zidx = 0;
/*     */     } 
/*  50 */     return rv;
/*     */   }
/*     */   
/*  53 */   public T peekHead() { return this.data[this.zidx]; }
/*     */   
/*     */   public void prepend(T t) {
/*  56 */     if (this.data.length == this.size) {
/*  57 */       Object[] datanew = new Object[this.size << 1];
/*  58 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  59 */        this.data = (T[])datanew;
/*  60 */       this.and = this.data.length - 1;
/*  61 */       this.zidx = 0;
/*     */     } 
/*  63 */     this.zidx = this.zidx + this.data.length - 1 & this.and;
/*  64 */     this.data[this.zidx] = t;
/*  65 */     this.size++;
/*     */   }
/*     */   public void prepend(Object... ts) {
/*  68 */     int snew = this.size + ts.length;
/*  69 */     if (this.data.length < snew) {
/*  70 */       snew |= snew >> 1;
/*  71 */       snew |= snew >> 2;
/*  72 */       snew |= snew >> 4;
/*  73 */       snew |= snew >> 8;
/*  74 */       snew |= snew >> 16;
/*  75 */       snew++;
/*  76 */       Object[] datanew = new Object[snew];
/*  77 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  78 */        this.data = (T[])datanew;
/*  79 */       this.and = this.data.length - 1;
/*  80 */       this.zidx = 0;
/*     */     } 
/*  82 */     this.zidx = this.zidx - ts.length + this.data.length & this.and;
/*  83 */     this.size += ts.length;
/*  84 */     int idx = this.zidx; byte b; int j; Object[] arrayOfObject;
/*  85 */     for (j = (arrayOfObject = ts).length, b = 0; b < j; ) { T t = (T)arrayOfObject[b];
/*  86 */       this.data[idx] = t;
/*  87 */       idx = idx + 1 & this.and;
/*     */       b++; }
/*     */   
/*     */   } public T tail() {
/*  91 */     T rv = this.data[this.zidx + --this.size & this.and];
/*  92 */     if (this.size < this.data.length >> 2) {
/*  93 */       Object[] datanew = new Object[this.data.length >> 1];
/*  94 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  95 */        this.data = (T[])datanew;
/*  96 */       this.and = this.data.length - 1;
/*  97 */       this.zidx = 0;
/*     */     } 
/*  99 */     return rv;
/*     */   }
/*     */   
/* 102 */   public T peekTail() { return this.data[this.zidx + this.size - 1 & this.and]; }
/*     */ 
/*     */   
/* 105 */   public int size() { return this.size; }
/*     */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\Queue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */