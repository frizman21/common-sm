package io.github.frizman21.common.sm;

import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an activity that can attached to a State or Transition that causes a
 * GenericTimeoutEvent to fire later.
 */
public class TimeoutActivity extends AbstractActivity {

  static Logger ExecLogger = LoggerFactory.getLogger("friz.cs.StateMachine.Execution");

  TimerTask task;
  long duration;
  Timer timer = new Timer();

  /**
   * This enables you to build a TimeoutActivity that will fire a
   * GenericTimeoutEvent later.

   * @param duration in milliseconds
   */
  public TimeoutActivity(long duration) {
    super();

    this.duration = duration;
  }

  @Override
  public void run() {
    task = new TimerTask() {

      @Override
      public void run() {
        try {
          machine.eventHappens(new GenericTimeoutEvent());
        } catch (ConfigException e) {
          ExecLogger.warn("exception when firing the timeout event", e);
        }
      }
    };

    timer.schedule(task, duration);
  }

  public boolean cancelTimeout() {
    return task.cancel();
  }

  @Override
  public String getName() {
    return "Setup Timeout";
  }
}
