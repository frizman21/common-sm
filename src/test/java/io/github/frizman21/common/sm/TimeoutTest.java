package io.github.frizman21.common.sm;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.frizman21.common.sm.CancelTimeoutActivity;
import io.github.frizman21.common.sm.ConfigException;
import io.github.frizman21.common.sm.GenericTimeoutEvent;
import io.github.frizman21.common.sm.State;
import io.github.frizman21.common.sm.StateMachine;
import io.github.frizman21.common.sm.TimeoutActivity;
import io.github.frizman21.common.sm.Transition;

public class TimeoutTest {

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
	public void testTimeoutEvent() throws ConfigException, InterruptedException {
		StateMachine machine = new StateMachine("CR Process");
		
		State Submitting = machine.createStartState("Submitting");
		State Approving = machine.createState("Approving", true);
		
		// transition when timeout fires
		Transition t0 = Submitting.addTransition("Submitted", GenericTimeoutEvent.class, Approving);

		// setup timeout after 100milliseconds
		TimeoutActivity timeoutAct = new TimeoutActivity(100);
		Submitting.add(timeoutAct);
		
		new Thread(machine).start();
		
		Thread.sleep(50);
		
		assertEquals(machine.getState(), Submitting);
		
		Thread.sleep(100); // something greater than 100ms
		
		assertEquals(machine.getState(), Approving);
	}
	
	
	/**
	 * The following two tests are paired. The first confirms that the cancel timeout performs as expected. 
	 * In the second test, the cancel is removed and the expected output is confirmed.
	 * 
	 * @throws ConfigException
	 * @throws InterruptedException
	 */
	@Test
	public void testCancelTimeout() throws ConfigException, InterruptedException {
		StateMachine machine = new StateMachine("CR Process");
		
		State Submitting = machine.createStartState("Submitting");
		State Approving  = machine.createState("Approving");
		State Analyzing  = machine.createState("Analyzing");
		
		
		// transition when timeout fires
		Transition t0 = Submitting.addTransition("Submitted", Next.class, Approving);
		Transition t1 = Approving.addTransition("Submitted", GenericTimeoutEvent.class, Analyzing);

		// setup timeout after 100milliseconds
		TimeoutActivity timeoutAct = new TimeoutActivity(100);
		Submitting.add(timeoutAct);
		
		Approving.add( new CancelTimeoutActivity( timeoutAct ) );
		
		new Thread(machine).start();
		
		machine.eventHappens(new Next());
		
		Thread.sleep(200);
		
		assertEquals(machine.getState(), Approving);
	}
	
	/**
	 * This is part two of the test above.
	 * 
	 * @throws ConfigException
	 * @throws InterruptedException
	 */
	@Test
	public void testWithoutCancelTimeout() throws ConfigException, InterruptedException {
		StateMachine machine = new StateMachine("CR Process");
		
		State Submitting = machine.createStartState("Submitting");
		State Approving  = machine.createState("Approving");
		State Analyzing  = machine.createState("Analyzing");
		
		// transition when timeout fires
		Transition t0 = Submitting.addTransition("Submitted", Next.class, Approving);
		Transition t1 = Approving.addTransition("Submitted", GenericTimeoutEvent.class, Analyzing);

		// setup timeout after 100milliseconds
		TimeoutActivity timeoutAct = new TimeoutActivity(100);
		Submitting.add(timeoutAct);
		
		// Approving.add( new CancelTimeoutActivity( timeoutAct ) );
		
		new Thread(machine).start();
		
		machine.eventHappens(new Next());
		
		Thread.sleep(200);
		
		assertEquals(machine.getState(), Analyzing);
	}

}
