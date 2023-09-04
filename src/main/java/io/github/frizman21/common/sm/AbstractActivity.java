package io.github.frizman21.common.sm;

public abstract class AbstractActivity implements Activity {

	protected StateMachine machine;

	@Override
	public void setStateMachine(StateMachine machine) {
		this.machine = machine;
	}

}
