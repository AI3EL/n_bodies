package etc;

import etc.Vector;

/*
 * In this implementation it is not important is some data is lost, as the primary goal is speed
 * In this implementation, the buffer is just an array of array
 * The processing node writes in Buffer[time % bufferSize]
 * We need a bufferSize > 1 because otherwise, the visualizer won't have the time to read it before it is overwritten
 */

public class Buffer {
	public Vector[][] data;
	public int size;
	
	public Buffer(int size, int n){
		data = new Vector[size][n];
		this.size=size;
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
