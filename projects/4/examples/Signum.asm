// Program: Signum.asm
// Computes: if RO>0
//              R1=1
//           else
//              R1=0

    @R0
    D=M

    @POSITIVE // using a label
    D;JGT

    @R1
    M=0

    @END
    0;JMP

(POSITIVE) // declaring a label
    @R1
    M=1

(END)
    @END
    0;JMP
