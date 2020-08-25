function bubbleSort(A)
    n = A.length
    do
        swapped = false
        for i = 1 to n-1
            // if this pair is out of order
            if A[i-1] > A[i] then
                // swap them and remember something changed

                temp = A[i-1]
                A[i-1] = A[i]
                A[i] = temp

                swapped = true
            endif
        next i
    until NOT swapped
endfunction

nums = [5, 7, 10, 8, 1, 4, 3, 6, 9, 2]

bubbleSort(nums)
print(nums)