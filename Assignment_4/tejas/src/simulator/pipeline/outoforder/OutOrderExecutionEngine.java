package pipeline.outoforder;

import java.io.FileWriter;
import java.io.IOException;
import config.EnergyConfig;
import memorysystem.CoreMemorySystem;
import pipeline.ExecutionCore;
import pipeline.ExecutionEngine;
import generic.Core;
import generic.GenericCircularQueue;
import generic.Instruction;

/**
 * execution engine comprises of : decode logic, ROB, instruction window, register files,
 * rename tables and functional units
 */
public class OutOrderExecutionEngine extends ExecutionEngine {

	//the containing core
	private Core core;

	//components of the execution engine
	private ICacheBuffer iCacheBuffer;
	private FetchLogic fetcher;
	private MicroOpCache microOpCache;
	private GenericCircularQueue<Instruction> fetchBuffer;
	private DecodeLogic decoder;
	private GenericCircularQueue<ReorderBufferEntry> decodeBuffer;
	private RenameLogic renamer;
	private GenericCircularQueue<ReorderBufferEntry> renameBuffer;
	private IWPushLogic IWPusher;
	private SelectLogic selector;
	private ExecutionLogic executer;
	private WriteBackLogic writeBackLogic;

	private ReorderBuffer reorderBuffer;
	private InstructionWindow instructionWindow;
	private RegisterFile integerRegisterFile;
	private RegisterFile vectorRegisterFile;
	private RenameTable integerRenameTable;
	private RenameTable vectorRenameTable;

	//Core-specific memory system (a set of LSQ, TLB and L1 cache)
	private OutOrderCoreMemorySystem outOrderCoreMemorySystem;

	//flags
	private boolean toStall1;					//if IW full
	//fetcher, decoder and renamer stall

	private boolean toStall2;					//if physical register cannot be
	//allocated to the dest of an instruction,
	//all subsequent processing must stall
	//fetcher and decoder stall

	private boolean toStall3;					//if LSQ full, and a load/store needs to be
	//allocated an entry
	//fetcher stall

	private boolean toStall4;					//if ROB full
	//fetcher stall

	private boolean toStall5;					//if branch mis-predicted
	//fetcher stall

	private boolean toStall6;					//decode is stalled because of a preceding serialization instruction
	//fetcher stall


	public long prevCycles;

	public OutOrderExecutionEngine(Core containingCore)
	{
		super(containingCore);

		core = containingCore;


		reorderBuffer = new ReorderBuffer(core, this);
		instructionWindow = new InstructionWindow(core, this);
		integerRegisterFile = new RegisterFile(core, core.getIntegerRegisterFileSize());
		integerRenameTable = new RenameTable(this, core.getNIntegerArchitecturalRegisters(), core.getIntegerRegisterFileSize(), integerRegisterFile, core.getNo_of_input_pipes());
		vectorRegisterFile = new RegisterFile(core, core.getVectorRegisterFileSize());
		vectorRenameTable = new RenameTable(this, core.getNVectorArchitecturalRegisters(), core.getVectorRegisterFileSize(), vectorRegisterFile, core.getNo_of_input_pipes());

		fetchBuffer = new GenericCircularQueue(Instruction.class, (core.getDecodeWidth()>core.getRenameWidth()?core.getDecodeWidth():core.getRenameWidth()));
		microOpCache = new MicroOpCache(core.getCoreConfig().NoOfMicroOpCacheEntries);
		fetcher = new FetchLogic(core, this);
		decodeBuffer = new GenericCircularQueue(ReorderBufferEntry.class, (core.getDecodeWidth()>core.getRenameWidth()?core.getDecodeWidth():core.getRenameWidth()));
		decoder = new DecodeLogic(core, this);
		renameBuffer = new GenericCircularQueue(ReorderBufferEntry.class, (core.getDecodeWidth()>core.getRenameWidth()?core.getDecodeWidth():core.getRenameWidth()));
		renamer = new RenameLogic(core, this);
		IWPusher = new IWPushLogic(core, this);
		selector = new SelectLogic(core, this);
		executer = new ExecutionLogic(core, this);
		writeBackLogic = new WriteBackLogic(core, this);


		toStall1 = false;
		toStall2 = false;
		toStall3 = false;
		toStall4 = false;
		toStall5 = false;
		toStall6 = false;
		prevCycles=0;
	}

	public ICacheBuffer getiCacheBuffer() {
		return iCacheBuffer;
	}

	public Core getCore() {
		return core;
	}

	public DecodeLogic getDecoder() {
		return decoder;
	}

	public RegisterFile getVectorRegisterFile() {
		return vectorRegisterFile;
	}

	public RenameTable getVectorRenameTable() {
		return vectorRenameTable;
	}

	public RegisterFile getIntegerRegisterFile() {
		return integerRegisterFile;
	}

	public RenameTable getIntegerRenameTable() {
		return integerRenameTable;
	}

	public ReorderBuffer getReorderBuffer() {
		return reorderBuffer;
	}

	public InstructionWindow getInstructionWindow() {
		return instructionWindow;
	}

	public void setInstructionWindow(InstructionWindow instructionWindow) {
		this.instructionWindow = instructionWindow;
	}

	public boolean isToStall1() {
		return toStall1;
	}

	public void setToStall1(boolean toStall1) {
		this.toStall1 = toStall1;
	}

	public boolean isToStall2() {
		return toStall2;
	}

	public void setToStall2(boolean toStall2) {
		this.toStall2 = toStall2;
	}

	public boolean isToStall3() {
		return toStall3;
	}

	public void setToStall3(boolean toStall3) {
		this.toStall3 = toStall3;
	}

	public boolean isToStall4() {
		return toStall4;
	}

	public void setToStall4(boolean toStall4) {
		this.toStall4 = toStall4;
	}

	public boolean isToStall5() {
		return toStall5;
	}

	public void setToStall5(boolean toStall5) {
		this.toStall5 = toStall5;
	}

	public boolean isToStall6() {
		return toStall6;
	}

	public void setToStall6(boolean toStall6) {
		this.toStall6 = toStall6;
	}

	public GenericCircularQueue<Instruction> getFetchBuffer() {
		return fetchBuffer;
	}

	public GenericCircularQueue<ReorderBufferEntry> getDecodeBuffer() {
		return decodeBuffer;
	}

	public GenericCircularQueue<ReorderBufferEntry> getRenameBuffer() {
		return renameBuffer;
	}
	
	public MicroOpCache getMicroOpCache() {
		return microOpCache;
	}

	public FetchLogic getFetcher() {
		return fetcher;
	}

	public RenameLogic getRenamer() {
		return renamer;
	}

	public IWPushLogic getIWPusher() {
		return IWPusher;
	}

	public SelectLogic getSelector() {
		return selector;
	}

	public ExecutionLogic getExecuter() {
		return executer;
	}

	public WriteBackLogic getWriteBackLogic() {
		return writeBackLogic;
	}

	@Override
	public void setInputToPipeline(GenericCircularQueue<Instruction>[] inpList) {

		fetcher.setInputToPipeline(inpList);

	}

	public OutOrderCoreMemorySystem getCoreMemorySystem()
	{
		return outOrderCoreMemorySystem;
	}

	public void setCoreMemorySystem(CoreMemorySystem coreMemorySystem) {
		this.coreMemorySystem = coreMemorySystem;
		this.outOrderCoreMemorySystem = (OutOrderCoreMemorySystem)coreMemorySystem;
		this.iCacheBuffer = new ICacheBuffer((int)(core.getDecodeWidth() *
				coreMemorySystem.getiCache().getLatency()/core.getStepSize()));
		this.fetcher.setICacheBuffer(iCacheBuffer);
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);

		EnergyConfig bPredPower =  getBranchPredictor().calculateAndPrintEnergy(outputFileWriter, componentName + ".bPred");
		totalPower.add(totalPower, bPredPower);

		EnergyConfig decodePower =  getDecoder().calculateAndPrintEnergy(outputFileWriter, componentName + ".decode");
		totalPower.add(totalPower, decodePower);

		EnergyConfig renamePower =  getRenamer().calculateAndPrintEnergy(outputFileWriter, componentName + ".rename");
		totalPower.add(totalPower, renamePower);

		EnergyConfig lsqPower =  getCoreMemorySystem().getLsqueue().calculateAndPrintEnergy(outputFileWriter, componentName + ".LSQ");
		totalPower.add(totalPower, lsqPower);

		EnergyConfig intRegFilePower =  getIntegerRegisterFile().calculateAndPrintEnergy(outputFileWriter, componentName + ".intRegFile");
		totalPower.add(totalPower, intRegFilePower);

		EnergyConfig floatRegFilePower =  getVectorRegisterFile().calculateAndPrintEnergy(outputFileWriter, componentName + ".floatRegFile");
		totalPower.add(totalPower, floatRegFilePower);

		EnergyConfig iwPower =  getInstructionWindow().calculateAndPrintEnergy(outputFileWriter, componentName + ".InstrWindow");
		totalPower.add(totalPower, iwPower);

		EnergyConfig robPower =  getReorderBuffer().calculateAndPrintEnergy(outputFileWriter, componentName + ".ROB");
		totalPower.add(totalPower, robPower);

		EnergyConfig fuPower =  getExecutionCore().calculateAndPrintEnergy(outputFileWriter, componentName + ".FuncUnit");
		totalPower.add(totalPower, fuPower);

		EnergyConfig resultsBroadcastBusPower =  getExecuter().calculateAndPrintEnergy(outputFileWriter, componentName + ".resultsBroadcastBus");
		totalPower.add(totalPower, resultsBroadcastBusPower);

		totalPower.printEnergyStats(outputFileWriter, componentName + ".total");

		return totalPower;
	}

	@Override
	public long getNumberOfBranches() {
		return reorderBuffer.branchCount;
	}

	@Override
	public long getNumberOfMispredictedBranches() {
		return reorderBuffer.mispredCount;
	}

	@Override
	public void setNumberOfBranches(long numBranches) {
		reorderBuffer.branchCount = numBranches;
	}

	@Override
	public void setNumberOfMispredictedBranches(long numMispredictedBranches) {
		reorderBuffer.mispredCount = numMispredictedBranches;
	}

	@Override
	public long getNumberOfJumps() {
		return reorderBuffer.jumpCount;
	}

	@Override
	public long getNumberOfMispredictedTargets() {
		return reorderBuffer.targetMispredCount;
	}
}