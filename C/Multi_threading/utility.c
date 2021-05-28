/*
 *  Name: Joshua Le Gresley 
 *  Purpose: Library to provide utility funtions.
 *  
 */

#include "utility.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*
 * Validates the command line arguments.
 * parameters:
 *   argc - An int for the number of command line args passed in
 *   argv - An  char array storing the passed in command line args
 *   *n - An int pointer to the store the converted command line arg for
 *  number of processes
 * returns: 1 if command line args are valid, 0 if they are invalid
 */
int parse_args(int argc, char arg[], int *vehicles, int *vehicle_count)
{
        if ((argc == 5) && ((*vehicles = atoi(arg)) > 0)) {
                *vehicle_count += *vehicles;
                return SUCCESS;
        }
        return FAILURE;
}

/*
 * Function to free allocated memory and clean up system semaphores
 * parameters:
 *   *data - A reference to the threads data array
 *   *threads - A reference to the pthread's array
 *   *sem1 - A reference to one of the semaphores used
 *   *sem2 - A reference to one of the semaphores used
 *   *sem3 - A reference to one of the semaphores used
 */
void clean(params_t *data, pthread_t *threads, int *sem1, int *sem2, int *sem3)
{
        free(data);
        free(threads);
        rm_sem(*sem1);
        rm_sem(*sem2);
        rm_sem(*sem3);
}

/*
 *  Uses a reference to a params_t object in an array to store the threads
 *  data
 * parameters:
 *   data - A reference to a params_t object to store the thread data
 *   vehicle_id - An int for the id of the vehicle
 *   i - An int to determine the direction of the vehicle
 *   dir - An int used to define the direction
 */
void thread_data_init(params_t *data, int vehicle_id, int i, int dir)
{
        data->id = vehicle_id;
        if (i == dir) {
                memcpy(data->direction, EAST, sizeof(EAST));
        } else {
                memcpy(data->direction, WEST, sizeof(WEST));
        }
}
