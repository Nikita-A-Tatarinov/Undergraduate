#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main()
{
    int     fd;
    size_t  size;
    char    string[14];
    //
    // Try to open a file named myfile in the current directory for input operations only.
    // Access rights 0666 - read-write for all categories of users.
    //
    if ((fd = open("myfile", O_RDONLY, 0666)) < 0) {
        printf("Can\'t open file\n");
        exit(-1);
    }
    //
    // Try to read 14 bytes from the file to our array,
    // i.e. the string "Hello, world!" along with the end-of-line sign.
    //
    size = read(fd, string, 14);

    printf("%s", string);

    if (size < 0) {
        printf("Can\'t read string\n");
        exit(-1);
    }

    if (close(fd) < 0) {
        printf("Can\'t close file\n");
    }

    return 0;
}
