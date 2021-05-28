/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Library to provided functionality for establishing and using
 *  shared memory.
*/

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include "mem.h"

/*
 * Estabalishes a file for use as shared memory
 * parameters:
 *   *fd - int pointer file descripter for the new shared file in memory
 *   *file - char array for the name of the file to create
 *   size - size of shared file to create
 * returns: 0 if successful completed, else returns -1
 */
int init_shared_file(int *fd, char *file, size_t size)
{
	*fd = open(file, O_RDWR | O_CREAT | O_EXCL, S_IRWXU);
	if (*fd < 0) {
		return open_shared_file(fd, file, size);
	} else {
		if (lseek(*fd, size - 1, SEEK_SET) == -1) {
			perror("lseek error!");
			return -1;
		}
		if (write(*fd, "", 1) != 1) {
			perror("write error!");
			return -1;
		}
	}
	return 0;
}

/*
 * Attempt to access file used for shared memory
 * parameters:
 *   *fd - int pointer file descripter for the shared file in memory
 *   *file - char array for the name of the file to access
 *   *size - size of shared file to create
 * returns: 0 if successful completed, else returns -1
 */
int open_shared_file(int *fd, char *file, size_t size)
{
	struct stat statbuf;
	*fd = open(file, O_RDWR, S_IRWXU);
	if (*fd < 0) {
		fprintf(stderr, "Couldn't open %s! Exiting\n", file);
		return -1;
	}
	if (fstat(*fd, &statbuf) < 0) {
		perror("fstat error!");
		return -1;
	}
	if (statbuf.st_size < size) {
		if (lseek(*fd, size - 1, SEEK_SET) == -1) {
			perror("lseek error!");
			return -1;
		}
		if (write(*fd, "", 1) != 1) {
			perror("write error!");
			return -1;
		}
	}
	if (fstat(*fd, &statbuf) < 0) {
		perror("fstat error!");
		return -1;
	}
	return 0;
}
