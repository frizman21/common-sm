package us.mikefrizzell.automaton;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutActivity extends AbstractActivity {
	
	static Logger ExecLogger = LoggerFactory.getLogger("friz.cs.StateMachine.Execution");
	
	StateMachine machine;
	TimerTask task;
	long duration;
	Timer timer = new Timer();
	
	public TimeoutActivity(long duration) {
		super();
		
		this.duration = duration;
	}

	@Override
	public void run() {
		task = new TimerTask() {
			
			@Override
	        public void run() {
	        	try {
					machine.eventHappens( new GenericTimeoutEvent() );
				} catch (ConfigException e) {
					ExecLogger.warn("exception when firing the timeout event", e);
				}
	        }
	    };
	    
		timer.schedule( task, duration );
	}

	public boolean cancelTimeout() {
		return task.cancel();
	}

	@Override
	public String getName() {
		return "Setup Timeout";
	}
}
