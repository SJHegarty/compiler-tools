/*    */ package localgoat.utilities;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class IOStreamLinker {
/*    */   private final InputStream in;
/*    */   
/*    */   public IOStreamLinker(InputStream in, OutputStream out) {
/* 11 */     this.in = in;
/* 12 */     this.out = out;
/*    */   } private final OutputStream out;
/*    */   public void transfer() throws IOException {
/* 15 */     while (this.in.available() != 0) {
/* 16 */       int value = this.in.read();
/* 17 */       if ((value & 0xFFFFFF00) != 0)
/* 18 */         return;  this.out.write(value);
/*    */     } 
/*    */   }
/*    */   
/* 22 */   public static void link(final InputStream in, final OutputStream out) { (new Thread() {
/*    */         public void run() {
/*    */           try {
/*    */             while (true) {
/* 26 */               int val = in.read();
/* 27 */               if ((val & 0xFFFFFF00) != 0)
/* 28 */                 return;  out.write(val);
/*    */             } 
/* 30 */           } catch (IOException e) {
/*    */             
/*    */             return;
/*    */           } 
/*    */         }
/* 35 */       }).start(); }
/*    */   
/*    */   public void transfer(boolean close) throws IOException {
/* 38 */     transfer();
/* 39 */     if (close) {
/* 40 */       this.in.close();
/* 41 */       this.out.close();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\IOStreamLinker.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */