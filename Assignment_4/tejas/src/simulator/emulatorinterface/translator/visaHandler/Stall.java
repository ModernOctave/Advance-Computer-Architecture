package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;

/**
 * Created by anand.j on 2/21/17.
 */
public class Stall implements DynamicInstructionHandler {
    @Override
    public int handle(int microOpIndex, Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) {
        /* Nothing to do */
        return ++microOpIndex;
    }
}
