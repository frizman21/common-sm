package io.github.frizman21.common.sm;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistentActivityTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void BaseTest() throws ConfigException, InterruptedException {
	  
	  StateMachine machine = new StateMachine("Persistent Activity State Machine");
      
	  // configure states
      State initial  = machine.createStartState("Initial");
      State recurringActivity   = machine.createState("RecurringActivityState");
      State terminating = machine.createState("TerminalState");

      // configure transitions
      initial.addTransition("into recurring", Next.class, recurringActivity);
      recurringActivity.addTransition("finish activity", Next.class, terminating);
      
      // setup activity
      MyPersistentActivity activity = new MyPersistentActivity();
      recurringActivity.add(activity);
      
      // start the state machine
      machine.startMachine(true);
      Thread.sleep(10);
      
      assertEquals( initial.getName(), machine.currentState.getName()  );
      
      machine.eventHappens(new Next());
      Thread.sleep(100);
      
      assertEquals( recurringActivity.getName(), machine.currentState.getName() );
      
      // it should be spinning.
      Thread.sleep(100);
      System.out.println("iterations: " + activity.iterations);
      assertTrue(activity.iterations > 5);
      
      
      machine.eventHappens(new Next());
      Thread.sleep(200);
      assertTrue(activity.finished);
      
      assertEquals( terminating.getName(), machine.currentState.getName() );
      
	}

  class MyPersistentActivity implements PersistentActivity {	
    @Override
    public String getName() {
      return "PersistentActivityTest";
    }
  
    @Override
    public void setStateMachine(StateMachine machine) {}
  
    boolean initCalled = false;
    
    @Override
    public void init(Properties props) throws ConfigException {
      initCalled = true;
    }
  
    boolean running = false;
    long iterationDelay = 10;
    int iterations = 0;
    boolean finished = false;
    
    @Override
    public void run() {
      System.out.println("running activity");
      running = true;
      iterations = 0;
        
      while(running) {
        try { 
          System.out.println("counting...");
          iterations++;
          Thread.sleep(iterationDelay);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      finished = true;
    }
  
    @Override
    public void kill() {
      running = false;
    }
  }
}
