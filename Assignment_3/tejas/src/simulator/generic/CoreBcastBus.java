package generic;

import java.util.Vector;

import emulatorinterface.RunnableThread;
import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;

public class CoreBcastBus extends SimulationElement{

	public Vector<Integer> toResume =  new Vector<Integer>();

	public CoreBcastBus() {
		super(PortType.Unlimited, 1, 1, 1, 1);
	}

	public void addToResumeCore(int id){
		this.toResume.add(id);
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		if(event.getRequestType() == RequestType.TREE_BARRIER_RELEASE){
			long barAddress = ((AddressCarryingEvent)event).getAddress();
			ArchitecturalComponent.cores.get(((AddressCarryingEvent)event).coreId).activatePipeline();
			if(((AddressCarryingEvent)event).coreId * 2 < BarrierTable.barrierList.get(barAddress).numThreads){
				this.getPort().put(new AddressCarryingEvent(
						0,eventQ,
						1,
						this, 
						this, 
						RequestType.TREE_BARRIER_RELEASE, 
						barAddress,
						((AddressCarryingEvent)event).coreId *2));
				this.getPort().put(new AddressCarryingEvent(
						0,eventQ,
						1,
						this, 
						this, 
						RequestType.TREE_BARRIER_RELEASE, 
						barAddress,
						((AddressCarryingEvent)event).coreId *2 + 1));
			}
		}
		else if(event.getRequestType() == RequestType.TREE_BARRIER){

			long barAddress = ((AddressCarryingEvent)event).getAddress();
			int coreId = ((AddressCarryingEvent)event).coreId;

			Barrier bar = BarrierTable.barrierList.get(barAddress);
			int numThreads = bar.getNumThreads();
			int level = (int) (Math.log(numThreads + 1)/Math.log(2));
			if(coreId >= Math.pow(2, level - 1) && coreId < Math.pow(2,level)){
				this.getPort().put(new AddressCarryingEvent(
						0,eventQ,
						1,
						this, 
						this, 
						RequestType.TREE_BARRIER,
						barAddress,
						(int)coreId/2));
			}
			else{
				System.out.println("Core Id : " + coreId );
				bar.addTreeInfo(coreId);
				if(bar.getTreeInfo(coreId) == 3){
					if(coreId == 1){
						//	BarrierTable.barrierReset(barAddress);
						this.getPort().put(new AddressCarryingEvent(
								0,eventQ,
								0,
								this, 
								this, 
								RequestType.TREE_BARRIER_RELEASE, 
								barAddress,
								1));
					}
					else{
						this.getPort().put(new AddressCarryingEvent(
								0,eventQ,
								1,
								this, 
								this, 
								RequestType.TREE_BARRIER, 
								barAddress,
								(int)coreId/2));
					}
				}
			}
		}
		else if(event.getRequestType() == RequestType.PIPELINE_RESUME){
			for(int i : toResume){
				ArchitecturalComponent.cores.get(i).activatePipeline();
				RunnableThread.setThreadState(i,false);
			}
			toResume.clear();
		}
		else{
			ArchitecturalComponent.cores.get((int) ((AddressCarryingEvent)event).getAddress()).sleepPipeline();
		}
	}

}
