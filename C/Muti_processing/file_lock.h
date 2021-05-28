/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Header file for filelocking functionality definitions defined
 *  in file_lock.c.
 */
#include <fcntl.h>

typedef struct flock fileLock_t;
typedef fileLock_t *fileLock_p;

fileLock_p make_lock(void);

int get_lock(fileLock_p lock, int fd);

int un_lock(fileLock_p lock, int fd);

void free_lock(fileLock_p lock);
