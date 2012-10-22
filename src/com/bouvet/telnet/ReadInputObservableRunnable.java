package com.bouvet.telnet;

import java.io.DataInputStream;
import java.io.IOException;

import com.bouvet.thread.ObservableRunnable;
import com.bouvet.thread.ThreadIOException;


/**
 * Runnable which reads from the given DataInputStream untill the given 
 * delimiter occurs. 
 * 
 * Note that this runnable is also Observable. This means that the result of this 
 * runnable is sent throw the registered Observers. This response can be either a
 * String or an IOException from this class, but a TimeoutThread may also add a 
 * TimeoutException.
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class ReadInputObservableRunnable extends  ObservableRunnable {
	private DataInputStream in;
	private String endDelim;
	
	/**
	 * Create a new  ReadInputObservableRunnable which reads from 
	 * the given DataInputStream untill the given delimiter occurs. 
	 * 
	 * @param in
	 * @param endDelim
	 */
	public ReadInputObservableRunnable(DataInputStream in, String endDelim){
		this.in=in;
		this.endDelim=endDelim;
	}

	/**
	 * Reads from the inputstream until the delimiter has been found or an 
	 * IOException thrown
	 * 
	 * Notifies any observers when finished
	 * 
	 */
	public void run() {
		StringBuffer sbServerResponse=new StringBuffer(100);
		try {
			
			do {
				char c = (char)in.readByte(); 
				sbServerResponse.append(c);
			} while (sbServerResponse.indexOf(endDelim)==-1);
			
			
			
			this.setChanged();
			this.notifyObservers(sbServerResponse.toString());	
		}catch (IOException e){
			this.setChanged();
			//we need to include the current buffer in the exception
			String currentBuffer =null;
			if(sbServerResponse!=null){
				currentBuffer=sbServerResponse.toString();
				if(currentBuffer!=null){
					currentBuffer= currentBuffer.trim();
				}
			}
			ThreadIOException extendedException = new ThreadIOException(e.getMessage(),currentBuffer);
			this.notifyObservers(extendedException);	
		}
	}
	


}
