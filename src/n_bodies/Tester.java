package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;

import lockfree.Buffer;
import lockfree.Clock;
import lockfree.ProcessingNode;
import lockfree.Visualizer;

import etc.Body;
import etc.Force;
import etc.GravitationnalForce;
import etc.Vector;

public class Tester {
	
	/*
	 * clock.time ==n -> proccessingNode should process bodies that have time n to time n+1
	 * nBuffers is the number of timesteps a processing thread can go over var time
	 * Hence there are nBuffers buffers
	 */
	
	static int WIDTH = 1200;
	static int HEIGHT = 800;
	
	static int n;
	static int delta;
	static Clock clock;
	static Body[] bodies;
	static Thread[] threads;
	static Force force;
	static Buffer buffer;
	
	/*
	 * Test with 2 bodies, of mass 1
	 */
	public static void test2b(int nThreads, int bufferSize, int maxTime){
		n = 2;
		delta = 1;
		bodies = new Body[n];
		clock = new Clock();
		buffer = new Buffer(bufferSize,n);
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
	
		bodies[0] = new Body(0, 1, new Vector(10,10), new Vector(0,0), new Vector(0,0));
		bodies[1] = new Body(1, 1, new Vector(500,500), new Vector(0,0), new Vector(0,0));

		
		for(int i=0; i< nThreads; i++){
			threads[i] = new Thread(new ProcessingNode(bodies, clock, 0, force, delta, buffer, maxTime));
			threads[i].start();
		}
		
		Visualizer visualizer = new Visualizer(n, maxTime, clock, buffer, WIDTH, HEIGHT);
	}
	
	/*
	 * Test with width*height bodies on a centered grid, each has mass 1
	 */
	public static void testGrid(int nThreads, int bufferSize, int maxTime, int width, int height){
		n = width * height;
		delta = 1;
		bodies = new Body[n];
		clock = new Clock();
		buffer = new Buffer(bufferSize,n);
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
		int xBegin = WIDTH/2 - (width * 20)/2 ;
		int yBegin = HEIGHT/2 - (height * 20)/2 ;

	
		for(int i=0; i< width ; i++){
			for(int j=0; j<height;j++){
				bodies[j + i*height] = new Body(j+i*height, 1, new Vector(xBegin + (i+1)*20, yBegin + (j+1)*20), new Vector(0,0), new Vector(0,0));
			}
		}

		
		for(int i=0; i< nThreads; i++){
			int first = (i * n) / nThreads;
			threads[i] = new Thread(new ProcessingNode(bodies, clock, first, force, delta, buffer, maxTime));
			threads[i].start();
		}
		
		Visualizer visualizer = new Visualizer(n, maxTime, clock, buffer, WIDTH, HEIGHT);

	}
	
	
	
	
	public static void main(String[] args){
		/*
		 * Works well on AI3EL's computer
		 */
		testGrid(4,100,1000,30,30);
	}
	
}
