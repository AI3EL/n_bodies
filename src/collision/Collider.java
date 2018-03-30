package collision;

import etc.Body;
import etc.Vector;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class Collider {
	public Collider(int n) {
		ufind = new AtomicIntegerArray(n);
	}
	// Not thread safe, meant to be used under synchronization when most
	// convenient.
	public void reset(int i) {
		ufind.set(i, i);
	}
	// Thread-safe, lockfree implementation of union 
	// Not implementing any optimisation (rank union, etc) because it is meant to work on many
	// small sets
	// Return false if the classes are already merged (including the case
	// where they are merged during the call by another thread,
	// else true
	public boolean merge(int i, int j) {
		int m, M;
		do {
			while(ufind.get(i) != i) {
				i = ufind.get(i);
			}
			while(ufind.get(j) != j) {
				j = ufind.get(j);
			}
			m = Math.min(i, j);
			M = Math.max(i, j);
		} while(!ufind.compareAndSet(m, m, M));
		return (m != M);
	}

	public int rep(int i) {
		while(ufind.get(i) != i) {
			i = ufind.get(i);
		}
		return i;
	}


	public static boolean collides(Body a, Body b) {
		return a.pos.distance(b.pos) <= a.radius + b.radius;
	}

	public static Body mergeBody(Body a, Body b, int time, int id, int n) {
		return new Body(time, id, a.mass + b.mass, a.q + b.q,
				(float)Math.sqrt(a.radius*a.radius + b.radius*b.radius),
				a.pos.mul(a.mass).add(b.pos.mul(b.mass)).mul(1.0f/(a.mass+b.mass)),
				a.speed.mul(a.mass).add(b.speed.mul(b.mass)).mul(1.0f/(a.mass+b.mass)),
				new Vector(),n);
	}

	AtomicIntegerArray ufind;
}

