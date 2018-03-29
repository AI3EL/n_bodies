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
	
	Clock clock;
	int first;
	Force force;
	float delta;
	Buffer buffer;
	int maxTime;
	int fillTime; // Each fillTime timesteps, the node puts in forces[][] the resultant forces of each
	boolean[][] isNegligible;
	SafeCounter counter;
	
	public ProcessingNode(SafeCounter counter,Clock clock, int first, Force force, float delta, Buffer buffer, int maxTime, boolean[][] isNegligible, int fillTime){
		this.first=first;
		this.force=force;
		this.delta=delta;
		this.buffer=buffer;
		this.clock=clock;
		this.maxTime = maxTime;
		this.isNegligible = isNegligible;
		this.fillTime = fillTime;
		this.counter=counter;
	}
	
	@Override
	public void run() {
		int currentTime = clock.time.get();
		//maxTime - 1 because body[i].time is incremented in the loop
		while(currentTime < maxTime-1){
			for(int i=0; i< buffer.nBody[currentTime % buffer.size]; i++){
				int curBody = (i+first) % buffer.nBody[currentTime % buffer.size];
				//System.out.println("curBody : " + curBody);
				if(buffer.updated[currentTime % buffer.size][curBody])	continue;
				// tryLock() tries to take the lock, if no continues
				// There is an alternative that tries for a certain amount of time before giving up
				
				// Need to think at whether we can use currentTime or time.get() ...
				counter.increment();
				if(buffer.bodies[currentTime % buffer.size][curBody].lock.tryLock()){
					//Lock is locked if and only if we enter the try
					try{
						
						//System.out.println("LOCKING thread :" + Thread.currentThread().getId() + " i : " + curBody + "bodyTime = " + buffer.bodies[currentTime % buffer.size][curBody].time+ "currentTime" + currentTime);
						
						// In case we entered just after someone has finished the job
						if( buffer.bodies[currentTime % buffer.size][curBody].time > currentTime )	break;	
						else{
							//System.out.println(bodies[curBody].toString());
							Body updatedBody = new Body(currentTime, curBody, buffer.bodies[currentTime % buffer.size][curBody].mass, buffer.bodies[currentTime % buffer.size][curBody].q, buffer.bodies[currentTime % buffer.size][curBody].radius, buffer.bodies[currentTime % buffer.size][curBody].pos, buffer.bodies[currentTime % buffer.size][curBody].speed, buffer.bodies[currentTime % buffer.size][curBody].acc, buffer.nBody[currentTime % buffer.size]);
							if(buffer.bodies[currentTime % buffer.size][curBody].time % fillTime == 0)	updatedBody.setAll(buffer.bodies[currentTime % buffer.size], force, delta, isNegligible, false);
							else	updatedBody.setAll(buffer.bodies[currentTime % buffer.size], force, delta, isNegligible, true);
							buffer.bodies[(currentTime + 1 )% buffer.size][curBody] = updatedBody;
							System.out.println(updatedBody.toString());

						}
					}finally {
						//System.out.println("Unlock by thread :" + Thread.currentThread().getId() + " i : " + curBody + "bodyTime = " + buffer.bodies[currentTime%buffer.size][curBody].time);
						buffer.updated[currentTime % buffer.size][curBody] = true;
						buffer.updated[(currentTime + 1 )% buffer.size][curBody] = false;
						buffer.bodies[currentTime % buffer.size][curBody].lock.unlock();
					}
				}
				counter.decrement();
			}	//for
			
			counter.lock.lock();
			if(counter.get()!=0)	counter.isNull.awaitUninterruptibly();
			
			buffer.waitWrite(currentTime); //Many threads will pass by here
			// Was down detectCollisino and with currentTIme + 1
			if(clock.time.compareAndSet(currentTime,currentTime+1)){
				//System.out.println("Time has changed : Thread : " + Thread.currentThread().getId() +"clockTime : "+clock.time.get());
			}
			currentTime = clock.time.get();
			//DetectCollisions will behave on the new Posittions that have been just calculated
			//DetectCollisions must be done when all positions have been set for this time -> counter
			// When a Thread arrives here it means that for each body : either he was set, either someone entered the lock
			
			try{
				// Useful so that only one thread does detectCollisions
				if(!buffer.mergeAlreadyDone[currentTime % buffer.size]){
					//System.out.println("Entered Lock d Thread : " + Thread.currentThread().getId() +"clockTime : "+clock.time.get());
					detectCollisions(currentTime);
					//System.out.println("nBody : " + buffer.nBody[currentTime % buffer.size]);
					//System.out.println("Clocktime :" + clock.time.get());
					buffer.mergeAlreadyDone[clock.time.get() % buffer.size] = true;
					buffer.mergeAlreadyDone[(clock.time.get() + 1) % buffer.size] = false;	//For the next time
				}
			} finally{ counter.lock.unlock();}
			
			first = first * buffer.nBody[(currentTime - 1 )% buffer.size] / buffer.nBody[(currentTime - 1 )% buffer.size];
		}
	}
	
	//updates collisioncCasses, NOT Thread Safe
	public void detectCollisions(int curTime){
		int oldNBody = buffer.nBody[(curTime-1)%buffer.size];
		Body[] bodies = buffer.bodies[curTime%buffer.size];
		boolean[] classAssigned = new boolean[oldNBody]; // Initializes at false
		int[] collisionClasses = new int[oldNBody];
		
		// At the end of this block, collisionClasses[i] contains the "root" of the collisionClass of i
		for(int i=0; i<oldNBody; i++)		collisionClasses[i]=i;
		for(int i=0; i<oldNBody; i++){
			for(int j=0; j<oldNBody;j++){
				if((i!=j) && (!classAssigned[j])&& (bodies[i].pos.distance(bodies[j].pos) <= bodies[i].radius + bodies[j].radius) ){
					collisionClasses[j] = collisionClasses[i];
					if(collisionClasses[i] == -1) collisionClasses[i]=i;
				}
			}
		}
		
		//Computes newNBody
		int newNBody=0;
		for(int i=0; i<oldNBody; i++){
			if(collisionClasses[i] == i)	newNBody++;
		}
		
		//Computes the roots of the equivalence classes
		int[] roots= new int[newNBody];
		int temp=0;
		for(int i=0; i<oldNBody; i++){
			if(collisionClasses[i] == i){
				roots[temp] = i;
				temp++;
			}
		}
		assert(newNBody == temp);
		
		//Creates and fill newBodies
		Body[] newBodies = new Body[newNBody];
		if(newNBody != oldNBody)System.out.println("Old :" + oldNBody + "New : " + newNBody + "Time :" + curTime);
		for(int i=0; i<newNBody;i++){
			Vector averagePos = new Vector();
			float maxMass=bodies[roots[i]].mass;
			float nMaxMass=0.0f;
			for(int j=0; j<oldNBody;j++){
				if(collisionClasses[j] == roots[i] && maxMass < bodies[j].mass)		maxMass=bodies[j].mass;
			}
			for(int j=0; j<oldNBody;j++){
				if(collisionClasses[j] == roots[i] && maxMass == bodies[j].mass){
					averagePos=averagePos.add(bodies[j].pos);
					nMaxMass++;
				}
			}
			averagePos = averagePos.mul(1/nMaxMass);

			float totalMass=0;
			float totalQ=0;
			float totalSquareRadius=0;
			Vector p = new Vector();
			int nInClass=0;
			
			for(int j=0; j<oldNBody;j++){
				if(collisionClasses[j] == roots[i])	nInClass++;
			}
			
			for(int j=0; j<oldNBody;j++){
				if(collisionClasses[j] == roots[i]){
					totalMass+=bodies[j].mass;
					totalQ+=bodies[j].q;
					totalSquareRadius+= (bodies[j].radius * bodies[j].radius);
					p=p.add(bodies[j].speed.mul(bodies[j].mass));
				}
				
			}
			//the speed of newBody is derives from  p conservation
			totalSquareRadius =  (float) Math.sqrt(totalSquareRadius);
			newBodies[i] = new Body(bodies[0].time,i, totalMass,totalQ, totalSquareRadius, averagePos, p.mul(1/(float)totalMass), new Vector(), newNBody);
			if(nInClass > 1){
				System.out.println("Global : mass "+ newBodies[i].mass + " posX "+ newBodies[i].pos.x+  " PosY : " +newBodies[i].pos.y);
			}
		}
		buffer.bodies[curTime%buffer.size] = newBodies;	
		buffer.nBody[curTime%buffer.size]=newNBody;
	}
}
