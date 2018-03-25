package lockfree;

import etc.Body;
import etc.Force;
import etc.Vector;
import etc.Buffer;


/*
 * ProcessingNode update the position of the bodies.
 * For this it uses a AtomicInteger counter that holds the value of the minimum time the bodies
 * The Thread tries to get the lock of a body(bodies[first] is the first it tries, generally, the firsts are equally distributed), if not possible he tries another one
 * Once body is locked, he checks that the time of the body is not exceeding the minimum time ( this will be optimized)
 * NOTE: when incrementing clock.time, every other Thread is trying to do the same or in pause
 * So we can use a Buffer with size 1 and just a forces array because every Thread IN A CRITICAL PART has the same time
 */


public class ProcessingNode implements Runnable {
	
	Body[] bodies;
	Clock clock;
	int first;
	Force force;
	float delta;
	Buffer buffer;
	int maxTime;
	int fillTime; // Each fillTime timesteps, the node puts in forces[][] the resultant forces of each
	boolean[][] isNegligible;
	Vector[][] forces;
	Vector[] totalForces;
	
	public ProcessingNode(Body[] bodies, Clock clock, int first, Force force, float delta, Buffer buffer, int maxTime, boolean[][] isNegligible, int fillTime){
		this.bodies=bodies;
		this.first=first;
		this.force=force;
		this.forces=forces;
		this.totalForces=totalForces;
		this.delta=delta;
		this.buffer=buffer;
		this.clock=clock;
		this.maxTime = maxTime;
		this.isNegligible = isNegligible;
		this.fillTime = fillTime;
	}
	
	@Override
	public void run() {
		int currentTime = clock.time.get();
		//maxTime - 1 because body[i].time is incremented in the loop
		while(currentTime < maxTime-1){
			boolean increment = true;
			for(int i=0; i< bodies.length; i++){
				int curBody = (i+first) % bodies.length;
				if(currentTime == bodies[curBody].time)	increment = false;
				// tryLock() tries to take the lock, if no continues
				// There is an alternative that tries for a certain amount of time before giving up
				
				// Need to think at whether we can use currentTime or time.get() ...
				if (currentTime == bodies[curBody].time && bodies[curBody].lock.tryLock()){	
					//Lock is locked if and only if we enter the try
					try{
						if( bodies[curBody].time > currentTime )	break;	
						else{
							//DEBUG :
							//System.out.println("Thread n° " + Thread.currentThread().getId() + " bodyTime : " + bodies[curBody].time + " Time : " + clock.time.get() + " CurrentTime " + currentTime + " Body n° : "+ curBody);
							//System.out.println(bodies[curBody].toString());
							if(bodies[curBody].time % fillTime == 0)	bodies[curBody].setAll(bodies, force, delta, isNegligible, false);
							else	bodies[curBody].setAll(bodies, force, delta, isNegligible, true);
							buffer.data[bodies[curBody].time % buffer.size][curBody]=bodies[curBody].pos;
						}
						
					}finally {
						bodies[curBody].lock.unlock();
					}
				}
			}
			
			if(increment)	{
				buffer.waitWrite(currentTime + 1);
				if(clock.time.compareAndSet(currentTime,currentTime+1)){
					//System.out.println(clock.time.get());
				}
			}
			currentTime = clock.time.get();
		}
	}	
}
