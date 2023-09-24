/*
 * Copyright (C) 2004-2021 Intel Corporation.
 * SPDX-License-Identifier: MIT
 */

#include <iostream>
#include <fstream>
#include "pin.H"
using std::cerr;
using std::endl;
using std::ios;
using std::ofstream;
using std::string;

ofstream OutFile;

// The running count of instructions is kept here
// make it static to help the compiler optimize docount
static UINT64 icount = 0, lcount = 0, scount = 0, bcount = 0, jcount = 0;

// This function is called before every instruction is executed
VOID do_icount() { icount++; }
VOID do_lcount() { lcount++; }
VOID do_scount() { scount++; }
VOID do_bcount() { bcount++; }
VOID do_jcount() { jcount++; }

// Pin calls this function every time a new instruction is encountered
VOID Instruction(INS ins, VOID* v)
{
    // Insert a call to do_icount before every instruction
    INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)do_icount, IARG_END);

    // Insert a call to do_lcount before every load instruction
    if (INS_IsMemoryRead(ins))
    {
        INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)do_lcount, IARG_END);
    }

    // Insert a call to do_scount before every store instruction
    if (INS_IsMemoryWrite(ins))
    {
        INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)do_scount, IARG_END);
    }

    // Insert a call to do_jcount before every unconditional jump
    if (INS_IsBranch(ins) && !INS_HasFallThrough(ins))
    {
        INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)do_jcount, IARG_END);
    }

    // Insert a call to do_cjcount before every conditional branch
    if (INS_IsBranch(ins) && INS_HasFallThrough(ins))
    {
        INS_InsertCall(ins, IPOINT_BEFORE, (AFUNPTR)do_bcount, IARG_END);
    }
}

KNOB< string > KnobOutputFile(KNOB_MODE_WRITEONCE, "pintool", "o", "insmix.out", "specify output file name");

// This function is called when the application exits
VOID Fini(INT32 code, VOID* v)
{
    // Write to a file since cout and cerr maybe closed by the application
    OutFile.setf(ios::showbase);
    OutFile << "Total Instructions: " << icount << endl;
    OutFile << "Load %: " << (float) lcount / icount * 100 << endl;
    OutFile << "Store %: " << (float) scount / icount * 100 << endl;
    OutFile << "Unconditional Jump %: " << (float) jcount / icount * 100 << endl;
    OutFile << "Conditional Branch %: " << (float) bcount / icount * 100 << endl;
    OutFile.close();
}

/* ===================================================================== */
/* Print Help Message                                                    */
/* ===================================================================== */

INT32 Usage()
{
    cerr << "This tool counts the number of dynamic instructions executed" << endl;
    cerr << endl << KNOB_BASE::StringKnobSummary() << endl;
    return -1;
}

/* ===================================================================== */
/* Main                                                                  */
/* ===================================================================== */
/*   argc, argv are the entire command line: pin -t <toolname> -- ...    */
/* ===================================================================== */

int main(int argc, char* argv[])
{
    // Initialize pin
    if (PIN_Init(argc, argv)) return Usage();

    OutFile.open(KnobOutputFile.Value().c_str());

    // Register Instruction to be called to instrument instructions
    INS_AddInstrumentFunction(Instruction, 0);

    // Register Fini to be called when the application exits
    PIN_AddFiniFunction(Fini, 0);

    // Start the program, never returns
    PIN_StartProgram();

    return 0;
}
