/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Library to provide matrix processing funtions.
 */

#include "process_matrix.h"
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

/*
 * Function to build a super sized matrix with padding of size depth
 * and the initial matrix inside.
 * parameters:
 *   depth - An int for the depth of the neighbours
 *   dim -  A int for the dimension of the initial matrx
 *   super[] - reference to the super-sized matrix memory
 *   initial[] - reference to the initial matrix memory
 * returns: 0 if succesful, -1 if not
 */
int get_super(int depth, int dim, int super[], int initial[])
{
        int row, col;
        if ((depth <= 0) || (dim <= 0)) {
                fprintf(stderr, "indexes out of range");
                return -1;
        } else {
                /* first fill with zeros */
                for (row = 0; row < dim + depth * 2; row++)
                        for (col = 0; col < dim + depth * 2; col++)
                                *(super + row * (dim + depth * 2) + col);

                /* then fill middle with initial matrix_a */
                for (row = 0; row < dim + depth * 2; row++)
                        for (col = 0; col < dim + depth * 2; col++) {
                                if (col >= depth && row >= depth &&
                                    col < dim + depth && row < dim + depth) {
                                        *(super + row * (dim + depth * 2) +
                                          col) =
                                            *(initial + (row - depth) * (dim) +
                                              (col - depth));
                                }
                        }
                return 1;
        }
}

/*
 * Calculates the mean of a matrix location from the surrounding neighbours
 * of depth = depth.
 * parameters:
 *   depth - An int for the depth of the neighbours
 *   dim -  A int for the dimension of the initial matrx
 *   blk - An int for the blk size of the current process
 *   super[] - reference to the super-sized matrix row of the process
 *   mean[] - reference to the mean matrix memory to be filled
 * returns: 0 if succesful, -1 if not
 */
int calculate_mean(int depth, int dim, int blk, int neighbourhood, int super[],
                   int mean[])
{
        int row, col;
        if ((depth <= 0) || (dim <= 0)) {
                fprintf(stderr, "indexes out of range");
                return -1;
        } else {
                for (row = 0; row < neighbourhood; row++)
                        for (col = 0; col < dim + depth * 2; col++) {
                                if (col >= depth && row >= depth &&
                                    col < dim + depth && row < blk + depth) {
                                        *(mean + (row - depth) * (dim) +
                                          (col - depth)) =
                                            neighbours_sum(depth, dim, col, row,
                                                           super) -
                                            ((*(super +
                                                row * (dim + depth * 2) +
                                                col)) *
                                             depth);
                                        *(mean + (row - depth) * (dim) +
                                          (col - depth)) =
                                            *(mean + (row - depth) * (dim) +
                                              (col - depth)) /
                                            (8 * depth);
                                }
                        }
                return 1;
        }
}

/*
 * calculates the sum of a points surrounding neighours
 * parameters:
 *   depth - An int for the depth of the neighbours
 *   dim -  A int for the dimension of the initial matrx
 *   col - An int for the current column
 *   row - An int for the current row
 *   super[] - reference to the super-sized matrix row of the process
 * returns: an int for the sum of surrounding neighbours
 */
int neighbours_sum(int depth, int dim, int col, int row, int super[])
{
        int sum = 0, i, j, step, layers;
        step = 1;
        layers = 3;
        for (i = col - 1; i >= col - depth; i--) {
                for (j = row - step; j < row - step + layers; j += step) {
                        sum +=
                            (*(super + j * (dim + depth * 2) + i)) +
                            (*(super + j * (dim + depth * 2) + (i + step))) +
                            (*(super + j * (dim + depth * 2) + (i + step * 2)));
                }
                step++;
                layers += 2;
        }
        return sum;
}
