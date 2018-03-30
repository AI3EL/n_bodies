package lockfree;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import etc.Body;
import etc.Force;
import etc.Vector;
import etc.Buffer;

import systems.PSystem;

/*
 * ProcessingNode update the position of the bodies.
 * For this it uses a AtomicInteger counter that holds the value of the minimum time the bodies
 * The Thread tries to get the lock of a body(bodies[first] is the first it tries, generally, the firsts are equally distributed), if not possible he tries another one
 * Once body is locked, he checks that the time of the body is not exceeding the minimum time ( this will be optimized)
 * NOTE: when incrementing clock.time, every other Thread is trying to do the same or in pause
 * So we can use a Buffer with size 1 and just a forces array because every Thread IN A CRITICAL PART has the same time
 * Log
 * When time was updated in the middle
 * Thread : 17Waking : currentTime is :1 clock is 1 Counter is : 0
Ending While Thread : 17 CurrentTime 1 BUFFER NBODY 25 25 0 0 0 0
Time has changed : Thread : 9clockTime : 2 CurreentTime :1
Thread : 14WaitingCounter, currentTime is :2 clock is 2 Counter is : 3
Thread : 11Waking : currentTime is :1 clock is 2 Counter is : 0
Ending While Thread : 11 CurrentTime 2 BUFFER NBODY 25 25 0 0 0 0
Time has changed : Thread : 11clockTime : 3 CurreentTime :2
OLDNBODY ==0 : Thread :11 CurrentTime 3
 Let say Thread A changes clock from 0 to 1.
 It does the merge it pauses just before doing currentTime=clock.time.get()
 After this, Thread B updates every position and changes clock from 1 to 2
 and stops before merging.
 Here Thread A wakes up and does currentTime = clock.time
 He does updates positions and enters the merge -> problem because merge at time 1 was not done ! 
 */


public class ProcessingNode implements Runnable {
	
	Force force;
	Buffer buffer;
	float delta;
	int maxTime;
	Clock clock;
	int fillTime; // Each fillTime timesteps, the node puts in forces[][] the resultant forces of each
	boolean[][] isNegligible;

	int first;
	
	public ProcessingNode(PSystem system, LockfreeEngine engine, int first){
		this.force = system.getForce();
		this.buffer = engine.buffer;
		this.delta = engine.delta;
		this.maxTime = engine.maxTime;
		this.clock = engine.clock;
		this.isNegligible = engine.isNegligible;
		this.fillTime = engine.fillTime;
		this.first=first;
	}
	
	@Override
	public void run() {
		int currentTime = 0;
		//maxTime - 1 because body[i].time is incremented in the loop
		while(currentTime < maxTime-1){
			for(int i=0; i< buffer.nBody[currentTime % buffer.size]; i++){
				int curBody = (i+first) % buffer.nBody[currentTime % buffer.size];
				
				if(buffer.updated[currentTime % buffer.size][curBody])	continue;
				buffer.counter[currentTime% buffer.size].increment();
				
				//The following block updates curBody
				if(buffer.bodies[currentTime % buffer.size][curBody].lock.tryLock()){
					labelTry : try{						
						// In case we entered just after someone has finished the job
						if( buffer.updated[currentTime % buffer.size][curBody] )	break labelTry;	
						else{
							Body updatedBody = new Body(currentTime, curBody, buffer.bodies[currentTime % buffer.size][curBody].mass, buffer.bodies[currentTime % buffer.size][curBody].q, buffer.bodies[currentTime % buffer.size][curBody].radius, buffer.bodies[currentTime % buffer.size][curBody].pos, buffer.bodies[currentTime % buffer.size][curBody].speed, buffer.bodies[currentTime % buffer.size][curBody].acc, buffer.nBody[currentTime % buffer.size]);
							if(buffer.bodies[currentTime % buffer.size][curBody].time % fillTime == 0)	updatedBody.setAll(buffer.bodies[currentTime % buffer.size], force, delta, isNegligible, false);
							else	updatedBody.setAll(buffer.bodies[currentTime % buffer.size], force, delta, isNegligible, true);
							buffer.bodies[(currentTime + 1 )% buffer.size][curBody] = updatedBody;

						}
					}finally {
						buffer.updated[currentTime % buffer.size][curBody] = true;
						buffer.updated[(currentTime + 1 )% buffer.size][curBody] = false;
						buffer.bodies[currentTime % buffer.size][curBody].lock.unlock();
					}
				}//if
				buffer.counter[currentTime% buffer.size].decrement();
			}//for
			
			// When here it means that for each body, either it is updated either it is being updated (meaning it is locked)
			// Before merging we have to wait that every position was updated
			buffer.counter[(currentTime) % buffer.size].lock.lock();
			if(buffer.counter[(currentTime) % buffer.size].get()!=0){
				buffer.counter[(currentTime) % buffer.size].isNull.awaitUninterruptibly();
			}
			
			//DetectCollisions will behave on the new Positions that have been just calculated
			try{
				// Useful so that only one thread does detectCollisions
				// buffer.mergeAlreadyDone[currentTime % buffer.size]) is protected by buffer.counter.lock
				if(!buffer.mergeAlreadyDone[currentTime % buffer.size]){

					detectCollisions(currentTime+1);

					buffer.mergeAlreadyDone[(currentTime) % buffer.size] = true;
					buffer.mergeAlreadyDone[(currentTime + 1) % buffer.size] = false;	//Is useful in case time > buffer.size
					buffer.waitWrite(currentTime+1);

				}
			} finally{ buffer.counter[(currentTime) % buffer.size].lock.unlock();}
				first = (first * buffer.nBody[(currentTime+1)% buffer.size]) / buffer.nBody[(currentTime )% buffer.size];
			

			
			if(clock.time.compareAndSet(currentTime,currentTime+1)){
				//System.out.println(clock.time.get());;
			}
			currentTime=clock.time.get();
		}
	}
	
	//updates collisioncCasses, NOT Thread Safe
	public void detectCollisions(int curTime){
		int oldNBody = buffer.nBody[(curTime-1) % buffer.size];
		Body[] bodies = buffer.bodies[curTime % buffer.size];
		boolean[] classAssigned = new boolean[oldNBody]; // Initializes at false
		int[] collisionClasses = new int[oldNBody];
		if(oldNBody==0)	System.out.println("OLDNBODY ==0 : Thread :" + Thread.currentThread().getId() + " CurrentTime " + curTime);
		
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
		
		//Creates and fill newBodies
		Body[] newBodies = new Body[newNBody];
		for(int i=0; i<newNBody;i++){
			Vector averagePos = new Vector();
			float maxRadius=bodies[roots[i]].radius;
			float nMaxRadius=0.0f;
			for(int j=0; j<oldNBody;j++){
				if(collisionClasses[j] == roots[i] && maxRadius < bodies[j].radius)		maxRadius=bodies[j].radius;
			}
			for(int j=0; j<oldNBody;j++){
				if(collisionClasses[j] == roots[i] && maxRadius == bodies[j].radius){
					averagePos=averagePos.add(bodies[j].pos);
					nMaxRadius++;
				}
			}
			averagePos = averagePos.mul(1/nMaxRadius);

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

		}
		buffer.bodies[curTime%buffer.size] = newBodies;	
		buffer.nBody[curTime%buffer.size]=newNBody;
	}
}
