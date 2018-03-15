package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * ProcessingNode update the position of the bodies.
 * For this it uses a AtomicInteger counter that holds the value of the minimum time the bodies
 * The Thread tries to get the lock of a body, if not possible he tries another one
 * Once body is locked, he checks that the time of the body is not exceeding the minimum time ( this will be optimized)
 */


public class ProcessingNode implements Runnable {
	
	Body[] bodies;
	AtomicInteger time;
	int first;
	Force force;
	int delta;
	float[][][] results;
	int maxTime;
	
	public ProcessingNode(Body[] bodies, AtomicInteger time, int first, Force force, int delta, float[][] buffer, int maxTime){
		this.bodies=bodies;
		this.first=first;
		this.force=force;
		this.delta=delta;
		this.results=results;
		this.time=time;
		this.maxTime = maxTime;
	}
	
	@Override
	public void run() {
		int currentTime = time.get();
		//maxTime - 1 because body[i].time is incremented in the loop
		while(currentTime < maxTime-1){
			boolean increment = true;
			for(int i=0; i< bodies.length; i++){
				
				if(currentTime == bodies[i].time)	increment = false;
				// tryLock() tries to take the lock, if no continues
				// There is an alternative that tries for a certain amount of time before giving up
				
				// Need to think at whether we can use currentTime or time.get() ...
				if (currentTime == bodies[i].time && bodies[i].lock.tryLock()){	
					try{
						//Checks if it actually takes you out of the try
						if( bodies[i].time > currentTime )	break;	
						else{
							//DEBUG :
							//System.out.println("Thread n° " + Thread.currentThread().getId() + " bodyTime : " + bodies[i].time + " Time : " + time.get() + " CurrentTime " + currentTime + " Body n° : "+i);
							//System.out.println(bodies[i].toString());

							bodies[i].setAll(bodies, force, delta);
							
							results[bodies[i].time][i][0]=bodies[i].pos.x;
							results[bodies[i].time][i][1]=bodies[i].pos.y;
						}
						
					}finally {
						bodies[i].lock.unlock();
					}
				}
			}
			
			if(increment)	time.compareAndSet(currentTime,currentTime+1);
			currentTime = time.get();
		}
	}
		
	

}
