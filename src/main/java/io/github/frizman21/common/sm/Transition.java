package io.github.frizman21.common.sm;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;

/**
 * This is the base class that represents the configuration of a transition between {@link State}s.
 */
public class Transition {
  private Logger configLogger = StateMachine.ConfigLogger;
  private Logger execLogger = StateMachine.ExecLogger;

  State from;
  State to;
  Class<Event> causalEvent;
  List<Activity> activities;
  String name;
  StateMachine machine;
  Properties props;

  Transition(String name, Class<Event> causalEvent, State from, State to, StateMachine machine) {
    super();
    this.causalEvent = causalEvent;
    this.from = from;
    this.to = to;
    this.name = name;
    this.machine = machine;

    activities = new ArrayList<Activity>();
  }

  /**
   * This allows you to add Activity when this Transition fires.

   * @param activity {@link Activity} to run on when this {@link Transition} occurs.
   * @return true if activity added to internal list.
   * @throws ConfigException if {@link Activity} constructor throw an error based 
             on {@link Properties}
   */
  public boolean add(Activity activity) throws ConfigException {

    configLogger.info("Adding activity (" + activity.getName() + ") to transition (" + name + ")");

    activity.setStateMachine(machine);
    activity.init(props);

    return activities.add(activity);
  }

  public String getName() {
    return name;
  }

  void init(Properties props) {
    this.props = props;
  }

}
