package io.github.frizman21.common.sm;

/**
 * This activity cancels a previous Timeout Activity that may be running.
 */
public class CancelTimeoutActivity extends AbstractActivity {

  TimeoutActivity timeoutActivity;

  public CancelTimeoutActivity(TimeoutActivity timeoutActivity) {
    super();
    this.timeoutActivity = timeoutActivity;
  }

  @Override
  public String getName() {
    return "CancelTimeoutActivity";
  }

  @Override
  public void run() {
    timeoutActivity.cancelTimeout();
  }
  
}
