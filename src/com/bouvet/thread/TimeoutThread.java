package com.bouvet.thread;

import java.util.Observer;


/**
 * A Thread class which monitors an ObservableRunnable and makes sure it is not 
 * running for more than the given time.
 * 
 * The ObservableRunnable runs in a new Thread create by this thread.
 * 
 * Both this Thread and the ObservableRunnable thread are daemon threads.
 * 
 * @author Dagfinn Parnas,bouvet
 */
public class TimeoutThread extends Thread{
	private long msTimeout;
	private ObservableRunnable monitoredRunnable;
	private static int id=0;

	public TimeoutThread(ObservableRunnable monitoredRunnable, long msTimeout) {
		this(monitoredRunnable,null,msTimeout);
	}
	
	public TimeoutThread(ObservableRunnable monitoredRunnable, Observer observer,long msTimeout) {
		this.monitoredRunnable = monitoredRunnable;
		if(observer!=null){
			monitoredRunnable.addObserver(observer);
		}
		this.msTimeout = msTimeout;
		setName("TimeoutThread:"+id++);
		//daemon thread indicating that JVM will exit if only TimeoutThreads remain
		setDaemon(true);
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		//System.out.println("Sleeping " + msTimeout);
		Thread workerThread = new Thread(monitoredRunnable);
		workerThread.setDaemon(true);
		workerThread.start();
		Thread myThread= Thread.currentThread();
		myThread.setPriority(Thread.MAX_PRIORITY);
		try {
			sleep(msTimeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (workerThread != null && workerThread.isAlive()) {
			//System.out.println("Stopping thread");
			//not recommended for large java applications, but not critical for our use
			monitoredRunnable.timeOutExceptionOccured(new TimeoutException("Timeout occured after "+msTimeout));
			
			//TODO:stop is deprecated, but should we do it anyhow?
			//At the moment we let it run as it is a daemon thread with clearly defined boundary
			//workerThread.stop();
		}
		//System.out.println("Thread (" +this.getName()+") has finished");
	}

}
