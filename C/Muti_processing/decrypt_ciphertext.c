#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <openssl/evp.h>
#include <openssl/aes.h>

/*
 * Initialize AES Cipher with key
 */
int aes_init(unsigned char *key_data, int key_data_len, EVP_CIPHER_CTX *e_ctx, EVP_CIPHER_CTX *d_ctx){

  int i;
  unsigned char key[32], iv[32];

  //Only use most significant 32 bytes of data if > 32 bytes
  if(key_data_len > 32) key_data_len = 32;

  //Copy bytes to the front of the key array
  for (i=0;i<key_data_len; i++){
     key[i] = key_data[i];
     iv[i] = key_data[i];
  }

  //Pad out to 32 bytes if key < 32 bytes
  for (i=key_data_len;i<32;i++){
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
 * Decrypt '*len' bytes of ciphertext
 * All data going in & out is considered binary (unsigned char[])
 */
unsigned char *aes_decrypt(EVP_CIPHER_CTX *e, unsigned char *ciphertext, int *len)
{
  // Plaintext length will always be equal to or less than length of ciphertext
  int p_len = *len, f_len = 0;
  unsigned char *plaintext = malloc(p_len);

  // Allows reusing of 'e' for multiple decryption cycles
  EVP_DecryptInit_ex(e, NULL, NULL, NULL, NULL);

  // Update plaintext, p_len is filled with the length of plaintext decrypted,
  // len is the size of ciphertext in bytes
  EVP_DecryptUpdate(e, plaintext, &p_len, ciphertext, *len);

  // Update plaintext with the final remaining bytes
  EVP_DecryptFinal_ex(e, plaintext+p_len, &f_len);

  return plaintext;
}

int main(int argc, char **argv){

    /* "opaque" encryption, decryption ctx structures that libcrypto uses to record
    status of enc/dec operations */
    EVP_CIPHER_CTX* en = EVP_CIPHER_CTX_new();
    EVP_CIPHER_CTX* de = EVP_CIPHER_CTX_new();

    unsigned char *key_data;
    int key_data_len;
    int len, clen;
    char *plaintext;

    //This is the ciphertext file to be decrypted
    FILE *myfile;
    myfile=fopen("cipher.txt","r");
    fseek(myfile, 0, SEEK_END);
    clen = ftell(myfile);
    rewind(myfile);
    unsigned char cipher_in[clen];
    fread(cipher_in, clen, 1, myfile);
    fclose(myfile);

    // the key_data is read from the argument list
    key_data = (unsigned char *)argv[1];
    key_data_len = strlen(argv[1]);

    printf("This is the key: %s \n", key_data);
    printf("It is %d bytes in length\n", key_data_len);   

    if (aes_init(key_data, key_data_len, en, de)) {
        printf("Couldn't initialize AES cipher\n");
        return -1;
    }

    printf("Ciphertext: %s\n", (char*)cipher_in);

    // Equivalent to strlen() for byte buffer(unsigned char array)
    len = sizeof(cipher_in)/sizeof(cipher_in[0]);
    plaintext = (char *)aes_decrypt(de, (unsigned char *)cipher_in, &len);
    printf("Plaintext: %s\n", plaintext);

    free(plaintext);
    EVP_CIPHER_CTX_free(en);
    EVP_CIPHER_CTX_free(de);

    return 0;
}
