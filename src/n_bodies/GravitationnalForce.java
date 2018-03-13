package n_bodies;

public class GravitationnalForce extends Force {
	
	@Override
	public Vector exerce(Body a, Body b) {
		if (a.pos.x == b.pos.x && a.pos.y == b.pos.y) return new Vector(0,0);
		Vector result = b.pos.sub(a.pos);
		float dist = result.distance(new Vector(0,0));
		result = result.mul(1/(dist*dist*dist));
		result = result.mul(a.mass*b.mass);		
		return result;
	}

}
