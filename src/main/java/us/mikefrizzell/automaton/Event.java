package us.mikefrizzell.automaton;

public class Event {

	String name;
	StateMachine machine;

	public String getName() {
		return name;
	}

	protected Event(String name) {
		super();
		this.name = name;
	}

	void setMachine(StateMachine machine) {
		this.machine = machine;
	}	
}
