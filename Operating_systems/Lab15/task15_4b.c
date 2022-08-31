#include <sys/types.h>
#include <signal.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h> 

char connected = 0;
char last_bit = 1;
char received = 0;

void send_bit(pid_t pid, char bit) {
    int signal;
    if (bit == 0) {
        signal = SIGUSR1;
    } else {
        signal = SIGUSR2;
    }
    if (kill(pid, signal) < 0) {
        printf("error while sending signal\n");
        exit(-1);
    }
}

void handler(int nsig) {
    char bit;
    if (nsig == SIGUSR1) {
        bit = 0;
    } else if (nsig == SIGUSR2) {
        bit = 1;
    }
    if (!connected && bit == 0) {
        connected = 1;
        return;
    }
    if (connected && !received) {
        last_bit = bit;
        received = 1;
        return;
    }
    printf("unexpected signal\n");
}

int main(void) {
    (void) signal(SIGUSR1, handler);
    (void) signal(SIGUSR2, handler);
    printf("receiver pid: %d\n", getpid());
    printf("input transmitter pid:");
    pid_t transmitter_pid;
    scanf("%d", &transmitter_pid);
    send_bit(transmitter_pid, 0);
    printf("waiting for transmitter\n");
    while(!connected);
    printf("connected: receiving message\n");
    int message = 0;
    for (int i = 0; i < 32; i++) {
        received = 0;
        send_bit(transmitter_pid, 1);
        while (!received);
        message |= (last_bit << i);
    }
    printf("received message: %d\n", message);
    return 0;
}