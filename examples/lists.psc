myList = [1, 2, 3, 4, 5] // List literal
print(myList)

myList.append(10) // Adds 10 to the end of the list
print(myList)
print(myList.length)

myList.pop(0) // Removes the first item in the list
print(myList)
print(myList.length)

myList.insert(1, 600) // Inserts the element '600' into the list so that it has the index 1
print(myList)
print(myList.length)

myList.remove(3) // Looks for the element '3' in the list and removes the first occurrence of it
print(myList)
print(myList.length)

print(myList.contains(600)) // Checks whether an element is in a list
print(myList.contains("hi"))

print(myList[1]) // Gets the item in the list with index 1

myList[2] = -50 // Overwrites the item in the list with index 2
print(myList)

myList[3] += 100 // Modifies the item in the list with index 3
print(myList)

// Nested lists:
nestedList = [[1, 2, 3], [4, 5, 6], [7, 8, 9]] // A 2D list
print(nestedList)

print(nestedList[0, 2]) // Get the item with the "coordinates" (0, 2)

nestedList[0, 2] += 1000 // Modify the item
print(nestedList)

print(nestedList[0][2]) // Slightly more explicit syntax, if you prefer. The two are equivalent