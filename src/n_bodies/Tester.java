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
import systems.TwoSolarSystems;

public class Tester {

	/*
	 * We assume that generally, n >> nThreads
	 * You can comment/uncomment Engine in tests to change the Engined used
	 * You can also comment/uncomment NegligibleNode to see the difference
	 * 
	 *
	 * Tests Results :
	 * 22s for testGrid(2,100,1000,30,30) with no NegligibleNode
	 * 16s for testGrid(2,100,1000,30,30) with NegligibleNode with fillTime at 50 and 5% error


	 */

	static int WIDTH = 1200;
	static int HEIGHT = 800;


	/*
	 * Test with width*height bodies on a centered grid, each has mass 1 
	 */
	public static void testGrid(int nThreads, int bufferSize, int maxTime, int width, int height, float delta,float speedUp){

		PSystem system = new GridSystem(width, height, WIDTH, HEIGHT);
		int n = system.getBodies().length;

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		// Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
		Engine engine  = new PrescheduledEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, speedUp, buffer, WIDTH, HEIGHT);

		engine.join();
	}
	
	/*
	 * Test with width*height bodies on a centered grid, each has q in (-20,20)
	 */
	public static void testGridElectric(int nThreads, int bufferSize, int maxTime, int width, int height, float delta,float speedUp){
		PSystem system = new ElectricGridSystem(width, height, WIDTH, HEIGHT);
		int n = system.getBodies().length;

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, speedUp, buffer, WIDTH, HEIGHT);
		engine.join();

	}
	
	public static void testSolarSystem(int nThreads, int bufferSize, int maxTime, int n, float delta,float speedUp){
		
		PSystem system = new SolarSystem(n, WIDTH, HEIGHT);

		Buffer buffer = new BlockingBuffer(bufferSize,n);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
		// Engine engine  = new PrescheduledEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, speedUp, buffer, WIDTH, HEIGHT);
		engine.join();

	}
	
	
	
	public static void threadScalabilityTest() {
		float delta = 0.2f;
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
	
public static void testTwoSolarSystems(int nThreads, int bufferSize, int maxTime, boolean sync, int n1, int sunRadius1, float sunMass1, int planetRadius1, float planetMass1, int n2, int sunRadius2, float sunMass2, int planetRadius2, float planetMass2, float delta,float speedUp){
		
		PSystem system = new TwoSolarSystems( sync, n1, sunRadius1, sunMass1, planetRadius1, planetMass1, n2, sunRadius2, sunMass2, planetRadius2, planetMass2, WIDTH, HEIGHT);

		Buffer buffer = new BlockingBuffer(bufferSize,n1+n2);
		system.initBuffer(buffer);

		Engine engine  = new LockfreeEngine(system, buffer, nThreads, delta, maxTime);
		// Engine engine  = new PrescheduledEngine(system, buffer, nThreads, delta, maxTime);

		engine.start();
		Visualizer visualizer = new Visualizer(delta, maxTime, speedUp, buffer, WIDTH, HEIGHT);
		engine.join();

	}
	
	public static void main(String[] args){

		//testGrid(10,10000,9000,10,10, 0.1f, 50.0f);
		//testGridElectric(4, 10000, 10000 ,6,6, 1.0f, 20.0f);
		testTwoSolarSystems(4,10000,1000000,true,30, 30, 300, 5, 5, 30, 30, 300, 5, 5,0.1f,20.0f);
		//threadScalabilityTest();

	}


}
