package io.github.frizman21.common.sm;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the core class of this library. Events must be registered. States are
 * created with this class. Events are fired against this object for the State 
 * Machine.
 *
 */
public class StateMachine implements Runnable {

  static Logger ConfigLogger = LoggerFactory.getLogger("friz.cs.StateMachine.Config");
  static Logger ExecLogger = LoggerFactory.getLogger("friz.cs.StateMachine.Execution");

  private long mainExecutionLoopSleepDuration = 10; 

  List<Event> registeredEvents;
  List<StateMachineListener> stateMachineListeners;
  
  List<State> states;
  State currentState;
  State startState;
  String name;
  Queue<Event> eventQueue;
  Map<String, Object> machineState;
  protected Properties props;
  private Transition activeTransition;
  boolean syncDefault = false;

  List<PersistentActivity> persistentRunningActivities = new ArrayList<>();

  public StateMachine(String name) {
    this(name, new Properties());
  }

  /**
   * Fully qualified constructor of the class.

   * @param name Used in logging. Helpful if you application is running more than one StateMachine.
   * @param props Used for configuration. It's pass to all running objects.
   */
  public StateMachine(String name, Properties props) {
    super();

    this.name = name;
    this.states = new ArrayList<State>();
    this.stateMachineListeners = new ArrayList<StateMachineListener>();
    this.registeredEvents = new ArrayList<Event>();
    eventQueue = new LinkedBlockingQueue<Event>();
    this.machineState = new HashMap<String, Object>();
    this.props = props;

  }

  public boolean register(Event event) {
    return registeredEvents.add(event);
  }

  public boolean add(StateMachineListener listener) {
    return stateMachineListeners.add(listener);
  }

  /**
   * Use to create a new initial {@link State}.

   * @param name Used in logging statements.
   * @return the result {@link State}
   * @throws ConfigException if bad properties sent to State
   */
  public State createStartState(String name) throws ConfigException {
    return createStartState(name, State.class);
  }

  /**
   * Use to create a new initial, custom {@link State}.

   * @param name name Used in logging statements.
   * @param clazz Custom class that extends {@link State} 
   * @return the newly created state.
   * @throws ConfigException if bad properties sent to State
   */
  public State createStartState(String name, Class<? extends State> clazz) throws ConfigException {

    // configuring start state
    if (startState == null) {
      try {
        // if we don't have one, set it up! 
        State state = // createState(name);
            clazz.getConstructor(String.class, Boolean.class, StateMachine.class)
                 .newInstance(name, false, this);
  
        this.startState = state;
        ConfigLogger.info("Declaring Start state (" + name + ")");
  
        return state;
        
      } catch (InstantiationException e) {
        throw new ConfigException("failed to create State", e);
      } catch (IllegalAccessException e) {
        throw new ConfigException("failed to create State", e);
      } catch (IllegalArgumentException e) {
        throw new ConfigException("failed to create State", e);
      } catch (InvocationTargetException e) {
        throw new ConfigException("failed to create State", e);
      } catch (NoSuchMethodException e) {
        throw new ConfigException("failed to create State", e);
      } catch (SecurityException e) {
        throw new ConfigException("failed to create State", e);
      }
      
      // if we have a start state - throw ConfigException.
    } else {
      String issueText = "State Machine already has a start state. Cannot have two start states.";
      ConfigLogger.warn(issueText); 
      // TODO: consier, should be both throw an exception and warn? or is this redundant?
      throw new ConfigException(issueText);
    }
  }
  
  public State createState(String name) throws ConfigException {
    return createState(name, false, State.class);
  }

  /**
   * Use this to create a Terminal State.

   * @param name Used in logging statements
   * @param isEndState mark true if this is a terminal state
   * @return {@link State}
   * @throws ConfigException if bad properties sent to State
   */
  public State createState(String name, boolean isEndState) throws ConfigException {
    return createState(name, isEndState, State.class); 
  }
  
  /**
   * Use this to create custom {@link State}.

   * @param name Used in logging statements
   * @param isEndState mark true if this is a terminal state
   * @param clazz Custom class that extends {@link State} 
   * @return the newly created {@link State}.
   * @throws ConfigException if bad properties sent to {@link State}.
   */
  public State createState(String name, boolean isEndState, Class<? extends State> clazz) 
      throws ConfigException {
    
    ConfigLogger.info("Create state (" + name + ")");
    try {
      
      State state = 
          // new State(name, isEndState, this);
          clazz.getConstructor(String.class, Boolean.class, StateMachine.class)
               .newInstance(name, isEndState, this);
      state.init(props);
      this.states.add(state);
  
      return state; 
      
    } catch (InstantiationException e) {
      throw new ConfigException("failed to create State", e);
    } catch (IllegalAccessException e) {
      throw new ConfigException("failed to create State", e);
    } catch (IllegalArgumentException e) {
      throw new ConfigException("failed to create State", e);
    } catch (InvocationTargetException e) {
      throw new ConfigException("failed to create State", e);
    } catch (NoSuchMethodException e) {
      throw new ConfigException("failed to create State", e);
    } catch (SecurityException e) {
      throw new ConfigException("failed to create State", e);
    }
  }

  /**
   * This 'starts' a Thread or executes run for this State Machine, which is required to
   * handle any asynchronous {@link Event} calls.

   * @param startThread True if you want to this function to start a thread and return. 
            False if you want the run function to be called.
   * @throws ConfigException Not sure why this would ever get called here.
   */
  public void startMachine(boolean startThread) throws ConfigException {
    if (startThread) {
      Thread thread = new Thread(this, "State Machine Event Launch Loop");
      thread.start();
    } else {
      run();
    }
  }

  /**
   * This should only execute once! Execute this to transition this state machine into 
   * its initial state.

   * @throws ConfigException Called if Configuration exception is thrown 
   */
  private void executeFirstTransition() throws ConfigException {
    // TODO: consider, should we initializing using a transition structure first? A transition 
    //       without a "from." this would allow API users to have access to another tier of 
    //       activities that they could fire just on startup - in particular if the first state 
    //       is transition during a restart cycle or something.

    if (startState == null) {
      String issueText = "Cannot start a stateMachine without an initial state.";

      ExecLogger.warn(issueText);
      throw new ConfigException(issueText);
    }

    // initialize machine state
    ExecLogger.debug("Initializing state machine at state " + this.startState.getName());
    this.currentState = startState;

    // communicate to listeners onEnterState
    for (StateMachineListener listener : this.stateMachineListeners) {
      try { 
        listener.onEnterState(this.currentState); 
      } catch (Exception e) { 
        ExecLogger.warn(e.getMessage(), e); 
      }
    }

    // run all the activities in the resulting state
    runActivities(this.currentState.activities);
  }


  public void eventHappens(Event event) throws ConfigException {
    eventHappens(event, syncDefault);
  }

  /**
   * Function used to fire an Event at the StateMachine.

   * @param event The Event being fired.
   * @param sync True if you want to block until complete. False if throw a 
            queue to be run by thread.
   * @throws ConfigException if configuration is wrong.
   */
  public void eventHappens(Event event, boolean sync) throws ConfigException {
    // TODO: confirm that the current state has this event as an option.
    ExecLogger.debug("Event " + event.getName() + " occured. sync: " + sync);
    
    if (sync) {
      executeEvent(event);
    } else {
      eventQueue.add(event);
    }
  }

  private Event activeEvent;
  private Semaphore eventSema = new Semaphore(1);

  /**
   * Execute this function when an event needs to tickle the state machine.

   * @param event that you're wanting to fire.
   * @throws ConfigException If configuration is wrong.
   */
  private void executeEvent(Event event) throws ConfigException {
    try {
      eventSema.acquire();

      ExecLogger.debug("Processing Event " + event.getName());
      this.activeEvent = event;

      // pick the transition based on the event type
      String className = event.getClass().getName();
      Transition transition = currentState.get(className);

      // verify that state machine structure has this state.
      if (transition == null) {
        String issueText = 
            "Current state (" + currentState.getName() + ") does not have a " 
                + "transition for that event type. (" + event.getName() + ")";

        ConfigLogger.warn(issueText);

        throw new ConfigException(issueText);
      }

      // 'kill' all persistently running activities
      for(PersistentActivity activity : persistentRunningActivities) {
        activity.kill();
      }
      persistentRunningActivities.clear();
      
      // mark the active transition
      activeTransition = transition;

      // communicate to listeners onExitState
      for (StateMachineListener listener : this.stateMachineListeners) {
        try { 
          listener.onExitState(transition.to); 
        } catch (Exception e) { 
          ExecLogger.warn(e.getMessage(), e); 
        }
      } 

      // communicate to listeners onTransition
      for (StateMachineListener listener : this.stateMachineListeners) {
        try { 
          listener.onTransition(transition); 
        } catch (Exception e) { 
          ExecLogger.warn(e.getMessage(), e); 
        }
      } 

      // run all the activities in the transition
      runActivities(transition.activities);

      // transition states
      ExecLogger.debug("Transitioning from " + this.currentState.getName() 
                                    + " to " + transition.to.getName() + " on " + event.getName());
      this.currentState = transition.to;

      // communicate to listeners onEnterState
      for (StateMachineListener listener : this.stateMachineListeners) {
        try { 
          listener.onEnterState(transition.to); 
        } catch (Exception e) { 
          ExecLogger.warn(e.getMessage(), e); 
        }
      }

      // run all the activities in the resulting state
      runActivities(this.currentState.activities);

      // clear the active transition
      activeTransition = null;

      // if this state is considered a termination state, flip the running flag to 
      //    kill the execution loop.
      if (this.currentState.isEndState()) {
        running = false;
        ExecLogger.debug("running is now false. thread should end soon.");
      }

    } catch (Exception e1) {
      ExecLogger.warn(e1.getMessage(), e1);
    } finally {
      eventSema.release();
    }

  }
  
  /**
   * Helper function to clean up / not cut-and-paste code.

   * @param activities List of Activity objects that need to be executed.
   */
  private void runActivities(List<Activity> activities) {
    // loop through all the activities and run them
    for (Activity activity : activities) {
      try {
        
        if(activity instanceof PersistentActivity) {
          ExecLogger.debug("PersistentActivity (" + activity.getName() + ") being run in a thread.");
          Thread thread = new Thread(activity);
          thread.start();
          
          ExecLogger.debug("Adding PersistentActivity (" + activity.getName() + ") to running activities list.");
          persistentRunningActivities.add((PersistentActivity) activity);
        } else {
          ExecLogger.debug("Activity (" + activity.getName() + ") being run.");
          activity.run();
        }
        
        // catch any exception that other people's crappy code let's through.
      } catch (Exception ex) {
        ExecLogger.warn("Activity " + activity.getName() + " threw an Exception.", ex);
        ex.printStackTrace();
      }
    }
  }

  private boolean running;

  @Override
  public void run() {
    // flip the running flag
    running = true;

    // fire up the first state
    try {
      executeFirstTransition();
    } catch (ConfigException e) {
      ConfigLogger.warn("startMachine() failed to run");
      return;
    }

    // main execution loop
    while (running) {

      if (eventQueue.size() > 0) {
        Event event = eventQueue.poll();

        try {
          this.executeEvent(event);
        } catch (ConfigException e) {
          ExecLogger.warn("Event execution failed: ", e);
        }
      } else {
        try {
          // ExecLogger.debug("sleeping for " + mainExecutionLoopSleepDuration 
          //                              + "ms waiting for next Event.");
          Thread.sleep(mainExecutionLoopSleepDuration);
        } catch (InterruptedException e) {
          ConfigLogger.warn("Main Execution Loop Sleep Excepted", e);
        }
      }
    }
  }

  /**
   * This will kill the internal thread that fires the next event.
   */
  public void kill() {
    // setting running to false will kill the main execution loop on the next iteration.
    this.running = false;
  }

  public boolean isMachineRunning() {
    return running;
  }

  /**
   * This can be used to wait for the StateMachine to achieve a terminal state.

   * @param timeout milliseconds
   * @return False if timeout was fired. True is terminal {@link State} reached.
   */
  public boolean waitUntilDone(long timeout) {
    long startTime = Calendar.getInstance().getTimeInMillis();

    while ((startTime + timeout) > Calendar.getInstance().getTimeInMillis()) {

      if (!running) {
        ExecLogger.debug("Running flag off. waitUntilDone succeeds.");
        return true;
      }

      try {
        Thread.sleep(mainExecutionLoopSleepDuration);
      } catch (InterruptedException e) {
        ConfigLogger.warn("sleep failed while WaitingUtilDone", e);
        return false;
      }
    }
    ExecLogger.debug("Timeout reached, waitUntilDone failed.");
    return false;
  }

  public State getState() {
    return currentState;
  }

  public Object getFromMachineState(Object key) {
    return machineState.get(key);
  }

  public Object putFromMachineState(String key, Object value) {
    return machineState.put(key, value);
  }

  public Transition getActiveTransition() {
    return activeTransition;
  }

  public Event getActiveEvent() {
    return activeEvent;
  }

  public int getEventQueueSize() {
    return eventQueue.size();
  }

  public void setSyncDefault(boolean syncDefault) {
    this.syncDefault = syncDefault;
  }
}
