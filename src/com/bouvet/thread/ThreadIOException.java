package com.bouvet.thread;

import java.io.IOException;


/**
 * IOException which can be thrown in an ObservableRunnable. 
 * Keeps track of the current buffer when the exception was thrown.
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class ThreadIOException extends IOException {
	private String currentBuffer;
	public ThreadIOException(String msg, String currentBuffer){
		super(msg);
		this.currentBuffer=currentBuffer;
	}
	
	/**
	 * Get the buffer when the exception was thrown
	 * 
	 * @return
	 */
	public String getCurrentBuffer(){
		return currentBuffer;
	}
}
