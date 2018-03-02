package n_bodies;

import java.util.LinkedList;

public class Body {
	int id;
	float mass;
	
	Vector pos;
	Vector speed;
	Vector acc;
	
	public Body(int id_, Vector pos_, Vector speed_, Vector acc_) {
		this.id=id_;
		pos=pos_;
		speed=speed_;
		acc=acc_;	
	}
	
	public void setAll(LinkedList<Body> others, Force f, float delta) {
		setAcc(others, f);
		setSpeed(delta);
		setPos(delta);
	}
	
	public void setAcc(LinkedList<Body> others, Force f) {
		acc.reset();
		for (Body other : others) {
			acc=acc.add(f.exerce(this,other));
		}
	}
	
	public void setSpeed (float delta) {
		speed=speed.add(acc.mul(delta)); // v(t+dt) = v(t) + a(t)*dt		
	}
	
	public void setPos (float delta) {
		pos=pos.add(speed.mul(delta)); // p(t+dt) = p(t) + v(t)*dt		
	}
}
