COMPILER = gcc
CFLAGS = -Wall -pedantic -g -pthread
COBJS = utility.o sem_ops.o

EXES = nordvik

all: ${EXES}

nordvik:  nordvik.c ${COBJS}
	${COMPILER} ${CFLAGS}  nordvik.c ${COBJS} -o nordvik

%.o: %.c %.h  makefile
	${COMPILER} ${CFLAGS} $< -c

clean:
	rm -f *~ *.o ${EXES}
