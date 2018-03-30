package systems;

import etc.Body;
import etc.Force;
import etc.Buffer;

public class PSystem {

	public void initBuffer(Buffer buffer) {
		for(int i=0; i< n ; i++){
			buffer.bodies[0][i] = bodies[i];
		}
	}

	public Body[] getBodies() {
		return bodies;
	}

	public Force getForce() {
		return force;
	}

	protected int n = 0;
	protected Body[] bodies;
	protected Force force;
}
