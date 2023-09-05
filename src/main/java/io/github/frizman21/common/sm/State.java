package io.github.frizman21.common.sm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;

public class State {

	private Logger ConfigLogger = StateMachine.ConfigLogger;
	private Logger ExecLogger = StateMachine.ExecLogger;
	
	private String name;
	private boolean isEndState;
	private Map<String,Transition> transitionMap;
	List<Activity> activities;
	StateMachine machine;
	Properties props;

	State(String name,boolean isEndState, StateMachine machine ) {
		super();
		this.name = name;
		this.isEndState = isEndState;
		this.machine = machine;
		
		activities = new ArrayList<Activity>();
		transitionMap = new HashMap<String,Transition>();
	}
	
	void init(Properties props) {
		this.props = props;
	}
	
	Transition get(String eventClassName) {
		return transitionMap.get(eventClassName);
	}

	/**
	 * 
	 * @param transitionName
	 * @param eventClass
	 * @param toState
	 * @return
	 */
	public Transition addTransition(String transitionName, Class eventClass, State toState) {
		ConfigLogger.info("Adding transition (" + transitionName + ") from " + name + " to " + toState.name);
		
		// get class name
		String className = eventClass.getName();
		
		// build transition
		Transition transition = new Transition(transitionName, eventClass, this, toState, machine);
		transition.init(props);
		
		// add to the transition data structure.
		transitionMap.put(className, transition);
		
		// return for the user to add Activities to.
		return transition;
	}
	
	/**
	 * Activities do not need to be thread safe.
	 * An activity object will be run each time through the state transition.
	 * Activities added to the state will be executed upon entering that state serially.
	 * 
	 * @param activity
	 * @return
	 * @throws ConfigException 
	 */
	public boolean add(Activity activity) throws ConfigException {
		ConfigLogger.info("Adding activity (" + activity.getName() +") to state (" + name + ")");
		
		activity.setStateMachine(machine);
		
		activity.init(props);
		
		return activities.add(activity);
	}

	public String getName() {
		return name;
	}

	public boolean isEndState() {
		return isEndState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		return Objects.equals(name, other.name);
	}
}
