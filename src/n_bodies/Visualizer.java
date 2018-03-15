package n_bodies;

// https://openclassrooms.com/courses/apprenez-a-programmer-en-java/le-fil-rouge-une-animation

import java.awt.*;
import java.awt.event.*; // Using AWT event classes and listener interfaces
import javax.swing.*;    // Using Swing's components and containers

 
public class Visualizer extends JFrame {
	
	private Panneau pan = new Panneau();
	int n;
	
	public Visualizer(int n){
		this.n = n;
		this.setTitle("n_bodies");
		this.setSize(1200,800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setContentPane(pan);
		this.setVisible(true);
		go();
	}
	
	
	private class Panneau extends JPanel{
		
		public Vector[] pos = new Vector[n];
		
		public void paintComponent(Graphics g){
			g.setColor(Color.black);
			for(int i=0; i<n ;i++){
				g.fillOval((int) pos[i].x, (int) pos[i].y, 10, 10);
			}
		}					
	}
}
