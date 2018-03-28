package etc;


public class GravitationnalForce extends Force {
	
	@Override
	public Vector exerce(Body a, Body b, boolean sameTime) {
		Vector bPos = (sameTime ? b.pos : b.precPos);
		if (a.pos.x == bPos.x && a.pos.y == bPos.y) return new Vector(0,0);
		Vector result = bPos.sub(a.pos);
		float dist = result.distance(new Vector(0,0));
		result = result.mul(1/(dist*dist*dist));
		result = result.mul(a.mass*b.mass);		
		return result;
	}

}
