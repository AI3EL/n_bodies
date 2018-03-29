package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import etc.Body;
import etc.Force;
import etc.GravitationnalForce;
import etc.Vector;
import etc.Buffer;
import etc.BlockingBuffer;
import etc.Engine;

import lockfree.Clock;
import lockfree.LockfreeEngine;
import lockfree.Visualizer;

import systems.PSystem;
import systems.GridSystem;

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

	static float delta;

	/*
	 * Test with 2 bodies, of mass 1
	 */
	/*
	public static void test2b(int nThreads, int bufferSize, int maxTime){
		n = 2;
		delta = 1.0f;
		bodies = new Body[n];
		clock = new Clock();
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
		boolean[][] isNegligible = new boolean[n][n];
		Vector[] initBuffPos = new Vector[n];
		float[] initBuffRadiuses = new float[n];
		Lock mergeLock = new ReentrantLock();

		int fillTime = 100;
		
		int[] nBody = new int[1];
		nBody[0] = n;

		bodies[0] = new Body(0,0, 100, 10, new Vector(10,10), new Vector(0,0), new Vector(0,0), n);
		bodies[1] = new Body(0,1, 100, 10, new Vector(30,30), new Vector(0,0), new Vector(0,0), n);

		for(int i=0; i< n ; i++){
			initBuffPos[i] = bodies[i].pos;
			initBuffRadiuses[i] = bodies[i].radius;
		}
		buffer = new BlockingBuffer(bufferSize,n, initBuffPos, initBuffRadiuses );
		for(int i=0; i< nThreads; i++){
			threads[i] = new Thread(new ProcessingNode(nBody,bodies, clock, i, force,delta, buffer, maxTime, isNegligible, fillTime, mergeLock));
			threads[i].start();
		}

		Visualizer visualizer = new Visualizer(delta, maxTime, 10.0f, buffer, WIDTH, HEIGHT);
	}
	*/

	/*
	 * Test with width*height bodies on a centered grid, each has mass 1
	 */
	public static void testGrid(int nThreads, int bufferSize, int maxTime, int width, int height){
		delta = 1.0f;

		PSystem system = new GridSystem(width, height, WIDTH, HEIGHT);
		int n = system.getBodies().length;

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, 5.0f, buffer, WIDTH, HEIGHT);
		engine.join();

	}

	public static void main(String[] args){
		//test2b(1,1000,500);
		//testGrid(2,10000,10,2,1);
		testGrid(2,1000,1000,2,1);
	}


}
