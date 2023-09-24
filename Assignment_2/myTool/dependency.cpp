#include "pin.H"
#include <iostream>
#include <fstream>
#include <vector>
#include <map>
#include <limits> // for numeric_limits

using std::cerr;
using std::endl;
using std::ios;
using std::ofstream;
using std::string;
using std::vector;

// Output file for results
ofstream OutFile;

// Structure to store information about each instruction
struct InstructionInfo {
    REG reg;
    INT addr;
};

// Global variables
vector<InstructionInfo> instructionArray; // Vector to store the order of instructions
std::map<REG, UINT64> regWriteInstructionOrder; // Map to store the order of register writes
std::map<REG, UINT64> regReadInstructionOrder; // Map to store the order of register reads
// std::map<INT, UINT64> storeInstructionOrder; // Map to store the order of store instructions

std::map<UINT64, UINT64> rawDistanceCounts; // Map to store RAW distance counts
std::map<UINT64, UINT64> warDistanceCounts; // Map to store WAR distance counts
std::map<UINT64, UINT64> wawDistanceCounts; // Map to store WAW distance counts
std::map<UINT64, UINT64> storeLoadDistanceCounts; // Map to store store-load distance counts

// Map to store the order of store instructions and a flag for each store indicating if a load has been recorded
std::map<INT, std::pair<UINT64, bool>> storeInstructionOrder;

// Map to store the order of register producers and consumers
std::map<REG, std::pair<UINT64, bool>> regReadProducers; // Map to store the order of register producers
std::map<REG, UINT64> regReadConsumers; // Map to store the order of register consumers

std::map<REG, std::pair<UINT64, bool>> regWriteProducers; // Map to store the order of register producers
std::map<REG, UINT64> regWriteConsumers; // Map to store the order of register consumers




// Maximum distances to consider
UINT64 RAW_MAX = 1000;
UINT64 WAR_MAX = 1000;
UINT64 WAW_MAX = 1000;
UINT64 storeLoad_Max = 1000;

// //Maximum number of instructions to store
// const size_t MAX_INSTRUCTION_COUNT = 10000;

// Function to record each instruction's address
VOID RecordInstruction(INT addr)
{
    // if (instructionArray.size() >= MAX_INSTRUCTION_COUNT)
    // {
    //     // Remove the oldest instruction
    //     instructionArray.erase(instructionArray.begin());
    // }
    instructionArray.push_back({REG_INVALID(), addr});
}

VOID RecordRegWriteOrder(REG reg, INT addr)
{
    
    // Check if the register was previously written to
    if (regWriteInstructionOrder.find(reg) != regWriteInstructionOrder.end())
    {
        UINT64 prevWriteIdx = regWriteInstructionOrder[reg];
        UINT64 writeIdx = static_cast<UINT64>(instructionArray.size() - 1);
        UINT64 wawDistance = writeIdx - prevWriteIdx;

        // Check if the WAW distance is within the limit
        if (wawDistance < WAW_MAX && writeIdx > prevWriteIdx && writeIdx != prevWriteIdx)
        {
            bool hasWriteInBetween = false;
            bool hasReadInBetween = false;

            for (UINT64 i = prevWriteIdx + 1; i < writeIdx; ++i)
            {
                if (instructionArray[i].reg == reg)
                {
                    hasWriteInBetween = true;
                    break;
                }
            }

            for (UINT64 i = writeIdx + 1; i < prevWriteIdx; ++i)
            {
                if (instructionArray[i].reg == reg)
                {
                    hasReadInBetween = true;
                    break;
                }
            }

            if (!hasWriteInBetween || !hasReadInBetween)
            {
                wawDistanceCounts[wawDistance]++;
            }
        }
    }

    // Update the current write order for the register
    regWriteInstructionOrder[reg] = static_cast<UINT64>(instructionArray.size() - 1);
    instructionArray.back().reg = reg;

    
    

    // Check if there's a consumer for this register
    if(regWriteProducers.find(reg) != regWriteProducers.end() && !regWriteProducers[reg].second)
    {
        UINT64 producerOrder = regWriteProducers[reg].first;
        UINT64 consumerOrder = static_cast<UINT64>(instructionArray.size() - 1);
        UINT64 warDistance = consumerOrder - producerOrder;

        // Check if the WAR distance is within the limit
        if (warDistance < WAR_MAX && consumerOrder > producerOrder)
        {
            warDistanceCounts[warDistance]++;

            // OutFile << "reg: " << REG_StringShort(reg) << ", Read Order: " << consumerOrder << ", Write Order: " << producerOrder << ", Distance: " << warDistance << std::endl;
        }

        // Mark the producer as having a recorded consumer
        regWriteProducers[reg].second = true;
    }
    

    // Update the regReadProducers map
    regReadProducers[reg] = std::make_pair(static_cast<UINT64>(instructionArray.size() - 1), false);



}


VOID RecordRegReadOrder(REG reg, INT addr)
{
    

    // Update the current read order for the register
    regReadInstructionOrder[reg] = static_cast<UINT64>(instructionArray.size() - 1);
    instructionArray.back().reg = reg;

    // Check if there's a producer for this register
    if (regReadProducers.find(reg) != regReadProducers.end() && !regReadProducers[reg].second)
    {
        UINT64 producerOrder = regReadProducers[reg].first;
        UINT64 consumerOrder = static_cast<UINT64>(instructionArray.size() - 1);
        UINT64 rawDistance = consumerOrder - producerOrder;

        // Check if the RAW distance is within the limit
        if (rawDistance < RAW_MAX && consumerOrder > producerOrder)
        {
            rawDistanceCounts[rawDistance]++;

            // OutFile << "reg: " << REG_StringShort(reg) << ", Read Order: " << consumerOrder << ", Write Order: " << producerOrder << ", Distance: " << rawDistance << std::endl;
        }

        // Mark the producer as having a recorded consumer
        regReadProducers[reg].second = true;
    }

    // Update the regWriteProducers map
    regWriteProducers[reg] = std::make_pair(static_cast<UINT64>(instructionArray.size() - 1), false);

}

// Function to record the order of store instructions and their addresses
VOID RecordStoreOrder(INT addr)
{
    storeInstructionOrder[addr] = std::make_pair(static_cast<UINT64>(instructionArray.size() - 1), false);
}


// Function to record the order of load instructions and their addresses
VOID RecordLoadOrder(INT addr)
{
    UINT64 loadIdx = static_cast<UINT64>(instructionArray.size() - 1);
    
    // Check if there's a store instruction associated with this load address
    if (storeInstructionOrder.find(addr) != storeInstructionOrder.end())
    {
        UINT64 storeOrder = storeInstructionOrder[addr].first;
        
        if (!storeInstructionOrder[addr].second && storeOrder < loadIdx && loadIdx - storeOrder < storeLoad_Max && storeOrder != loadIdx)
        {
            // Calculate distance and count for the first consecutive load after the store
            UINT64 loadStoreDistance = loadIdx - storeOrder;
            storeLoadDistanceCounts[loadStoreDistance]++;
            
            // Mark the store as having a recorded load
            storeInstructionOrder[addr].second = true;
        }
    }
}

// Instrumentation function for instructions
VOID Instruction(INS ins, VOID *v)
{
    INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)RecordInstruction, IARG_INST_PTR, IARG_END);

    for (UINT64 i = 0; i < INS_OperandCount(ins); ++i)
    {
        if (INS_OperandIsReg(ins, i) && INS_OperandWritten(ins, i)) // Check if it's a register write
        {
            REG reg = INS_OperandReg(ins, i);
            INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)RecordRegWriteOrder, IARG_UINT64, reg, IARG_INST_PTR, IARG_END);
        }
        else if (INS_OperandIsReg(ins, i) && INS_OperandRead(ins, i)) // Check if it's a register read
        {
            REG reg = INS_OperandReg(ins, i);
            INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)RecordRegReadOrder, IARG_UINT64, reg, IARG_INST_PTR, IARG_END);
        }
    }
    if (INS_IsMemoryWrite(ins))
    {
        INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)RecordStoreOrder, IARG_MEMORYWRITE_EA, IARG_END);
    }
    if (INS_IsMemoryRead(ins))
    {
        INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)RecordLoadOrder, IARG_MEMORYREAD_EA, IARG_END);
    }
}

// Knob to specify the output file name
KNOB<string> KnobOutputFile(KNOB_MODE_WRITEONCE, "pintool", "o", "dependency.out", "specify output file name");

// Fini function called when the instrumentation is finished
VOID Fini(INT32 code, VOID *v)
{
    OutFile.setf(ios::showbase);
    
    OutFile << "instructionArray size: " << instructionArray.size() << std::endl;
    // for (size_t i = 0; i < instructionArray.size(); i++)
    // {   
    //     InstructionInfo info = instructionArray[i];
    //     REG reg = info.reg;
    //     UINT64 readIdx = i;

    //     if (reg != REG_INVALID())
    //     {
    //         // Check if the register was previously written to
    //         if (regWriteInstructionOrder.find(reg) != regWriteInstructionOrder.end())
    //         {
    //             UINT64 writeIdx = regWriteInstructionOrder[reg];

    //             if (writeIdx - readIdx < WAR_MAX && writeIdx > readIdx && writeIdx != readIdx)
    //             {
    //                 bool hasWriteInBetween = false;
    //                 bool hasReadInBetween = false;

    //                 for (UINT64 j = readIdx + 1; j < writeIdx; ++j)
    //                 {
    //                     if (instructionArray[j].reg == reg)
    //                     {
    //                         hasWriteInBetween = true;
    //                         break;
    //                     }
    //                 }

    //                 for (UINT64 j = writeIdx + 1; j < readIdx; ++j)
    //                 {
    //                     if (instructionArray[j].reg == reg)
    //                     {
    //                         hasReadInBetween = true;
    //                         break;
    //                     }
    //                 }

    //                 if (!hasWriteInBetween || !hasReadInBetween)
    //                 {
    //                     UINT64 warDistance = writeIdx - readIdx;
    //                     warDistanceCounts[warDistance]++;
    //                 }
    //             }

    //             if (readIdx - writeIdx < RAW_MAX && readIdx > writeIdx && readIdx != writeIdx)
    //             {
    //                 bool hasReadInBetween = false;
    //                 bool hasWriteInBetween = false;

    //                 for (UINT64 j = writeIdx + 1; j < readIdx; ++j)
    //                 {
    //                     if (instructionArray[j].reg == reg)
    //                     {
    //                         hasReadInBetween = true;
    //                         break;
    //                     }
    //                 }

    //                 for (UINT64 j = readIdx + 1; j < writeIdx; ++j)
    //                 {
    //                     if (instructionArray[j].reg == reg)
    //                     {
    //                         hasWriteInBetween = true;
    //                         break;
    //                     }
    //                 }

    //                 if (!hasReadInBetween || !hasWriteInBetween)
    //                 {
    //                     UINT64 rawDistance = readIdx - writeIdx;
    //                     rawDistanceCounts[rawDistance]++;

    //                     // OutFile << "reg: " << REG_StringShort(reg) << ", Read Order: " << readIdx << ", Write Order: " << writeIdx << ", Distance: " << rawDistance << std::endl;
    //                 }
    //             }
    //         }
    //     }
    // }

    // Output the RAW distance counts
    OutFile << "RAW Distances:" << std::endl;
    for (UINT64 i = 1; i <= RAW_MAX; ++i)
    {
        UINT64 count = rawDistanceCounts[i];
        OutFile << "RAW Distance: " << i
                << ", Count: " << count << std::endl;
    }
    // for (const auto &entry : rawDistanceCounts)
    // {
    //     UINT64 rawDistance = entry.first;
    //     UINT64 count = entry.second;
    //     OutFile << "RAW Distance: " << rawDistance
    //             << ", Count: " << count << std::endl;
    // }

    // Output the WAR distance counts
    OutFile << "WAR Distances:" << std::endl;
    for(UINT64 i = 1; i <= WAR_MAX; ++i)
    {
        UINT64 count = warDistanceCounts[i];
        OutFile << "WAR Distance: " << i
                << ", Count: " << count << std::endl;
    }

    // for (const auto &entry : warDistanceCounts)
    // {
    //     UINT64 warDistance = entry.first;
    //     UINT64 count = entry.second;
    //     OutFile << "WAR Distance: " << warDistance
    //             << ", Count: " << count << std::endl;
    // }

    // Output the WAW distance counts
    OutFile << "WAW Distances:" << std::endl;
    for (UINT64 i = 1; i <= WAW_MAX; ++i)
    {
        UINT64 count = wawDistanceCounts[i];
        OutFile << "WAW Distance: " << i
                << ", Count: " << count << std::endl;
    }

    // for (const auto &entry : wawDistanceCounts)
    // {
    //     UINT64 wawDistance = entry.first;
    //     UINT64 count = entry.second;
    //     OutFile << "WAW Distance: " << wawDistance
    //             << ", Count: " << count << std::endl;
    // }

    // Output the store-load distance counts
    OutFile << "Store-Load Distances:" << std::endl;
    for (UINT64 i = 1; i <= storeLoad_Max; ++i)
    {
        UINT64 count = storeLoadDistanceCounts[i];
        OutFile << "Store-Load Distance: " << i
            << ", Count: " << count << std::endl;
    }

    // for (const auto &entry : storeLoadDistanceCounts)
    // {
    //     UINT64 storeLoadDistance = entry.first;
    //     UINT64 count = entry.second;
    //     OutFile << "Store-Load Distance: " << storeLoadDistance
    //         << ", Count: " << count << std::endl;
    // }

    // Close the output file
    OutFile.close();
}

// Entry point
int main(int argc, char *argv[])
{
    PIN_Init(argc, argv);
    OutFile.open(KnobOutputFile.Value().c_str());

    for (UINT64 i = 1; i <= RAW_MAX; ++i)
    {
        rawDistanceCounts[i] = 0;
        warDistanceCounts[i] = 0;
        wawDistanceCounts[i] = 0;
        storeLoadDistanceCounts[i] = 0;
    }

    INS_AddInstrumentFunction(Instruction, 0);
    PIN_AddFiniFunction(Fini, 0);
    PIN_StartProgram();
    return 0;
}
