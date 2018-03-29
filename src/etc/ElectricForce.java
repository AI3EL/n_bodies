package etc;


public class ElectricForce extends Force {
	
	@Override
	public Vector exerce(Body a, Body b) {
		if (a.pos.x == b.pos.x && a.pos.y == b.pos.y) return new Vector(0,0);
		Vector result = b.pos.sub(a.pos);
		float dist = result.distance(new Vector(0,0));
		result = result.mul(1/(dist*dist*dist));
		result = result.mul(-a.q*b.q);		
		return result;
	}

}
