package n_bodies;

public class Vector {
	float x;
	float y;
	
	public Vector (float a, float b) {
		x=a;
		y=b;
	}
	
	public void reset() {
		x = y = 0;
	}
	
	public Vector add(Vector v) {
		return new Vector(x+v.x, y+v.y);
	}
	
	public Vector mul(float lambda) {
		return new Vector(x*lambda, y*lambda);
	}
	
	public Vector sub(Vector v) {
		return new Vector(x-v.x, y-v.y);
	}
	
	public float distance(Vector v) {
		return (Math.abs(x-v.x)+Math.abs(y-v.y)) ;
	}
}

