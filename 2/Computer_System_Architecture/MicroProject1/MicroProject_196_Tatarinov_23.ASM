format PE console
entry start

include 'win32a.inc'

section '.data' data readable writable

        nFactorialStr db '%d! = %d', 10, 0
        nIsTooHugeStr db '%d! is too huge for double machine word', 10, 0

        n             dd 1

section '.code' code readable executable

; Начало программы (а также цикл по n).
start:
        ; Добавляем число, факториал которого
        ; необходимо посчитать, в стек - в
        ; качестве параметра для процедуры.
        push dword[n]
        ; Вызываем саму процедуру подсчёта
        ; факториала.
        call factorialProc
        ; Процедура возвращает значение через
        ; регистр eax. Если данные некорректны
        ; (что невозможно данных условиях), то
        ; вернётся -1. Если переполнение, то 0.
        ; В противном случае факториал числа.
        ; В данной ситуации, достаточно сравнить
        ; eax с 0.
        cmp eax, 0
        jne isSuitable
        jmp isNotSuitable
; Метка вывода факториала числа (помещается в
; двойное машинное слово).
isSuitable:
        ; Добавляем в стек значение факториала n,
        ; число n и информативную строку.
        push eax
        push [n]
        push nFactorialStr
        ; Выводим в консоль.
        call [printf]
        ; Очищаем стек.
        add esp, 12
        ; Увеличиваем значение n.
        mov ebx, [n]
        inc ebx
        mov [n], ebx
        ; Возвращаемся к началу программы.
        jmp start
; Метка вывода числа, факториал которого не
; помещается в двойное машинное слово.
isNotSuitable:
        ; Добавляем в стек число n и
        ; информативню строку.
        push [n]
        push nIsTooHugeStr
        ; Выводим в консоль.
        call [printf]
        ; Очищаем стек.
        add esp, 8
; Метка окончания программы.
finish:
        call [getch]
        push 0
        call [ExitProcess]

; Процедура вычисления факториала числа.
; Принимает через стек 1 параметр - число n,
; факториал которого необходимо вычислить.
; Результат возращается в регистре eax.
; Если результат не вмещается в двойное
; машинное слово, то в eax помещается 0.
; Если в качетсве параметра передано
; некорректное значение, то в eax
; помещается -1.
factorialProc:
        ; Помещаем в стек регистр ebp, который
        ; принято использовать для обращения к
        ; параметрам внутри процедуры.
        push ebp
        ; Копируем значение регистра esp в
        ; регистр ebp, тем самым запоминаем
        ; в нём положение вершины стека для
        ; возможности адресовать параметры
        ; относительно регистра ebp.
        mov ebp, esp
        ; Передано число, меньшее 1
        ; (некорректные данные).
        cmp dword[ebp+8], 0
        jle wrongInput
        ; Обнуляем значение регистра ebx,
        ; в котором будем хранить значения
        ; результатов вычислений.
        xor ebx, ebx
        ; Делаем его равным единице для
        ; последующего пошагового домножения.
        inc ebx
; Цикл подсчёта факториала.
factorialLoop:
        ; Очищаем регистры eax и edx на каждой
        ; итерации цикла.
        xor eax, eax
        xor edx, edx
        ; Копируем значение числа, факториал
        ; которого нужно вычислить в регистр eax.
        mov eax, dword[ebp+8]
        ; Домножаем регистр eax на сохранённый
        ; ранее результат.
        mul ebx
        ; В результате работы mul верхняя часть
        ; результата сохраняется в регистр edx,
        ; а нижняя - в регистр eax. Значит, если
        ; регистр edx не равен 0, то значение
        ; факториала числа не помещается в
        ; двойное машинное слово. В таком случае
        ; возвращащется 0.
        cmp edx, 0
        jne tooHugeNum
        ; Если же edx равен 0, то используем
        ; данный регистр для перезаписи данных.
        mov edx, dword[ebp+8]
        dec edx
        mov dword[ebp+8], edx
        ; Если edx равен 0, то факториал посчитан.
        cmp edx, 0
        ; В таком случае, заканчиваем работу.
        je finishFactorialProc
        ; В противном случае, запоминаем в ebx
        ; текущее значение.
        mov ebx, eax
        ; Прыгаем на метку цикла.
        jmp factorialLoop
; Метека некорректных данных.
wrongInput:
        ; Обнуляем регистр eax.
        xor eax, eax
        ; Превращаем eax в -1.
        dec eax
        ; Прыгаем на шаг окончания работы.
        jmp finishFactorialProc
; Метка переполнения.
tooHugeNum:
        ; Обнуляем eax, так как переполнение.
        xor eax, eax
; Метка окончания работы.
finishFactorialProc:
        ; Убираем из стека регистр ebx.
        pop ebp
        ; Удаляем из стека двойное машинное
        ; слово, поданное на вход.
        ret 4

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