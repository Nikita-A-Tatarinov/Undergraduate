#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include<fcntl.h>

int main() {
    int fd[2];
    //
    // Try to create a pipe
    //
    if (pipe(fd) < 0) {
        printf("Can\'t open pipe\n");
        exit(-1);
    }

    //Флаг, чтобы pipe не блокировался.
    fcntl(fd[1], F_SETFL, O_NONBLOCK);

    ssize_t size = 1, pipe_size = -1;
    for(; size == 1; pipe_size++, size = write(fd[1], "0", 1));

    printf("Pipe size: %zd", pipe_size);

    return 0;
}
