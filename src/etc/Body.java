package etc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * The function setAll updates acc, speed, pos and time
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
	}
	
	public void setAll(Body[] others, Force f, float delta, boolean[][] isNegligible, boolean negligibleMode) {
		setForces(others, f, isNegligible, negligibleMode);
		setAcc(others, f);
		setSpeed(delta);
		setPos(delta);
		time++;
	}
	
	//Suppose it is given reinitialized arrays
	public void setForces(Body[] others, Force f,  boolean[][] isNegligible, boolean negligibleMode){
		for (int i=0; i< others.length; i++) {
			// Is useful so that totalForce is never equal to 0
			Vector newTotalForce = new Vector();
			if((!negligibleMode) || (!isNegligible[this.id][i])){
				forces[i] = f.exerce(this,others[i]);
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
		pos=pos.add(speed.mul(delta)); // p(t+dt) = p(t) + v(t)*dt		
	}
	
	public String toString (){
		String result = "Position : "+ "("+ String.valueOf(pos.x) + "," + String.valueOf(pos.y) + ")";
		result += " / Speed : "+ "("+ String.valueOf(speed.x) + "," + String.valueOf(speed.y) + ")";
		result += " / Acc : "+ "("+ String.valueOf(acc.x) + "," + String.valueOf(acc.y) + ")";
		return result;
	}
}
