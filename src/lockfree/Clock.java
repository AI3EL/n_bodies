package lockfree;
import java.util.concurrent.atomic.AtomicInteger;

public class Clock {
	public AtomicInteger time;
	
	public Clock(){
		time= new AtomicInteger(0);
	}
}
