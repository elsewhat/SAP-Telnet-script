package com.bouvet.telnet;

/**
 * An exception indicating that the initialization has failed.
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class InitFailedException extends TelnetException {
	public InitFailedException(String msg){
		super(msg,-1);
	}

}
