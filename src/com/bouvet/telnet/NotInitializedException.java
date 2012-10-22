package com.bouvet.telnet;

/**
 * An exception indicating that the SAPTelnetScript has not been initialized
 * @author Dagfinn Parnas,bouvet
 */
public class NotInitializedException extends TelnetException {
	public NotInitializedException(String msg){
		super(msg,-2);
	}

}
