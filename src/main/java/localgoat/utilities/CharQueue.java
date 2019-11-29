/*     */ package localgoat.utilities;public class CharQueue {
/*     */   private static final int dbits = 2;
/*     */   
/*     */   public static void main(String... args) {
/*   5 */     CharQueue cq = new CharQueue();
/*   6 */     for (int i = 0; i < 2000; ) { cq.append(new Object[] { "aaaaaqbcaaaaabcaaaaaabcaaaaaobd" }); i++; }
/*     */     
/*   8 */     char[] c = cq.toCharArray();
/*   9 */     long time = System.currentTimeMillis();
/*  10 */     for (int i = 0; i < 2000; i++) {
/*  11 */       CharQueue n = new CharQueue();
/*  12 */       n.append(c);
/*  13 */       n.replace("aaaaabc", "g");
/*     */     } 
/*     */     
/*  16 */     time = System.currentTimeMillis() - time;
/*  17 */     System.out.println(time);
/*     */   }
/*     */   private int zidx;
/*  20 */   public void insert(int index, String data) { insert(index, data.toCharArray()); }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  26 */   private char[] data = new char[4];
/*  27 */   private int and = this.data.length - 1;
/*     */   private int size;
/*     */   
/*     */   public void append(Object... objects) {
/*     */     byte b;
/*     */     int i;
/*     */     Object[] arrayOfObject;
/*  34 */     for (i = (arrayOfObject = objects).length, b = 0; b < i; ) { Object o = arrayOfObject[b]; append(String.valueOf(o).toCharArray()); b++; }
/*     */   
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void append(char c) {
/*  41 */     if (this.data.length == this.size) expand(this.size << 1); 
/*  42 */     this.data[this.zidx + this.size++ & this.and] = c;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void append(char... cs) {
/*  50 */     int snew = this.size + cs.length;
/*  51 */     if (this.data.length < snew) expand(exp(snew));
/*     */     
/*  53 */     int idx = this.zidx + this.size & this.and;
/*  54 */     int length = this.data.length - idx;
/*  55 */     if (cs.length > length) {
/*  56 */       System.arraycopy(cs, 0, this.data, idx, length);
/*  57 */       System.arraycopy(cs, length, this.data, 0, cs.length - length);
/*  58 */       this.size += cs.length;
/*     */     } else {
/*     */       
/*  61 */       System.arraycopy(cs, 0, this.data, idx, cs.length);
/*  62 */       this.size += cs.length;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public char head() {
/*  70 */     char rv = this.data[this.zidx];
/*  71 */     this.zidx = this.zidx + 1 & this.and;
/*  72 */     this.size--;
/*  73 */     if (this.size < this.data.length >> 2) {
/*  74 */       char[] datanew = new char[this.data.length >> 1];
/*     */       
/*  76 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/*  77 */        this.data = datanew;
/*  78 */       this.and = this.data.length - 1;
/*  79 */       this.zidx = 0;
/*     */     } 
/*  81 */     return rv;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  88 */   public char peekHead() { return this.data[this.zidx]; }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void prepend(Object... objects) {
/*  95 */     for (int i = objects.length - 1; i >= 0; i--) {
/*  96 */       prepend(String.valueOf(objects[i]).toCharArray());
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void prepend(char c) {
/* 104 */     if (this.data.length == this.size) expand(this.size << 1); 
/* 105 */     this.zidx = this.zidx + this.data.length - 1 & this.and;
/* 106 */     this.data[this.zidx] = c;
/* 107 */     this.size++;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void prepend(char... cs) {
/* 115 */     int snew = this.size + cs.length;
/* 116 */     if (this.data.length < snew) expand(exp(snew));
/*     */     
/* 118 */     this.zidx = this.zidx - cs.length + this.data.length & this.and;
/* 119 */     this.size += cs.length;
/* 120 */     int length = this.data.length - this.zidx;
/* 121 */     if (cs.length > length) {
/* 122 */       System.arraycopy(cs, 0, this.data, this.zidx, length);
/* 123 */       System.arraycopy(cs, length, this.data, 0, cs.length - length);
/*     */     } else {
/* 125 */       System.arraycopy(cs, 0, this.data, this.zidx, cs.length);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public char tail() {
/* 133 */     char rv = this.data[this.zidx + --this.size & this.and];
/* 134 */     if (this.size < this.data.length >> 2) {
/* 135 */       char[] datanew = new char[this.data.length >> 1];
/* 136 */       for (int i = 0; i < this.size; ) { datanew[i] = this.data[i + this.zidx & this.and]; i++; }
/* 137 */        this.data = datanew;
/* 138 */       this.and = this.data.length - 1;
/* 139 */       this.zidx = 0;
/*     */     } 
/* 141 */     return rv;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 148 */   public char peekTail() { return this.data[this.zidx + this.size - 1 & this.and]; }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 155 */   public int size() { return this.size; }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void replace(char o, char n) {
/* 163 */     for (int i = 0; i < this.data.length; i++) {
/* 164 */       if (this.data[i] == o) this.data[i] = n;
/*     */     
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 174 */   public void replace(String o, String n) { replace(o.toCharArray(), n.toCharArray()); }
/*     */   
/*     */   private void replace(char[] co, char[] cn) {
/* 177 */     char[] data = toCharArray();
/* 178 */     this.size = this.zidx = 0;
/* 179 */     int limit = data.length - co.length + 1;
/* 180 */     boolean[] dp = new boolean[co.length];
/* 181 */     for (int i = 0; co[i] == co[0]; ) { dp[i] = true; i++; }
/*     */      int i;
/* 183 */     label43: for (i = 0; i < limit; ) {
/* 184 */       if (data[i] == co[0]) {
/* 185 */         int depth = 1;
/*     */         
/*     */         while (true) {
/* 188 */           if (depth >= co.length) {
/*     */ 
/*     */ 
/*     */             
/* 192 */             append(cn);
/* 193 */             i += co.length; continue label43;
/*     */           } 
/*     */           if (data[i + depth] != co[depth]) {
/* 196 */             if (dp[depth - 1] && data[i + depth] == co[0]) {
/* 197 */               append(co[0]);
/* 198 */               if (++i < limit)
/*     */                 continue;  break;
/*     */             } 
/* 201 */             for (int u = 0; u < depth; ) { append(co[u]); u++; }
/* 202 */              i += depth; continue;
/*     */           }  depth++;
/*     */         } 
/*     */         break;
/*     */       } 
/* 207 */       append(data[i++]);
/*     */     } 
/* 209 */     if (i < limit) i = limit; 
/* 210 */     for (; i < data.length; i++)
/* 211 */       append(data[i]); 
/*     */   }
/*     */   
/*     */   public void insert(int index, char... cs) {
/* 215 */     if (index < this.size >> 1) {
/* 216 */       char[] pre = toCharArray(0, index);
/* 217 */       this.zidx = this.zidx + index & this.and;
/* 218 */       this.size -= index;
/* 219 */       prepend(cs);
/* 220 */       prepend(pre);
/*     */     } else {
/*     */       
/* 223 */       char[] post = toCharArray(index, this.size);
/* 224 */       this.size -= post.length;
/* 225 */       append(cs);
/* 226 */       append(post);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 233 */   public String toString() { return new String(toCharArray()); }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public char[] toCharArray() {
/* 240 */     char[] rv = new char[this.size];
/* 241 */     int length = this.data.length - this.zidx;
/* 242 */     if (length < this.size) {
/* 243 */       System.arraycopy(this.data, this.zidx, rv, 0, length);
/* 244 */       System.arraycopy(this.data, 0, rv, length, this.size - length);
/*     */     } else {
/*     */       
/* 247 */       System.arraycopy(this.data, this.zidx, rv, 0, this.size);
/*     */     } 
/* 249 */     return rv;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private char[] toCharArray(int idx0, int idx1) {
/* 255 */     char[] rv = new char[idx1 - idx0];
/* 256 */     for (int i = idx0; i < idx1; i++) {
/* 257 */       rv[i - idx0] = this.data[i + this.zidx & this.and];
/*     */     }
/* 259 */     return rv;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final int exp(int v) {
/* 267 */     v |= v >> 1;
/* 268 */     v |= v >> 2;
/* 269 */     v |= v >> 4;
/* 270 */     v |= v >> 8;
/* 271 */     v |= v >> 16;
/*     */     
/* 273 */     return ++v;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final void expand(int c) {
/* 280 */     char[] datanew = new char[c];
/* 281 */     int length = this.data.length - this.zidx;
/* 282 */     System.arraycopy(this.data, this.zidx, datanew, 0, length);
/* 283 */     System.arraycopy(this.data, 0, datanew, length, this.zidx);
/* 284 */     this.data = datanew;
/* 285 */     this.and = this.data.length - 1;
/* 286 */     this.zidx = 0;
/*     */   }
/*     */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\CharQueue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */