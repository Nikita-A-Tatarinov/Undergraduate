#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int main()
{
    int     fd1[2], fd2[2], result;

    size_t size1, size2;
    char  resstring1[14];
    char  resstring2[14];

    if (pipe(fd1) < 0) {
        printf("Can\'t open pipe 1\n");
        exit(-1);
    }
    if (pipe(fd2) < 0) {
        printf("Can\'t open pipe 2\n");
        exit(-1);
    }

    result = fork();

    if (result < 0) {
        printf("Can\'t fork child\n");
        exit(-1);
    } else if (result > 0) {

        /* Parent process */

        if (close(fd1[0]) < 0) {
            printf("parent: Can\'t close reading side of pipe 1\n"); exit(-1);
        }
        if (close(fd2[1]) < 0) {
            printf("parent: Can\'t close writing side of pipe 2\n"); exit(-1);
        }

        size1 = write(fd1[1], "Hello, world!", 14);
        size2  = read(fd2[0], resstring2, 14);

        if (size1 != 14) {
            printf("parent: Can\'t write all string to pipe 1\n");
            exit(-1);
        }
        if (size2 < 0) {
            printf("parent: Can\'t read string from pipe 2\n");
            exit(-1);
        }

        printf("Parent exit, resstring2:%s\n", resstring2);

        if (close(fd1[1]) < 0) {
            printf("parent: Can\'t close writing side of pipe \n"); exit(-1);
        }
        if (close(fd2[0]) < 0) {
            printf("parent: Can\'t close reading side of pipe 2\n"); exit(-1);
        }

    } else {

        /* Child process */

        if (close(fd1[1]) < 0) {
            printf("child: Can\'t close writing side of pipe 1\n"); exit(-1);
        }
        if (close(fd2[0]) < 0) {
            printf("child: Can\'t close reading side of pipe 2\n"); exit(-1);
        }

        size1 = read(fd1[0], resstring1, 14);
        size2 = write(fd2[1], "Hello, world!", 14);

        if (size1 < 0) {
            printf("child: Can\'t read string from pipe 1\n");
            exit(-1);
        }
        if (size2 != 14) {
            printf("child: Can\'t write all string to pipe 2\n");
            exit(-1);
        }

        printf("Child exit, resstring1:%s\n", resstring1);

        if (close(fd1[0]) < 0) {
            printf("child: Can\'t close reading side of pipe 1\n"); exit(-1);
        }
        if (close(fd2[1]) < 0) {
            printf("child: Can\'t close writing side of pipe 2\n"); exit(-1);
        }
    }

    return 0;
}
