/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Header file for main program provides system libraries and
 *  definitions of constants
 */
#include <errno.h>
#include <fcntl.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#define SUCCESS 1
#define FAILURE 0
#define SIZE 4
#define MAXWAIT 25
#define CROSSINGTIME 4
#define EAST "east"
#define WEST "west"
