package lockfree;

import java.util.concurrent.locks .*;

import etc.Vector;


public class Buffer {
	public Vector[][] data;
	public int size;
	
	public Buffer(int size, int n){
		data = new Vector[size][n];
		this.size=size;
	}
}
