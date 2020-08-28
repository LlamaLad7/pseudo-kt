function test1(x)
    // If statements
    if x < 10 then
        print("x was less than 10")
    elseif x < 50 then
        print("x was less than 50")
    elseif x < 100 then
        print("x was less than 100")
    else
        print("x was bigger than expected. It was: " + str(x))
    endif
endfunction

test1(5)
test1(30)
test1(90)
test1(600)

function test2(x)
    // Switch statements
    switch x:
        case 10:
            print("x was 10, which is valid")
        case 50:
            print("x was 50, which is valid")
        default:
            print("x was " + str(x) + ", which is invalid")
    endswitch
endfunction

test2(2)
test2(10)
test2(50)
test2(100)
test2(-5)