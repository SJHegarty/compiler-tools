/*    */ package localgoat.utilities;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class StringOutputStream
/*    */   extends OutputStream {
/*    */   private final OutputStream out;
/*    */   
/* 10 */   public StringOutputStream(OutputStream out) { this.out = out; }
/*    */ 
/*    */   
/* 13 */   public void write(String... strings) throws IOException { writed("\n", strings); }
/*    */   
/*    */   public void writed(String delimiter, String... strings) throws IOException {
/* 16 */     char[] dca = delimiter.toCharArray(); byte b; int i; String[] arrayOfString;
/* 17 */     for (i = (arrayOfString = strings).length, b = 0; b < i; ) { String s = arrayOfString[b];
/* 18 */       write(s.toCharArray());
/* 19 */       write(dca);
/*    */       b++; }
/*    */   
/*    */   } private void write(char[] chars) throws IOException {
/* 23 */     for (int i = 0; i < chars.length; i++) {
/* 24 */       this.out.write(chars[i]);
/*    */     }
/*    */   }
/*    */   
/* 28 */   public void write(int arg0) throws IOException { this.out.write(arg0); }
/*    */ 
/*    */   
/* 31 */   public void close() throws IOException { this.out.close(); }
/*    */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\StringOutputStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */