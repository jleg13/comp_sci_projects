/*
 *  Name: Joshua Le Gresley (
 *  Purpose: Header file for utility funtion definitions defined
 *  in utility.c.
 */

#include <stdio.h>
#include <mpi.h>

int parse_args(int argc, char *argv[], int *, int *, int *);

void mpi_bail();

void mpi_success();

int check_for_error(int, char fname[], char message[], MPI_Comm);

int allocate_matrix_mem(int **, int **, int **, int **, int **,
                     int, int, int, int, MPI_Comm);

void deallocate_matrix_mem(int **,int **, int **, int **, int **);
