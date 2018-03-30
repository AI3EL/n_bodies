package prescheduled;

import etc.Engine;
import etc.Buffer;
import etc.Body;

import systems.PSystem;

import collision.Collider;

import java.util.concurrent.CyclicBarrier;

public class PrescheduledEngine extends Engine {
	Buffer buffer;
	float delta;
	int maxTime;
	CyclicBarrier moveBarrier;
	CyclicBarrier collideBarrier;
	CyclicBarrier mergeBarrier;
	Collider collider;

	Body[] bodies;
	Body[] nextBodies;
	int[] nextBodiesId;
	int nextSize;


	int nThreads;
	int n;
	int currentFrame;

	public PrescheduledEngine(PSystem system, Buffer buffer, int nThreads, float delta, int maxTime) {
		this.buffer = buffer;
		this.delta = delta;
		this.maxTime = maxTime;
		
		this.bodies = system.getBodies();

		this.nThreads = nThreads;
		this.n = system.getBodies().length;

		this.currentFrame = 1;
		// We could reuse some barriers, but I prefer to keep the
		// possibility to add different actions at any moment
		this.moveBarrier = new CyclicBarrier(nThreads);
		this.collideBarrier = new CyclicBarrier(nThreads, new Runnable() {
			public void run() {
				allocateNextBodies();
			}
		});
		this.mergeBarrier = new CyclicBarrier(nThreads, new Runnable() {
			public void run() {
				advance();
			}
		});

		collider = new Collider(n);

		int n = system.getBodies().length;

		this.threads = new PrescheduledNode[nThreads];
		for(int i=0; i< nThreads; i++){
			threads[i] = new PrescheduledNode(system, this, i);
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
		n = nextSize;
		bodies = nextBodies;
		buffer.nBody[currentFrame % buffer.size] = n;
		buffer.waitWrite(currentFrame);
		currentFrame++;
	}
	
	private void allocateNextBodies() {
		int collisions = 0;
		for(PrescheduledNode t : threads) {
			collisions += t.collisionCount;
		}
		nextSize = n - collisions;
		nextBodies = new Body[nextSize];
		nextBodiesId = new int[nextSize];
		int i = 0;
		for(int ni = 0; ni < nextSize; ni++) {
			while(collider.rep(i) != i)
				i++;
			nextBodiesId[ni] = i;
			i++;
		}
	}

	private PrescheduledNode[] threads;
}
