// D=10
@10
D=A

// D++
D=D+1

// D=RAM[17]
@17
D=M

// RAM[17]=D
@17
M=D

// RAM[17]=10
@10
D=A
@17
M=D

// RAM[3]=RAM[5]
@5
D=M
@3
M=D


