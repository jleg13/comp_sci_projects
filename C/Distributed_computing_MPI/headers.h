/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Header file for main program provides system libraries and
 *  definitions of constants
 */

#include "matrix.h"
#include "utility.h"
#include "process_matrix.h"
#include "mpi.h"
#include <fcntl.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#define SUCCESS 1
#define FAILURE 0
#define ROOT 0
#define MPI_ERROR_CHECK(X, Y)                                                  \
        {                                                                      \
                int _err_rtn_;                                                 \
                if ((_err_rtn_ = X) < 0) {                                     \
                        fprintf(stderr,                                        \
                                "MPI error code %d: %s failed -- aborting\n",  \
                                _err_rtn_, #X);                                \
                        (Y);                                                   \
                }                                                              \
        }
        