package lockfree;

import etc.Engine;
import etc.Buffer;

import systems.PSystem;

import java.util.concurrent.locks.*;

public class LockfreeEngine extends Engine {
	Buffer buffer;
	float delta;
	int maxTime;
	
    Clock clock;
	int [] nBody;
	boolean [][] isNegligible;
	int fillTime;
	SafeCounter counter;

	public LockfreeEngine(PSystem system, Buffer buffer, int nThreads, float delta, int maxTime) {
		this.buffer = buffer;
		this.delta = delta;
		this.maxTime = maxTime;

		int n = system.getBodies().length;

		this.clock = new Clock();
		this.isNegligible = new boolean[n][n];
		this.fillTime = 50;
		this.counter = new SafeCounter(0);

		this.threads = new Thread[nThreads];
		for(int i=0; i< nThreads; i++){
			int first = (i * n) / nThreads;
			threads[i] = new Thread(new ProcessingNode(system, this, first));
		}

		Thread negligibleNode = new Thread(new NegligibleNode( (float)0.95, clock, maxTime, isNegligible, buffer, fillTime));
		negligibleNode.start();

	}

	public void start() {
		for(Thread t : threads){
			t.start();
		}
	}

	public void join() {
		try {
			for(Thread t : threads){
				t.join();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Thread[] threads;
}
