package io.github.frizman21.common.sm;

/**
 * 
 * @author Mike Frizzell
 *
 */
public interface StateMachineListener {

	public void onEnterState(State state);
	
	public void onExitState(State state);
	
	public void onTransition(Transition transition);
}
