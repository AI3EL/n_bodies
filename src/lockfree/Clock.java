package lockfree;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * In this implementation clock is just an atomic integer wrapper
 */

public class Clock {
	public AtomicInteger time;
	
	public Clock(){
		time= new AtomicInteger(0);
	}
}
