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
import systems.TwoBodiesSystem;
import systems.GridSystem;
import systems.SolarSystem;

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
	public static void test2b(int nThreads, int bufferSize, int maxTime){
		delta = 0.1f;

		PSystem system = new TwoBodiesSystem();
		int n = system.getBodies().length;

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, 10.0f, buffer, WIDTH, HEIGHT);
		engine.join();
	}

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
	
	public static void testSolarSystem(int nThreads, int bufferSize, int maxTime, int n){
		delta = 1.0f;
		
		PSystem system = new SolarSystem(n, WIDTH, HEIGHT);

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, 20.0f, buffer, WIDTH, HEIGHT);
		engine.join();

	}

	public static void main(String[] args){
		//test2b(1,1000,500);
		//testGrid(2,10000,10,2,1);
		//testGrid(2,1000,1000,15,15);
		testSolarSystem(2,1000,500,30);
	}


}
