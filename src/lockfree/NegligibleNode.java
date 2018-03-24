package lockfree;

import etc.Body;
import etc.Vector;

/*
 * err is the proportion of the total force vector we accept to lose for approximation
 * deletedNorm holds the cumulative approximation of lost of norm 
 * Should we redefine negligible points after a certain number of body position updates or after a certain amount of time ?
 * Here we do a mix
 * We look a negligible force with aleatory beginning points
 * At each fillTime we recalculate isNegligible
 * Could optimize by calculating half of forces as F(A,B) = F(B,A)
 */

public class NegligibleNode implements Runnable {
	
	boolean[][] isNegligible;
	int n, fillTime;
	long lastTime;
	Clock clock;
	int maxTime;
	Body[] bodies;
	float err;
	
	public NegligibleNode(float err, Clock clock, int maxTime, boolean[][] isNegligible, Body[] bodies, int fillTime){
		this.isNegligible = isNegligible;
		n = bodies.length;
		lastTime = clock.time.get();
		this.fillTime=fillTime;
		this.maxTime=maxTime;
		this.err=err;
		this.bodies=bodies;
		this.clock = clock;
	}
	
	@Override
	public void run(){
		while(clock.time.get()<maxTime){			
			if(clock.time.get() - lastTime < fillTime){
				try {
					Thread.sleep(100); // To be changed !
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				lastTime=clock.time.get();
				int bi = (int) (Math.random() * n);
				int bj = (int) (Math.random() * n);
				for(int i=0; i<n;i++){
					int ri = (bi + i) % n;
					// Here we need that bodies[ri].totalForce is always a totalForce vector, this is why we create newTotalForce in setForce
					Vector currentTotalForce = bodies[ri].totalForce;
					float initialNorm = currentTotalForce.distance(new Vector(0,0));
					for(int j=0; j<n;j++){
						int rj = (bj + j) % n;
						Vector currentApproximation = currentTotalForce.sub(bodies[ri].forces[rj]);
						Vector approximationDiff = bodies[ri].totalForce.sub(currentApproximation);
						float approximationNormDiff = approximationDiff.distance(new Vector(0,0));
						if(approximationNormDiff > err * initialNorm){
							isNegligible[ri][rj] = true ;
							currentTotalForce = currentApproximation;
						}
						else isNegligible[ri][rj]=false;	
					}
				}
			}
		}
	}
	
}
