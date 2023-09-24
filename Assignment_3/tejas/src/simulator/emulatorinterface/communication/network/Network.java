package emulatorinterface.communication.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import main.CustomObjectPool;
import config.EmulatorConfig;
import config.EmulatorType;
import config.SystemConfig;
import emulatorinterface.communication.Encoding;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.communication.Packet;
import generic.CircularPacketQueue;

public class Network extends IpcBase implements Encoding {
	
	public static int portStart = 9000;
	ServerSocket serverSocket[];
	Socket clientSocket[];
	BufferedInputStream inputStream[];
	int maxApplicationThreads;

	// 2KB buffer for network data
	final int bufferSize = 16 * 1024;
	byte inputBytes[][];
	int numOverflowBytes[];
		
	public Network() {
		
		this.maxApplicationThreads = (SystemConfig.maxNumJavaThreads*SystemConfig.numEmuThreadsPerJavaThread);
		
		inputBytes = new byte[maxApplicationThreads][bufferSize];
		numOverflowBytes = new int[maxApplicationThreads];
		serverSocket = new ServerSocket[maxApplicationThreads];
		clientSocket = new Socket[maxApplicationThreads];
		inputStream = new BufferedInputStream[maxApplicationThreads];
		

		for(int tidApp = 0; tidApp<maxApplicationThreads; tidApp++) {
			
			int portNumber = 0;
			
			try {
				portNumber = portStart+tidApp;
				
				serverSocket[tidApp] = new ServerSocket(portNumber);
				serverSocket[tidApp].setSoTimeout(1000); // set time-out of 1 second.

				clientSocket[tidApp] = null;
				numOverflowBytes[tidApp] = 0;
			} catch (Exception e) {
				for(int i=0; i<tidApp; i++) {
					try {
						serverSocket[i].close();
					} catch (IOException ioE) {
						misc.Error.showErrorAndExit("error in closing socket on server side for tidApp : " + i);
					}
				}
				//tidApp must be zero for next iteration
				tidApp = -1;
				portStart += maxApplicationThreads;
				//e.printStackTrace();
				//misc.Error.showErrorAndExit("error in opening socket on server side for tidApp : " + tidApp);
			}
			
//			System.out.println("Thread: "+tidApp+" binded to Port "+portNumber+" successfully!!");
		}
		System.out.println("All sockets initialize successfully!! PortStart is "+portStart);
	}
	
	// Free buffers, free memory , deallocate any stuff.
	public void finish() {
		System.out.println("Closing network connection");
		
		for(int i=0; i<maxApplicationThreads; i++) {
			try {
				clientSocket[i].close();
			} catch (IOException e) {
				// Not exiting because all the packets have been received correctly
				System.err.println("Error in closing network connection for tidApp = " + i);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initIpc() {
	}

	@Override
	public int fetchManyPackets(int tidApp, CircularPacketQueue fromEmulator) {
		
		// int positionInQueueBeforeReading = CustomObjectPool.getCustomAsmCharPool().currentPosition(tidApp);
		
		if(tidApp>=maxApplicationThreads) {
			misc.Error.showErrorAndExit("Network cannot handle tid=" + tidApp);
		}
		
		// If you are reading from a thread for the first time, open the connection with thread first
		if(clientSocket[tidApp]==null) {
			
			// This socket has been tested before.
			if(serverSocket[tidApp]==null) {
				return 0;
			}

			try {
				try{
					clientSocket[tidApp] = serverSocket[tidApp].accept();
				} catch (SocketException e) {
					misc.Error.showErrorAndExit("error in setting timeout for tidApp = " + tidApp);
				} catch (SocketTimeoutException e) {
					
					if(tidApp==0) {
						misc.Error.showErrorAndExit("error in accepting connected for tidApp = " + tidApp);
					}
					
					// close this socket now. There is a very high chance that this thread may not connect again.
					serverSocket[tidApp].close();
					serverSocket[tidApp] = null;
					System.out.println("Network timed out for tidApp = " + tidApp);
					return 0;
				}
				
				String address = clientSocket[tidApp].getInetAddress().getHostName();
				System.out.println("tidApp : "+ tidApp +" received connection request from " + address);
				inputStream[tidApp] = new BufferedInputStream(clientSocket[tidApp].getInputStream());
				
				// ip +packet-type + disassembly
				inputStream[tidApp].mark(8+8+64);

			} catch (IOException ioe) {
				ioe.printStackTrace();
				misc.Error.showErrorAndExit("error in accepting connection for tidApp : " + tidApp);
			}
		}

		int numPacketsRead = 0;

		try {
			int numBytesRead = inputStream[tidApp].available();
			
			// asynchronously determine the number of bytes available
			if(numBytesRead == 0) {
				return 0;
			}

			if(numBytesRead > (bufferSize-numOverflowBytes[tidApp])) {
				numBytesRead = (bufferSize - numOverflowBytes[tidApp]);
			}
			
			inputStream[tidApp].read(inputBytes[tidApp], numOverflowBytes[tidApp], numBytesRead);
			
			numBytesRead += numOverflowBytes[tidApp];
			numOverflowBytes[tidApp]=0;
			
			int numBytesConsumed = 0;
			int maxSize = fromEmulator.spaceLeft();
			if(EmulatorConfig.emulatorType==EmulatorType.pin) {
				
			} else if (EmulatorConfig.emulatorType==EmulatorType.qemu) {
				
				for(int numPacketsAdded=0; numPacketsAdded<maxSize; numPacketsAdded++) {
		
					// we must be able to read at-least 3 longs
					if((numBytesRead-numBytesConsumed) < (3*8)) {
						resetInputBytes(tidApp, numBytesRead, numBytesConsumed); 
						break;
					} else {
						long ip = getLong(inputBytes[tidApp], numBytesConsumed);
						numBytesConsumed += 8;
						long value = getLong(inputBytes[tidApp], numBytesConsumed);
						numBytesConsumed += 8;
						long tgt = -1;

						if(value==ASSEMBLY) {
							if((numBytesRead-numBytesConsumed)<64) {
								numBytesConsumed -= 16; // return two longs
								resetInputBytes(tidApp, numBytesRead, numBytesConsumed);
								break;
							} else {
								CustomObjectPool.getCustomAsmCharPool().enqueue(tidApp, inputBytes[tidApp], numBytesConsumed);
								numBytesConsumed += 64;
							}
						} else {
							tgt = getLong(inputBytes[tidApp], numBytesConsumed);
							numBytesConsumed += 8;
						}
						
						numPacketsRead++;
						
						if(value==-1) {
							System.out.println("End packet received for tidApp = " + tidApp);
							
							if(numBytesConsumed!=numBytesRead) {
								misc.Error.showErrorAndExit("For tidApp = " + tidApp + " : Some bytes are in the " +
										"stream even after the last packet(-1)");
							}
						}
						
						fromEmulator.enqueue(ip, value, tgt);
					}
				}
				
			} else {
				misc.Error.showErrorAndExit("Invalid emulator type : " + EmulatorConfig.emulatorType);
			}
		} catch (IOException e) {
			e.printStackTrace();
			misc.Error.showErrorAndExit("error in fetching packet for tidApp : " + tidApp);
		}
		
		// print debug messages
//		int numAsmPackets = 0;
//		for(int i=0; i<numPacketsRead; i++) {
//			if(fromEmulator.get(i).value==ASSEMBLY) {
//				String assembly = new String(CustomObjectPool.
//						getCustomAsmCharPool().peek(tidApp, positionInQueueBeforeReading + numAsmPackets));
//				numAsmPackets++;
//				System.out.println("$$$ :" + i + " : " + fromEmulator.get(i) + " : " + assembly);
//			} else {
//				System.out.println("$$$ :" + i + " : " + fromEmulator.get(i));
//			}
//		}
		
		return numPacketsRead;
	}

	private void resetInputBytes(int tidApp, int numBytesRead,
			int numBytesConsumed) {
		
		for(int i=0; i<(numBytesRead-numBytesConsumed); i++) {
			inputBytes[tidApp][i] = inputBytes[tidApp][i+numBytesConsumed];
		}
		
		numOverflowBytes[tidApp] = numBytesRead - numBytesConsumed;
	}

	private long getLong(byte[] inputBytes, int offset) {
		long value = 0;
		//for (int i = 0; i < 8; i++)
		for (int i = 7; i >= 0; i--)
		{
		   value = (value << 8) + (inputBytes[i+offset] & 0xff);
		}
		return value;
	}

	public void errorCheck(int tidApp, long totalReads) {
		// Error check not required for network code.
	}
}