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
 */


public class ProcessingNode implements Runnable {
	
	Force force;

	Buffer buffer;
	float delta;
	int maxTime;

	Clock clock;
	int fillTime; // Each fillTime timesteps, the node puts in forces[][] the resultant forces of each
	boolean[][] isNegligible;
	Lock mergeLock;
	SafeCounter counter;

	int first;
	
	public ProcessingNode(PSystem system, LockfreeEngine engine, int first){
		this.force = system.getForce();
		this.buffer = engine.buffer;
		this.delta = engine.delta;
		this.maxTime = engine.maxTime;
		this.clock = engine.clock;
		this.isNegligible = engine.isNegligible;
		this.fillTime = engine.fillTime;
		this.mergeLock = engine.mergeLock;
		this.counter = engine.counter;
		this.first=first;
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
							//System.out.println(updatedBody.toString() + "/Time : " + currentTime);

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
			
			if(clock.time.compareAndSet(currentTime,currentTime+1)){
				//System.out.println("Time has changed : Thread : " + Thread.currentThread().getId() +"clockTime : "+clock.time.get());
			}
			currentTime = clock.time.get();
			
			counter.lock.lock();
			if(counter.get()!=0)	counter.isNull.awaitUninterruptibly();
			
			//DetectCollisions will behave on the new Positions that have been just calculated
			//DetectCollisions must be done when all positions have been set for this time -> counter
			// When a Thread arrives here it means that for each body : either he was set, either someone entered the lock
			try{
				// Useful so that only one thread does detectCollisions
				if(!buffer.mergeAlreadyDone[currentTime % buffer.size]){
					buffer.waitWrite(currentTime); //Many threads will pass by here

					//System.out.println("Entered Lock d Thread : " + Thread.currentThread().getId() +"clockTime : "+clock.time.get());
					detectCollisions(currentTime);
					//System.out.println("nBody : " + buffer.nBody[currentTime % buffer.size]);
					//System.out.println("Clocktime :" + clock.time.get());
					buffer.mergeAlreadyDone[(currentTime) % buffer.size] = true;
					buffer.mergeAlreadyDone[(currentTime + 1) % buffer.size] = false;	//For the next time
				}
			} finally{ counter.lock.unlock();}
			
			first = first * buffer.nBody[(currentTime - 1 )% buffer.size] / buffer.nBody[(currentTime - 1 )% buffer.size];
		}
	}
	
	//updates collisioncCasses, NOT Thread Safe
	public void detectCollisions(int curTime){
		int oldNBody = buffer.nBody[(curTime-1) % buffer.size];
		Body[] bodies = buffer.bodies[curTime % buffer.size];
		boolean[] classAssigned = new boolean[oldNBody]; // Initializes at false
		int[] collisionClasses = new int[oldNBody];
		
		// At the end of this block, collisionClasses[i] contains the "root" of the collisionClass of i
		for(int i=0; i<oldNBody; i++)		collisionClasses[i]=i;
		for(int i=0; i<oldNBody; i++){
			for(int j=0; j<oldNBody;j++){
				try{
					if((i!=j) && (!classAssigned[j])&& (bodies[i].pos.distance(bodies[j].pos) <= bodies[i].radius + bodies[j].radius) ){
						collisionClasses[j] = collisionClasses[i];
						if(collisionClasses[i] == -1) collisionClasses[i]=i;
					}
				}
				catch(NullPointerException e){
					System.out.println("ERROR : NullPointerException : "+ e);
					System.out.println("classAssigned length : "+ classAssigned.length);
					System.out.println("OldNBody : " + oldNBody + "Bodies.length : "+ bodies.length + " i :" + i + " j : "+ j );
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
