function binarySearch(A, left, right, x)
    if left > right then
        return -1
    endif

    mid = (left + right) DIV 2
    if x == A[mid] then
        return mid
    elseif x < A[mid] then
        return binarySearch(A, left, mid - 1, x)
    else
        return binarySearch(A, mid + 1, right, x)
    endif
endfunction

A = {2, 5, 6, 8, 9, 10}
left = 0
right = A.length - 1

for i = 0 to 10
    index = binarySearch(A, left, right, i)
    if index != -1 then
        print("Element found at index " + str(index))
    else
        print("Element not found in the list")
    endif
next i
