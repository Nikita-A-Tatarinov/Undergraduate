#include <signal.h>

int main(void) {
  (void) signal(SIGINT, SIG_IGN);
  //добавляем игнорирование сигнала ctrl+4
  (void) signal(SIGQUIT, SIG_IGN);
  while(1);
  return 0;
}
