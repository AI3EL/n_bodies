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

		int fillTime = 100;
		

		bodies[0] = new Body(0,0, 100, 10, new Vector(10,10), new Vector(0,0), new Vector(0,0), n);
		bodies[1] = new Body(0,1, 100, 10, new Vector(30,30), new Vector(0,0), new Vector(0,0), n);

		buffer = new BlockingBuffer(bufferSize,n, bodies );
		SafeCounter counter = new SafeCounter(0);

		for(int i=0; i< nThreads; i++){
			threads[i] = new Thread(new ProcessingNode(counter, clock, i, force,delta, buffer, maxTime, isNegligible, fillTime));
			threads[i].start();
		}

		//Visualizer visualizer = new Visualizer(delta, maxTime, 1.0f, buffer, WIDTH, HEIGHT);
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
	
	/*
	public static void testSolarSystem(int nThreads, int bufferSize, int maxTime, int n){
		
		//v= sqrt(GM/r) but here G = 1
		final int sunRadius = 50;
		final float sunMass = 1000.0f;
		final int planetRadius=10;
		final float planetMass = 10.0f;
		
		delta = 1.0f;
		bodies = new Body[n];
		clock = new Clock();
		threads = new Thread[nThreads];
		force = new GravitationnalForce();
		int xBegin = WIDTH/2 - ( sunRadius)/2 ;
		int yBegin = HEIGHT/2 - (sunRadius)/2 ;
		boolean[][] isNegligible = new boolean[n][n];

		int fillTime = 50;

		bodies[0] = new Body(0,0, sunMass, sunRadius, new Vector(xBegin, yBegin), new Vector(0,0), new Vector(0,0), n);

		for(int i=1; i< n ; i++){
			double r = (Math.random() * (HEIGHT/2-sunRadius-planetRadius)) + sunRadius + planetRadius;
			double teta = (Math.random()-0.5) * 2 * Math.PI;
			float x =(float) (xBegin + r*Math.cos(teta));
			float y =(float) (yBegin + r*Math.sin(teta));
			float realD = (float) Math.sqrt(sunMass/r);

			float vx =(float) (-Math.sin(teta)*realD);
			float vy =(float) (Math.cos(teta)*realD);

			bodies[i] = new Body(0,i, planetMass, planetRadius, new Vector(x, y), new Vector(vx,vy), new Vector(0,0), n);
		}

		buffer = new BlockingBuffer(bufferSize,n,bodies );

		SafeCounter counter = new SafeCounter(0);
		
		for(int i=0; i< nThreads; i++){
			int first = (i * n) / nThreads;
			threads[i] = new Thread(new ProcessingNode(counter, clock, first, force, delta, buffer, maxTime, isNegligible, fillTime));
			threads[i].start();
		}

		Thread negligibleNode = new Thread(new NegligibleNode( (float)0.95, clock, maxTime, isNegligible, buffer, fillTime));
		negligibleNode.start();

		Visualizer visualizer = new Visualizer(delta, maxTime, 20.0f, buffer, WIDTH, HEIGHT);
	}
	*/

	public static void main(String[] args){
		//test2b(1,1000,500);
		//testGrid(2,10000,10,2,1);
		testGrid(2,1000,1000,15,15);
		// testSolarSystem(2,1000,500,30);
	}


}
