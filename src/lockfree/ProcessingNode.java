package lockfree;

import etc.Body;
import etc.Force;


/*
 * ProcessingNode update the position of the bodies.
 * For this it uses a AtomicInteger counter that holds the value of the minimum time the bodies
 * The Thread tries to get the lock of a body(bodies[first] is the first it tries, generally, the firsts are equally distributed), if not possible he tries another one
 * Once body is locked, he checks that the time of the body is not exceeding the minimum time ( this will be optimized)
 */


public class ProcessingNode implements Runnable {
	
	Body[] bodies;
	Clock clock;
	int first;
	Force force;
	int delta;
	Buffer buffer;
	int maxTime;
	
	public ProcessingNode(Body[] bodies, Clock clock, int first, Force force, int delta, Buffer buffer, int maxTime){
		this.bodies=bodies;
		this.first=first;
		this.force=force;
		this.delta=delta;
		this.buffer=buffer;
		this.clock=clock;
		this.maxTime = maxTime;
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
					//Each time we enter here the lock is locked
					try{
						//Checks if it actually takes you out of the try
						if( bodies[curBody].time > currentTime )	break;	
						else{
							//DEBUG :
							//System.out.println("Thread n° " + Thread.currentThread().getId() + " bodyTime : " + bodies[curBody].time + " Time : " + clock.time.get() + " CurrentTime " + currentTime + " Body n° : "+ curBody);
							//System.out.println(bodies[curBody].toString());

							bodies[curBody].setAll(bodies, force, delta);
							buffer.data[bodies[curBody].time % buffer.size][curBody]=bodies[curBody].pos;
						}
						
					}finally {
						bodies[curBody].lock.unlock();
					}
				}
			}
			
			if(increment)	{
				 clock.time.compareAndSet(currentTime,currentTime+1);
			}
			currentTime = clock.time.get();
		}
	}
		
	

}
