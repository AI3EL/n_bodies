package n_bodies;

abstract public class Force {
	
	//Returns the force ecerced by b on a
	abstract public Vector exerce(Body a, Body b);
}
