package io.github.frizman21.common.sm;

/**
 * This is a Listener that can be fired based on State Machine behaviors.
 */
public interface StateMachineListener {

  public void onEnterState(State state);

  public void onExitState(State state);

  public void onTransition(Transition transition);

}
