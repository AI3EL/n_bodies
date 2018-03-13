package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;


public class Tester {
	
	//time ==n -> proccessingNode should process bodies that have time n to time n+1
	
	static int n;
	static int delta;
	static int maxTime;
	static AtomicInteger time;
	static Buffer buffer;
	static Body[] bodies;
	static float[][][] results;
	static Thread[] threads;
	static Force force;
	
	
	public static void main(String[] args){
		
		n = 2;
		delta = 1;
		maxTime = 10;
		bodies = new Body[n];
		time = new AtomicInteger(0);
		results = new float[maxTime][n][2];
		threads = new Thread[n];
		force = new GravitationnalForce();
	
		bodies[0] = new Body(0, 100, new Vector(0,0), new Vector(0,0), new Vector(0,0));
		bodies[1] = new Body(1, 1, new Vector(10,10), new Vector(0,0), new Vector(0,0));

		
		for(int i=0; i< 2; i++){
			threads[i] = new Thread(new ProcessingNode(bodies, time, 0, force, delta, results, maxTime));
			threads[i].start();
		}
		for(int i=0; i< 2; i++){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
		for(int i=0; i< maxTime; i++){
			String line=" ";
			for(int j=0; j< n ; j++){
				line+= "(" + String.valueOf(results[i][j][0]) + "," + String.valueOf(results[i][j][1]) +")" + " ";
			}
			System.out.println(line);
		}
		
	}
	
}
