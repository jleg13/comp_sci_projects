/*
 *  Name: Joshua Le Gresley
 *  Purpose: Header file for utility funtion definitions defined
 *  in utility.c.
 */
#include "sem_ops.h"
#include <pthread.h>
#include <stdio.h>

#define EAST "east"
#define WEST "west"
#define SUCCESS 1
#define FAILURE 0
#define SIZE 4
typedef struct {
        int id;
        char direction[sizeof(EAST)];
} params_t;

int parse_args(int argc, char arg[], int *, int *);
void thread_data_init(params_t *data, int, int, int);
void clean(params_t *data, pthread_t *threads, int *, int *, int *);
