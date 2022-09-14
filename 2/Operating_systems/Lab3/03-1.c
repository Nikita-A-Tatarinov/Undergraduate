#include <stdio.h>
#include <unistd.h>

int main() {
    printf("Current process ID: %d\nParent process ID: %d\n", getpid(), getppid());
    return 0;
}
