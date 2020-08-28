array names[10] // Array declaration. Makes an array with 10 'null's
print(names)

myArray = {1, 2, 3, 4, 5} // Array literal
print(myArray)

print(myArray.contains(5)) // Checks whether an element is in an array
print(myArray.contains("hi"))

print(myArray[1]) // Gets the item in the array with index 1

myArray[2] = -50 // Overwrites the item in the array with index 2
print(myArray)

myArray[3] += 100 // Modifies the item in the array with index 3
print(myArray)

// Nested arrays
array board[8,8] // 2D array declaration. Makes a list containing 8 lists, each of which contain 8 'null's
print(board)

nestedArray = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}} // A 2D array
print(nestedArray)

print(nestedArray[0, 2]) // Get the item with the "coordinates" (0, 2)

nestedArray[0, 2] += 1000 // Modify the item
print(nestedArray)

print(nestedArray[0][2]) // Slightly more explicit syntax, if you prefer. The two are equivalent