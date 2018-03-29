package prescheduled;

import etc.Engine;
import etc.Buffer;

import systems.PSystem;

import java.util.concurrent.CyclicBarrier;

public class PrescheduledEngine extends Engine {
	Buffer buffer;
	float delta;
	int maxTime;
	CyclicBarrier barrier;

	int currentFrame;

	public PrescheduledEngine(PSystem system, Buffer buffer, int nThreads, float delta, int maxTime) {
		this.buffer = buffer;
		this.delta = delta;
		this.maxTime = maxTime;

		this.n = system.getBodies().length;

		this.currentFrame = 1;
		this.barrier = new CyclicBarrier(nThreads, new Runnable() {
			public void run() {
				advance();
			}
		});

		int n = system.getBodies().length;

		this.threads = new Thread[nThreads];
		for(int i=0; i< nThreads; i++){
			int first = (i * n) / nThreads;
			int last = ((i+1) * n) / nThreads;
			threads[i] = new Thread(new PrescheduledNode(system, this, first, last));
		}

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

	private void advance() {
		buffer.nBody[currentFrame % buffer.size] = n;
		buffer.waitWrite(currentFrame);
		currentFrame++;
	}

	private int n;
	private Thread[] threads;
}
