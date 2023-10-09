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

import pipeline.outoforder.ReorderBufferEntry;


public class LSQEntry
{
        private int indexInQ;
        private LSQEntryType type;

        private ReorderBufferEntry robEntry;
        private long addr;
        private boolean valid;
        private boolean issued;
        private boolean forwarded;//Whether the load has got its value or not
        private boolean canBeRemoved;
        private boolean removed; //If the entry has been committed and removed from the LSQ
        
        public enum LSQEntryType {LOAD, STORE};
        
        public LSQEntry(LSQEntryType type, ReorderBufferEntry robEntry)
        {
                this.type = type;
                this.robEntry = robEntry;
                valid = false;
                issued = false;
                forwarded = false;
                canBeRemoved = false;
                removed = true;
        }
        
        public void recycle()
        {
                robEntry = null;
                valid = false;
                issued = false;
                forwarded = false;
                canBeRemoved = false;
                removed = false;
        }

        public LSQEntryType getType() {
                return type;
        }

        public void setType(LSQEntryType type) {
                this.type = type;
        }
        
        public long getAddr() {
                return addr;
        }

        public void setAddr(long addr) {
                this.addr = addr;
        }

        public boolean isValid() {
                return valid;
        }

        public void setValid(boolean valid) {
                
                if(this.valid == true && valid == true)
                {
                        System.out.println("entry already valid");
                }
                this.valid = valid;
        }

        public boolean isIssued() {
                return issued;
        }

        public void setIssued(boolean issued) {
                this.issued = issued;
        }

        public boolean isForwarded() {
                return forwarded;
        }

        public void setForwarded(boolean forwarded) {
                
                if(this.forwarded == true && forwarded == true)
                {
                        System.out.println("entry already forwarded");
                }
                
                this.forwarded = forwarded;
        }
        
        public boolean isCanBeRemoved() {
			return canBeRemoved;
		}

		public void setCanBeRemoved(boolean canBeRemoved) {
			this.canBeRemoved = canBeRemoved;
		}
        
        protected boolean isRemoved() {
                return removed;
        }

        public void setRemoved(boolean removed) {
                this.removed = removed;
        }
        
        public ReorderBufferEntry getRobEntry() {
                return robEntry;
        }
        
        public void setRobEntry(ReorderBufferEntry robEntry) {
                this.robEntry = robEntry;
        }

        protected void setIndexInQ(int indexInQ) {
                this.indexInQ = indexInQ;
        }

        public int getIndexInQ() {
                return indexInQ;
        }

        public String toString() {
                return "[(LSQEntry) " + type + " : " + addr + " : " + indexInQ; 
        }
        
        
}

