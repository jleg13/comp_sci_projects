/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Library to provide utility funtions.
 */

#include "utility.h"
#include <fcntl.h>
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*
 * Validates the command line arguments.
 * parameters:
 *   argc - An int for the number of command line args passed in
 *   *argv -  A pointer to char array storing the passed in command line args
 *   *fd - A pointer to int array to the store filedescriptors
 *   *np - An int pointer to the store the dimemensions of matrices
 *   nprocs - Number of processes selected
 * returns: 1 if command line args are valid, 0 if they are invalid
 */
int parse_args(int argc, char *argv[], int *fd, int *dim, int *depth)
{
        if ((argc != 5) || ((fd[0] = open(argv[1], O_RDONLY)) == -1) ||
            ((fd[1] = open(argv[2], O_WRONLY | O_CREAT, 0666)) == -1) ||
            ((*dim = atoi(argv[3])) <= 0) || ((*depth = atoi(argv[4])) <= 0)) {
                fprintf(stderr,
                        "Usage: mpirun -np <dimension> %s "
                        "<matrix_a> <matrix_b> <dimension> <depth>\n",
                        argv[0]);
                return (-1);
        }
        return 0;
}

/*
 * Function to end mpi program correctly if failed
 */
void mpi_bail()
{
        MPI_Finalize();
        exit(EXIT_FAILURE);
}

/*
 * Function to end mpi program correctly when successful
 */
void mpi_success()
{
        MPI_Finalize();
        exit(EXIT_SUCCESS);
}

/*
 * Checks for erros in MPI operations
 * parameters:
 *   local_ok - An int for checking status
 *   fname[] -  A char array contain calling function name
 *   message[] - A char array holding error message to display
 *   comm - A MPI_comm type holding refernce to the current mpi comm
 * returns: 0 if no errors, -1 if there are errors.
 */
int check_for_error(int local_ok, char fname[], char message[], MPI_Comm comm)
{
        int ok;
        MPI_Allreduce(&local_ok, &ok, 1, MPI_INT, MPI_MIN, comm);
        if (ok == 0) {
                int my_rank;
                MPI_Comm_rank(comm, &my_rank);
                if (my_rank == 0) {
                        fprintf(stderr, "Proc %d > In %s, %s\n",
                         my_rank, fname, message);
                        fflush(stderr);
                }
                return -1;
        }
        return 0;
}

/*
 * Allocates dynamic memory for maticies.
 * parameters:
 *   **initial - Pointer to pointer type to the start of the initial array
 *   **mean -  Pointer to pointer type to the start of the mean array
 *   **super - Pointer to pointer type to the start of the super array
 *   **initial_row - Pointer to pointer type to the start of the initial_row
 *      array
 *   **mean_row - Pointer to pointer type to the start of the mean_row array
 *   dim - Int for matrix dimensions
 *   blk - Int for process block size
 *   depth - Int for depth of neighbours
 *   neighbourhood - Int for matix dimensions* depth*2
 *   comm -  A MPI_comm type holding refernce to the current mpi comm
 * returns: 1 memory allocated, 0 if not allocated.
 */
int allocate_matrix_mem(int **initial, int **mean, int **super,
                        int **initial_row, int **mean_row, int dim, int blk,
                        int depth, int neighbourhood, MPI_Comm comm)
{
        int local_ok = 1;

        *initial = malloc((dim) * (dim) * sizeof(int));
        *mean = malloc((dim) * (dim) * sizeof(int));
        *super = malloc((dim + depth * 2) * (dim + depth * 2) * sizeof(int));
        *initial_row = malloc(neighbourhood * (dim + depth * 2) * sizeof(int));
        *mean_row = malloc(dim * sizeof(int));

        if (*initial == NULL || *mean == NULL || *super == NULL ||
            *initial_row == NULL || *mean_row == NULL)
                local_ok = 0;
        return check_for_error(local_ok, "allocate_matrix_mem",
                               "Can't allocate local arrays", comm);
}

/*
 * Deallocates dynamic memory.
 * parameters:
 *   **initial - Pointer to pointer type to the start of the initial array
 *   **mean -  Pointer to pointer type to the start of the mean array
 *   **super - Pointer to pointer type to the start of the super array
 *   **initial_row - Pointer to pointer type to the start of the initial_row
 *     array
 *   **mean_row - Pointer to pointer type to the start of the mean_row array
 */
void deallocate_matrix_mem(int **mat_a, int **mat_b, int **mat_c, int **mat_d,
                           int **mat_e)
{
        free(*mat_a);
        free(*mat_b);
        free(*mat_c);
        free(*mat_d);
        free(*mat_e);
}
