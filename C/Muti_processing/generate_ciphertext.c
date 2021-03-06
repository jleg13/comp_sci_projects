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
 * Encrypt '*len' bytes of plaintext
 * All data going in & out is considered binary (unsigned char[])
 */
unsigned char *aes_encrypt(EVP_CIPHER_CTX *e, unsigned char *plaintext, int *len)
{
  // Max ciphertext len for 'n' bytes of plaintext is n + AES_BLOCK_SIZE -1 bytes
  int c_len = *len + AES_BLOCK_SIZE, f_len = 0;
  unsigned char *ciphertext = malloc(c_len);

  // Allows reusing of 'e' for multiple encryption cycles
  EVP_EncryptInit_ex(e, NULL, NULL, NULL, NULL);

  // update ciphertext, c_len is filled with the length of ciphertext generated,
  // len is the size of plaintext in bytes
  EVP_EncryptUpdate(e, ciphertext, &c_len, plaintext, *len);

  // Update ciphertext with the final remaining bytes
  EVP_EncryptFinal_ex(e, ciphertext+c_len, &f_len);

  *len = c_len + f_len;
  return ciphertext;
}

int main(int argc, char **argv){

    /* "opaque" encryption, decryption ctx structures that libcrypto uses to record
    status of enc/dec operations */
    EVP_CIPHER_CTX* en = EVP_CIPHER_CTX_new();
    EVP_CIPHER_CTX* de = EVP_CIPHER_CTX_new();

    unsigned char *key_data;
    int key_data_len;
    int len, plen;
    unsigned char *ciphertext;

    //This is the plaintext file to be encoded
    FILE *myplainfile;    
    myplainfile=fopen("plain.txt","r");
    fseek(myplainfile, 0, SEEK_END);
    plen = ftell(myplainfile);
    rewind(myplainfile);
    char plainText[plen];
    fread(plainText, plen, 1, myplainfile);

    fclose(myplainfile);

    /* the key_data is read from the argument list */
    key_data = (unsigned char *)argv[1];
    key_data_len = strlen(argv[1]);

    if (aes_init(key_data, key_data_len,  en, de)) {
        printf("Couldn't initialize AES cipher\n");
        return -1;
    }

    /* The enc/dec functions deal with binary data and not C strings. strlen() will 
    return length of the string without counting the '\0' string marker. We always
    pass in the marker byte to the encrypt/decrypt functions so that after decryption 
    we end up with a legal C string */
    len = sizeof(plainText)/sizeof(plainText[0])+1;

    ciphertext = aes_encrypt(en, (unsigned char *)plainText, &len);
    
    //print cipher bytes to stdout;
    int cipher_length = len;
    int cp = 0;
    for(cp = 0;cp < cipher_length; cp++){
    	printf("%c", ciphertext[cp]); 
    }

    free(ciphertext);
    EVP_CIPHER_CTX_free(en);
    EVP_CIPHER_CTX_free(de);

    return 0;
}
