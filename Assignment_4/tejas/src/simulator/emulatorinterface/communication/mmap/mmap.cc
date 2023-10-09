#include "mmap.h"
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>

namespace IPC
{
/*Mmap();
int analysisFn (int tid,uint64_t ip, int value, uint64_t tgt);
void onThread_start (int tid);
int onThread_finish (int tid);
bool unload ();
~Mmap();*/

#define FILESIZE ( (COUNT+5) * sizeof(packet))

void
Mmap::get_lock(packet *map) {
	map[COUNT+1].value = 1; 				// flag[0] = 1
	msync(map+COUNT*4, 5 * 4, MS_SYNC);
	map[COUNT+3].value = 1; 				// turn = 1
	msync(map+COUNT*4, 5 * 4, MS_SYNC);
	while((map[COUNT+2].value == 1) && (map[COUNT+3].value == 1)) {}
}

void
Mmap::release_lock(packet *map) {
	map[COUNT + 1].value = 0;
	msync(map+COUNT*4, 5 * 4, MS_SYNC);
}


Mmap::Mmap (int maxNumActiveThreads, void (*lock)(int), void (*unlock)(int)) : IPCBase(maxNumActiveThreads, lock, unlock)
{
	//FILEPATH is shared through common.h
	 fd = open (FILEPATH, O_RDWR | O_CREAT | O_TRUNC, (mode_t) 0600);
	  if (fd == -1)
	    {
	      perror ("Error opening file for writing");
	      exit (EXIT_FAILURE);
	    }

	 int result = write (fd, "", 1);
	  if (result != 1)
	    {
	      close (fd);
	      perror ("Error writing last byte of the file");
	      exit (EXIT_FAILURE);
	    }

	  /* Now the file is ready to be mmapped.
	   */
	  tldata[0].map = (packet *) mmap (0, FILESIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
	  if (tldata[0].map == (packet *)MAP_FAILED)
	    {
	      close (fd);
	      perror ("Error mmappp ing the file");
	      exit (EXIT_FAILURE);
	    }



	// initialise book-keeping variables for each of the threads
	THREAD_DATA *myData;
	for (int t=0; t<MaxThreads; t++) {
		myData = &tldata[t];
		myData->tlqsize = 0;
		myData->in = 0;
		myData->out = 0;
		myData->sum = 0;
//		myData->tlq = new packet[locQ];
		myData->map = tldata[0].map+(COUNT+5)*t;		// point to the correct index in file
	}

}


/* If local queue is full, write to the shared memory and then write to localQueue.
 * else just write at localQueue at the appropriate index i.e. at 'in'
 */
int
Mmap::analysisFn (int tid,uint64_t ip, int val, uint64_t addr)
{
	THREAD_DATA *myData = &tldata[tid];

	// if my local queue is full, I should write to the shared memory and return if cannot return
	// write immediately, so that PIN can yield this thread.
	if (myData->tlqsize == locQ) {
		if (Mmap::filewrite(tid,0)==-1) return -1;
	}

	// log the packet in my local queue
	packet *myQueue = myData->tlq;
	uint32_t *in = &(myData->in);
	packet *sendPacket = &(myQueue[*in]);

	sendPacket->ip = (uint64_t)ip;
	sendPacket->value = val;
	sendPacket->tgt = (uint64_t)addr;

	*in = (*in + 1) % locQ;
	myData->tlqsize++;
	return 0;
}

void
Mmap::onThread_start (int tid)
{
	THREAD_DATA *myData = &tldata[tid];
	packet *map = myData->map;

	map[COUNT].value = 0; // queue size pointer
	map[COUNT + 1].value = 0; // flag[0] = 0
	map[COUNT + 2].value = 0; // flag[1] = 0
}

int
Mmap::onThread_finish (int tid)
{
	THREAD_DATA *myData = &tldata[tid];

	// keep writing till we empty our local queue
	while (myData->tlqsize !=0) {
		if (Mmap::filewrite(tid,0)==-1) return -1;
	}

	// last write to our shared memory. This time write a -1 in the 'value' field of the packet
	return Mmap::filewrite(tid,1);
}

void
endian_swap (packet& x)
{
	/*(x>>56) |
        ((x<<40) & 0x00FF000000000000LLU) |
        ((x<<24) & 0x0000FF0000000000LLU) |
        ((x<<8)  & 0x000000FF00000000LLU) |
        ((x>>8)  & 0x00000000FF000000) |
        ((x>>24) & 0x0000000000FF0000) |
        ((x>>40) & 0x000000000000FF00) |
        (x<<56);
        */
    /*x = (x >> 24) |
     ((x << 8) & 0x00FF0000) | ((x >> 8) & 0x0000FF00) | (x << 24);
     */

	//Swap endinaness accordingly
}

int
Mmap::filewrite (int tid, int last)
{
	int queue_size;
	int numWrite;

	THREAD_DATA *myData = &tldata[tid];
	packet* map = myData->map;

	get_lock(map);
	queue_size = map[COUNT].value;
	release_lock(map);
	numWrite = COUNT - queue_size;

	// if numWrite is 0 this means cant write now. So should yield.
	if (numWrite==0) return -1;

	// if last packet then write -1 else write the actual packets
	if (last ==0) {

		// write 'numWrite' or 'local_queue_size' packets, whichever is less
		numWrite = numWrite<myData->tlqsize ? numWrite:myData->tlqsize;

		for (int i=0; i< numWrite; i++) {

			// for checksum
			myData->sum+=myData->tlq[(myData->out+i)%locQ].value;

			// copy from local buffer to file with appropriate swappings.
			endian_swap((myData->tlq[(myData->out+i)%locQ]));
			memcpy(&(map[(myData->prod_ptr+i)%COUNT]),
					&(myData->tlq[(myData->out+i)%locQ]),
					sizeof(packet));
		}
	}
	else {
		numWrite = 1;
		map[myData->prod_ptr % COUNT].value = -1;
	}

	// some bookkeeping of the threads state.
	myData->out = (myData->out + numWrite)%locQ;
	myData->tlqsize=myData->tlqsize-numWrite;
	myData->prod_ptr = (myData->prod_ptr + numWrite) % COUNT;

	// update queue_size
	get_lock(map);
	queue_size = map[COUNT].value;
	queue_size += numWrite;

	myData->tot_prod += numWrite;

	map[COUNT].value = queue_size;
	map[COUNT+4].value = myData->tot_prod;
	release_lock(map);

	return 0;
}


bool
Mmap::unload() {
	 if (munmap (tldata[0].map, FILESIZE) == -1)
	    {
	      perror ("Error un-mmapping the file");
	      /* Decide here whether to close(fd) and exit() or not. Depends... */
	    }
}

Mmap::~Mmap ()
{
	for (int t=0; t<MaxThreads; t++) {
		delete tldata[t].tlq;
	}
	close (fd);
}


} // namespace IPC
