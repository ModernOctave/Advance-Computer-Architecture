package emulatorinterface.communication.filePacket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import main.Main;
import misc.Numbers;
import emulatorinterface.translator.x86.objparser.*;
import emulatorinterface.translator.x86.operand.OperandTranslator;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.InstructionList;
import generic.Operand;
import emulatorinterface.translator.InvalidInstructionException;


public class FileWrite {

	/**
	 * @param args
	 * @throws IOException 
	 */
	/*public static void main(String[] args) throws IOException {*/
	// TODO Auto-generated method stub
	String basefilename;
	int maxnumofactivethreads;
	BufferedReader input;
	Hashtable<Long, StringBuilder> hashtable;
	String line;
	long instructionPointer;
	String operation = null;
	String operand1 = null, operand2 = null, operand3 = null;
	
	StringBuilder sb = new StringBuilder();
	
	public FileWrite(int maxNumActiveThreads, String baseFileName){
		basefilename=new String(baseFileName);
		maxnumofactivethreads=maxNumActiveThreads;
		//System.out.println(basefilename+"***********************");
		
		String exec=Main.getEmulatorFile();
		input = ObjParser.readObjDumpOutput(exec);
		hashtable=new Hashtable<Long, StringBuilder>();

		
	
		while ((line = ObjParser.readNextLineOfObjDump(input)) != null) 
		{
			if (!(ObjParser.isContainingObjDumpAssemblyCode(line))) {
				continue;
			}

			String assemblyCodeTokens[] = ObjParser.tokenizeObjDumpAssemblyCode(line);

			// read the assembly code tokens
			instructionPointer = Numbers.hexToLong(assemblyCodeTokens[0]);
			
			//instructionPrefix = assemblyCodeTokens[1];
			operation = assemblyCodeTokens[2];
			operand1 = assemblyCodeTokens[3];
			operand2 = assemblyCodeTokens[4];
			operand3 = assemblyCodeTokens[5];
			StringBuilder asm=new StringBuilder();
			
			if(operand1!=null&& operand1.contains("<")) {
				String[] temp=operand1.split(" ");
				operand1=temp[0];
			}
			
			if(operand1==null)
				asm.append(operation);
			else if(operand2==null)
			{  asm.append(operation+" "+operand1);
				
			}else if(operand3==null)
				asm.append(operation+" "+operand1+","+operand2);
			else {
				asm.append(operation+" "+operand1+","+operand2+","+operand3);
			}
			
			hashtable.put(instructionPointer, asm);
			
		}		
	}

	void printToFileIpValAddr(int tid,Long ip,Long val, Long addr, StringBuilder asmstring) throws IOException{
		if(val!=27)
		sb.append(ip+" "+ val+" "+ addr+"\n");
		else
			sb.append(ip+" "+ val+" "+ asmstring+"\n");
       		File f = new File(basefilename+"_"+tid+".gz");
		    GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(f));
		
		byte[] data = sb.toString().getBytes();
		out.write(data, 0, data.length);
		

		out.close();
	}

	public void analysisFn (int tid,Long ip, Long val, Long addr, StringBuilder asmstring) throws IOException, InvalidInstructionException
	{   	if(val==8) {
		//timer instructions
	}	
	else {
			if(val!=28) {
		printToFileIpValAddr(tid, ip, val, addr,asmstring);
			}
			else {
				if(hashtable.containsKey(ip))
					
			printToFileIpValAddr(tid, ip, 27L, addr,hashtable.get(ip));
				}
			
	}
	}
}
