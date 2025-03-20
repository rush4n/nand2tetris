// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

(CHECK)
    @KBD
    D=M

    @SETBLACK
    D;JGT

    @SETWHITE
    0;JMP

(SETBLACK)
    @i
    M=0

    @SCREEN
    D=A
    @address
    M=D

(BLACKLOOP)
    @KBD
    D=M
    @CHECK
    D;JLE

    @i
    D=M
    @8192
    D=D-A
    @CHECK
    D;JEQ

    @address
    D=M
    @i
    A=D+M
    M=-1

    @i
    M=M+1

    @BLACKLOOP
    0;JMP

(SETWHITE)
    @j
    M=0

(WHITELOOP)
    @KBD
    D=M
    @CHECK
    D;JGT

    @j
    D=M
    @8192
    D=D-A
    @CHECK
    D;JEQ

    @address
    D=M
    @j
    A=D+M
    M=0

    @j
    M=M+1

    @WHITELOOP
    0;JMP