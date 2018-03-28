package etc;

import etc.Vector;

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
	
	public Buffer(int size, int n, Vector[] pos, float radiuses[]){
		this.pos = new Vector[size][n];
		this.radiuses = new float[size][n];
		this.nBody = new int[size];
		this.size=size;
		this.mergeAlreadyDone = new boolean[size];
		this.pos[0] = pos;
		this.radiuses[0] = radiuses;
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
