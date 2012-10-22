package com.bouvet.telnet;

/**
 * General exception of Telnet client. 
 * Includes an exit code.
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class TelnetException extends Exception {
	private int exitCode=0;
	public TelnetException(String msg){
		super(msg);
	}
	
	public TelnetException(String msg, int exitCode){
		super(msg);
		this.exitCode=exitCode;
	}

	public int getExitCode(){
		return exitCode;
	}
}
