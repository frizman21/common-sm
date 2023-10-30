package io.github.frizman21.common.sm;

/**
 * This is the base class for Events that can be fired for a state machine.
 */
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
