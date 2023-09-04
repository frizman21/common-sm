package io.github.frizman21.common.sm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Mike Frizzell
 *
 */
public class StateMachine implements Runnable {

	static Logger ConfigLogger = LoggerFactory.getLogger("friz.cs.StateMachine.Config");
	static Logger ExecLogger = LoggerFactory.getLogger("friz.cs.StateMachine.Execution");
	
	private long mainExecutionLoopSleepDuration=10; 
	
	List<Event> registeredEvents;
	List<StateMachineListener> stateMachineListeners;
	List<State> states;
	State currentState;
	State startState;
	String name;
	Queue<Event> eventQueue;
	Map<String,Object> machineState;
	
	public StateMachine(String name) {
		super();
	
		this.name = name;
		this.states	= new ArrayList<State>();
		this.stateMachineListeners = new ArrayList<StateMachineListener>();
		this.registeredEvents = new ArrayList<Event>();
		eventQueue = new LinkedBlockingQueue<Event>();
		this.machineState = new HashMap<String,Object>();
		
	}

	public boolean register(Event event) {
		return registeredEvents.add(event);
	}

	public boolean add(StateMachineListener listener) {
		return stateMachineListeners.add(listener);
	}

	public State createStartState(String name) throws ConfigException {
		
		// configuring start state
		if(startState == null) { // if we don't have one, 
			// set it up! 
			State state = createState(name);
			
			this.startState = state;
			ConfigLogger.info("Declaring Start state (" + name + ")");
			
			return state;
			
		// if we have a start state - throw ConfigException.	
		} else { 	
			String issueText = "State Machine already has a start state. Cannot have two start states.";
			ConfigLogger.warn(issueText); // TODO: consier, should be both throw an exception and warn? or is this redundant?
			throw new ConfigException(issueText);
		}		
	}
	
	public State createState(String name) {
		return createState(name,false);
	}
		
	public State createState(String name, boolean isEndState) {
		ConfigLogger.info("Create state (" + name + ")");
		State state = new State(name,isEndState,this);
		
		this.states.add(state);
		
		return state;
	}
	
	public void startMachine(boolean startThread) throws ConfigException {
		if(startThread) {
			Thread thread = new Thread(this,"State Machine Event Launch Loop");
			thread.start();
		} else {
			run();
		}
	}
	
	/**
	 * This should only execute once! Execute this to transition this state machine into its initial state.
	 * @throws ConfigException
	 */
	private void executeFirstTransition() throws ConfigException {
		// TODO: consider, should we initializing using a transition structure first? A transition without a "from."
		//       this would allow API users to have access to another tier of activities that they could fire just on 
		//       startup - in particular if the first state is transition during a restart cycle or something.
		
		if(startState == null) {
			String issueText = "Cannot start a stateMachine without an initial state.";
			
			ExecLogger.warn(issueText);
			throw new ConfigException(issueText);
		}
		
		// initialize machine state
		ExecLogger.debug("Initializing state machine at state " + this.startState.getName());
		this.currentState = startState;
		
		// communicate to listeners onEnterState
		for(StateMachineListener listener : this.stateMachineListeners) 
			try { listener.onEnterState(this.currentState); } catch(Exception e) { ExecLogger.warn(e.getMessage(),e); }
				
		// run all the activities in the resulting state
		runActivities(this.currentState.activities);
	}
	
	public void eventHappens(Event event) throws ConfigException {
		// TODO: confirm that the current state has this event as an option.
		ExecLogger.debug("Event " + event.getName() + " occured.");
		
		eventQueue.add(event);
	}
		
	/**
	 * Execute this function when an event needs to tickle the state machine.
	 * @param event
	 * @throws ConfigException
	 */
	private void executeEvent(Event event) throws ConfigException {
		// pick the transition based on the event type
		String className = event.getClass().getName();
		Transition transition = currentState.get(className);
		
		// verify that state machine structure has this state.
		if(transition == null) {
			String issueText = 
					"Current state (" + currentState.getName() + ") does not have a " +
					"transition for that event type. (" + event.getName() + ")";
			
			ConfigLogger.warn(issueText);
			
			throw new ConfigException(issueText);
		}
		
		// communicate to listeners onExitState
		for(StateMachineListener listener : this.stateMachineListeners) 
			try { listener.onExitState(transition.to); } catch(Exception e) { ExecLogger.warn(e.getMessage(),e); } 
			
		// communicate to listeners onTransition
		for(StateMachineListener listener : this.stateMachineListeners) 
			try { listener.onTransition(transition); } catch(Exception e) { ExecLogger.warn(e.getMessage(),e); } 
				
		// run all the activities in the transition
		runActivities(transition.activities);
		
		// transition states
		ExecLogger.debug("Transitioning from " + this.currentState.getName() + " to " + transition.to.getName() + " on " + event.getName());
		this.currentState = transition.to;
		
		// communicate to listeners onEnterState
		for(StateMachineListener listener : this.stateMachineListeners) 
			try { listener.onEnterState(transition.to); } catch(Exception e) { ExecLogger.warn(e.getMessage(),e); }
				
		// run all the activities in the resulting state
		runActivities(this.currentState.activities);
		
		// if this state is considered a termination state, flip the running flag to kill the execution loop.
		if(this.currentState.isEndState()) {
			running = false;
			ExecLogger.debug("running is now false. thread should end soon.");
		}
	}
	
	/**
	 * Helper function to clean up / not cut-and-paste code.
	 * 
	 * @param activities
	 */
	private static void runActivities(List<Activity> activities) {
		// loop through all the activities and run them
		for(Activity activity : activities) {
			try {
				
				ExecLogger.debug("Activity (" + activity.getName() + ") being run.");
				activity.run();
				
			// catch any exception that other people's crappy code let's through. 	
			} catch(Exception ex) {
				ExecLogger.warn("Activity " + activity.getName() + " threw an Exception.", ex);
				ex.printStackTrace();
			}
		}
	}
	
	private boolean running;
	
	@Override
	public void run() {
		// flip the running flag
		running = true;

		// fire up the first state
		try {
			executeFirstTransition();			
		} catch (ConfigException e) {
			ConfigLogger.warn("startMachine() failed to run");
			return;
		}
		
		// main execution loop
		while(running) {
			
			if(eventQueue.size() > 0) {
				Event event = eventQueue.poll();
				
				try {
					this.executeEvent(event);
				} catch (ConfigException e) {
					ExecLogger.warn("Event execution failed: ", e);
				}
			} else {
				try {
//					ExecLogger.debug("sleeping for " + mainExecutionLoopSleepDuration + "ms waiting for next Event.");
					Thread.sleep(mainExecutionLoopSleepDuration);
				} catch (InterruptedException e) {
					ConfigLogger.warn("Main Execution Loop Sleep Excepted",e);
				}
			}
		}
	}
	
	/**
	 * This will kill the internal thread that fires the next event.
	 */
	public void kill() {
		// setting running to false will kill the main execution loop on the next iteration.
		this.running = false;
	}
	
	public boolean isMachineRunning() {
		return running;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean waitUntilDone(long timeout) {
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		while((startTime + timeout) > Calendar.getInstance().getTimeInMillis()) {
			
			if(!running) {
				ExecLogger.debug("Running flag off. waitUntilDone succeeds.");
				return true;
			}
			
			try {
				Thread.sleep(mainExecutionLoopSleepDuration);
			} catch (InterruptedException e) {
				ConfigLogger.warn("sleep failed while WaitingUtilDone", e);
				return false;
			}
		}
		ExecLogger.debug("Timeout reached, waitUntilDone failed.");
		return false;
	}

	public State getState() {
		return currentState;
	}

	public Object getFromMachineState(Object key) {
		return machineState.get(key);
	}

	public Object putFromMachineState(String key, Object value) {
		return machineState.put(key, value);
	}
	
}
