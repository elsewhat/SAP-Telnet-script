package com.bouvet.thread;

import java.util.Observable;

/**
 * An abstract class which is both Observable and Runnable.
 * 
 * 
 * @author Dagfinn Parnas,bouvet
 */
public abstract class ObservableRunnable extends Observable implements Runnable {

	/**
	 * Handle a timeout exception in the runnable by notifiy observers
	 * 
	 * @param t The TimeoutException
	 */
	public void timeOutExceptionOccured(TimeoutException t){
		//System.out.println("Observable runnable received TimeOutException "+t.getMessage());
		setChanged();
		notifyObservers(t);
	}
}
