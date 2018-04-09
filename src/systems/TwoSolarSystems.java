package systems;

import etc.Vector;
import etc.Body;
import etc.GravitationnalForce;

public class TwoSolarSystems extends PSystem {

	public TwoSolarSystems(boolean sync, int n1, int sunRadius1, float sunMass1, int planetRadius1, float planetMass1, int n2, int sunRadius2, float sunMass2, int planetRadius2, float planetMass2,int displayWidth, int displayHeight) {
		this.n = n1+n2;
		bodies = new Body[n];
		force = new GravitationnalForce();
		
		/*
		 * First Solar System
		 */
		
		int xBegin = displayWidth/2 - ( sunRadius1)/2 - displayWidth/2;
		int yBegin = displayHeight/2 - (sunRadius1)/2 + 2*sunRadius1;
		
		Vector systemSpeed = new Vector(1,0); 

		bodies[0] = new Body(0,0, sunMass1, 0, sunRadius1, new Vector(xBegin, yBegin), systemSpeed, new Vector(0,0), n);

		for(int i=1; i< n1 ; i++){
			double r = (Math.random() * (displayHeight/2-sunRadius1-planetRadius1)) + sunRadius1 + planetRadius1;
			double teta = (Math.random()-0.5) * 2 * Math.PI;
			float x =(float) (xBegin + r*Math.cos(teta));
			float y =(float) (yBegin + r*Math.sin(teta));
			float realD = (float) Math.sqrt(sunMass1/r);

			float vx =(float) (-Math.sin(teta)*realD);
			float vy =(float) (Math.cos(teta)*realD);

			bodies[i] = new Body(0,i, planetMass1, 0, planetRadius1, new Vector(x, y), systemSpeed.add(new Vector(vx,vy)), new Vector(0,0), n);
		}
		
		/*
		 * Second Solar System
		 */
		
		xBegin = displayWidth/2 - ( sunRadius2)/2 + displayWidth/2;
		yBegin = displayHeight/2 - (sunRadius2)/2 - 2*sunRadius2;
		
		systemSpeed = new Vector(-1,0); 

		bodies[n1] = new Body(0,0, sunMass2, 0, sunRadius2, new Vector(xBegin, yBegin), systemSpeed, new Vector(0,0), n);

		for(int i=n1+1; i< n ; i++){
			double r = (Math.random() * (displayHeight/2-sunRadius2-planetRadius2)) + sunRadius2 + planetRadius2;
			double teta = (Math.random()-0.5) * 2 * Math.PI;
			float x =(float) (xBegin + r*Math.cos(teta));
			float y =(float) (yBegin + r*Math.sin(teta));
			float realD = (float) Math.sqrt(sunMass2/r);
			float vx, vy;
			if(sync){
				vx =(float) (-Math.sin(teta)*realD);
				 vy =(float) (Math.cos(teta)*realD);
			}
			else{
				 vx =(float) (Math.sin(teta)*realD);
				 vy =(float) (-Math.cos(teta)*realD);
			}
			bodies[i] = new Body(0,i, planetMass2, 0, planetRadius2, new Vector(x, y), systemSpeed.add(new Vector(vx,vy)), new Vector(0,0), n);
		}
	}
}
