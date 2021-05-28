/*******************************************************************************
 *  Name: Joshua Le Gresley 
 * 
 *  Purpose:  This program is a simple convolution matrix filter. Using MPI
 *  the program can process a square data matrix using a convolution filter
 *  that replaces each element with the mean of its neighbours.
 *  This implementation handles any square matrix of arbitrary size and also
 *  handles a neighbourhood depth of any arbitrary size. This implementation
 *  is unable to handle MPI processes of arbitrary number as specified in the
 *  assignment description. Hence the program only works when:
 *
 *         matrix dimensions = number of MPI processes
 *
 *
 *  Desrciption: To compile use the provided makefile by running the 'make'
 *  command. To run the exectutable use the command in the format:
 *
 *     mpirun -np <dimension> convol_filter <matrix_a> <matrix_b>
 *                         <dimension>  <depth>\n",
 *
 *  dimension - is the number of processes.
 *  matrix_a - is a file created by the attached matrix library for the initial
 *             matrix
 *  matrix_a - is a file created by the attached matrix library for the result
 *             matrix containing the mean values
 *  dimension - is the size of the matrix
 *  depth - is the depth of the neighbours
 ******************************************************************************/

#include "headers.h"

int main(int argc, char *argv[])
{
        int me, fd[2], i, nprocs, dim, depth, neighbourhood, blk;
        int *initial, *mean, *super, *initial_row, *mean_row;
        MPI_Comm comm;

        /* set-up MPI program with error checking */
        MPI_Init(&argc, &argv);
        comm = MPI_COMM_WORLD;
        MPI_ERROR_CHECK(MPI_Comm_rank(comm, &me), mpi_bail());
        MPI_ERROR_CHECK(MPI_Comm_size(comm, &nprocs), mpi_bail());
        /* parent code */
        if (me == ROOT) {
                if (parse_args(argc, argv, fd, &dim, &depth) < 0) {
                        mpi_bail();
                }
                /* check dimensions */
                if (dim == nprocs) {
                        blk = dim / nprocs;
                        neighbourhood = depth * 2 + blk;
                } else {
                        fprintf(stderr, "Dimensions must be equal to nproc in "
                                        "this implementation\n");
                        mpi_bail();
                }
        }

        /* broadcast data to all */
        MPI_Bcast(&dim, 1, MPI_INT, ROOT, comm);
        MPI_Bcast(&depth, 1, MPI_INT, ROOT, comm);
        MPI_Bcast(&neighbourhood, 1, MPI_INT, ROOT, comm);
        MPI_Bcast(&blk, 1, MPI_INT, ROOT, comm);

        /* allocate matrix memory */
        if (allocate_matrix_mem(&initial, &mean, &super, &initial_row,
                                &mean_row, dim, blk, depth, neighbourhood,
                                comm) < 0) {
                fprintf(stderr, "Allocation of memory failed\n");
                mpi_bail();
        }

        if (me == ROOT) {
                /* Initialise initial matrix */
                if (get_row(fd[0], dim * dim, 1, initial) == -1) {
                        fprintf(stderr, ": Initialization of initial failed\n");
                        deallocate_matrix_mem(&initial, &mean, &super,
                                              &initial_row, &mean_row);
                        mpi_bail();
                }

                /* Initialize supersized matrix */
                if (get_super(depth, dim, super, initial) == -1) {
                        fprintf(stderr, "Initialization of super failed\n");
                        deallocate_matrix_mem(&initial, &mean, &super,
                                              &initial_row, &mean_row);
                        mpi_bail();
                }
        }

        /* Scatter super matrix to the n processes */
        for (i = 0; i < neighbourhood; i++)
                if (MPI_Scatter(
                        &*(super + i * (dim + depth * 2) + 0), dim + depth * 2,
                        MPI_INT, &*(initial_row + i * (dim + depth * 2) + 0),
                        dim + depth * 2, MPI_INT, ROOT, comm) != MPI_SUCCESS) {
                        fprintf(stderr, "Scattering of super failed\n");
                        mpi_bail();
                }

        /* compute my row mean */
        if (calculate_mean(depth, dim, blk, neighbourhood, initial_row,
                           mean_row) == -1) {
                fprintf(stderr, "Calculating mean failed\n");
                deallocate_matrix_mem(&initial, &mean, &super, &initial_row,
                                      &mean_row);
                mpi_bail();
        }

        /* Gather rows of mean matrix */
        if (MPI_Gather(&*(mean_row + 0 * (dim) + 0), dim, MPI_INT,
                       &*(mean + 0 * (dim) + 0), dim, MPI_INT, ROOT,
                       comm) != MPI_SUCCESS) {
                fprintf(stderr, "Gathering of means failed\n");
                mpi_bail();
        }

        if (me == ROOT) {
                /* write the matrix to a file */
                if (set_row(fd[1], dim * dim, 1, mean) == -1) {
                        fprintf(stderr, "Writing of result matrix failed\n");
                        deallocate_matrix_mem(&initial, &mean, &super,
                                              &initial_row, &mean_row);
                        mpi_bail();
                }
                fprintf(stderr, "Successfully applied convolution "
                                "filter on matrix\n");
        }

        deallocate_matrix_mem(&initial, &mean, &super, &initial_row, &mean_row);
        MPI_Finalize();
        exit(EXIT_SUCCESS);
}
