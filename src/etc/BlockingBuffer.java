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
	public boolean waitWrite(int frame) {
		boolean waited = false;

		lock.lock();
		try {
			if(lastFrame == frame) {
				last = (last + 1) % size;
				lastFrame++;
				notEmpty.signal();
			}
			if(lastFrame == (frame + 1) && last == first) {
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
			notFull.signalAll();
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
	private int last = 1;
	private int lastFrame = 1;
	private Lock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();
}
