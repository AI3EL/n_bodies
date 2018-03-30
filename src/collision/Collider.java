package collision;

import etc.Body;

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
	public void merge(int i, int j) {
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
	}

	public static boolean collides(Body a, Body b) {
		return a.pos.distance(b.pos) <= a.radius + b.radius;
	}

	AtomicIntegerArray ufind;
}

