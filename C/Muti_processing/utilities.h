/*
 *  Name: Joshua Le Gresley (jlegresl@une.edu.au)
 *  Student_ID: 220197638
 *  Unit: COSC330: Assignment 1
 *  Purpose: Header file for utility funtion definitions defined
 *  in utility.c.
 *  Code is developed from COSC330 lecture examples.
 */
#include <stdio.h>

typedef struct flock fileLock_t;
typedef fileLock_t *fileLock_p;

int parse_args(int argc, char *argv[], int *);

unsigned char *read_file(FILE *, int *, char name[]);

int display_message(char desc[], unsigned char file[], int len);

int free_files(unsigned char *cipher_in, char *plain_in, fileLock_p lock);
