/*******************************************************************************
 *  Name: Joshua Le Gresley 
 * 
 *  Purpose: This program uses Nvidia CUDA C to create a parallel 
 *  implementation of the Mandelbrot algorithm. It produces a bitmap image 
 *  which contains the fractal representation of the Mandelbrot set. Program
 *  variables can be set from the commandline and include resolution, width,
 *  height, xcenter and ycenter.
 *  The program utilises code sourced from UNE COSC330 lectures and practicals.
 *
 *  Desrciption: To compile use the provided makefile by running the 'make'
 *  command. To run the exectutable use the command in the format:
 *
 *     ./mandelbrot_parallel <resolution> <width> <height> <xcenter>
 *                                  <ycenter>"
 *
 *  resolution - is a double value for the 'zoom' level of the image.
 *  width - is an int value for the width of the image
 *  height - is an int value for the height of the image
 *  xcenter - is a double value for X Position of the fractal in the image 
 *             between 1 and -1
 *  ycenter - is a double value for Y Position of the fractal in the image 
 *             between 1 and -1
 ******************************************************************************/

#include "bmpfile.h"
#include <stdio.h>
/*Mandelbrot values*/
#define MAX_ITER 1000
/*Colour Values*/
#define COLOUR_DEPTH 255
#define COLOUR_MAX 240.0
#define GRADIENT_COLOUR_MAX 230.0

#define FILENAME "my_mandelbrot_fractal.bmp"

typedef struct {
        double resolution;
        int width;
        int height;
        int *x;
        int *y;
        double xcenter;
        double ycenter;
        int xoffset;
        int yoffset;
        rgb_pixel_t *image;
} Matrix;

/**
 * Computes the color gradiant
 * color: the output vector
 * x: the gradiant (beetween 0 and 360)
 * min and max: variation of the RGB channels (Move3D 0 -> 1)
 * Check wiki for more details on the colour science:
 * en.wikipedia.org/wiki/HSL_and_HSV
 */
__device__ void ground_color_mix(double *color, double x, double min,
                                 double max)
{
        /*
         * Red = 0
         * Green = 1
         * Blue = 2
         */
        double pos_slope = (max - min) / 60;
        double neg_slope = (min - max) / 60;

        if (x < 60) {
                color[0] = max;
                color[1] = pos_slope * x + min;
                color[2] = min;
                return;
        } else if (x < 120) {
                color[0] = neg_slope * x + 2.0 * max + min;
                color[1] = max;
                color[2] = min;
                return;
        } else if (x < 180) {
                color[0] = min;
                color[1] = max;
                color[2] = pos_slope * x - 2.0 * max + min;
                return;
        } else if (x < 240) {
                color[0] = min;
                color[1] = neg_slope * x + 4.0 * max + min;
                color[2] = max;
                return;
        } else if (x < 300) {
                color[0] = pos_slope * x - 4.0 * max + min;
                color[1] = min;
                color[2] = max;
                return;
        } else {
                color[0] = max;
                color[1] = min;
                color[2] = neg_slope * x + 6 * max;
                return;
        }
}

/**
 * CUDA Kernel Device code
 * Determines where the pixel isvreferencing in the mandelbrot set.
 * Then sets the pixel colour.
 */
__global__ void mandelbrot(Matrix mandel_grid)
{
        int i = blockDim.x * blockIdx.x + threadIdx.x;
        rgb_pixel_t pixel = {0, 0, 0, 0};

        if (i < mandel_grid.width * mandel_grid.height) {
                /* Determine where in the mandelbrot set, the pixel is
                referencing */

                double x = mandel_grid.xcenter +
                           (mandel_grid.xoffset + mandel_grid.x[i]) /
                               mandel_grid.resolution;
                double y = mandel_grid.ycenter +
                           (mandel_grid.yoffset - mandel_grid.y[i]) /
                               mandel_grid.resolution;

                /* Mandelbrot stuff */
                double a = 0;
                double b = 0;
                double aold = 0;
                double bold = 0;
                double zmagsqr = 0;
                int iter = 0;
                double x_col;
                double color[3];

                /* Check if the x,y coord are part of the mendelbrot set
                - refer to the algorithm */
                while (iter < MAX_ITER && zmagsqr <= 4.0) {
                        ++iter;
                        a = (aold * aold) - (bold * bold) + x;
                        b = 2.0 * aold * bold + y;

                        zmagsqr = a * a + b * b;

                        aold = a;
                        bold = b;
                }

                /* Generate the colour of the pixel from the iter value */
                /* You can mess around with the colour settings to use
                 * different gradients */
                /* Colour currently maps from royal blue to red */
                x_col = (COLOUR_MAX - ((((float)iter / ((float)MAX_ITER) *
                                         GRADIENT_COLOUR_MAX))));
                ground_color_mix(color, x_col, 1, COLOUR_DEPTH);
                pixel.red = color[0];
                pixel.green = color[1];
                pixel.blue = color[2];
                mandel_grid.image[i] = pixel;
        }
}

/*
 * Validates the command line arguments.
 * parameters:
 *   argc - An int for the number of command line args passed in
 *   *argv -  A pointer to char array storing the passed in command line args
 *   *res - An int pointer for zoom of the image
 *   *width - An int pointer for width of image
 *   *height - An int pointer for height of image
 *   *xcenter - A double pointer for image center, +- 1.0
 *   *ycenter - A double pointer for image center, +- 1.0
 * returns: 0 if command line args are valid, -1 if they are invalid
 */
int parse_args(int argc, char *argv[], double *res, int *width, int *height,
               double *xcenter, double *ycenter)
{
        if ((argc != 6) || (*res = atof(argv[1])) <= 0.0 ||
            (*width = atoi(argv[2])) <= 0.0 ||
            (*height = atoi(argv[3])) <= 0.0 ||
            (*xcenter = atof(argv[4])) > 1.0 ||
            (*ycenter = atof(argv[5])) >= 1.0 ||
            (*xcenter = atof(argv[4])) < -1.0 ||
            (*ycenter = atof(argv[5])) <= -1.0) {
                fprintf(stderr,
                        "Usage: %s <resolution> "
                        "<width> <height> <xcenter> <ycenter>\n",
                        argv[0]);
                return (-1);
        }
        return 0;
}

/**
 * Host code
 */
int main(int argc, char **argv)
{
        int i = 0, col, row, num_elements;
        /* Error code to check return values for CUDA calls */
        cudaError_t err = cudaSuccess;
        Matrix mandel_grid;
        bmpfile_t *bmp;

        /* get input parameters */
        if (parse_args(argc, argv, &mandel_grid.resolution, &mandel_grid.width,
                       &mandel_grid.height, &mandel_grid.xcenter,
                       &mandel_grid.ycenter) < 0) {
                exit(EXIT_FAILURE);
        }

        /* Allocate the host memory */
        num_elements = mandel_grid.width * mandel_grid.height;
        size_t size = num_elements * sizeof(rgb_pixel_t);
        mandel_grid.image = (rgb_pixel_t *)malloc(size);

        bmp = bmp_create(mandel_grid.width, mandel_grid.height, 32);

        size_t grid_size = num_elements * sizeof(int);
        mandel_grid.x = (int *)malloc(grid_size);
        mandel_grid.y = (int *)malloc(grid_size);

        /* Verify that allocation succeeded */
        if (mandel_grid.image == NULL || bmp == NULL || mandel_grid.x == NULL 
                || mandel_grid.y == NULL) {
                fprintf(stderr, "Failed to allocate host memory!\n");
                exit(EXIT_FAILURE);
        }

        /* determine pixel grid position */
        for (col = 0; col < mandel_grid.width; col++) {
                for (row = 0; row < mandel_grid.height; row++) {
                        mandel_grid.x[col * mandel_grid.height + row] = col;
                        mandel_grid.y[col * mandel_grid.height + row] = row;
                }
        }

        /* calculate offset */
        mandel_grid.xoffset = -(mandel_grid.width - 1) / 2;
        mandel_grid.yoffset = (mandel_grid.height - 1) / 2;

        /* Now we will need to copy everything over to the GPU*/
        /* Allocate the device memory */
        Matrix d_mandel_grid;
        d_mandel_grid.resolution = mandel_grid.resolution;
        d_mandel_grid.width = mandel_grid.width;
        d_mandel_grid.height = mandel_grid.height;
        d_mandel_grid.xcenter = mandel_grid.xcenter;
        d_mandel_grid.ycenter = mandel_grid.ycenter;
        d_mandel_grid.xoffset = mandel_grid.xoffset;
        d_mandel_grid.yoffset = mandel_grid.yoffset;

        err = cudaMalloc(&d_mandel_grid.image, size);
        if (err != cudaSuccess) {
                fprintf(
                    stderr,
                    "Failed to allocate device image matrix "
                    "(error code %s)!\n",
                    cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        err = cudaMalloc(&d_mandel_grid.x, grid_size);
        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to allocate device x coordinates "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        err = cudaMalloc(&d_mandel_grid.y, grid_size);
        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to allocate device y coordinate matrix "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        /* copy memory to cuda device */
        err = cudaMemcpy(d_mandel_grid.image, mandel_grid.image, size,
                         cudaMemcpyHostToDevice);

        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to copy image from host to device "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        err = cudaMemcpy(d_mandel_grid.x, mandel_grid.x, grid_size,
                         cudaMemcpyHostToDevice);

        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to copy x coordinates from host to device "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        err = cudaMemcpy(d_mandel_grid.y, mandel_grid.y, grid_size,
                         cudaMemcpyHostToDevice);

        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to copy y coordinates from host to device "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        /* Launch the mandelbrot CUDA Kernel */
        int threadsPerBlock = 256;
        int blocksPerGrid =
            (num_elements + threadsPerBlock - 1) / threadsPerBlock;
        printf("CUDA kernel launch with %d blocks of %d threads\n",
               blocksPerGrid, threadsPerBlock);

        mandelbrot<<<blocksPerGrid, threadsPerBlock>>>(d_mandel_grid);
        err = cudaGetLastError();

        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to launch mandelbrot kernel "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        /* copy memory back from device */
        err = cudaMemcpy(mandel_grid.image, d_mandel_grid.image, size,
                         cudaMemcpyDeviceToHost);

        if (err != cudaSuccess) {
                fprintf(stderr,
                        "Failed to copy image from host to device "
                        "(error code %s)!\n",
                        cudaGetErrorString(err));
                exit(EXIT_FAILURE);
        }

        /* set pixels from device calculations in bmp file */
        for (i = 0; i < num_elements; i++) {
                bmp_set_pixel(bmp, mandel_grid.x[i], mandel_grid.y[i],
                              mandel_grid.image[i]);
        }

        bmp_save(bmp, FILENAME);
        // Free device memory
        cudaFree(d_mandel_grid.image);
        cudaFree(d_mandel_grid.x);
        cudaFree(d_mandel_grid.y);
        free(mandel_grid.image);
        free(mandel_grid.x);
        free(mandel_grid.y);
        bmp_destroy(bmp);

        return 0;
}
