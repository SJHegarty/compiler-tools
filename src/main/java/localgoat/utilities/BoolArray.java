/*    */ package localgoat.utilities;
/*    */ 
/*    */ 
/*    */ public class BoolArray
/*    */ {
/*    */   private final int[] data;
/*    */   private final int length;
/*  8 */   private static final int[] MASK = new int[32]; static  {
/*  9 */     for (int i = 0; i < MASK.length; ) { MASK[i] = 1 << 31 - i; i++; }
/*    */   
/*    */   } public BoolArray(int length) {
/* 12 */     this.data = new int[length + 31 >> 5];
/* 13 */     this.length = length;
/*    */   }
/*    */   public void assign(int index, boolean value) {
/* 16 */     if (value) {
/* 17 */       this.data[index >> 5] = this.data[index >> 5] | MASK[index & 0x1F];
/*    */       return;
/*    */     } 
/* 20 */     this.data[index >> 5] = this.data[index >> 5] & (MASK[index & 0x1F] ^ 0xFFFFFFFF);
/*    */   }
/*    */   
/* 23 */   public boolean get(int index) { return ((this.data[index >> 5] & MASK[index & 0x1F]) != 0); }
/*    */ 
/*    */   
/* 26 */   public int getLength() { return this.length; }
/*    */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\BoolArray.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */