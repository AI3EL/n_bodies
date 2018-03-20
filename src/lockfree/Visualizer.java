package lockfree;

// https://openclassrooms.com/courses/apprenez-a-programmer-en-java/le-fil-rouge-une-animation
/*
 * The visualizer refresh each dt*10 and takes the last image calculated
 */

import java.awt.*;
import java.awt.event.*; // Using AWT event classes and listener interfaces
import javax.swing.*;    // Using Swing's components and containers


import etc.Vector;

 
public class Visualizer extends JFrame {
	
	private Panneau pan;
	int n;
	int maxTime;
	long currentTime;
	long lastTime;
	int frozenTime;
	int dt;
	
	Clock clock;
	Buffer buffer;

	
	public Visualizer(int n, int maxTime, Clock clock, Buffer buffer, int width, int height){
		pan = new Panneau(buffer.data[0]);
		this.dt=1;
		this.buffer=buffer;
		this.lastTime=this.currentTime;
		this.n = n;
		this.maxTime=maxTime;
		this.clock=clock;
		this.setTitle("n_bodies");
		this.setSize(width,height);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setContentPane(pan);
		this.setVisible(true);
		go();
	}
	
	private void go(){
		while(clock.time.get()<maxTime){
			currentTime = System.currentTimeMillis();
			if(currentTime - lastTime < 10 * dt){
				try {
					Thread.sleep(10*dt - (currentTime - lastTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				lastTime = currentTime;
				frozenTime = clock.time.get();
				for(int i=0; i< n; i++){
					//This is ok because buffer is filled for clock.time.get()
					pan.pos[i] = buffer.data[frozenTime%buffer.size][i];
				}
				pan.repaint();
			}
		}
	}
	
	
	private class Panneau extends JPanel{
		
		public Vector[] pos;
		
		public Panneau(Vector[] pos){
			this.pos = pos;
		}
		
		//paintComponent is called by pan.repaint()
		
		public void paintComponent(Graphics g){
			//Erase everything
			g.setColor(Color.white);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			g.setColor(Color.black);
			for(int i=0; i<n ;i++){
				g.fillOval((int) pos[i].x, (int) pos[i].y, 10, 10);
			}
		}					
	}
}
