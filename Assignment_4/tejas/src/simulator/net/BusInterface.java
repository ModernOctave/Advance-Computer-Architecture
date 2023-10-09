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

	Contributors:  Eldhose Peter
*****************************************************************************/

package net;

import main.ArchitecturalComponent;
import dram.MainMemoryDRAMController;
import generic.CommunicationInterface;
import generic.Event;

public class BusInterface implements CommunicationInterface{

	Bus bus;
	public BusInterface(Bus bus) {
		super();
		this.bus = bus;
	}
	
	/*
	 * Messages are coming from simulation elements(cores, cache banks) in order to pass it to another
	 * through electrical snooping Bus.
	 */
	@Override
	public void sendMessage(Event event) {
		bus.sendBusMessage(event);		
	}

	@Override
	public MainMemoryDRAMController getNearestMemoryController(int chanNum) {
		// TODO Auto-generated method stub
		return ArchitecturalComponent.getMainMemoryDRAMController(this,chanNum);
	}
}
