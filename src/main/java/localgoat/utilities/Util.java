/*     */ package localgoat.utilities;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.util.Iterator;
/*     */ import javax.swing.Box;
/*     */ import javax.swing.BoxLayout;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Util
/*     */ {
/*     */   public static Iterable<Integer> n(final int value) {
/*  18 */     return new Iterable<Integer>() {
/*     */         public Iterator<Integer> iterator() {
/*  20 */           return new Iterator<Integer>() {
/*     */               int index;
/*     */               
/*  23 */               public boolean hasNext() { return (this.index < value); }
/*     */ 
/*     */               
/*  26 */               public Integer next() { return Integer.valueOf(this.index++); }
/*     */ 
/*     */               
/*  29 */               public void remove() { this.index++; }
/*     */             };
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*  35 */   private static final Dimension d = new Dimension(150, 30);
/*     */   public static JTextField tf(String label) {
/*  37 */     JTextField rv = new JTextField(label);
/*  38 */     rv.setPreferredSize(d);
/*  39 */     rv.setMaximumSize(d);
/*  40 */     rv.setMinimumSize(d);
/*     */     
/*  42 */     return rv;
/*     */   }
/*  44 */   static int HEIGHT = 30;
/*     */   public static <Q> JComboBox cb(Object... os) {
/*  46 */     JComboBox rv = new JComboBox(os);
/*  47 */     return rv;
/*     */   }
/*     */   public static JTextField tf() {
/*  50 */     JTextField rv = new JTextField();
/*  51 */     return rv;
/*     */   }
/*     */   public static Container gx(Component c, Container cx) {
/*  54 */     setX(cx);
/*  55 */     cx.add(c);
/*  56 */     cx.add(Box.createHorizontalGlue());
/*  57 */     return cx;
/*     */   }
/*     */   
/*  60 */   public static Container gx(Component c) { return gx(c, new Container()); }
/*     */   
/*     */   public static Container gy(Component c, Container cy) {
/*  63 */     setY(cy);
/*  64 */     cy.add(c);
/*  65 */     cy.add(Box.createVerticalGlue());
/*  66 */     return cy;
/*     */   }
/*     */   
/*  69 */   public static Container gy(Component c) { return gy(c, new Container()); }
/*     */ 
/*     */   
/*  72 */   public static void exc() { throw new RuntimeException(); }
/*     */   
/*     */   public static String shortLabel(String s) {
/*  75 */     int index = s.indexOf(' ');
/*  76 */     if (index == -1) return s; 
/*  77 */     return s.substring(0, index); } public static void p(Object... os) {
/*     */     byte b;
/*     */     int i;
/*     */     Object[] arrayOfObject;
/*  81 */     for (i = (arrayOfObject = os).length, b = 0; b < i; ) { Object o = arrayOfObject[b]; System.out.println(o); b++; }
/*     */   
/*     */   }
/*     */ 
/*     */   
/*  86 */   public static <T> T[] array(Object... ts) { return (T[])ts; }
/*     */   
/*     */   public static Container cx() {
/*  89 */     Container rv = new Container();
/*  90 */     rv.setLayout(new BoxLayout(rv, 0));
/*  91 */     return rv;
/*     */   }
/*     */   public static Container cy() {
/*  94 */     Container rv = new Container();
/*  95 */     rv.setLayout(new BoxLayout(rv, 1));
/*     */     
/*  97 */     return rv;
/*     */   }
/*     */   
/* 100 */   public static void setX(Container c) { c.setLayout(new BoxLayout(c, 0)); }
/*     */ 
/*     */   
/* 103 */   public static void setY(Container c) { c.setLayout(new BoxLayout(c, 1)); }
/*     */ 
/*     */   
/* 106 */   public static void setB(Container c) { c.setLayout(new BorderLayout()); }
/*     */ }


/* Location:              C:\Users\Tony\Downloads\lib.jar\\utilities\Util.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.2
 */