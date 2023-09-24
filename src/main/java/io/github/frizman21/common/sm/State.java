package io.github.frizman21.common.sm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;

/**
 * This is the State in StateMachine. Move between States with a Transition, on an Event. 
 */
public class State {

  private Logger configLogger = StateMachine.ConfigLogger;
  private Logger execLogger = StateMachine.ExecLogger;

  private String name;
  private boolean isEndState;
  private Map<String, Transition> transitionMap;
  List<Activity> activities;
  StateMachine machine;
  Properties props;
  
  public State(String name, Boolean isEndState, StateMachine machine) {
    super();
    this.name = name;
    this.isEndState = isEndState;
    this.machine = machine;

    activities = new ArrayList<Activity>();
    transitionMap = new HashMap<String, Transition>();
  }

  void init(Properties props) {
    this.props = props;
  }

  Transition get(String eventClassName) {
    return transitionMap.get(eventClassName);
  }

  /**
   * This is the method to create a link between {@link State}s using a Transition.

   * @param transitionName The name is used in logging output.
   * @param eventClass This is the type of {@link Event} that causes this {@link Transition} to fire
   * @param toState This is the {@link State} that the {@link Transition} will take us to.
   * @return The result Transition. This object is useful for adding {@link Activity} to it.
   */
  public Transition addTransition(String transitionName, Class eventClass, State toState) {
    configLogger.info("Adding transition (" + transitionName + ") from " 
                     + name + " to " + toState.name);

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

   * @param activity The {@link Activity} to call when this {@link State} is activated.
   * @return true if Activity is added to the internal list.
   * @throws ConfigException if init function on {@link Activity} through an exception
   */
  public boolean add(Activity activity) throws ConfigException {
    configLogger.info("Adding activity (" + activity.getName() + ") to state (" + name + ")");

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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    State other = (State) obj;
    return Objects.equals(name, other.name);
  }
}
