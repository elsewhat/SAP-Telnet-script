package com.bouvet.thread;

/**
 * An exception which is thrown when a Timeout has occured
 * @author Dagfinn Parnas,bouvet
 */
public class TimeoutException extends Exception {
	
	public TimeoutException(String msg){
		super(msg);
	}

}
