/*    */ package localgoat.utilities;
/*    */ 
/*    */ 
/*    */ public class Capsule<T>
/*    */ {
/*    */   private T t;
/*    */   
/*    */   public Capsule() {}
/*    */   
/* 10 */   public Capsule(T t) { this.t = t; }
/*    */ 
/*    */ 
/*    */   
/* 14 */   public synchronized void setT(T t) { this.t = t; }
/*    */ 
/*    */   
/* 17 */   public synchronized T getT() { return this.t; }
/*    */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\Capsule.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */