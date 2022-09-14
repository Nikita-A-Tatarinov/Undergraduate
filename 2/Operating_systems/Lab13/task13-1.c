#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main() {
    int fd;

    if ((fd = open("a0", O_CREAT | O_WRONLY, 0666)) < 0) {
        printf("error while creating file\n");
        exit(-1);
    }
    if (close(fd) < 0) {
        printf("error while closing file\n");
        exit(-1);
    }

    char from[15];
    char to[15];
    int i;
    for (i = 0;; i++) {
        sprintf(from, "a%d", i);
        sprintf(to, "a%d", i + 1);
        if (symlink(from, to) == -1) {
            printf("error while creating link\n");
            exit(-1);
        }
        if ((fd = open(to, O_WRONLY, 0666)) < 0) {
            break;
        }
        if (close(fd) < 0) {
            printf("error while closing link\n");
            exit(-1);
        }
    }
    printf("recursion depth = %d\n", i);
    return 0;
}