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
	float maxTime;
	long currentTime;
	long lastTime;
	float dt;
	int maxfps = 200;
	float speedup;
	int interval;
	
	Clock clock;
	Buffer buffer;

	private int currentFrame = 0;
	private long startTime;

	private int displayedFrames = 0;
	private int droppedFrames = 0;
	private int usedFrames = 1;
	private int underruns = 0;
	private long lastStatus = 0;

	
	public Visualizer(float delta, float  maxTime, float speedup, Buffer buffer, int width, int height){
		pan = new Panneau(buffer.pos[0], buffer.radiuses[0], buffer.nBody[0],0);
		this.dt = delta;
		this.interval = 1000 / maxfps;
		this.buffer=buffer;
		this.lastTime=this.currentTime;
		this.maxTime=maxTime;
		this.speedup = speedup;
		

		this.setTitle("n_bodies");
		this.setSize(width,height);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setContentPane(pan);
		this.setVisible(true);

		go();
	}

	
	private void go(){
		startTime = System.currentTimeMillis();
		boolean underrun;
		int retrieved;
		int nextFrame;
		while(currentFrame < maxTime){
			underrun = false;
			retrieved = 0;
			currentTime = System.currentTimeMillis();
			nextFrame = (int)(speedup * (currentTime - startTime) / dt / 1000f);
			while(currentFrame < nextFrame && !underrun) {
				if(buffer.pollRead()) {
					currentFrame++;
					retrieved++;
				} else {
					underrun = true;
					underruns++;
				}
			}
			usedFrames += retrieved;
			if(retrieved > 1) {
				droppedFrames += (retrieved - 1);
			}
			
			pan.frameNum = currentFrame % buffer.size;
			pan.n = buffer.nBody[currentFrame % buffer.size];
			for(int i=0; i<pan.n ; i++){
				pan.pos[i] = buffer.pos[currentFrame % buffer.size][i];
				pan.radiuses[i] = buffer.radiuses[currentFrame % buffer.size][i];
			}
			pan.repaint();

			displayedFrames++;

			if(currentTime - lastStatus > 2000) {
				status();
			}
			lastTime = currentTime;
			currentTime = System.currentTimeMillis();
			if(currentTime - lastTime < interval ){
				try {
					Thread.sleep(interval  - (currentTime - lastTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			currentTime = System.currentTimeMillis();
			//if(displayedFrames > 50) return;
		}
	}
	
	private void status() {
		long currentTime = System.currentTimeMillis();
		long dobs = currentTime - lastStatus;
		System.out.println(String.format(
					"[%d ms] Shown %d Computed %d Dropped %d ShownUnique %d Underrun %d FPS %.1f (max %d) Speedup %.2f (goal %.2f)",
					dobs,
					displayedFrames,
					usedFrames,
					droppedFrames,
					usedFrames - droppedFrames,
					underruns,
					displayedFrames * 1000f / dobs,
					maxfps,
					usedFrames * dt * 1000f / dobs,
					speedup));
		lastStatus = currentTime;
		displayedFrames = 0;
		droppedFrames = 0;
		usedFrames = 0;
		underruns = 0;
	}
		
	
	private class Panneau extends JPanel{
		
		public Vector[] pos;
		public float[] radiuses;
		public int n;
		public int frameNum; // For DEBUG

		
		// Initialises Panneau with a copy of pos
		public Panneau(Vector[] pos, float[] radiuses, int n, int frameNum){
			this.radiuses = new float[radiuses.length];
			this.pos = new Vector[pos.length];
			this.n=n;
			this.frameNum = frameNum;
			System.arraycopy(pos, 0, this.pos, 0, n);
			System.arraycopy(radiuses, 0, this.radiuses, 0, n);

		}
		
		//paintComponent is called by pan.repaint()
		
		public void paintComponent(Graphics g){
			g.setColor(Color.black);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			g.setColor(Color.white);
			for(int i=0; i<n ;i++){
				g.fillOval((int) pos[i].x, (int) pos[i].y, (int) radiuses[i], (int) radiuses[i]);
			}
		}					
	}
}
