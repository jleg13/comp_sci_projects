/*******************************************************************************
 *  Name: Joshua Le Gresley 
 *  Purpose: A multiprocess program utilising shared memory for inter-process
 *  communication. This program carries out a partial key search to
 *  establish the original full encyption key used to generate a piece of
 *  cipher-text. Locking is used to ensure processes have exclusive access to
 *  write the full encyption key to shared memory (if found).
 *  Desrciption: To compile use the provided makefile by running the 'make'
 *  command. To run the exectutable use the command in the format:
 *
 *      parallel_search_keyspace <num. procs.> <partial key>
 *
 *  num. procs : Is the number of parallel processes to be forked
 *  partial key: Is the known part of the full 256 bit encyption key
 *
 *  The program requires two files in its path named:
 *
 *  cipher.txt - Contains the cipher text generated on the encyption
 *  plain.txt - Contains the message that was encypted
 ******************************************************************************/

#include "file_lock.h"
#include "mem.h"
#include "utilities.h"
#include <errno.h>
#include <openssl/aes.h>
#include <openssl/evp.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>
#include <math.h>

#define SUCCESS 1
#define FAILURE 0
#define KEYLEN 32

typedef struct shared {
        unsigned long counter;
        unsigned char trialkey[KEYLEN];
} shared_t;

/*
 *  Uses partial key from command line args and conditions the given value
 *  by shortening or padding with zeros to be 32 characters long.
 *  parameters:
 *      *key_len - An int pointer for length of partial key
 *      p_key - A char array storing the passed in command line args
 *      *k, *i, *t - Are unsigned array pointers to the store copys of the
 *              partial key for the decyption process
 * returns: an unsigned char array storing the partial key
 */
int condition_p_key(int *key_len, int *trial_len, char p_key[],
                    unsigned char *k, unsigned char *t)
{
        int j;
        unsigned char *key_data;
        key_data = (unsigned char *)p_key;
        *key_len = strlen((char *)key_data);
        *trial_len = KEYLEN;
        // Only use most significant 32 bytes of data if > 32 bytes
        if (*key_len > KEYLEN) {
                *key_len = KEYLEN;
        }
        // Copy bytes to the front of the key array
        for (j = 0; j < *key_len; j++) {
                k[j] = key_data[j];
                if (t != NULL)
                        t[j] = key_data[j];
        }

        // If the key data < 32 bytes, pad the remaining bytes with 0s
        for (j = *key_len; j < KEYLEN; j++) {
                k[j] = 0;
                if (t != NULL)
                        t[j] = 0;
        }
        return SUCCESS;
}

/*
 * Initialize AES Cipher with key
 * parameters:
 *   key_data - unsigned char array holding the original partial key
 *   key_data_len - int value for the length of the key_data array
 *   *e_ctx - SSL encryption related object
 *   *d_ctx - SSL decryption related object
 * returns:
 */
int aes_init(unsigned char *key_data, int key_data_len, EVP_CIPHER_CTX *e_ctx,
             EVP_CIPHER_CTX *d_ctx)
{
        int i;
        unsigned char key[32], iv[32];

        if (key_data_len > KEYLEN)
                key_data_len = KEYLEN;

        for (i = 0; i < key_data_len; i++) {
                key[i] = key_data[i];
                iv[i] = key_data[i];
        }
        for (i = key_data_len; i < KEYLEN; i++) {
                key[i] = 0;
                iv[i] = 0;
        }

        EVP_CIPHER_CTX_init(e_ctx);
        EVP_EncryptInit_ex(e_ctx, EVP_aes_256_cbc(), NULL, key, iv);
        EVP_CIPHER_CTX_init(d_ctx);
        EVP_DecryptInit_ex(d_ctx, EVP_aes_256_cbc(), NULL, key, iv);

        return 0;
}

/*
 * Decrypt *len bytes of ciphertext
 * All data going in & out is considered binary (unsigned char[])
 * parameters:
 *   *e - SSL encryption related object
 *   *ciphertext - unsigned char pointer to copy of conditioned partial key
 *   *len - int pointer to conditioned partial key
 * returns:
 */
unsigned char *aes_decrypt(EVP_CIPHER_CTX *e, unsigned char *ciphertext,
                           int *len)
{
        // plaintext always <= length of ciphertext
        int p_len = *len, f_len = 0;
        unsigned char *plaintext = malloc(p_len);

        EVP_DecryptInit_ex(e, NULL, NULL, NULL, NULL);
        EVP_DecryptUpdate(e, plaintext, &p_len, ciphertext, *len);
        EVP_DecryptFinal_ex(e, plaintext + p_len, &f_len);

        return plaintext;
}

int main(int argc, char *argv[])
{
        int fd, i, nproc, lval, key_data_len, cipher_length, plain_length,
            status, trial_key_length;
        FILE *mycipherfile = NULL;
        FILE *myplainfile = NULL;
        unsigned long maxSpace;
        pid_t childpid, wpid;
        char *plaintext, *plain_in;
        unsigned char *cipher_in;
        void *start_addr;
        unsigned char key[KEYLEN], trialkey[KEYLEN];

        // Define shared memory lock
        size_t size = sizeof(shared_t);
        shared_t *shared;
        fileLock_p lock = make_lock();
        if (lock == NULL)
                exit(EXIT_FAILURE);

        // Parse args
        if (!parse_args(argc, argv, &nproc)) {
                printf("Usage: parallel_search_keyspace <num. procs.> <partial "
                       "key>\n");
                exit(EXIT_FAILURE);
        }
        // Map Shared memory
        if (init_shared_file(&fd, "sharedFile", size) != 0) {
                exit(EXIT_FAILURE);
        }
        // map it to memory
        start_addr = mmap(0, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
        if (start_addr == MAP_FAILED) {
                perror("mmap failed");
                exit(EXIT_FAILURE);
        }
        // zero the memory
        memset(start_addr, 0, size);
        shared = start_addr;

        // read in files and display contents
        if ((myplainfile = fopen("plain.txt", "r")) == NULL) {
                perror("Unable to access required file");
                free_lock(lock);
                exit(EXIT_FAILURE);
        }

        plain_in = (char *)read_file(myplainfile, &plain_length, "\nPlain");

        if ((mycipherfile = fopen("cipher.txt", "r")) == NULL) {
                perror("Unable to access required file");
                free_lock(lock);
                free(plain_in);
                exit(EXIT_FAILURE);
        }
        cipher_in = (unsigned char *)read_file(mycipherfile, &cipher_length,
                                               "\nCiphertext");

        // process patial key
        condition_p_key(&key_data_len, &trial_key_length, argv[2], key,
                        trialkey);

        unsigned long keyLowBits = 0;
        keyLowBits = ((unsigned long)(key[24] & 0xFFFF) << 56) |
                     ((unsigned long)(key[25] & 0xFFFF) << 48) |
                     ((unsigned long)(key[26] & 0xFFFF) << 40) |
                     ((unsigned long)(key[27] & 0xFFFF) << 32) |
                     ((unsigned long)(key[28] & 0xFFFF) << 24) |
                     ((unsigned long)(key[29] & 0xFFFF) << 16) |
                     ((unsigned long)(key[30] & 0xFFFF) << 8);

        // define search space
        maxSpace =
            ((unsigned long)1 << ((trial_key_length - key_data_len) * 8)) - 1;

        // Fork Processes
        for (i = 1; i <= nproc; ++i) {

                if ((childpid = fork()) < 0) {

                        perror("error in fork");
                        free_files(cipher_in, plain_in, lock);
                        exit(EXIT_FAILURE);

                } else if (childpid && i == nproc) {
                        
                        while ((wpid = wait(&status)) > 0) {
                        }

                        lval = get_lock(lock, fd);
                        if (lval == -1) {
                                printf("%s: dvisory locking failed -- %s",
                                       argv[0], strerror(errno));
                                free_files(cipher_in, plain_in, lock);
                                exit(EXIT_FAILURE);
                        }
                        if (shared->counter == 0) {
                                printf("Encryption key not Found\n");
                                un_lock(lock, fd);
                                free_files(cipher_in, plain_in, lock);
                                exit(EXIT_FAILURE);
                        } else {
                                display_message("\nOK: enc/dec ok for",
                                                (unsigned char *)plain_in,
                                                plain_length);
                                //Finds number of digits in process number for str len                
                                int len = floor (log10 (abs ((int)shared->counter))) + 1;              
                                char counter_str[len];
                                sprintf(counter_str, "%lu", shared->counter);
                                display_message("Process no.",
                                                (unsigned char *)counter_str,
                                                strlen(counter_str));
                                display_message("Full Key", shared->trialkey,
                                                trial_key_length);
                        }
                        un_lock(lock, fd);
                        free_files(cipher_in, plain_in, lock);
                        exit(EXIT_SUCCESS);

                } else if (childpid == 0) {
                        break;
                }
        }
        // define entry point for search space in child process
        unsigned long counter = (maxSpace / nproc) * (i - 1);

        for (; counter <= ((maxSpace / nproc) * i); counter++) {

                if (shared->counter == 0) {

                        unsigned long trialLowBits = keyLowBits | counter;

                        trialkey[25] = (unsigned char)(trialLowBits >> 48);
                        trialkey[26] = (unsigned char)(trialLowBits >> 40);
                        trialkey[27] = (unsigned char)(trialLowBits >> 32);
                        trialkey[28] = (unsigned char)(trialLowBits >> 24);
                        trialkey[29] = (unsigned char)(trialLowBits >> 16);
                        trialkey[30] = (unsigned char)(trialLowBits >> 8);
                        trialkey[31] = (unsigned char)(trialLowBits);

                        EVP_CIPHER_CTX *en = EVP_CIPHER_CTX_new();
                        EVP_CIPHER_CTX
                        *de = EVP_CIPHER_CTX_new();

                        if (aes_init(trialkey, trial_key_length, en, de)) {
                                perror("Couldn't initialize "
                                       "AES cipher\n");
                                free_files(cipher_in, plain_in, lock);
                                exit(EXIT_FAILURE);
                        }
                        // Test permutation
                        plaintext = (char *)aes_decrypt(
                            de, (unsigned char *)cipher_in, &cipher_length);

                        // Cleanup Cipher Allocated memory
                        EVP_CIPHER_CTX_cleanup(en);
                        EVP_CIPHER_CTX_cleanup(de);
                        EVP_CIPHER_CTX_free(en);
                        EVP_CIPHER_CTX_free(de);

                        if (strncmp(plaintext, plain_in, plain_length) == 0) {

                                lval = get_lock(lock, fd);
                                if (lval == -1) {
                                        printf("%s:dvisory locking failed"
                                               "-- %s",
                                               argv[0], strerror(errno));
                                        free(plaintext);
                                        free_files(cipher_in, plain_in, lock);
                                        exit(EXIT_FAILURE);
                                }
                                shared->counter = counter;
                                memcpy(shared->trialkey, trialkey,
                                       trial_key_length);

                                un_lock(lock, fd);
                                free(plaintext);
                                free_files(cipher_in, plain_in, lock);
                                exit(EXIT_SUCCESS);
                        }

                } else {
        
                        free_files(cipher_in, plain_in, lock);
                        exit(EXIT_SUCCESS);
                }

                free(plaintext);
        }
        exit(EXIT_FAILURE);
}
