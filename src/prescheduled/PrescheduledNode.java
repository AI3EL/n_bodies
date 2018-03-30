package prescheduled;

import etc.Engine;
import etc.Buffer;
import etc.Body;

import systems.PSystem;

import collision.Collider;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class PrescheduledNode extends Thread {
	int collisionCount;

	public PrescheduledNode(PSystem system, PrescheduledEngine engine, int taskid) {
		this.system = system;
		this.engine = engine;
		this.taskid = taskid;
	}

	public void run() {
		first = (taskid * engine.n) / engine.nThreads;
		last = ((taskid+1) * engine.n) / engine.nThreads;
		while(engine.currentFrame < engine.maxTime) {
			bodies = engine.bodies;
			try {
				move();
				for(int i = first; i < last; i++)
					engine.collider.reset(i);
				engine.moveBarrier.await();

				detectCollisions();
				engine.collideBarrier.await();

				first = (taskid * engine.nextSize) / engine.nThreads;
				last = ((taskid+1) * engine.nextSize) / engine.nThreads;
				merge();
				copyToBuffer();
				engine.mergeBarrier.await();
			} catch(BrokenBarrierException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void move() {
		for(int i = first; i < last; i++) {
			Body b = bodies[i];
			b.setAll(bodies, system.getForce(), engine.delta, null, false);
		}
	}

	private void detectCollisions() {
		collisionCount = 0;
		for(int i = first; i < last; i++) {
			for(int j = 0; j < engine.n; j++) {
				if(((i%2) != (j%2)) ^ (i < j)) {
					if(Collider.collides(bodies[i], bodies[j])) {
						if(engine.collider.merge(i, j)) {
							collisionCount++;
						}
					}
				}
			}
		}
	}

	private void merge() {
		int i;
		for(int ni = first; ni < last; ni++) {
			i = engine.nextBodiesId[ni];
			Body b = bodies[i];
			engine.nextBodies[ni] = new Body(b.time, ni, b.mass, b.q, b.radius, b.pos, b.speed, b.acc, engine.nextSize);
			for(int j = 0; j < engine.n; j++) {
				if(j != i && engine.collider.rep(j) == i) {
					engine.nextBodies[ni] = Collider.mergeBody(engine.nextBodies[ni], bodies[j], engine.currentFrame, ni, engine.nextSize);
				}
			}
		}
	}

	private void copyToBuffer() {
		for(int i = first; i < last; i++) {
			Body b = engine.nextBodies[i];
			// Looks like we're missing a copy
			// constructor...
			engine.buffer.bodies[engine.currentFrame % engine.buffer.size][i] = new Body(
					b.time, b.id, b.mass, b.q, b.radius, 
					b.pos, b.speed, b.acc,
					b.n);
		}
	}

	private int taskid;
	private int first;
	private int last;
	private PSystem system;
	private PrescheduledEngine engine;
	private Body[] bodies;
}

