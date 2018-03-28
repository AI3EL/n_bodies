package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;

import lockfree.Clock;
import lockfree.NegligibleNode;
import lockfree.ProcessingNode;
import lockfree.Visualizer;

import etc.Body;
import etc.Force;
import etc.GravitationnalForce;
import etc.Vector;
import etc.Buffer;
import etc.BlockingBuffer;

public class Tester {

	/*
	 * We assume that generally, n >> nThreads
	 * clock.time ==n -> proccessingNode should process bodies that have time n to time n+1
	 * nBuffers is the number of timesteps a processing thread can go over var time
	 * Hence there are nBuffers buffers
	 *
	 * Tests Results :
	 * 22s for testGrid(2,100,1000,30,30) with no NegligibleNode
	 * 16s for testGrid(2,100,1000,30,30) with NegligibleNode with fillTime at 50 and 5% error


	 */

	static int WIDTH = 1200;
	static int HEIGHT = 800;

	static int n;
	static float delta;
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
		delta = 1.f;
		bodies = new Body[n];
		clock = new Clock();
		buffer = new BlockingBuffer(bufferSize,n);
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
		boolean[][] isNegligible = new boolean[n][n];

		int fillTime = 100;



		bodies[0] = new Body(0, 1, new Vector(10,10), new Vector(0,0), new Vector(0,0), n);
		bodies[1] = new Body(1, 1, new Vector(500,500), new Vector(0,0), new Vector(0,0), n);


		for(int i=0; i< nThreads; i++){
			threads[i] = new Thread(new ProcessingNode(bodies, clock, i, force,delta, buffer, maxTime, isNegligible, fillTime));
			threads[i].start();
		}

		Visualizer visualizer = new Visualizer(n, delta, maxTime, 1000.0f, buffer, WIDTH, HEIGHT);
	}

	/*
	 * Test with width*height bodies on a centered grid, each has mass 1
	 */
	public static void testGrid(int nThreads, int bufferSize, int maxTime, int width, int height){
		n = width * height;
		delta = 0.1f;
		bodies = new Body[n];
		clock = new Clock();
		buffer = new BlockingBuffer(bufferSize,n);
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
		int xBegin = WIDTH/2 - (width * 20)/2 ;
		int yBegin = HEIGHT/2 - (height * 20)/2 ;
		boolean[][] isNegligible = new boolean[n][n];

		int fillTime = 50;


		for(int i=0; i< width ; i++){
			for(int j=0; j<height;j++){
				bodies[j + i*height] = new Body(j+i*height, 1, new Vector(xBegin + (i+1)*20, yBegin + (j+1)*20), new Vector(0,0), new Vector(0,0), n);
			}
		}


		for(int i=0; i< nThreads; i++){
			int first = (i * n) / nThreads;
			threads[i] = new Thread(new ProcessingNode(bodies, clock, first, force, delta, buffer, maxTime, isNegligible, fillTime));
			threads[i].start();
		}

		Thread negligibleNode = new Thread(new NegligibleNode( (float)0.95, clock, maxTime, isNegligible, bodies, fillTime));
		negligibleNode.start();

		Visualizer visualizer = new Visualizer(n, delta, maxTime, 10.0f, buffer, WIDTH, HEIGHT);

	}


	public static void main(String[] args){
		testGrid(2,100,10000,30,30);
	}

}
