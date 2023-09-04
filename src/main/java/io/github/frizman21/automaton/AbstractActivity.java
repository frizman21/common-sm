package io.github.frizman21.automaton;

public abstract class AbstractActivity implements Activity {

	protected StateMachine machine;

	@Override
	public void setStateMachine(StateMachine machine) {
		this.machine = machine;
	}

}
