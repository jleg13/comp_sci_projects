/*
 *  Name: Joshua Le Gresley (jlegresl@une.edu.au)
 *  Student_ID: 220197638
 *  Unit: COSC330: Assignment 1
 *  Purpose: Library to provide utility funtions.
 *  Code is developed from COSC330 lecture examples.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "utilities.h"
#include "file_lock.h"

#define SUCCESS 1
#define FAILURE 0

/*
 * Validates the command line arguments.
 * parameters:
 *   argc - An int for the number of command line args passed in
 *   argv - An  char array storing the passed in command line args
 *   *n - An int pointer to the store the converted command line arg for
 *   number of processes
 * returns: 1 if command line args are valid, 0 if they are invalid
 */
int parse_args(int argc, char *argv[], int *n)
{
        if ((argc == 3) && ((*n = atoi(argv[1])) > 0)) {
                return SUCCESS;
        }
        return FAILURE;
}

/*
 * Attempts to read in the specifed file, will return with failure
 * if the file cannot be accessed else will return an unsigned array
 * holding the file data.
 * parameters:
 *    *file - A pointer to FILE object to access data
 *    filename - A string of the file name to be read from
 *    *len - An int pointer to length of data array
 * returns: An unsigned char array storing file data
 */
unsigned char *read_file(FILE *file, int *len, char name[])
{
        fseek(file, 0, SEEK_END);
        *len = ftell(file);
        int tmp_len = *len;
        rewind(file);
        unsigned char *file_in = malloc(tmp_len + 1);
        fread(file_in, tmp_len, 1, file);
        fclose(file);
        display_message(name, file_in, tmp_len);
        return file_in;
}

/*
 * Displays content to display
 * parameters:
 *    desc - char array to label output
 *    file - unsigned char array holding the data to display
 *    len - int value of the length of the data
 * returns: int on completion
 */
int display_message(char desc[], unsigned char file[], int len)
{
        int y;
        printf("%s: ", desc);
        for (y = 0; y < len; y++) {
                printf("%c", file[y]);
        }

        printf("\n");
        return 0;
}

/*
 * Attempts to read in the specifed file, will return with failure
 * if the file cannot be accessed else will return an unsigned array
 * holding the file data.
 * parameters:
 *    cipher_in - unsigned char array to free
 *    plain_in - char array to free 
 *    lock - lock object to free
 * returns: int on completion
 */
int free_files(unsigned char *cipher_in, char *plain_in, fileLock_p lock)
{
        free(cipher_in);
        free(plain_in);
        free_lock(lock);
        return 0;
}
