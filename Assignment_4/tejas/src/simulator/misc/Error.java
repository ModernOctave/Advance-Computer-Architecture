/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Prathmesh Kallurkar, Abhishek Sagar
 *****************************************************************************/

package misc;

import main.Emulator;
import main.Main;
import emulatorinterface.translator.InvalidInstructionException;
import generic.GlobalClock;
import generic.Operand;

public class Error 
{
	public static void showErrorAndExit(String message)
	{
		System.out.flush();
		System.err.flush();
		System.err.println(message);
		System.err.println("Time : " + GlobalClock.getCurrentTime());
		System.err.println("emulator command is : " + Emulator.getEmulatorCommand());
		new Exception().printStackTrace();
		shutDown("");
		System.exit(1);
	}

	public static void shutDown(String message) 
	{
		try {
			Main.getEmulator().forceKill();
		} catch(Exception e) {
			
		} finally {
			System.out.print(message);
			System.exit(0);
		}
	}

	public static void invalidOperation(String operation, Operand operand1, 
			Operand operand2, Operand operand3) throws InvalidInstructionException
	{
		String msg;
		
		msg=("\n\tIllegal operands to a " + operation + ".");
		msg+=("\n\tOperand1 : " + operand1);
		msg+=("\tOperand2 : " + operand2);
		msg+=("\tOperand3 : " + operand3);
		
//		System.out.print(msg);
//		System.exit(0);
		
		throw new InvalidInstructionException(msg, false);
	}

	public static void invalidOperand(String operandString) throws InvalidInstructionException
	{
		String msg;
		
		msg=("\n\tInvalid operand string : " + operandString + ".");
		
		throw new InvalidInstructionException(msg, false);
	}

}