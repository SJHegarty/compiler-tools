/*    */ package localgoat.utilities;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class MultiOutputStream extends OutputStream {
/*    */   private final OutputStream[] streams;
/*    */   
/*    */   public MultiOutputStream(OutputStream... streams) {
/* 10 */     this.streams = new OutputStream[streams.length];
/* 11 */     for (int i = 0; i < streams.length; ) { this.streams[i] = streams[i]; i++; }
/*    */      } public void write(int arg0) throws IOException { byte b; int i;
/*    */     OutputStream[] arrayOfOutputStream;
/* 14 */     for (i = (arrayOfOutputStream = this.streams).length, b = 0; b < i; ) { OutputStream o = arrayOfOutputStream[b]; o.write(arg0); b++; }
/*    */      }
/*    */ 
/*    */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\MultiOutputStream.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */