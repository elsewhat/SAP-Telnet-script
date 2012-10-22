package com.bouvet.telnet;

import java.io.IOException;

import com.bouvet.thread.ThreadIOException;
import com.bouvet.thread.TimeoutException;

/**
 * An extension of the TelnetClient providing some extra logic 
 * for SAP J2EE telnet interface
 * 
 * Prompt is defined as the char sequence 13,10,62
 * 
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class SAPTelnetClient extends TelnetClient{
	protected String prompt=new String (new char[]{13,10,62});
	protected String logoutCommand="quit";
	protected final String LOGIN_FAILED_RESPONSE="Login failed";

	/**
	 * Create a new SAPTelnetClient
	 * 
	 * @param host The host to connect to
	 * @param port The port to connect to 
	 * @param msTimeout The maximum processing time for each command
	 * @throws IOException
	 */
	public SAPTelnetClient(String host, int port,long msTimeout)throws IOException {
		super(host,port,msTimeout);	
	}
	
	/**
	 * Create a new SAPTelnetClient
	 * 
	 * @param host The host to connect to
	 * @param port The port to connect to 
	 * @throws IOException
	 */	
	public SAPTelnetClient(String host, int port)throws IOException {
		super(host,port);	
	}
	
	/**
	 * Login the given user to the telnet session.
	 * 
	 * @param userName Username of the user
	 * @param password Password of the user
	 * @return The output of the telnet session
	 * @throws InvalidLoginException If the login details are not accepted by the SAP J2EE
	 * @throws TimeoutException If the login doesn't complete within the given timeout
	 */
	public String login(String userName,String password) throws InvalidLoginException,TimeoutException{
		StringBuffer sbOutput=new StringBuffer(500);
		try {
			sbOutput.append(waitFor("Login: "));
			sbOutput.append(sendAndWait(userName,"Password: "));
			sbOutput.append(sendAndWait(password,prompt));
			return sbOutput.toString();
		} catch(ThreadIOException e){
			//rethrow the exception and include the current response of the telnet session
			throw new InvalidLoginException(e.getCurrentBuffer());
		}
	}

	/**
	 * Send a command to the telnet session
	 * 
	 * @param command 
	 * @return The output of the telnet session
	 * @throws IOException
	 * @throws TimeoutException If the command doesn't complete within the given timeout
	 */
	public String sendCommand(String command)
		throws IOException, TimeoutException {
		
		return sendAndWait(command,prompt);
	}

	/**
	 * Logout the user from the telnet session by issuing the quit command
	 * 
	 * @return The output of the telnet session
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public String logout() throws IOException, TimeoutException {
		try {
			//issue logout command
			String result= sendAndWait(logoutCommand,prompt);
		}finally {
			//close sockets and streams
			close();
			return "User is logged out";
		}
		
		
	}

}
