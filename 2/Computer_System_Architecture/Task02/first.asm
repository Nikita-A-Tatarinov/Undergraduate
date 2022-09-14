format PE console
entry start

include 'win32a.inc'

section '.data' data readable writable

        strVecSize     db 'size of vector? ', 0
        strIncorSize   db 'Incorrect size of vector = %d', 10, 0
        strVecElemI    db 'Element %d: ', 0
        strScanInt     db '%d', 0
        strVecElemOut  db '[%d] = %d', 10, 0

        vec_size       dd 0 ; ������ �������� ������� (� �������� ����������� �� 1 � ���������� ������ ��������� �������)
        in_vec         rd 100 ; ������� ������
        out_vec        rd 100 ; �������� ������
        tmp            dd ? ; ���������� ��� ������������ ������ �� �������� �������� � �������� ������ � ����
        i              dd ? ; �������

section '.code' code readable executable

; ������ ���������
start:
        ; ����� ��������� ��� ����� ������� �������
        call inputVecSize
        ; ����� ��������� ��� ����� �������� �������
        call inputVec
        ; ����� ��������� ��� ������������ ��������� �������
        call generateOutVec
        ; ����� ��������� ��� ������ ��������� �������
        call outputVec
finish:
        call [getch]
push 0
        call [ExitProcess]

; ��������� ����� ������� �������� �������
inputVecSize:
        ; ������ � ����
        push strVecSize
        ; ������� ������ �� ����� � �������
        call [printf]
        ; ������� ���� (��� ���������� ������ ��������)
        add esp, 4

        ; ����� ������� ������� � ���� ��� ���������� � �������
        push vec_size
        ; ������ � ����
        push strScanInt
        ; ��������� ������ ������� � ������� � vec_size
        call [scanf]
        ; ������� ����
        add esp, 4
        add esp, 4

        ; �������� �������� ������� ������� � eax
        mov eax, [vec_size]
        ; �������� �� eax 1 (��������� ������������ � ���� �����, � ������� �� sub)
        cmp eax, 1
        ; ���� ��������� ������ ���� ����� 0, �� ������� � ����� wrongInput
        jle wrongInput
        ; �������� �� eax 100
        cmp eax, 100
        ; ���� ��������� ������ 0, �� ������� � ����� wringInput
        jg wrongInput
        jmp endInputVecSize
; ���� �������� ������ ����� �������� ������������
wrongInput:
        ; ����� ������ ������� � ���� ��� ������
        push vec_size
        ; ������ � ����
        push strIncorSize
        ; ������� ������ �� ����� � �������
        call [printf]
        ; ������� ����
        add esp, 4
        add esp, 4
        ; ������� � ����� ��������� ���������
        jmp finish
; ��������� ����� �������
endInputVecSize:
        ret

; ��������� ����� �������
inputVec:
        ; �������� ������� ecx
        xor ecx, ecx
        ; �������� � ������� ebx ����� �������� �������
        mov ebx, in_vec
; ���� ����� �������� �������
inVecLoop:
        ; �������� ������� ebx � �������� tmp
        mov [tmp], ebx
        ; �������� �� ecx �������� ������� �������
        cmp ecx, [vec_size]
        ; ���� ��������� ������ ���� ����� 0, �� ������� � ����� endInputVec
        jge endInputVec

        ; �������� �������� �� �������� ecx  � �������� ��������
        mov [i], ecx
        ; ������� ecx  � ����
        push ecx
        ; ������ � ����
        push strVecElemI
        ; ������� ������ �� ����� � �������
        call [printf]
        ; ������� ����
        add esp, 4
        add esp, 4

        ; ������� ebx � ����
        push ebx
        ; ������ � ����
        push strScanInt
        ; ��������� �������� ���������� �������� �������
        call [scanf]
        ; ������� ����
        add esp, 4
        add esp, 4

        ;�������� �������� �������� � ecx
        mov ecx, [i]
        ; �������������� �������� ��������
        inc ecx
        ; �������� �������� tmp � �������
        mov ebx, [tmp]
        ; ������� ����� ���������� �� 4 ����� ������ (�������� ����� ���������� ��������)
        add ebx, 4
        ; ������� � ����� �����
        jmp inVecLoop
; ��������� ����� �������
endInputVec:
        ret

; ��������� ����������� �������� �������
generateOutVec:
        ; ������� ������� eax
        xor eax, eax
        ; ������� ������� ecx
        xor ecx, ecx
        ; �������� ����� �������� ������� � ������� ebx
        mov ebx, in_vec
        ; �������� ����� ��������� ������� � ������� edx
        mov edx, out_vec
        ; �������������� �������� �������� �������, ������� �������� ��������� �������
        dec [vec_size]
; ���� ������������ ��������� �������
outVecLoop:
        ; �������� �� �������� ecx �������� ������� ��������� �������
        cmp ecx, [vec_size]
        ; ���� ��������� ������ ���� ����� 0, �� ������� �� ����� ������������ �������
        jge endOutVector
        ; �������� �������� ���������� �������� � eax
        mov eax, [ebx + ecx * 4]
        ; ���������� � �������� �������� ���������� ��������
        add eax, [ebx + (ecx + 1) * 4]
        ; ������� eax  � ����
        push eax
        ; �������� �� ����� ��������� � ��������� ������� ��������� �������
        pop dword [edx + ecx * 4]
        ; �������������� ������� eax
        inc ecx
        ; ������� � ����� �����
        jmp outVecLoop
; ��������� ������������ ��������� �������
endOutVector:
        ret

; ��������� ������ �������
outputVec:
        ; ������� ������� ecx
        xor ecx, ecx
        ; �������� ����� ������� �������� ��������� ������� � ������� edx
        mov ebx, out_vec
; ���� ������ �������
outputVecLoop:
        ; �������� �������� �� �������� ebx d �������� tmp
        mov [tmp], ebx
        ; �������� �� �������� ecx �������� ������� ��������� �������
        cmp ecx, [vec_size]
        ; ���� ��������� ������ ���� ����� 0, �� ������� � ����� ��������� ������
        jge endOutputVec
        ; �������� �������� �������� ecx � �������� ��������
        mov [i], ecx

        ; ��������� �������� ebx � ����
        push dword [ebx]
        ; �������� �������� � ����
        push ecx
        ; ������ � ����
        push strVecElemOut
        ; ������� ������ �� ����� � �������
        call [printf]
        ; ������� ����
        add esp, 4
        add esp, 4
        add esp, 4

        ; �������� �������� �������� � ������� ecx
        mov ecx, [i]
        ; �������������� ������� ecx
        inc ecx
        ; �������� �������� tmp � ������� ebx
        mov ebx, [tmp]
        ; ���������� � �������� ebx 4
        add ebx, 4
        ; ������� � ����� �����
        jmp outputVecLoop
; ��������� ������ �������
endOutputVec:
        ret






;-------------------------------third act - including HeapApi------------------------�

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