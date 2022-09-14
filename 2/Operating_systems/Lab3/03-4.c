
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>

int main(int argc, char *argv[], char *envp[]) {
    pid_t pid = fork();
    if (pid == -1) {
        printf("An error took place\n");
    } else if (pid == 0) {
        printf("New process with ID %d. Parent process ID: %d.\n", getpid(), getppid());
        (void) execle("/bin/cat", "/bin/cat", "03-4.c", 0, envp);
        printf("Error on program start\n");
        exit(-1);
    } else {
        printf("Old process with ID %d. New child process ID: %d.\n", getpid(), pid);
    }
    return 0;
}
