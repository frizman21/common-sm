package io.github.frizman21.common.sm;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.frizman21.common.sm.Activity;
import io.github.frizman21.common.sm.ConfigException;
import io.github.frizman21.common.sm.State;
import io.github.frizman21.common.sm.StateMachine;
import io.github.frizman21.common.sm.StateMachineListener;
import io.github.frizman21.common.sm.Transition;

public class StateMachineTest {

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
	public final void BasicTransitions() {	
		try {
			StateMachine machine = new StateMachine("CR Process");
			
			State Submitting = machine.createStartState("Submitting");
			State Approving = machine.createState("Approving");
			State Analyzing = machine.createState("Analyzing");
			State Implementing = machine.createState("Implementing");
			State Reviewing = machine.createState("Reviewing");
			State Closing = machine.createState("Closing");
			State Closed = machine.createState("Closed", true);
			
			Submitting.		addTransition("Submitted", Next.class, Approving);
			Approving.		addTransition("Approved", Next.class, Implementing);
			Approving.		addTransition("Approved", Denied.class, Analyzing);
			Analyzing.		addTransition("Analyzed", Next.class, Approving);
			Implementing.	addTransition("Implemented", Next.class, Reviewing);
			Reviewing.		addTransition("Reviewed", Next.class, Closing);
			Reviewing.		addTransition("Reviewed", Denied.class, Implementing);
			Closing.		addTransition("Closed", Next.class, Closed);
			Closing.		addTransition("Closed", Denied.class, Reviewing);
			
			machine.startMachine(true);
			machine.eventHappens(new Next());
			machine.eventHappens(new Denied());
			machine.eventHappens(new Next());
			machine.eventHappens(new Next());
			machine.eventHappens(new Next());
			machine.eventHappens(new Denied());
			machine.eventHappens(new Next());
			machine.eventHappens(new Next());
			machine.eventHappens(new Denied());
			machine.eventHappens(new Next());
			machine.eventHappens(new Next());
			
			boolean result = machine.waitUntilDone(200);
			if(!result)
				fail("Didn't finish in time");
		} catch (ConfigException e) {
		    e.printStackTrace();
			fail("Valid Configuration failed to take.");
		}
	}
	
	@Test
	public final void ActivityTest() {
		StateMachine machine = new StateMachine("CR Process");
		
		try {
			State Submitting = machine.createStartState("Submitting");
			Submitting.add(new Activity() {
				Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
				public void run() 		{	logger.debug("I am trying to Submit now!"); }
				public String getName() {	return "Submitting Activity";	}
				public void setStateMachine(StateMachine machine) {}
				public void init(Properties props) throws ConfigException {}
			});
			State Approving = machine.createState("Approving");
			Approving.add(new Activity() {
				Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
				public void run() 		{	logger.debug("I am trying to get approved now!"); }
				public String getName() {	return "Approving Activity";	}
				public void setStateMachine(StateMachine machine) {}
				public void init(Properties props) throws ConfigException {}
			});
			State Exiting = machine.createState("Exiting",true);
			
			Transition submit = Submitting.addTransition("Submit", Next.class, Approving);
			submit.add(new Activity() {
				Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
				public void run() 		{	logger.debug("Writing Form!"); }
				public String getName() {	return "Write Form";	}
				public void setStateMachine(StateMachine machine) {}
				public void init(Properties props) throws ConfigException {}
			});
			Transition deny = Approving.addTransition("Deny", Denied.class, Submitting);
			deny.add(new Activity() {
				Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
				public void run() 		{	logger.debug("Writing Denial Form!"); }
				public String getName() {	return "Deny Form";	}
				public void setStateMachine(StateMachine machine) {}
				public void init(Properties props) throws ConfigException {}
			});
			Approving.addTransition("Exiting", ExitEvent.class, Exiting);
			
			machine.startMachine(true);
			machine.eventHappens(new Next());
			machine.eventHappens(new Denied());
			machine.eventHappens(new Next());
			machine.eventHappens(new Denied());
			machine.eventHappens(new Next());
			machine.eventHappens(new ExitEvent());
			
			boolean result = machine.waitUntilDone(200);
			
			if(!result)
				fail("Didn't finish in time");
		} catch (ConfigException e) {
			fail("Valid Configuration failed to take.");
			e.printStackTrace();
		}
		
	}
	
	@Test
	public final void ListenerTest() {
		Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
		logger.info("Starting Listener Test");
		
		StateMachine machine = new StateMachine("CR Process");
		
		machine.add(new StateMachineListener() {
			Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
			
			@Override
			public void onEnterState(State state) {
				logger.debug("onEnterState callback on State (" + state.getName() + ")");
			}

			@Override
			public void onExitState(State state) {
				logger.debug("onExitState callback on State (" + state.getName() + ")");	
			}

			@Override
			public void onTransition(Transition transition) {
				logger.debug("onTransition callback on Transition (" + transition.getName() + ")");
			}
			
		});
		
		try {
			
			State Submitting = machine.createStartState("Submitting");
			State Approving = machine.createState("Approving");
			State Exiting = machine.createState("Exiting",true);
			Submitting.addTransition("Submit", Next.class, Approving);
			Approving.addTransition("Deny", Denied.class, Submitting);
			Approving.addTransition("Exit", ExitEvent.class, Exiting);
			
			machine.startMachine(true);
			
			logger.info("causing event Next");
			machine.eventHappens(new Next());
			
			logger.info("causing event Denied");
			machine.eventHappens(new Denied());
			
			logger.info("causing event Next");
			machine.eventHappens(new Next());
			
			logger.info("causing event Denied");
			machine.eventHappens(new Denied());
			
			logger.info("causing event Next");
			machine.eventHappens(new Next());
			
			logger.info("causing event Denied");
			machine.eventHappens(new ExitEvent());
			
			boolean result = machine.waitUntilDone(200);
			if(!result)
				fail("Didn't finish in time");
			
		} catch (ConfigException e) {
			fail("Valid Configuration failed to take.");
			e.printStackTrace();
		}
	}
	
	@Test
    public final void CustomStateTest() throws ConfigException, InterruptedException {
	  Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
      logger.info("Starting Custom State Test");
      
      StateMachine machine = new StateMachine("CR Process");
      State first = machine.createStartState("First Custom", CustomState.class);
      State custom = machine.createState("Second Custom", false, CustomState.class);
      State end = machine.createState("end", true);
      
      assertTrue(custom instanceof CustomState);
      
      first.addTransition("t0", Next.class, custom);
      custom.addTransition("t1", Next.class, end);
      
      machine.startMachine(true);
      
      Thread.sleep(10);
      
      assertEquals(machine.getState(), first);
      
      machine.eventHappens(new Next(),true);

      assertEquals(machine.getState(), custom);
      
      machine.eventHappens(new Next(),true);
      
      assertEquals(machine.getState(), end);
      
      boolean result = machine.waitUntilDone(200);
      
      assertTrue(result);
      
	}
}
