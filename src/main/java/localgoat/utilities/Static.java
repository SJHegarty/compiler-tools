/*    */ package localgoat.utilities;
/*    */ 
/*    */ import java.io.FileInputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ 
/*    */ public class Static
/*    */ {
/*  9 */   public static String utl_readfile(String fname) throws IOException { return utl_readis(new FileInputStream(fname)); }
/*    */   
/*    */   public static String utl_readis(InputStream in) throws IOException {
/* 12 */     StringBuilder builder = new StringBuilder();
/* 13 */     while (in.available() != 0) {
/* 14 */       int i = in.read();
/* 15 */       if ((i & 0xFFFFFF00) != 0)
/* 16 */         break;  builder.append((char)i);
/*    */     } 
/* 18 */     return builder.toString();
/*    */   }
/*    */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\Static.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */