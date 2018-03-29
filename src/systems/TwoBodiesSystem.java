
package systems;

import etc.Vector;
import etc.Body;
import etc.GravitationnalForce;

public class TwoBodiesSystem extends PSystem {
	public TwoBodiesSystem() {
		n = 2;
		bodies = new Body[n];

		bodies[0] = new Body(0,0, 100, 5, new Vector(50,50), new Vector(0,0), new Vector(0,0), n);
		bodies[1] = new Body(0,1, 100, 5, new Vector(100,100), new Vector(0,0), new Vector(0,0), n);

		force = new GravitationnalForce();
	}
}

