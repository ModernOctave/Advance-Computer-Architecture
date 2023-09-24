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

	Contributors:  Moksh Upadhyay, Rajshekar, Prathmesh
*****************************************************************************/
package generic;

public enum RequestType {
//	TLB_SEARCH,
//	TLB_ADDRESS_READY,
//	CACHE_REQUEST,
//	BUS_REQUEST,
//	PORT_REQUEST,
	
	PERFORM_DECODE,
	DECODE_COMPLETE,
	ALLOC_DEST_REG,
	RENAME_COMPLETE,
	FUNC_UNIT_AVAILABLE,
	LOAD_ADDRESS_COMPUTED,
	ICACHE_EXEC_COMPLETE,
	EXEC_COMPLETE,
	WRITEBACK_ATTEMPT,
	WRITEBACK_COMPLETE,
	PERFORM_COMMITS,
	MISPRED_PENALTY_COMPLETE,
	BOOT_PIPELINE,
	BROADCAST,
	
	Tell_LSQ_Addr_Ready,
	Validate_LSQ_Addr,
	Attempt_L1_Issue,
	Cache_Read,
	
	Cache_Write,

	Mem_Response,
	LSQ_Commit,
	
	//banked memory element's request types
	CacheBank_Read,
	CacheBank_Write,
	
	MemBank_Response,
	Main_MemBank_Read,
	Main_MemBank_Write,
	Main_MemBank_Response,
	
	//added by harveenk
	//----->
	Main_Mem_Access,
	//Main_Mem_Write,
	//Main_Mem_Read,
	Mem_Cntrlr_State_Update,
	Rank_Response,
	Column_Read_Complete,
	//<-----
	//added by harveenk
	
	MESI_RWITM,
//	Mem_Response_with_State,
	Request_for_copy,
	Request_for_modified_copy,
	Reply_with_shared_copy,
	Write_Modified_to_sharedmem, 
	

	TOKEN,
	LOCAL_TOKEN,
	PIPELINE_RESUME,
	PIPELINE_SLEEP,
	Migrate_Block,
	TREE_BARRIER,
	TREE_BARRIER_RELEASE,
	
	// Directory Junk
//	ReadMissDirectoryUpdate,
//	WriteMissDirectoryUpdate,
//	WriteHitDirectoryUpdate,
//	EvictionDirectoryUpdate,
//	MemResponseDirectoryUpdate,
//	MESI_Invalidate,
	
	EvictCacheLine,
	
	DirectoryWriteHit,
	DirectoryReadMiss,
	DirectoryWriteMiss,
	
	// Acknowledgements
	AckEvictCacheLine,
	AckDirectoryWriteHit,
	
	DirectoryCachelineForwardRequest,
	DirectoryEvictedFromSharedCache,
	DirectoryEvictedFromCoherentCache,
	DirectorySharedToExclusive,
	
	Cache_Hit, Cache_Miss,

	Tlb_Miss_Response, Send_Migrate_Block,    
}
