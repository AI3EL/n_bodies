package etc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * The function setAll updates acc, speed, pos and time
 * n is the number of other bodies
 * totalForce is the resultant of all forces applied on the Body
 */

public class Body {
	
	public  final Lock  lock = new ReentrantLock  ();	
	
	public int id;
	public int time;
	public float mass;
	public int n;
	
	public Vector pos;
	public Vector speed;
	public Vector acc;
	
	public Vector precPos;
	
	public Vector[] forces;
	public Vector totalForce;
	
	public Body(int id_, float mass,  Vector pos_, Vector speed_, Vector acc_, int n) {
		this.id=id_;
		this.mass=mass;
		pos=pos_;
		speed=speed_;
		acc=acc_;	
		time = 0;
		this.n = n;
		forces = new Vector[n];
		for(int i=0; i< n; i++)	forces[i] = new Vector();
		totalForce = new Vector();
		precPos=pos_;
	}
	
	public void setAll(Body[] others, Force f, float delta, boolean[][] isNegligible, boolean negligibleMode) {
		setForces(others, f, isNegligible, negligibleMode);
		setAcc(others, f);
		setSpeed(delta);
		setPos(delta);
		time++;
	}
	
	public void setForces(Body[] others, Force f,  boolean[][] isNegligible, boolean negligibleMode){
		for (int i=0; i< others.length; i++) {
			// Is useful so that totalForce is never equal to 0 (otherwise there are concurrent issues)
			Vector newTotalForce = new Vector();
			if(!(negligibleMode && isNegligible[this.id][i])){
				boolean sameTime = (this.time == others[i].time);
				forces[i] = f.exerce(this,others[i],sameTime);
				newTotalForce = newTotalForce.add(forces[i]);
			}
			totalForce = newTotalForce;
		}
	}
		
	public void setAcc(Body[] others, Force f) {
		acc.reset();
		for (int i=0; i< forces.length; i++) {
			acc=acc.add(forces[i]);
		}
		acc = acc.mul(1/this.mass);
	}
	
	public void setSpeed (float delta) {
		speed=speed.add(acc.mul(delta)); // v(t+dt) = v(t) + a(t)*dt		
	}
	
	public void setPos (float delta) {
		precPos=pos;
		pos=pos.add(speed.mul(delta)); // p(t+dt) = p(t) + v(t)*dt		
	}
	
	public String toString (){
		String result = "Position : "+ "("+ String.valueOf(pos.x) + "," + String.valueOf(pos.y) + ")";
		result += " / Speed : "+ "("+ String.valueOf(speed.x) + "," + String.valueOf(speed.y) + ")";
		result += " / Acc : "+ "("+ String.valueOf(acc.x) + "," + String.valueOf(acc.y) + ")";
		return result;
	}
}
