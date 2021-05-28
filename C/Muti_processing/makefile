COMPILER = gcc
OPENSSL = /usr/local/opt/openssl/include
OPENSSL2 = /usr/local/opt/openssl/lib
CFLAGS = -Wall -pedantic -g -lcrypto -I ${OPENSSL} -L ${OPENSSL2}
COBJS = file_lock.o mem.o utilities.o


EXES = generate_ciphertext decrypt_ciphertext search_keyspace parallel_search_keyspace

all: ${EXES}

generate_ciphertext: generate_ciphertext.c 
	${COMPILER} ${CFLAGS}  generate_ciphertext.c -o generate_ciphertext

decrypt_ciphertext: decrypt_ciphertext.c 
	${COMPILER} ${CFLAGS}  decrypt_ciphertext.c -o decrypt_ciphertext

search_keyspace: search_keyspace.c
	${COMPILER} ${CFLAGS}  search_keyspace.c -o search_keyspace

parallel_search_keyspace: parallel_search_keyspace.c ${COBJS}
	${COMPILER} ${CFLAGS}  parallel_search_keyspace.c ${COBJS} -o parallel_search_keyspace

%.o: %.c %.h  makefile
	${COMPILER} ${CFLAGS} $< -c
	

clean: 
	rm -f *~ *.o ${EXES}




