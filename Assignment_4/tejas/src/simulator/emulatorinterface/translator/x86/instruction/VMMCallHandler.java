package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.InstructionList;
import generic.Operand;

/**
 * Created by anand.j on 2/21/17.
 */
public class VMMCallHandler implements X86StaticInstructionHandler {

    @Override
    public void handle(long instructionPointer, Operand operand1, Operand operand2, Operand operand3,
                       InstructionList instructionArrayList, TempRegisterNum tempRegisterNum)
            throws InvalidInstructionException {
        /* TODO: Add functionality later */
    }
}
