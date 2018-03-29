package etc;

import etc.Vector;

import java.util.Arrays;

/*
 * In this implementation it is not important is some data is lost, as the primary goal is speed
 * In this implementation, the buffer is just an array of array
 * The processing node writes in Buffer[time % bufferSize]
 * We need a bufferSize > 1 because otherwise, the visualizer won't have the time to read it before it is overwritten
 */

public class Buffer {
	public Vector[][] pos;
	public float[][] radiuses;
	public int[] nBody;
	public int size;
	public boolean[] mergeAlreadyDone;
	
	public Buffer(int size, int n) {
		this.pos = new Vector[size][n];
		this.radiuses = new float[size][n];
		this.nBody = new int[size];
		Arrays.fill(this.nBody, n);
		this.size=size;
		this.mergeAlreadyDone = new boolean[size];
	}
	public Buffer(int size, int n, Vector[] pos, float radiuses[]){
		this(size, n);
		System.arraycopy(pos, 0, this.pos[0], 0, n);
		System.arraycopy(radiuses, 0, this.radiuses[0], 0, n);
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
