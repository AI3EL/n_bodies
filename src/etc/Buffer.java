package etc;

import etc.Vector;

import java.util.Arrays;

import lockfree.SafeCounter;

/*
 * In this implementation it is not important is some data is lost, as the primary goal is speed
 * In this implementation, the buffer is just an array of array
 * The processing node writes in Buffer[time % bufferSize]
 * We need a bufferSize > 1 because otherwise, the visualizer won't have the time to read it before it is overwritten
 *
 * For the role of synchronization functions, see BlockingBuffer.
 * Here we just return true everytime, which means we never block and allow
 * threads to overwrite data without restriction.
 */

public class Buffer {
	public Body[][] bodies;
	public int[] nBody;
	public int size;
	public boolean[] mergeAlreadyDone;
	public boolean[] updated[];
	public SafeCounter[] counter;
	
	public Buffer(int size, int n){
		this.bodies = new Body[size][n];
		this.nBody = new int[size];
		nBody[0] = n;
		this.size=size;
		this.mergeAlreadyDone = new boolean[size];
		this.updated = new boolean[size][n];
		this.counter = new SafeCounter[size];
		for(int i=0; i<size;i++)	this.counter[i] = new SafeCounter(0);
	}

	public Buffer(int size, int n, Body[] bodies){
		this(size, n);
		System.arraycopy(bodies, 0, this.bodies[0], 0, n);
	}

	public boolean waitWrite(int frame)
	{
		return true;
	}

	public boolean waitRead()
	{
		return true;
	}

	public boolean pollRead() {
		return true;
	}

}
