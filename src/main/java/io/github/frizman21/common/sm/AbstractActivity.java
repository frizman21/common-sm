package io.github.frizman21.common.sm;

import java.util.Properties;

public abstract class AbstractActivity implements Activity {

	protected StateMachine machine;
	protected Properties props;

	@Override
	public void setStateMachine(StateMachine machine) {
		this.machine = machine;
	}
	
	@Override
	public void init(Properties props) throws ConfigException {
		this.props = props;
	}

}
