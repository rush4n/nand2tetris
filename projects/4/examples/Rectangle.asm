// Program: Rectangle.asm
// Draws a filled rectangle at the screen's top left corner.
// The rectangle's width is 16 pixels, and it's height is RAM[0]
// Usage: put a non-negative number (rectangle's height) in RAM[0]

    @R0
    D=M
    @n
    M=D

    @i
    M=0

    @SCREEN
    D=A
    @address
    M=D

(LOOP)
    @i
    D=M
    @n
    D=D-M
    @END
    D;JGT

    @address
    A=M
    M=-1

    @i 
    M=M+1

    @32
    D=A
    @address
    M=D+M

    @LOOP
    0;JMP

(END)
    @END
    0;JMP