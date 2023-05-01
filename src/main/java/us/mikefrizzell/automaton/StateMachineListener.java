package us.mikefrizzell.automaton;

/**
 * 
 * @author 1042090
 *
 */
public interface StateMachineListener {

	public void onEnterState(State state);
	
	public void onExitState(State state);
	
	public void onTransition(Transition transition);
}
