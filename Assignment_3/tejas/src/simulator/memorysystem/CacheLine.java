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
package memorysystem;

import java.util.LinkedList;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

public class CacheLine implements Cloneable
{
	private long tag;
	private long timestamp;
	private long address;
	private MESI state = MESI.INVALID;
	private boolean isDirectory = false;
	
	private LinkedList<Cache> sharers = null;
	
	public CacheLine(boolean isDirectory)
	{
		this.setTag(-1);
		this.setState(MESI.INVALID);
		this.setTimestamp(0);
		this.setAddress(-1);
		this.isDirectory = isDirectory;
		
		if(isDirectory==true) {
			sharers = new LinkedList<Cache>();
		}
	}
	
	private void checkIsDirectory() {
		if(isDirectory==false) {
			misc.Error.showErrorAndExit("This method is supposed to be used by a directory only !!");
		}
	}
	
	public Cache getOwner() {
		
		checkIsDirectory();
		
		if(sharers.size()==0) {
			return null;
		} else if (sharers.size()==1) {
			return sharers.get(0); 
		} else {
			misc.Error.showErrorAndExit("This directory entry has multiple owners : " + this);
			return null;
		}
	}
	
	public boolean isSharer(Cache c) {
		checkIsDirectory();
		return (this.sharers.indexOf(c)!=-1);
	}
	
	public void addSharer(Cache c) {
		checkIsDirectory();
		if(this.state==MESI.INVALID) {
			misc.Error.showErrorAndExit("Unholy mess !!");
		}
		
		// You cannot add a new sharer for a modified entry.
		// For same entry, if you try to add an event, it was because the cache sent multiple requests for 
		// the same cache line which triggered the memResponse multiple times. For the time being, just ignore this hack.
		if(this.state==MESI.MODIFIED && this.sharers.size()>0 && this.sharers.get(0)!=c) {
			misc.Error.showErrorAndExit("You cannot have multiple owners for a modified state !!\n" +
					"currentOwner : " + getOwner().containingMemSys.getCore().getCore_number() + 
					"\nnewOwner : " + c.containingMemSys.getCore().getCore_number() + 
					"\naddr : " + this.getAddress());
		}
		
		// You cannot add a new sharer for exclusive entry.
		// For same entry, if you try to add an event, it was because the cache sent multiple requests for 
		// the same cache line which triggered the memResponse multiple times. For the time being, just ignore this hack.
		if(this.state==MESI.EXCLUSIVE && this.sharers.size()>0 && this.sharers.get(0)!=c) {
			misc.Error.showErrorAndExit("You cannot have multiple owners for exclusive state !!\n" +
					"currentOwner : " + getOwner().containingMemSys.getCore().getCore_number() + 
					"\nnewOwner : " + c.containingMemSys.getCore().getCore_number() + 
					"\naddr : " + this.getAddress());
		}
		
		if(this.isSharer(c)==true) {
			return;
		}
		
		this.sharers.add(c);
	}
	
	public void clearAllSharers() {
		checkIsDirectory();
		this.sharers.clear();
	}
	
	public void removeSharer(Cache c) {
		checkIsDirectory();
		if(this.isSharer(c)==false) {
			misc.Error.showErrorAndExit("Trying to remove a sharer which is not a sharer !!");
		}
		
		this.sharers.remove(c);
	}


	public Object clone()
    {
        try {
            // call clone in Object.
            return super.clone();
        } catch(CloneNotSupportedException e) {
            System.out.println("Cloning not allowed.");
            return this;
        }
    }
	
	public boolean hasTagMatch(long tag)
	{
		if (tag == this.getTag()) {
			return true;
		} else {
			return false;
		}
	}
	
	public long getTag() {
		return tag;
	}

	public void setTag(long tag) {
		this.tag = tag;
	}


	public boolean isValid() {
		if (state != MESI.INVALID)
			return true;
		else
			return false;
	}

	public double getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isModified() {
		if (state == MESI.MODIFIED)
			return true;
		else 
			return false;
	}
/*
	protected void setModified(boolean modified) {
		this.modified = modified;
	}
*/
	public MESI getState() {
		return state;
	}

	public void setState(MESI state) {
		this.state = state;
	}

	public long getAddress() {
		return address;
	}

	public void setAddress(long address) {
		this.address = address;
	}
	
	public LinkedList<Cache> getSharers() {
		checkIsDirectory();
		return sharers;
	}
	
	public Cache getFirstSharer() {
		checkIsDirectory();
		return sharers.get(0);
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append("addr = " + this.getAddress() + " : "  + "state = " + this.getState());
		if(this.isDirectory) {
			s.append(" cores : " );
		
			for(Cache c : sharers) {
				s.append(c.containingMemSys.getCore().getCore_number() + " , ");
			}
		}
		
		return s.toString();
	}
}
