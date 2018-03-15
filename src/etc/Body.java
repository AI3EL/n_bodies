package etc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Body {
	
	public  final Lock  lock = new ReentrantLock  ();	
	
	public int id;
	public int time;
	public float mass;
	
	public Vector pos;
	public Vector speed;
	public Vector acc;
	
	public Body(int id_, float mass,  Vector pos_, Vector speed_, Vector acc_) {
		this.id=id_;
		this.mass=mass;
		pos=pos_;
		speed=speed_;
		acc=acc_;	
		time = 0;
	}
	
	public void setAll(Body[] others, Force f, float delta) {
		setAcc(others, f);
		setSpeed(delta);
		setPos(delta);
		time++;
	}
	
	public void setAcc(Body[] others, Force f) {
		acc.reset();
		for (int i=0; i< others.length; i++) {
			acc=acc.add(f.exerce(this,others[i]));
			
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
