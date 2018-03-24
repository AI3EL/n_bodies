package lockfree;

// https://openclassrooms.com/courses/apprenez-a-programmer-en-java/le-fil-rouge-une-animation
/*
 * The visualizer refresh each dt*10 and takes the last image calculated
 */

import java.awt.*;
import java.awt.event.*; // Using AWT event classes and listener interfaces
import javax.swing.*;    // Using Swing's components and containers

import etc.Vector;
import etc.Buffer;

 
public class Visualizer extends JFrame {
	
	private Panneau pan;
	int n;
	float maxTime;
	long currentTime;
	long lastTime;
	int frozenTime;
	float dt;
	int maxfps = 200;
	float speedup = 1.0f;
	int interval;
	
	Clock clock;
	Buffer buffer;

	
	public Visualizer(int n, float delta, float  maxTime, Buffer buffer, int width, int height){
		pan = new Panneau(buffer.data[0]);
		this.dt = delta;
		this.interval = 1000 / maxfps;
		this.buffer=buffer;
		this.lastTime=this.currentTime;
		this.n = n;
		this.maxTime=maxTime;
		this.setTitle("n_bodies");
		this.setSize(width,height);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setContentPane(pan);
		this.setVisible(true);
		go();
	}

	
	private int lastOwnTime = 0;
	private int ownTime = 0;

	private void go(){
		lastTime = System.currentTimeMillis();
		while(ownTime < maxTime){
			currentTime = System.currentTimeMillis();
			while((ownTime - lastOwnTime)*dt*speedup*1000.0 < (currentTime - lastTime) || ownTime==0) {
				buffer.waitRead();
				ownTime++;
			}
			for(int i=0; i< n; i++){
				pan.pos[i] = buffer.data[ownTime % buffer.size][i];
			}
			pan.repaint();
			lastTime = currentTime;
			lastOwnTime = ownTime;
			currentTime = System.currentTimeMillis();
			while(currentTime - lastTime < interval ){
				currentTime = System.currentTimeMillis();
				/*
				try {
					Thread.sleep(interval  - (currentTime - lastTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				*/
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
