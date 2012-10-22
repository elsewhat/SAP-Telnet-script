package com.bouvet.telnet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Properties;

import com.bouvet.thread.TimeoutException;


/**
 * Class which represents the running of a batch job against SAP J2EE telnet script interface
 * 
 * Properties to program are given in a standard java Properties format and consist of the following 
 * properties:
 * <ul>
 * <li>ServerHostName: The host name of the SAP J2EE to contact
 * <li>TelnetPort: The port SAP J2EE telnet is running on (usually 50008)
 * <li>Username: The username to connect with (usually administrator)
 * <li>Password: The password of the username
 * <li>OperationTimeoutMS(optional): How long each telnet command is allowed to execute for
 * </ul>
 * 
 * <p>The command file contains one command pr line which is to be executed. Note that 
 * the script will login to a dispatcher node, so if you need access to a server node issue
 * the command jump 0 on the first line. After the command file has been completed, or if an exception 
 * occurs the quit command is sent in order to terminate the telnet session.
 * 
 * <p>The output of the telnet session will be written to System.out, whilst error messages
 * will be redirected to System.err
 * 
 * <p>Error codes for the program are as follows:
 * <ul>
 * <li>-1: Initialize failed
 * <li>-2: Not initialized (when calling run before init)
 * <li>-3: Invalid login to SAP J2EE
 * <li>-4: Connection failed to SAP J2EE. J2EE down, wrong connection properties or firewall blocking access
 * <li>-5: A timeout of one of the commands
 * <li>-9: A general telnet exception(possibly called if the J2EE closes the connection)
 * <ul>
 * 
 * @author Dagfinn Parnas, Bouvet
 */
public class SAPTelnetScript {
	protected static String VERSION="0.10";
	
	protected String strOptionsFile;
	protected String strCommandFile;
	/*The reader of the command file*/
	protected BufferedReader commandReader=null;
	/*Properties of the program*/
	protected Properties optionsProperties=null;
	
	protected boolean isInitialized=false;
	protected boolean initFailed=false;
	
	/*Property names*/
	protected final String SERVER_HOST_NAME_PROPERTY="ServerHostName";
	protected final String TELNET_PORT_NAME_PROPERTY="TelnetPort";
	protected final String USERNAME_PORT_NAME_PROPERTY="Username";
	protected final String PASSWORD_PORT_NAME_PROPERTY="Password";
	protected final String OPERATION_TIMEOUT_PORT_NAME_PROPERTY="OperationTimeoutMS";
	
	protected String serverHostName=null;
	protected int telnetPort;
	protected String username = null;
	protected String password= null;
	protected long operationTimeout= TelnetClient.DEFAULT_TIMEOUT_MS;
	
	/**
	 * Main method which starts the SAPTelnetScript program
	 * 
	 * @param args The command line arguments to this programs, should be two strings
	 */
	public static void main(String[] args) {
		//please do not remove the following line
		System.out.println("SAPTelnetScript Version " +VERSION+" Perform batch jobs against the telnet interface of SAP J2EE\nAuthor: Dagfinn Parnas, Bouvet");
		//Start initializing parameters
		if(args.length!=2){
			System.err.println("Usage: java SAPTelnetScript optionsFile commandFile");
			System.exit(-1);
		}else {
			System.out.println("Running with options file "+args[0] + " and command file "+args[1]);
		}
		try {
			SAPTelnetScript batchScript = new SAPTelnetScript(args[0],args[1]);
			batchScript.init();
			batchScript.run();	
		}catch (TelnetException e){
			System.err.println(e.getMessage());
			System.err.println("Exiting application with error code:"+e.getExitCode());
			System.exit(e.getExitCode());
		}
	}
		
	/**
	 * Create an object for batch telnet operations against the SAP J2EE telnet interface
	 * 
	 * @param optionsFile The file containing the options of the batch script
	 * @param commandFile The file containing the actual commands to run 
	 */
	public SAPTelnetScript(String optionsFile, String commandFile) {
		this.strOptionsFile=optionsFile;
		this.strCommandFile=commandFile;
	}

	/**
	 * Initialize the telnet script
	 * 
	 * Checks that the files exist and with contains the proper parameters
	 * 
	 * @throws InitFailedException if the initialization fails
	 */
	public void init() throws InitFailedException {
		//open command reader
		try {
			File commandFile = new File (strCommandFile);
			if (commandFile == null || !commandFile.exists() || !commandFile.canRead()){
				throw new IOException ("Command file is null or doesn't exist or cannot be read");
			}
			commandReader=new BufferedReader(new FileReader(commandFile));
		}catch (IOException e){
			initFailed=true;
			throw new InitFailedException("Problems command file from "+strCommandFile);
		}
		//open options properties file
		try {
			File optionsFile = new File (strOptionsFile);
			if (optionsFile == null || !optionsFile.exists() || !optionsFile.canRead()){
				throw new IOException ("Options file is null or doesn't exist or cannot be read");
			}
			InputStream propertiesInputStream=new FileInputStream(optionsFile);
			optionsProperties=new Properties();
			optionsProperties.load(propertiesInputStream);
		}catch (IOException e){
			initFailed=true;
			throw new InitFailedException("Problems reading options from "+strOptionsFile);
		}

		//can assume both command file and properties are loaded at this moment
		
		if(!optionsProperties.containsKey(SERVER_HOST_NAME_PROPERTY) || 
			!optionsProperties.containsKey(USERNAME_PORT_NAME_PROPERTY) ||
			!optionsProperties.containsKey(PASSWORD_PORT_NAME_PROPERTY) ||
			!optionsProperties.containsKey(TELNET_PORT_NAME_PROPERTY)) {
			
			String msg = "One or more mandatory properties are missing in the options file."+
				"\n"+SERVER_HOST_NAME_PROPERTY+": "   + (optionsProperties.containsKey(SERVER_HOST_NAME_PROPERTY)?"OK":"MISSING")+
				"\n"+USERNAME_PORT_NAME_PROPERTY+": " + (optionsProperties.containsKey(USERNAME_PORT_NAME_PROPERTY)?"OK":"MISSING")+
				"\n"+PASSWORD_PORT_NAME_PROPERTY+": " + (optionsProperties.containsKey(PASSWORD_PORT_NAME_PROPERTY)?"OK":"MISSING")+
				"\n"+TELNET_PORT_NAME_PROPERTY+ ": "  + (optionsProperties.containsKey(TELNET_PORT_NAME_PROPERTY)?"OK":"MISSING");
			
			initFailed=true;
			throw new InitFailedException(msg);	
		}  
		
		//get properties
		serverHostName=optionsProperties.getProperty(SERVER_HOST_NAME_PROPERTY);
		username=optionsProperties.getProperty(USERNAME_PORT_NAME_PROPERTY);
		password=optionsProperties.getProperty(PASSWORD_PORT_NAME_PROPERTY);
		String strTelnetPort = optionsProperties.getProperty(TELNET_PORT_NAME_PROPERTY);
		
		try {
			telnetPort = Integer.parseInt(strTelnetPort);
		}catch(NumberFormatException e){
			initFailed=true;
			throw new InitFailedException("Telnet port is not an integer, but "+strTelnetPort);
		} 
		
		//optional property
		if(optionsProperties.containsKey(OPERATION_TIMEOUT_PORT_NAME_PROPERTY)){
			String strOperationTimeout = optionsProperties.getProperty(OPERATION_TIMEOUT_PORT_NAME_PROPERTY);
			try {
				operationTimeout = Long.parseLong(strOperationTimeout);
			}catch(NumberFormatException e){
				operationTimeout=TelnetClient.DEFAULT_TIMEOUT_MS;
				System.err.println(OPERATION_TIMEOUT_PORT_NAME_PROPERTY + " is not a valid long value. Therefore using default value which is"+operationTimeout+ " milliseconds");
			} 
			
		}
		isInitialized=true;
	}

	/**
	 * Method which will be called for post processing of the output from
	 * the telnet script.
	 * 
	 * Default behaviour is to print out the output to System.out
	 * 
	 * @param command The last command issued
	 * @param output The output from the telnet session
	 */
	protected void postProcessResult(String command,String output){
		System.out.print(output);
	}
	/**
	 * Retrieve the next telnet command to be executed
	 * If null is returned this significes that there are no more commands
	 * 
	 * 
	 * @return The next telnet command or null if there are no more commands
	 */
	protected String nextCommand()throws IOException{
		String line=null;
		while((line=commandReader.readLine())!=null){
				//skip blank and commented lines
				line=line.trim();
				if("".equals(line) || line.startsWith("#") ||line.startsWith("//")){
					continue;
				}else {
					return line;
				}
		}
		//no more command
		return null;
	}

	

	/**
	 * Run the telnet script.
	 * After the script has finished, it closes the telnet session.
	 * 
	 * @throws NotInitializedException If init() has not been called successfully first
	 * @throws TimeoutException If a telnet command exeeds the configured timeout
	 * @throws TelnetException In case of a connect problem or a general IO problem 
	 */
	public void run()throws TelnetException{
		if(!isInitialized){
			throw new NotInitializedException("Cannot call run before init() is called on object");
		}
		//Do the actual work
		SAPTelnetClient client=null;
		try {	
			System.out.println("Connecting to "+serverHostName+":"+telnetPort + " with user "+username);
			client= new SAPTelnetClient(serverHostName,telnetPort,operationTimeout);
			//login
			String loginOutput=client.login(username,password);
			postProcessResult("login",loginOutput);	
			
			//get command and issue to telnet client
			String command=null;
			while((command=nextCommand())!=null){
				String output=client.sendCommand(command);
				postProcessResult(command,output);	
			}
		} catch (TimeoutException t){
			throw new TelnetException("A timeout occured during the running of the script. Message:"+t.getMessage(),-54);
		}catch (ConnectException e){
			throw new TelnetException("Could not connect to "+serverHostName + ":"+telnetPort + ". Either your server and port settings are wrong, the J2EE engine is down or a firewall is blocking your connection attempt",-4);
		}catch (IOException e){
			e.printStackTrace();
			throw new TelnetException("An general IOException occured:"+e.getMessage(),-9);
		}finally{
			//logout client
			String output;
			try {
				output = client.logout();
				System.out.print("quit\n"+output);
			//not handling exceptions while attempting to close telnet session
			} catch (IOException e1) {
				//e1.printStackTrace();
			} catch (TimeoutException e1) {
				//e1.printStackTrace();
			}
		}

		
	}
		
	
}