#include <sys/ipc.h>
#include <sys/sem.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int D(int semid, struct sembuf *buf, int n){
  buf->sem_op = -n;
  buf->sem_flg = 0;
  buf->sem_num = 0;
  return semop(semid, buf, 1);
}

int A(int semid, struct sembuf *buf, int n){
  buf->sem_op = n;
  buf->sem_flg = 0;
  buf->sem_num = 0;
  return semop(semid, buf, 1);
}

int Z(int semid, struct sembuf *buf) {
  buf->sem_op = 0;
  buf->sem_flg = 0;
  buf->sem_num = 0;
  return semop(semid, buf, 1);
}

int main()
{
  //Количество вводов/выводов.  
  int N;
  printf("Input N: ");
  scanf("%d", &N);

  int           semid;
  char          pathname[]="task09-3.c";
  key_t         key;
  struct sembuf mybuf;
  if ((key = ftok(pathname, 0)) < 0) {
    printf("Can\'t generate key\n");
    exit(-1);
  }
  if ((semid = semget(key, 1, 0666 | IPC_CREAT)) < 0) {
    printf("Can\'t create semaphore set\n");
    exit(-1);
  }

  int    fd[2], result;
  size_t size;
  char   resstring[14] = "Hello, world!";
  if (pipe(fd) < 0) {
    printf("Can\'t open pipe\n");
    exit(-1);
  }

  result = fork();

  //Изначально, значение семафора равно 0.
  //С помощью D(S, 1) блокируем дочерний процесс.
  //После записи через write() в родительском процессе
  //разблокируем дочерний процесс с помощью A(S, 2) и 
  //блокируем текущий с помощью. Дочерний процесс открывается 
  //(значение семафора равно 1 после разблокировки), происходит
  //чтение из pipe с помощью read() и запись в pipe с помощью write.
  //Закрываем дочерний поток с помощью D(S, 1), этим же открывая 
  //родительский поток. Если дочерний процесс продолжит выполнение, 
  //его будет ждать блок D(S, 1), описанный выше. Продолжается 
  //родительский процесс, в котором происходит чтение из pipe 
  //с помощью read. Дялее, мы вновь входим в циклы в обоих процессах 
  //с тем же значением семафора и с тем же состоянием проццессов 
  //дочерний процесс заблокирован с помощью D(S, 1), а в родительском 
  //процессе происходит запись в pipe с помощью write), то есть
  //все дальнейшие изменения происходят по уже описанному алгоритму.

  if (result < 0) {
    printf("Can\'t fork child\n");
    exit(-1);
  } else if (result > 0) {
    /* Parent process */
    int i;
    for (i = 0; i < N; i++){
      resstring[i % 14] = '0' + (i % 10);
      size = write(fd[1], resstring, 14);
      if (size != 14) {
        printf("Can\'t write all string to pipe\n");
        exit(-1);
      }
      printf("Parent write: %s\n", resstring);
      A(semid, &mybuf, 2);
      Z(semid, &mybuf);

      size = read(fd[0], resstring, 14);
      if (size != 14){
        printf("Can\'t read string\n");
        exit(-1);
      }
      printf("Parent read: %s\n", resstring);
    }

    if (close(fd[0]) < 0) {
      printf("parent: Can\'t close reading side of pipe\n");
      exit(-1);
    }
    if (close(fd[1]) < 0) {
      printf("parent: Can\'t close writing side of pipe\n"); 
      exit(-1);
    }

    printf("Parent exit\n");

  } else {
    /* Child process */
    int i;
    for (i = 0; i < N; i++){
      D(semid, &mybuf, 1);
      size = read(fd[0], resstring, 14);
      if(size != 14){
        printf("Can\'t read string\n");
        exit(-1);
      }
      printf("Child read: %s\n", resstring);

      resstring[i % 14] = '0' + ((i + 1) % 10);
      size = write(fd[1], resstring, 14);
      if (size != 14) {
        printf("Can\'t write all string to pipe\n");
        exit(-1);
      }
      printf("Child write: %s\n", resstring);
      D(semid, &mybuf, 1);
    }

    if (close(fd[0]) < 0) {
      printf("child: Can\'t close reading side of pipe\n");
      exit(-1);
    }
    if (close(fd[1]) < 0) {
      printf("child: Can\'t close writing side of pipe\n"); 
      exit(-1);
    }

    printf("Child exit\n");
  }

  return 0;
}