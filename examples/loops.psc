for i = 0 to 10 // For-loop. Prints 0-10 inclusive
    print(i)
next i

i = 10

while i >= 0 // While loop. Prints 10-0 inclusive
    print(i)
    i -= 1
endwhile

x = 1
do // Do-until loop. Prints the powers of 2 from 1-512 inclusive
    print(x)
    x *= 2
until x > 512