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

	Contributors:  Moksh Upadhyay
*****************************************************************************/
package config;

import memorysystem.nuca.NucaCache.Mapping;
import memorysystem.nuca.NucaCache.NucaType;
import generic.PortType;

public class CacheConfig 
{
	public long operatingFreq;
	public long stepSize;
	
	public WritePolicy writePolicy;	
	public String nextLevel;
	public int blockSize;
	public int assoc;
	public int size;
	public int numEntries;
	public int readLatency;
	public int writeLatency;
	
	public PortType portType;
	public int readPorts;
	public int writePorts;
	public int readWritePorts;
	public int portReadOccupancy;
	public int portWriteOccupancy;
	
	public int numberOfBuses;
	public int busOccupancy;
	public int mshrSize;
	public NucaType nucaType;
	public Mapping mapping;
	
	public boolean collectWorkingSetData = false;
	public long workingSetChunkSize = -1;
	
	public CacheEnergyConfig power;
	
	public String cacheName;
	public int numComponents;
	public boolean firstLevel = false;
	public CacheDataType cacheDataType = null;
	public String nextLevelId;

	public String coherenceName;

	public boolean isDirectory = false;
	
	public static enum WritePolicy{
		WRITE_BACK, WRITE_THROUGH
	}
	
	public static enum PrefetcherType{
		None, Power4
	}
	
	public PrefetcherType prefetcherType;
	
	public int AMAT;

	//Getters and setters
	
	public WritePolicy getWritePolicy() {
		return writePolicy;
	}

	public String getNextLevel() {
		return nextLevel;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getAssoc() {
		return assoc;
	}

	public int getSize() {
		return size;
	}

	public int getReadLatency() {
		return readLatency;
	}

	public void setReadLatency(int readLatency) {
		this.readLatency = readLatency;
	}

	public int getWriteLatency() {
		return writeLatency;
	}

	public void setWriteLatency(int writeLatency) {
		this.writeLatency = writeLatency;
	}

	public int getReadPorts() {
		return readPorts;
	}

	public void setReadPorts(int readPorts) {
		this.readPorts = readPorts;
	}

	public int getWritePorts() {
		return writePorts;
	}

	public void setWritePorts(int writePorts) {
		this.writePorts = writePorts;
	}

	public int getReadWritePorts() {
		return readWritePorts;
	}

	public void setReadWritePorts(int readWritePorts) {
		this.readWritePorts = readWritePorts;
	}

	public int getPortReadOccupancy() {
		return portReadOccupancy;
	}

	public void setPortReadOccupancy(int portReadOccupancy) {
		this.portReadOccupancy = portReadOccupancy;
	}

	public int getPortWriteOccupancy() {
		return portWriteOccupancy;
	}

	public void setPortWriteOccupancy(int portWriteOccupancy) {
		this.portWriteOccupancy = portWriteOccupancy;
	}

	protected void setWritePolicy(WritePolicy writePolicy) {
		this.writePolicy = writePolicy;
	}

	protected void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}

	protected void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	protected void setAssoc(int assoc) {
		this.assoc = assoc;
	}

	protected void setSize(int size) {
		this.size = size;
	}

	public int getNumberOfBuses() {
		return numberOfBuses;
	}
	public int getBankSize()
	{
		return size/(SystemConfig.nocConfig.numberOfColumns * SystemConfig.nocConfig.numberOfRows);
	}
	public NucaType getNucaType() {
		return nucaType;
	}

	public void setNucaType(NucaType nucaType) {
		this.nucaType = nucaType;
	}

	public int getBusOccupancy() {
		return busOccupancy;
	}

	public void setBusOccupancy(int busOccupancy) {
		this.busOccupancy = busOccupancy;
	}

	public int getAMAT() {
		return AMAT;
	}

	public void setAMAT(int aMAT) {
		AMAT = aMAT;
	}
	

	//	public boolean isFirstLevel() {
//		return isFirstLevel;
//	}
//
//	public void setFirstLevel(boolean isFirstLevel) {
//		this.isFirstLevel = isFirstLevel;
//	}	
	
	
}
