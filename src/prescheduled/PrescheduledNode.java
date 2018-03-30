package prescheduled;

import etc.Engine;
import etc.Buffer;
import etc.Body;

import systems.PSystem;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class PrescheduledNode implements Runnable {
	public PrescheduledNode(PSystem system, PrescheduledEngine engine, int first, int last) {
		this.system = system;
		this.engine = engine;
		this.first = first;
		this.last = last;
		this.bodies = system.getBodies();
	}

	public void run() {
		while(engine.currentFrame < engine.maxTime) {
			for(int i = first; i < last; i++) {
				Body b = bodies[i];
				b.setAll(bodies, system.getForce(), engine.delta, null, false);
				// Looks like we're missing a copy
				// constructor...
				engine.buffer.bodies[engine.currentFrame % engine.buffer.size][i] = new Body(
						b.time, b.id, b.mass, b.q, b.radius, 
						b.pos, b.speed, b.acc,
						b.n);
			}
			try {
				engine.barrier.await();
			} catch(BrokenBarrierException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private int first;
	private int last;
	private PSystem system;
	private PrescheduledEngine engine;
	private Body[] bodies;
}

