package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import etc.Body;
import etc.ElectricForce;
import etc.Force;
import etc.GravitationnalForce;
import etc.Vector;
import etc.Buffer;
import etc.BlockingBuffer;
import etc.Engine;

import lockfree.LockfreeEngine;
import lockfree.Visualizer;

import prescheduled.PrescheduledEngine;

import systems.ElectricGridSystem;
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

	/*
	 * Test with 2 bodies, of mass 1
	 */
	public static void test2b(int nThreads, int bufferSize, int maxTime){
		float delta = 0.1f;

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

		float delta = 0.1f;
		PSystem system = new GridSystem(width, height, WIDTH, HEIGHT);
		int n = system.getBodies().length;

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, 10.0f, buffer, WIDTH, HEIGHT);
		engine.join();
	}
	
	public static void testGridElectric(int nThreads, int bufferSize, int maxTime, int width, int height){
		float delta = 0.1f;
		PSystem system = new ElectricGridSystem(width, height, WIDTH, HEIGHT);
		int n = system.getBodies().length;

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, 10.0f, buffer, WIDTH, HEIGHT);
		engine.join();

	}
	
	public static void testSolarSystem(int nThreads, int bufferSize, int maxTime, int n){
		float delta = 1.0f;
		
		PSystem system = new SolarSystem(n, WIDTH, HEIGHT);

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, 20.0f, buffer, WIDTH, HEIGHT);
		engine.join();

	}
	
	
	
	// Serious bugs on emorice's setup
	// On AI3EL's too :(
	public static void threadScalabilityTest() {
		float delta = 0.1f;
		int width = 5;
		int height = 5;
		int maxTime = 1000;
		int bufferSize  = 2000;

		System.out.println("Threads  | time (s)");
		System.out.println("=========|=============");
		for(int nThreads = 1; nThreads < 21; nThreads++) {
			PSystem system = new GridSystem(width, height, WIDTH, HEIGHT);
			int n = system.getBodies().length;

			Buffer buffer = new BlockingBuffer(bufferSize,n);
			system.initBuffer(buffer);

			Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
			//Engine engine  = new PrescheduledEngine(system, buffer, nThreads, delta, maxTime);

			long t0 = System.nanoTime();
			engine.start();
			engine.join();
			long t = System.nanoTime() - t0;
			System.out.println(String.format("%3d      | %3.3f", nThreads, (float)t / 1000000000));
		}
	}
	
	public static void main(String[] args){

		//test2b(1,1000,500);
		//testGrid(10,2000,1000,5,5);
		testGridElectric(4, 1000, 1000 ,5,5);
		//testSolarSystem(2,1000,500,30);
		//threadScalabilityTest();
	}


}
