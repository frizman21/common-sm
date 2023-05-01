package us.mikefrizzell.automaton;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Transition {
	private Logger ConfigLogger = StateMachine.ConfigLogger;
	private Logger ExecLogger = StateMachine.ExecLogger;
	
	State from, to;
	Class<Event> causalEvent;
	List<Activity> activities;
	String name;
	StateMachine machine;
		
	Transition(String name, Class<Event> causalEvent, State from, State to, StateMachine machine) {
		super();
		this.causalEvent = causalEvent;
		this.from = from;
		this.to = to;
		this.name = name;
		this.machine = machine;
		
		activities = new ArrayList<Activity>();
	}

	public boolean add(Activity activity) {
		
		ConfigLogger.info("Adding activity (" + activity.getName() +") to transition (" + name + ")");
		
		activity.setStateMachine(machine);
		
		return activities.add(activity);
	}

	public String getName() {
		return name;
	}
	
	
	
	

}
