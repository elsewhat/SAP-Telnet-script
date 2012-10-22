package com.bouvet.telnet;

/**
 * An exception indicating that the login was not successful
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class InvalidLoginException extends TelnetException {
	public InvalidLoginException(String msg){
		super(msg,-3);
	}

}
