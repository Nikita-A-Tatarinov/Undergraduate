#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>

int main()
{
  int     *head;     // Pointer to head of shared memory
  char    *src;      // Pointer to shared memory content
  int     shmid;     // IPC descriptor for the shared memory
  char    pathname[] = "task7_1a.c"; // The file name used to generate the key.
                                 // A file with this name must exist in the current directory.
  //File content is to be shared.
  FILE *f = fopen(pathname, "r");
  if(f == NULL){
      printf("Can\'t open src\n");
      exit(-1);
  }
  fseek(f , 0 , SEEK_END);
  long file_size = ftell(f);
  rewind (f);

  key_t   key;       // IPC key
  //
  // Generate an IPC key from the file name 06-1a.c in the current directory
  // and the instance number of the shared memory 0.
  //
  if ((key = ftok(pathname,0)) < 0) {
    printf("Can\'t generate key\n");
    exit(-1);
  }

  //
  // Trying to create a shared memory exclusively for the generated key, that is,
  // if it already exists for this key, the system call will return a negative value.
  // The memory size is defined as the size of an array of three integer variables,
  // access rights 0666 - reading and writing are allowed for everyone.
  //
    if ((shmid = shmget(key, sizeof(int)+file_size*sizeof(char), 0666|IPC_CREAT|IPC_EXCL)) < 0) {
    //
    // In case of an error, try to determine: whether it arose due to the fact
    // that the shared memory segment already exists or for some other reason.
    //
    if (errno != EEXIST) {
      printf("Can\'t create shared memory\n");
      exit(-1);
    } else {
      //
      // If because the shared memory already exists,
      // try to get its IPC descriptor and,
      // if successful, reset the Initialization flag.
      //
        if ((shmid = shmget(key, sizeof(int)+file_size*sizeof(char), 0)) < 0) {
        printf("Can\'t find shared memory\n");
        exit(-1);
      }
    }
  }
  //
  // Try to map shared memory to the address space of the current process.
  // Note that for proper comparison we explicitly convert the value -1 to a pointer to an integer.
  //
  if ((head = (int *)shmat(shmid, NULL, 0)) == (int *)(-1)) {
    printf("Can't attach shared memory\n");
    exit(-1);
  }
  //Content starts where the head finishes.
  src = (char *)(head + 1);
  if(file_size != fread(src, sizeof(char), file_size, f)){
      printf("Can\'t read file\n");
      exit(-1);
  }

  fclose(f);
  if (shmdt(head) < 0) {
    printf("Can't detach shared memory\n");
    exit(-1);
  }
  return 0;
}
