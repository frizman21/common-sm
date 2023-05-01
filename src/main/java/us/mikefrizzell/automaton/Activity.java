package us.mikefrizzell.automaton;

/**
 * An activity is run upon entering a state. They are run serially. The run() function should
 * return in a timely fasion - as it is holding up any other state processing. 
 * 
 * @author 1042090
 *
 */
public interface Activity extends Runnable {
	public String getName();
	
	public void setStateMachine(StateMachine machine);
}
