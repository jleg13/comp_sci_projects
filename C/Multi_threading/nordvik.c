/*******************************************************************************
 *  Name: Joshua Le Gresley 
 *  Purpose: Program is a variation of the readers and writers problem. It
 *  utilises M.J. Rochkind's sem_ops.h, simplified semaphore operations from
 *  'Advanced UNIX Programming'. Each thread created represents a vehicle that
 *  travels in an East or West direction. Synchronisation of these threads
 *  allows cars(readers), and trucks (writers) to gain access to the shared
 *  resource (the bridge). Only one truck may pass at any time, but multiple
 *  cars can travel in the same direction. This program utilises code sourced
 *  from UNE COSC330 lectures and practicals.
 *
 *  Desrciption: To compile use the provided makefile by running the 'make'
 *  command. To run the exectutable use the command in the format:
 *      nordvik <num.east_cars> <num.east_trucks>
 *                       <num. west_cars> <num.west_trucks>
 *
 *  Values are simply int values that tell the program how many of each vehicle
 *  from which direction need to be created.
 ******************************************************************************/
#include "headers.h"
#include "utility.h"

// global definitions
int cars_east = 0;
int cars_west = 0;
int car_east_sem;
int car_west_sem;
int bridge_sem;
int vehicles[SIZE];

/*
 * Thread entry point function for 'trucks' to gain sole access to the
 * bridge
 * parameters:
 *   void *arg - thread data is passed in as void type but cast to params_t
 */
void *truck(void *arg)
{
        // cast argument
        params_t truck = *((params_t *)arg);
        sleep(rand() % MAXWAIT);

        P(bridge_sem);
        printf("Truck %d going %s on the bridge\n", truck.id, truck.direction);
        sleep(CROSSINGTIME);
        printf("Truck %d going %s off the bridge\n", truck.id, truck.direction);
        V(bridge_sem);

        pthread_exit(NULL);
}

/*
 *  Thread entry point function for 'cars'. Depending on the cars direction,
 *  East or West the first car that gains access secures the bridge that allows
 *  multiple cars to access the bridge, the last car off opens up access to the
 *  shared resource.
 * parameters:
 *   void *arg - thread data is passed in as void type but cast to params_t
 */
void *car(void *arg)
{
        params_t car = *((params_t *)arg);
        sleep(rand() % MAXWAIT);
        // cars travelling east coming on
        if (strcmp(car.direction, EAST)) {
                P(car_east_sem);
                cars_east++;
                if (cars_east == 1)
                        P(bridge_sem);
                sleep(1);
                V(car_east_sem);
                // cars travelling west coming on
        } else {
                P(car_west_sem);
                cars_west++;
                if (cars_west == 1)
                        P(bridge_sem);
                sleep(1);
                V(car_west_sem);
        }

        printf("Car %d going %s on the bridge\n", car.id, car.direction);
        sleep(CROSSINGTIME);
        printf("Car %d going %s off the bridge\n", car.id, car.direction);

        // cars travelling east going off
        if (strcmp(car.direction, EAST)) {
                P(car_east_sem);
                cars_east--;
                if (cars_east == 0)
                        V(bridge_sem);
                V(car_east_sem);
        // cars travelling west going off
        } else {
                P(car_west_sem);
                cars_west--;
                if (cars_west == 0)
                        V(bridge_sem);
                V(car_west_sem);
        }
        pthread_exit(NULL);
}

int main(int argc, char *argv[])
{
        int i, j, err, vehicle_id = 0, vehicle_count = 0;

        // Create private semaphores
        car_east_sem = semtran(IPC_PRIVATE);
        car_west_sem = semtran(IPC_PRIVATE);
        bridge_sem = semtran(IPC_PRIVATE);
        // Initialise semaphores to 1
        V(bridge_sem);
        V(car_east_sem);
        V(car_west_sem);

        // Parse args
        for (i = 0; i < SIZE; i++) {
                if (!parse_args(argc, argv[i + 1], &vehicles[i],
                                &vehicle_count)) {
                        printf("Usage: nordvik <num. east_cars> <num."
                               " east_trucks.> <num. west_cars> <num. "
                               "west_trucks>\n");
                        exit(EXIT_FAILURE);
                }
        }

        // allocate memory for thread id's
        pthread_t *threads =
            (pthread_t *)calloc(vehicle_count, sizeof(pthread_t));

        // allocate memory for thread data
        params_t *thread_data =
            (params_t *)calloc(vehicle_count, sizeof(params_t));

        // check the memory allocation
        if ((threads == NULL) || (thread_data == NULL)) {
                fprintf(stderr, "Memory Allocation Failed\n");
                exit(EXIT_FAILURE);
        }

        // spin off a thread for each vehicle passing in id and direction
        for (i = 0; i < SIZE; i++) {
                // create threads for cars
                if (i == 0 || i == 2) {
                        for (j = 0; j < vehicles[i]; j++) {

                                thread_data_init(&thread_data[vehicle_id],
                                                 vehicle_id, i, 0);

                                if ((err = pthread_create(
                                         &threads[vehicle_id], NULL, car,
                                         &thread_data[vehicle_id])) != 0) {
                                        fprintf(stderr,
                                                "pthread_create: (%d)%s\n", err,
                                                strerror(err));
                                        exit(EXIT_FAILURE);
                                }
                                vehicle_id++;
                        }
                // create threads for trucks
                } else {
                        for (j = 0; j < vehicles[i]; j++) {

                                thread_data_init(&thread_data[vehicle_id],
                                                 vehicle_id, i, 1);

                                if ((err = pthread_create(
                                         &threads[vehicle_id], NULL, truck,
                                         &thread_data[vehicle_id])) != 0) {
                                        fprintf(stderr,
                                                "pthread_create: (%d)%s\n", err,
                                                strerror(err));
                                        exit(EXIT_FAILURE);
                                }
                                vehicle_id++;
                        }
                }
        }

        // use pthread_join to force main to wait for threads
        for (i = 0; i < vehicle_count; i++)
                pthread_join(threads[i], NULL);

        clean(thread_data, threads, &car_east_sem, &car_west_sem, &bridge_sem);

        exit(SUCCESS);
}
