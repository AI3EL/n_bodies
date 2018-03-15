package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;

public class Tester {
	
	//time ==n -> proccessingNode should process bodies that have time n to time n+1
	
	static int n;
	static int delta;
	static AtomicInteger time;
	static Buffer buffer;
	static Body[] bodies;
	static float[][][] results;
	static Thread[] threads;
	static Force force;
	
	public static void test2b(int nThreads, int maxTime){
		n = 2;
		delta = 1;
		bodies = new Body[n];
		time = new AtomicInteger(0);
		results = new float[maxTime][n][2];
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
	
		bodies[0] = new Body(0, 100, new Vector(0,0), new Vector(0,0), new Vector(0,0));
		bodies[1] = new Body(1, 1, new Vector(10,10), new Vector(0,0), new Vector(0,0));

		
		for(int i=0; i< nThreads; i++){
			threads[i] = new Thread(new ProcessingNode(bodies, time, 0, force, delta, results, maxTime));
			threads[i].start();
		}
		for(int i=0; i< nThreads; i++){
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
	
	public static void testGrid(int nThreads, int maxTime, int width, int height){
		n = width * height;
		delta = 1;
		bodies = new Body[n];
		time = new AtomicInteger(0);
		results = new float[maxTime][n][2];
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
	
		for(int i=0; i< width ; i++){
			for(int j=0; j<height;j++){
				bodies[j + i*height] = new Body(j+i*height, 10, new Vector(i,j), new Vector(0,0), new Vector(0,0));
			}
		}

		
		for(int i=0; i< nThreads; i++){
			threads[i] = new Thread(new ProcessingNode(bodies, time, 0, force, delta, results, maxTime));
			threads[i].start();
		}
		for(int i=0; i< nThreads; i++){
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
	
	
	
	
	public static void main(String[] args){
		/*
		 * 	testGrid(4,100, 10, 100);	//Takes like 3s
		 * 	testGrid(4,100, 100, 100);	//Takes more than 2'
		 */
		
	}
	
}
