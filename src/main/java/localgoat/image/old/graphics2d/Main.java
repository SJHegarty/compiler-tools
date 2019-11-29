package localgoat.image.old.graphics2d;

import localgoat.image.old.graphics2d.filter.BrushGenerator;
import localgoat.image.old.graphics2d.filter.PaintBrush;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;


public class Main{
	public static void main(String... args){
		JFrame frame = new JFrame();
		int w = 800, h = 600;
		final PixelGrid6 pg = new PixelGrid6(w, h);


		Container c = new Container();
		c.setLayout(new BorderLayout());

		Component image_panel = pg.getImagePanel();


		Container side = new Container();
		side.setLayout(new BoxLayout(side, 1));
		side.add(new JScrollPane(pg.getThumbPanel(), 22, 31));

		Container buttons = new Container();
		buttons.setLayout(new BoxLayout(buttons, 0));

		JButton lbutton = new JButton("+");
		lbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				pg.addLayer();
			}
		});
		buttons.add(lbutton);

		buttons.add(Box.createHorizontalGlue());
		side.add(buttons);

		c.add(new JScrollPane(image_panel), "Center");
		c.add(side, "East");


		final ColourChooser chooser0 = new ColourChooser();
		final ColourChooser chooser1 = new ColourChooser();
		Container south = new Container();
		south.setLayout(new BoxLayout(south, 0));
		south.add(chooser0);
		south.add(chooser1);
		c.add(south, "South");
		ColourChangeListener ccl = new ColourChangeListener(){
			public void ColourChanged(){
				pg.setFilter((Filter) new PaintBrush(pg, chooser0.getSelectedColour(), chooser1.getSelectedColour(), 50), 1);
			}
		};
		pg.setFilter((Filter) new FunctionFilter(pg, BrushGenerator.getCircle(-16777216, -16777216, 60), Function.ERASE), 3);

		chooser0.addColourChangeListener(ccl);
		chooser1.addColourChangeListener(ccl);
		chooser0.trigger();

		frame.getContentPane().add(c);
		frame.setSize(w, h);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(3);
		frame.setVisible(true);
	}
}