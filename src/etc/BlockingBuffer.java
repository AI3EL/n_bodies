package etc;

import etc.Buffer;

import java.util.concurrent.locks.*;

/*
 * Container for storing the data frames in a circular buffer.
 * Conditions are used to wait for data to be put into the buffer or for space
 * to be available to store some more data.
 */

public class BlockingBuffer extends Buffer {
	public BlockingBuffer(int size, int n, Body[] bodies)
	{
		super(size, n, bodies);
	}

	public BlockingBuffer(int size, int n)
	{
		super(size, n);
	}

	/// Signals a frame is ready for rendering and wait for room.
	/** This function must be called by the engine to :
	 *   - signal the frame "frame" is ready for rendering, and potentially
	 *   allow the visualizer to read it.
	 *   - wait for the location of the frame "(frame + 1)" to be available
	 *   (i.e. all data in it has been displayed or dropped) for
	 *   overwriting.
	 *
	 *   The function is reentrant, to the extent of :
	 *    - multiple threads can signal and wait for the same frame at the
	 *    same time, and will be released together ;
	 *    - a thread can can signal and wait for a frame that has already be
	 *    signaled and waited for, and will not be blocked in this case.
	 *
	 *   Computing threads MUST NOT write to ANY frame, including especially "frame",
	 *   after any of them has  called (even if not yet returned from) this function.
	 *   Computing threads MUST NOT write to any other frame than
	 *   "frame + 1" after the function has returned, and until the call for
	 *   the following frame has been made.
	 *   Skipping frames (e.g. waiting for frame 4 then 6 without calling
	 *   for 5 inbetween) is not supported.
	 */
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
			while(lastFrame == (frame + 1) && last == first) {
				waited = true;
				notFull.await();
				if(lastFrame == (frame + 1) && last == first) {
					System.out.println("Spurious wakeup !");
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return waited;
	}

	/// Signals a frame is ready for overwriting and wait for the next one
	/** Signals that a frame (starting with 0) has been displayed or dropped
	 * and waits for the next one to be available. No further reads to
	 * "frame" are allowed after the call has been made.
	 *  This function is NOT reentrant.
	 */
	@Override
	public boolean waitRead() {
		boolean waited = false;
		lock.lock();
		try {
			first = (first + 1) % size;
			notFull.signalAll();
			while(first == last) {
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

	/// Signals a frame is ready for overwriting if the next one is ready
	/** Checks if a new frame is available.
	 *  If not, returns false. No signaling is done, so the corresponding frame
	 *  is still readable in this case
	 *  If yes, signals that the corresponding frame (starting with 0) has been displayed or dropped
	 *  No further reads to "frame" are allowed after the call has been
	 *  successfully made.
	 *  This function is NOT reentrant.
	 */
	@Override
	public boolean pollRead() {
		boolean success = false;
		lock.lock();
		if((first + 1) % size != last) {
			first = (first + 1) % size;
			notFull.signalAll();
			success = true;
		}
		lock.unlock();
		return success;
	}

	private int first = 0;
	private int last = 1;
	private int lastFrame = 1;
	private Lock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();
}
