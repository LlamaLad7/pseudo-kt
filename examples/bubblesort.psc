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

array nums[10]
nums[0] = 5
nums[1] = 7
nums[2] = 10
nums[3] = 8
nums[4] = 1
nums[5] = 4
nums[6] = 3
nums[7] = 6
nums[8] = 9
nums[9] = 2

bubbleSort(nums)
print(nums)