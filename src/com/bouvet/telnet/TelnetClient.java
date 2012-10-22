package com.bouvet.telnet;
import java.io.*;
import java.net.Socket;

import com.bouvet.thread.BasicObserver;
import com.bouvet.thread.ThreadIOException;
import com.bouvet.thread.TimeoutException;
import com.bouvet.thread.TimeoutThread;

/**
 * TelnetClient providing some simple function for establishing a telnet session
 * Each command to the telnet session has a particular timeout, and if this is exeeded
 * an exception is thrown
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class TelnetClient{
	public final static long DEFAULT_TIMEOUT_MS=10000;
	protected final static long DEFAULT_POLL_TIME_MS=20;
	
	protected DataInputStream inStream;
	protected PrintStream outStream;
	protected Socket telnetSocket;
	protected long msTimeout=DEFAULT_TIMEOUT_MS;
	
	/**
	 * Create a new TelnetClient
	 * 
	 * @param host The host to connect to
	 * @param port The port to connect to 
	 * @param msTimeout The maximum processing time for each command
	 * @throws IOException If not connected
	 */
	public TelnetClient(String host, int port,long msTimeout)throws IOException {
		this.msTimeout=msTimeout;
		telnetSocket = new Socket(host, port);
		outStream = new PrintStream(telnetSocket.getOutputStream());
		inStream = new DataInputStream(telnetSocket.getInputStream());
		//System.out.println("socket:"+telnetSocket);
		if(!telnetSocket.isConnected()){
			throw new IOException("Could not connect to telnet on host "+host + " and port "+port);
		}
	}
	/**
	 * Create a new TelnetClient
	 * 
	 * @param host The host to connect to
	 * @param port The port to connect to 
	 * @throws IOException If not connected
	 */
	public TelnetClient(String host, int port)throws IOException {
		this(host,port,DEFAULT_TIMEOUT_MS);
	}
	
	/**
	 * Send a command to the server
	 * 
	 * @param command
	 */
	public void send(String command) {
		outStream.println(command);
	}
	
	/**
	 * Send a command to the server and wait for 
	 * a specific response. 
	 * All server response from send to wait is returned
	 * 
	 * @param command
	 */
	public String sendAndWait(String command,String strWaitFor)throws TimeoutException,ThreadIOException  {
		outStream.println(command);
		return waitFor(strWaitFor);
	}
	
	
	/**
	 * Wait for a specific response from the server. 
	 * A TimeoutException is thrown if the specific response is not retrieved
	 * within the set timeout
	 * 
	 * 
	 * 
	 * @param waitFor
	 * @return The received response from the server
	 */
	protected String waitFor(String strWaitFor) throws TimeoutException,ThreadIOException {
		//The observer for the runnable
		BasicObserver taskObserver=new BasicObserver();
		//Add a timeout thread which encapsulated the runnable worker
		Thread timeoutThread= new TimeoutThread(
			new ReadInputObservableRunnable(inStream,strWaitFor),
			taskObserver,
			getTimeout());
		//start the runnable and the timeoutthread
		timeoutThread.start();	
		
		while(!taskObserver.hasResponse()){
			//System.out.println("DEBUG:Waiting no response");
			try {
				Thread.sleep(DEFAULT_POLL_TIME_MS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		Object observableResponse = taskObserver.getResponse();
		
		if(observableResponse==null){
			return null;
		}else if(observableResponse instanceof TimeoutException){
			//System.out.println("Throwing on exception " +observableResponse);
			TimeoutException e=(TimeoutException)observableResponse;
			throw e;
		}else if(observableResponse instanceof ThreadIOException){
			//System.out.println("Throwing on exception " +observableResponse);
			ThreadIOException e=(ThreadIOException)observableResponse;
			throw e;
		}else {//we want to return a string, so use toString of whatever object is there
			return ""+ observableResponse;
		}

	}
	/**
	 * Get the command timeout
	 * 
	 * @return Timeout in milliseconds
	 */
	public long getTimeout(){
		return msTimeout;
	}
	/**
	 * Set the command timeout
	 * 
	 * @param msTimeout Timeout in milliseconds
	 */
	public void setTimeout(long msTimeout){
		this.msTimeout=msTimeout;
	}
	
	/**
	 * Close the connection to the telnet session
	 * This is done quitely (IOExceptions are suppressed)
	 */
	public void close(){
		if (outStream!=null){
			outStream.close();	
		}
		if (inStream!=null){
			try {
				inStream.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}	
		}
		if (telnetSocket!=null){
			try {
				telnetSocket.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}	
		}			

	
	}
	
}