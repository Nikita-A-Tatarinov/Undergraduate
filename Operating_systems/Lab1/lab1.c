#include <stdio.h>
#include <stdlib.h>

int main() {
    printf("UID: %d\nGID: %d\n", getuid(), getgid());
    return 0;
}
