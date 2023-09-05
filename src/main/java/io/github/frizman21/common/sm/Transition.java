package io.github.frizman21.common.sm;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;

public class Transition {
	private Logger ConfigLogger = StateMachine.ConfigLogger;
	private Logger ExecLogger = StateMachine.ExecLogger;
	
	State from, to;
	Class<Event> causalEvent;
	List<Activity> activities;
	String name;
	StateMachine machine;
	Properties props;
		
	Transition(String name, Class<Event> causalEvent, State from, State to, StateMachine machine) {
		super();
		this.causalEvent = causalEvent;
		this.from = from;
		this.to = to;
		this.name = name;
		this.machine = machine;
		
		activities = new ArrayList<Activity>();
	}

	public boolean add(Activity activity) throws ConfigException {
		
		ConfigLogger.info("Adding activity (" + activity.getName() +") to transition (" + name + ")");
		
		activity.setStateMachine(machine);
		activity.init(props);
		
		return activities.add(activity);
	}

	public String getName() {
		return name;
	}
	
	void init(Properties props) {
		this.props = props;
	}	
	
}
