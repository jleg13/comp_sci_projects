/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Library to provide functionality for file locking.
 *  Code is sourced from COSC330 lecture examples.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "file_lock.h"

/*
 * Creates a block of new memory for a new lock
 * returns: new fileLock_p struct
 */
fileLock_p make_lock(void)
{
	fileLock_p retval = (fileLock_p) calloc(1, sizeof(fileLock_t));
	if (retval == NULL) {
		perror("makeLock: calloc failed!");
	}
	return retval;
}

/*
 * Obtains a write lock on the file descriptor.
 * parameters:
 *   lock - the fileLock_p struct to lock
 *   fd - the associated file descriptor
 * returns: call to fcntl() returns 0 if successful completed, else
 * returns -1
 */
int get_lock(fileLock_p lock, int fd)
{
	memset(lock, 0, sizeof(fileLock_t));
	lock->l_type = F_WRLCK;
	return fcntl(fd, F_SETLKW, lock);
}

/*
 * Unlocks the write lock on file descriptor.
 * parameters:
 *   lock - the fileLock_p struct to unlock
 *   fd - the associated file descriptor
 * returns: call to fcntl() returns 0 if successful completed, else
 * returns -1
 */
int un_lock(fileLock_p lock, int fd)
{
	lock->l_type = F_UNLCK;
	return fcntl(fd, F_SETLKW, lock);
}

/*
 * Frees the memory of lock made by a makeLock
 * parameters:
 *   lock - the fileLock_p struct to free from memory
 */
void free_lock(fileLock_p lock)
{
	free(lock);
}
