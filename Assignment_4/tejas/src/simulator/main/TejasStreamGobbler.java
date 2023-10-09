package main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

//@author Apoorva, Sakshi, Prathmesh

public class TejasStreamGobbler extends Thread {
	InputStream inputStream;
	InputStreamReader inputStreamReader;
	BufferedReader bufferedReader;
	String stream;
	boolean exit = false;

	// inspired by the original code of StreamGobbler in Java library
	public TejasStreamGobbler(String stream, InputStream inputStream) {
		this.inputStream = inputStream;
		inputStreamReader = new InputStreamReader(inputStream);
		bufferedReader = new BufferedReader(inputStreamReader);
		this.stream = stream;
	}

	public void run() {
		while(true) {
			try {
				if(inputStream.available()>0) {
					String line = bufferedReader.readLine();
					if(line!=null) {
						System.out.println("[" + stream + "] " + line);
					}
				}
			} catch(Exception e) {
				//System.err.println("Got an exception in reading " + stream + " stream!!");
			}
			
			// exit==true line has to be added after the try...catch block. 
			// Otherwise the thread may go into an infinite loop
			if(exit==true) {
				break;
			}			
		}
		
		System.out.println("Completed execution for the stream gobbler of " + stream + " stream!!");
	}
	
	public void terminate() {
		exit = true;				
	}
}
