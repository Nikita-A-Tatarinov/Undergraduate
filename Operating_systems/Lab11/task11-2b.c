#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

int main(void)
{
  int msqid;
  char pathname[]="task11-1a.c";
  key_t  key;
  int i, len, maxlen;

  struct mymsgbuf
  {
    long mtype;
    struct {
      short sinfo;
      float finfo;
    } info;
  } mybuf;
  maxlen = sizeof(mybuf.info);

  if ((key = ftok(pathname,0)) < 0) {
    printf("Can\'t generate key\n");
    exit(-1);
  }

  if ((msqid = msgget(key, 0666 | IPC_CREAT)) < 0) {
    printf("Can\'t get msqid\n");
    exit(-1);
  }

  while (1) {
    //
    // In an infinite loop, accept messages of any type in FIFO order
    // until a message of type LAST_MESSAGE is received.
    //
    if (( len = msgrcv(msqid, (struct msgbuf *) &mybuf, maxlen, 1, 0)) < 0) {
      printf("Can\'t receive message from queue\n");
      exit(-1);
    }
    //
    // If the received message has zero length,
    // then finish receiving messages and start sending them.
    // Otherwise, print the text of the received message.
    //
    else if (len == 0) {
      printf("task2b received the last message of type 1 and is ready to send messages\n");
      break;
    }

    printf("task2b received message of type 1 with info: sinfo = %d, finfo = %f\n", 
           mybuf.info.sinfo, mybuf.info.finfo);
  }

  /* Send information */

  for (i = 1; i <= 5; i++) {
    //
    // Fill in the structure for the message.
    //
    mybuf.mtype = 2;
    mybuf.info.sinfo = 5 * i;
    mybuf.info.finfo = 5.0 * (float)i / 7.0;
    //
    // Send the message. If there is an error,
    // report it and delete the message queue from the system.
    //
    if (msgsnd(msqid, (struct msgbuf *) &mybuf, maxlen, 0) < 0) {
      printf("Can\'t send message to queue\n");
      msgctl(msqid, IPC_RMID, (struct msqid_ds *) NULL);
      exit(-1);
    }
    printf("task2b sent message of type 2 with info: sinfo = %d, finfo = %f\n", 
           mybuf.info.sinfo, mybuf.info.finfo);
  }

  /* Send the last message */
  len = 0;

  if (msgsnd(msqid, (struct msgbuf *) &mybuf, len, 0) < 0) {
    printf("Can\'t send message to queue\n");
    msgctl(msqid, IPC_RMID, (struct msqid_ds *) NULL);
    exit(-1);
  }
  printf("task2a sent the last message of type 2 and is ready to finish\n");

  return 0;
}
