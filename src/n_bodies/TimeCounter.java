package n_bodies;

public class TimeCounter {
	public int time;
	
	public synchronized void increment(){
		time++;
	}
}
