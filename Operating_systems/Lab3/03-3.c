#include <stdio.h>

int main(int argc, char *argv[], char *envp[]) {
    printf("Command line arguments:\n");
    for (int i = 0; i < argc; i++) {
        printf("\t%s\n", argv[i]);
    }
    printf("Environmental parameters:\n");
    for (int i = 0; envp[i] != NULL; i++) {
        printf("\t%s\n", envp[i]);
    }
}
