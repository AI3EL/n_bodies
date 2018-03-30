package systems;

import etc.Vector;
import etc.Body;
import etc.ElectricForce;

/*
 * Generates a grid of particles with mass 1 and charges aleatory between -20,20
 */

public class ElectricGridSystem extends PSystem {
	public ElectricGridSystem(int width, int height, int displayWidth, int displayHeight) {
		n = width * height;
		bodies = new Body[n];
		force = new ElectricForce();
		int xBegin = displayWidth/2 - (width * 30)/2 ;
		int yBegin = displayHeight/2 - (height * 30)/2 ;

		for(int i=0; i< width ; i++){
			for(int j=0; j<height;j++){
				bodies[j + i*height] = new Body(0,j+i*height, 1, (float) (Math.random()-0.5)*20, 5,
						new Vector(xBegin + (i+1)*30, yBegin + (j+1)*30),
						new Vector(0,0), new Vector(0,0), n);
			}
		}
	}
}

