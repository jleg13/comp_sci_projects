COMPILER = nvcc
CFLAGS = -I /usr/local/cuda-11.0/samples/common/inc
COBJS = bmpfile.o
EXES =  mandelbrot_parallel
CFILES = my_mandelbrot_fractal.bmp
all: ${EXES}

mandelbrot_parallel: mandelbrot_parallel.cu ${COBJS}
	${COMPILER} ${CFLAGS} mandelbrot_parallel.cu ${COBJS} -o mandelbrot_parallel

%.o: %.c %.h  makefile
	${COMPILER} ${CFLAGS} $< -c 

clean:
	rm -f *.o *~ ${EXES} ${CFILES}
