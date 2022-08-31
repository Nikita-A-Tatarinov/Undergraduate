#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>

int main() {
    pid_t pid = fork();
    if (pid == -1) {
        printf("An error took place\n");
    } else if (pid == 0) {
        printf("New process with ID %d. Parent process ID: %d.\n", getpid(), getppid());
    } else {
        printf("Old process with ID %d. New child process ID: %d.\n", getpid(), pid);
        exit(0);
    }
    return 0;
}
