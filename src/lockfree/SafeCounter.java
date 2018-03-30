package lockfree;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SafeCounter {
	private int n;
	Lock lock;
	Condition isNull;
	
	public SafeCounter(int n){
		this.n =n;
		lock = new ReentrantLock();
		isNull=lock.newCondition();
	}
	
	public int get(){
		lock.lock();
		try{
			return n;
		} finally{
			lock.unlock();

		}
	}
	
	public void increment(){
		lock.lock();
		try{
			n++;
		} finally{
			lock.unlock();

		}
	}
	public synchronized void decrement(){
		lock.lock();
		try{
			n--;
			if(n==0)	isNull.signalAll();

		} finally{
			lock.unlock();

		}
	}
}
