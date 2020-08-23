do
    choice = input("What would you like to do? Enter \"read\" or \"write\": ")
until choice == "read" OR choice == "write"

path = input("Enter a file name: ")

if choice == "read" then
    // Read the file
    file = openRead(path)
    while NOT file.endOfFile()
        print(file.readLine())
    endwhile
else
    // Write to the file
    file = openWrite(path)

    while true
        line = input("Enter a line which should be written to the file, or nothing to finish: ")
        if line == "" then
            break
        endif
        file.writeLine(line)
    endwhile
endif

file.close()