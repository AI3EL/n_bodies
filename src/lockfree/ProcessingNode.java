package lockfree;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	Lock mergeLock;
	int[] nBody; // Size one, for reference 
	
	public ProcessingNode(int[] nBody,Body[] bodies, Clock clock, int first, Force force, float delta, Buffer buffer, int maxTime, boolean[][] isNegligible, int fillTime, Lock mergeLock){
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
		this.nBody = nBody; 
		this.mergeLock=mergeLock;
	}
	
	@Override
	public void run() {
		int currentTime = clock.time.get();
		//maxTime - 1 because body[i].time is incremented in the loop
		while(currentTime < maxTime-1){
			boolean increment = true;
			for(int i=0; i< nBody[0]; i++){
				int curBody = (i+first) % nBody[0];
				//System.out.println("curBody : " + curBody);
				if(currentTime == bodies[curBody].time)	increment = false;
				// tryLock() tries to take the lock, if no continues
				// There is an alternative that tries for a certain amount of time before giving up
				
				// Need to think at whether we can use currentTime or time.get() ...
				if ((currentTime == bodies[curBody].time) && bodies[curBody].lock.tryLock()){	
					//Lock is locked if and only if we enter the try
					try{
						if( bodies[curBody].time > currentTime )	break;	
						else{
							//DEBUG :
							System.out.println("LOCKING thread :" + Thread.currentThread().getId() + " i : " + curBody + "bodyTime = " + bodies[curBody].time+ "currentTime" + currentTime);
							//System.out.println(bodies[curBody].toString());
							if(bodies[curBody].time % fillTime == 0)	bodies[curBody].setAll(bodies, force, delta, isNegligible, false);
							else	bodies[curBody].setAll(bodies, force, delta, isNegligible, true);

							buffer.pos[bodies[curBody].time % buffer.size][curBody]=bodies[curBody].pos;
							buffer.radiuses[bodies[curBody].time % buffer.size][curBody]=bodies[curBody].radius;
						}
						
					}finally {
						System.out.println("Unlock by thread :" + Thread.currentThread().getId() + " i : " + curBody + "bodyTime = " + bodies[curBody].time);
						bodies[curBody].lock.unlock();
					}
				}
			}
			
			if(increment)	{
				mergeLock.lock();
				try{
					// Useful so that only one thread does detectCollisions
					if(!buffer.mergeAlreadyDone[currentTime % buffer.size]){
						System.out.println("Entered Lock : Thread : " + Thread.currentThread().getId() +"clockTime : "+clock.time.get());
						detectCollisions();
						System.out.println("nBody : " + nBody[0]);
						System.out.println("time :" + clock.time.get());
						buffer.mergeAlreadyDone[clock.time.get() % buffer.size] = true;
						buffer.mergeAlreadyDone[(clock.time.get() + 1) % buffer.size] = false;	//For the next time
						buffer.nBody[clock.time.get() % buffer.size] = nBody[0];
					}
				} finally{ mergeLock.unlock();}
				buffer.waitWrite(currentTime + 1);
				if(clock.time.compareAndSet(currentTime,currentTime+1)){
					System.out.println("Time has changed : Thread : " + Thread.currentThread().getId() +"clockTime : "+clock.time.get());
				}
			}
			currentTime = clock.time.get();
		}
	}
	
	//updates collisioncCasses, NOT Thread Safe
	public void detectCollisions(){
		System.out.println("BEGIN of detectedCollisions Thread : " + Thread.currentThread().getId());
		boolean[] classAssigned = new boolean[nBody[0]]; // Initializes at false
		int[] collisionClasses = new int[nBody[0]];
		
		// At the end of this block, collisionClasses[i] contains the "root" of the collisionClass of i
		for(int i=0; i<nBody[0]; i++)		collisionClasses[i]=i;
		for(int i=0; i<nBody[0]; i++){
			if(classAssigned[i])	continue;
			for(int j=0; j<nBody[0];j++){
				if((!classAssigned[j])&& (bodies[i].pos.distance(bodies[j].pos) <= bodies[i].radius + bodies[j].radius) ){
					collisionClasses[j] = collisionClasses[i];
					if(collisionClasses[i] == -1) collisionClasses[i]=i;
				}
			}
		}
		
		//Computes newNBody
		int newNBody=0;
		for(int i=0; i<nBody[0]; i++){
			if(collisionClasses[i] == i)	newNBody++;
		}
		
		//Computes the roots of the equivalence classes
		int[] roots= new int[newNBody];
		int temp=0;
		for(int i=0; i<nBody[0]; i++){
			if(collisionClasses[i] == i){
				roots[temp] = i;
				temp++;
			}
		}
		assert(newNBody == temp);
		
		//Creates and fill newBodies
		Body[] newBodies = new Body[newNBody];
		for(int i=0; i<newNBody;i++){
			
			float totalMass=0;
			float totalSquareRadius=0;
			Vector averagePos = bodies[roots[i]].pos;
			Vector p = new Vector();
			int nInClass=0;
			
			for(int j=0; j<nBody[0];j++){
				if(collisionClasses[j] == roots[i])	nInClass++;
			}
			for(int j=0; j<nBody[0];j++){
				if(collisionClasses[j] == roots[i]){
					totalMass+=bodies[j].mass;
					totalSquareRadius+= (bodies[j].radius * bodies[j].radius);
					Vector tempVec = bodies[j].pos.sub(bodies[roots[i]].pos);

					averagePos = averagePos.add(tempVec.mul(1/(float)nInClass));

					p=p.add(bodies[j].speed.mul(bodies[j].mass));
				}
			}
			//the speed of newBody is derives from  p conservation
			totalSquareRadius =  (float) Math.sqrt(totalSquareRadius);
			newBodies[i] = new Body(bodies[0].time,i, totalMass, totalSquareRadius, averagePos, p.mul(1/(float)totalMass), new Vector(), newNBody);
			
			// Should we update acc here or not ? 
		}
		bodies= newBodies;
		nBody[0]=newNBody;
		System.out.println("END of detectedCollisions Thread : " + Thread.currentThread().getId());
		
	}
}
