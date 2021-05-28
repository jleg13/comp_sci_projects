COMPILER = mpicc
CFLAGS = -Wall -pedantic -g 
COBJS = utility.o matrix.o process_matrix.o

EXES = mkRandomMatrix getMatrix convol_filter

all: ${EXES}

convol_filter:	convol_filter.c ${COBJS}
	${COMPILER} ${CFLAGS}  convol_filter.c ${COBJS} -o convol_filter

getMatrix:	getMatrix.c ${COBJS}
	${COMPILER} ${CFLAGS} getMatrix.c ${COBJS} -o getMatrix

mkRandomMatrix:    mkRandomMatrix.c  ${COBJS}
	${COMPILER} ${CFLAGS} mkRandomMatrix.c ${COBJS} -o mkRandomMatrix

%.o: %.c %.h  makefile
	${COMPILER} ${CFLAGS} $< -c

clean:
	rm -f *~ *.o ${EXES}