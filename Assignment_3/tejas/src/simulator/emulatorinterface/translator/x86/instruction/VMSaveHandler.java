package emulatorinterface.translator.x86.instruction;

import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.registers.TempRegisterNum;
import generic.Instruction;
import generic.InstructionList;
import generic.Operand;

///**
// * Created by anand.j on 2/21/17.
// */
//public class VMSaveHandler implements X86StaticInstructionHandler {
//
//    public static long STALL_CYCLES = 5;
//
//    @Override
//    public void handle(long instructionPointer, Operand operand1, Operand operand2, Operand operand3,
//                       InstructionList instructionArrayList, TempRegisterNum tempRegisterNum)
//            throws InvalidInstructionException {
//        /* TODO: Just stalling the cpu for now. Add functionality later */
//        instructionArrayList.appendInstruction(Instruction.getStallInstruction(STALL_CYCLES));
//    }
//}
