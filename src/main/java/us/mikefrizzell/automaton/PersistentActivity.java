package us.mikefrizzell.automaton;

/**
 * Persistent activities are run within a thread upon entering a state.
 * 
 * @author Mike Frizzell
 *
 */
public interface PersistentActivity extends Activity {

	/**
	 * This function is called upon an event that causes a state transition. It is assumed that the 
	 * thread running the run() function will be terminated upon completion of this function call. The 
	 * StateMachine will forcable destroy the thread after this call.
	 */
	public void kill();
	
}
