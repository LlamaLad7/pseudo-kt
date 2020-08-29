charmap = {" ", ".", ":", "-", "=", "+", "*", "#", "%", "@"}
for y = -1.3 to 1.3 step 0.1
    string = ""
    for x = -2.1 to 1.06 step 0.04
        zi = 0
        zr = 0
        i = 0
        while i < 100
            if zi*zi+zr*zr >= 4 then
                break
            endif
            oldZr = zr
            oldZi = zi
            zr = oldZr*oldZr-oldZi*oldZi+x
            zi = 2*oldZr*oldZi+y
            i += 1
        endwhile
        string += charmap[i MOD 10]
    next x
    print(string)
next y