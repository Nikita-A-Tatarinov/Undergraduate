format PE console
entry start

include 'win32a.inc'

section '.data' data readable writable

        strVecSize     db 'size of vector? ', 0
        strIncorSize   db 'Incorrect size of vector = %d', 10, 0
        strVecElemI    db 'Element %d: ', 0
        strScanInt     db '%d', 0
        strVecElemOut  db '[%d] = %d', 10, 0

        vec_size       dd 0 ; размер входного массива (в процессе уменьшается на 1 и получается размер выходного массива)
        in_vec         rd 100 ; входной массив
        out_vec        rd 100 ; выходной массив
        tmp            dd ? ; переменная для запоминаниия адреса на элементы массивов в процессе работы с ними
        i              dd ? ; счётчик

section '.code' code readable executable

; начало программы
start:
        ; вызов процедуры для ввода размера массива
        call inputVecSize
        ; вызов процедуры для ввода входного массива
        call inputVec
        ; вызов процедуры для формирования выходного массива
        call generateOutVec
        ; вызов процедуры для вывода выходного массива
        call outputVec
finish:
        call [getch]
push 0
        call [ExitProcess]

; процедура ввода размера входного массива
inputVecSize:
        ; строку в стек
        push strVecSize
        ; выводим данные из стека в консоль
        call [printf]
        ; очищаем стек (для корректной работы процедур)
        add esp, 4

        ; адрес размера массива в стек для считывания с консоли
        push vec_size
        ; строка в стек
        push strScanInt
        ; считываем размер массива с консоли в vec_size
        call [scanf]
        ; очищаем стек
        add esp, 4
        add esp, 4

        ; копируем значение размера массива в eax
        mov eax, [vec_size]
        ; вычитаем из eax 1 (результат возвращается в виде флага, в отличие от sub)
        cmp eax, 1
        ; если результат меньше либо равен 0, то прыгаем в метку wrongInput
        jle wrongInput
        ; вычитаем из eax 100
        cmp eax, 100
        ; если результат больше 0, то прыгаем в метку wringInput
        jg wrongInput
        jmp endInputVecSize
; если введённый размер стека оказался некорректным
wrongInput:
        ; адрес размер массива в стек для вывода
        push vec_size
        ; строку в стек
        push strIncorSize
        ; выводим данные из стека в консоль
        call [printf]
        ; очищаем стек
        add esp, 4
        add esp, 4
        ; прыгаем в метку окончания программы
        jmp finish
; окончание ввода размера
endInputVecSize:
        ret

; процедура ввода массива
inputVec:
        ; обнуляем регистр ecx
        xor ecx, ecx
        ; копируем в регистр ebx адрес входного массива
        mov ebx, in_vec
; цикл ввода входного массива
inVecLoop:
        ; копируем регистр ebx в значение tmp
        mov [tmp], ebx
        ; вычитаем из ecx значение размера массива
        cmp ecx, [vec_size]
        ; если результат больше либо равен 0, то прыгаем в метку endInputVec
        jge endInputVec

        ; копируем значение из регистра ecx  в значение счётчика
        mov [i], ecx
        ; регистр ecx  в стек
        push ecx
        ; строку в стек
        push strVecElemI
        ; выводим данные из стека в консоль
        call [printf]
        ; очищаем стек
        add esp, 4
        add esp, 4

        ; регистр ebx в стек
        push ebx
        ; строку в стек
        push strScanInt
        ; считываем значение очередного элемента массива
        call [scanf]
        ; очищаем стек
        add esp, 4
        add esp, 4

        ;копируем значение счётчика в ecx
        mov ecx, [i]
        ; инкрементируем значение регистра
        inc ecx
        ; копируем значение tmp в регистр
        mov ebx, [tmp]
        ; смещаем адрес переменной на 4 байта вправо (получаем адрес следующего элемента)
        add ebx, 4
        ; прыгаем в метку цикла
        jmp inVecLoop
; окончание ввода массива
endInputVec:
        ret

; процедура фомирования выхоного массива
generateOutVec:
        ; очищаем регистр eax
        xor eax, eax
        ; очищаем регистр ecx
        xor ecx, ecx
        ; копируем адрес входного массива в регистр ebx
        mov ebx, in_vec
        ; копируем адрес выходного массива в регистр edx
        mov edx, out_vec
        ; декриментируем значение входного массива, получая значение выходного массива
        dec [vec_size]
; цикл формирования выходного массива
outVecLoop:
        ; вычитаем из регистра ecx значение размера выходного массива
        cmp ecx, [vec_size]
        ; если результат больше либо равен 0, то выходим из цикла формирования массива
        jge endOutVector
        ; копируем значение очередного элемента в eax
        mov eax, [ebx + ecx * 4]
        ; прибавляем к регистру значение следующего элемента
        add eax, [ebx + (ecx + 1) * 4]
        ; регистр eax  в стек
        push eax
        ; значение из стека сохраняем в очередной элемент выходного массива
        pop dword [edx + ecx * 4]
        ; инкрементируем регистр eax
        inc ecx
        ; прыгаем в метку цикла
        jmp outVecLoop
; окончание формирования выходного массива
endOutVector:
        ret

; процедура вывода массива
outputVec:
        ; очищаем регистр ecx
        xor ecx, ecx
        ; копируем адрес первого элемента выходного массива в регистр edx
        mov ebx, out_vec
; цикл вывода массива
outputVecLoop:
        ; копируем значение из регистра ebx d значение tmp
        mov [tmp], ebx
        ; вычитаем из регистра ecx значение размера выходного массива
        cmp ecx, [vec_size]
        ; если рещультат больше либо равен 0, то прыгаем в метку окончания вывода
        jge endOutputVec
        ; копируем значение регистра ecx в значение счётчика
        mov [i], ecx

        ; значчение регистра ebx в стек
        push dword [ebx]
        ; значение счётчика в стек
        push ecx
        ; строку в стек
        push strVecElemOut
        ; выводим данные из стека в консоль
        call [printf]
        ; очищаем стек
        add esp, 4
        add esp, 4
        add esp, 4

        ; копируем значение счётчика в регистр ecx
        mov ecx, [i]
        ; инкрементируем регистр ecx
        inc ecx
        ; копируем значение tmp в регистр ebx
        mov ebx, [tmp]
        ; прибавляем к регистру ebx 4
        add ebx, 4
        ; прыгаем в метку цикла
        jmp outputVecLoop
; окончание вывода массива
endOutputVec:
        ret






;-------------------------------third act - including HeapApi------------------------—

section '.idata' import data readable
library kernel, 'kernel32.dll',\
msvcrt, 'msvcrt.dll',\
user32,'USER32.DLL'

include 'api\user32.inc'
include 'api\kernel32.inc'
import kernel,\
ExitProcess, 'ExitProcess',\
HeapCreate,'HeapCreate',\
HeapAlloc,'HeapAlloc'
include 'api\kernel32.inc'
import msvcrt,\
printf, 'printf',\
scanf, 'scanf',\
getch, '_getch'