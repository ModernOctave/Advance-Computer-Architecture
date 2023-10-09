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

	Contributors:  Prathmesh Kallurkar, Rajshekar Kalyappam
*****************************************************************************/

package generic;

//TODO needs to renamed
public enum OperationType 
{
	inValid,
	integerALU,
	integerMul,
	integerDiv,
	floatALU,
	floatMul,
	floatDiv,
	integerVectorALU,
	integerVectorMul,
	floatVectorALU,
	floatVectorMul,
	FMA,
	vectorFMA,
	load,
	loadAGU,
	store,
	storeAGU,
	jump,
	branch,
	//integerMov,
	//floatMov,
	//xchg,
	AES,
	vectorString,
	bitScan,
	vectorShuffle,
	LEA,
	acceleratedOp,
	nop,
	
	cpuid,
	mfence,
	sfence,
	lfence,
	
	read_prefetch,
	write_prefetch,
	//TODO Software interrupt can also be modelled as a far jump
	interrupt,
	no_of_types,
	//sync type for causility check
	sync
}