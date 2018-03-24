package etc;

import etc.Buffer;

import java.util.concurrent.locks.*;

/*
 * Container for storing the data frames in a circular buffer.
 * Conditions are used to wait for data to be put into the buffer or for space
 * to be available to store some more data.
 */

public class BlockingBuffer extends Buffer {
	public BlockingBuffer(int size, int n)
	{
		super(size, n);
	}

	@Override
	public boolean waitWrite() {
		boolean waited = false;
		lock.lock();
		try {
			last = (last + 1) % size;
			notEmpty.signal();
			if(last == first) {
				waited = true;
				notFull.await();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return waited;
	}

	@Override
	public boolean waitRead() {
		boolean waited = false;
		lock.lock();
		try {
			first = (first + 1) % size;
			notFull.signal();
			if(first == last) {
				waited = true;
				notEmpty.await();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return waited;
	}

	private int first = 0;
	private int last = 0;
	private Lock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();
}
