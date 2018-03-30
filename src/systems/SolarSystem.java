
package systems;

import etc.Vector;
import etc.Body;
import etc.GravitationnalForce;

public class SolarSystem extends PSystem {
	final int sunRadius = 50;
	final float sunMass = 1000.0f;
	final int planetRadius=10;
	final float planetMass = 10.0f;

	public SolarSystem(int n, int displayWidth, int displayHeight) {
		this.n = n;
		bodies = new Body[n];
		force = new GravitationnalForce();

		int xBegin = displayWidth/2 - ( sunRadius)/2 ;
		int yBegin = displayHeight/2 - (sunRadius)/2 ;


		bodies[0] = new Body(0,0, sunMass, 0, sunRadius, new Vector(xBegin, yBegin), new Vector(0,0), new Vector(0,0), n);

		for(int i=1; i< n ; i++){
			double r = (Math.random() * (displayHeight/2-sunRadius-planetRadius)) + sunRadius + planetRadius;
			double teta = (Math.random()-0.5) * 2 * Math.PI;
			float x =(float) (xBegin + r*Math.cos(teta));
			float y =(float) (yBegin + r*Math.sin(teta));
			float realD = (float) Math.sqrt(sunMass/r);

			float vx =(float) (-Math.sin(teta)*realD);
			float vy =(float) (Math.cos(teta)*realD);

			bodies[i] = new Body(0,i, planetMass, 0, planetRadius, new Vector(x, y), new Vector(vx,vy), new Vector(0,0), n);
		}
	}
}

