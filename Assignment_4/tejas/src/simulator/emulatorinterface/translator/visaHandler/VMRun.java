package emulatorinterface.translator.visaHandler;

import emulatorinterface.DynamicInstructionBuffer;
import generic.Instruction;

/**
 * Created by anand.j on 4/8/17.
 */
public class VMRun implements DynamicInstructionHandler {

    @Override
    public int handle(int microOpIndex, Instruction microOp, DynamicInstructionBuffer dynamicInstructionBuffer) {
        return ++microOpIndex;
    }
}
