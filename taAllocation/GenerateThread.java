package taAllocation;

public class GenerateThread extends Thread {

	Constraints cons;
	Facts facts = null;
	/**
	 * Constructor for multi-threading generate facts
	 * 
	 * @param cons takes in a constraints object
	 */
	public GenerateThread(Constraints cons) {
		this.cons = cons;
	}
	/**
	 * Method to stop the multi-thread generating of facts
	 */
	public void requestStop() {
		cons.stopGenerate();
	}
	/**
	 *  Method to run a multi-thread generations of facts 
	 * 
	 */
	public void run() {
		try {
			facts = cons.generateFacts();
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	/**
	 * Method for multi-thread to get new facts
	 * 
	 * @return newfacts generated by run
	 */
	public Facts getNewFacts() {
		return facts;
	}
}
