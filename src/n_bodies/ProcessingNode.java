package n_bodies;

import java.util.concurrent.atomic.AtomicInteger;


public class ProcessingNode implements Runnable {
	
	Body[] bodies;
	AtomicInteger time;
	int first;
	Force force;
	int delta;
	float[][][] results;
	int maxTime;
	
	public ProcessingNode(Body[] bodies, AtomicInteger time, int first, Force force, int delta, float[][][] results, int maxTime){
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
		while(currentTime < maxTime-1){
			boolean increment = true;
			currentTime = time.get();
			for(int i=0; i< bodies.length; i++){
				
				if(currentTime == bodies[i].time)	increment = false;
				if (time.get() == bodies[i].time && bodies[i].lock.tryLock()){
					try{
						if(time.get() > bodies[i].time)	break;	//Checks if it actually takes you out of the try
						else{
							System.out.println("Time : " + bodies[i].time);
							System.out.println("ID : "+i);
							System.out.println(bodies[i].toString());
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
			
		}
	}
		
	

}
