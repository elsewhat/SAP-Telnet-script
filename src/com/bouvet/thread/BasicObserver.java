package com.bouvet.thread;

import java.util.Observable;
import java.util.Observer;

/**
 * Simple Observer that allows one to check if a response has been given
 * 
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class BasicObserver implements Observer{
	private boolean hasResponse=false;
	private Object response=null;

	/** 
	 * Observer method which is called when the Observable is updated
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		response=arg;
		hasResponse=true;
	}

	/**
	 * Return if a response has been observed
	 * 
	 * 
	 * @return
	 */
	public boolean hasResponse(){
		return hasResponse;
	}
	
	/**
	 * Return the response or null if no response exist
	 * 
	 * @return
	 */
	public Object getResponse(){
		return response;
	}
	/**
	 * Reset the response 
	 * This means that hasResponse returns false and
	 * getResponse returns null
	 * 
	 * 
	 *
	 */
	public void resetResponse(){
		hasResponse=false;
		response=null;
	}

}
