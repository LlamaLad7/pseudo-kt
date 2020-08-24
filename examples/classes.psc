class Pet
    private name

    public function new(givenName)
        name=givenName
    endfunction

    public function sayHello()
        print("Hi! My name is " + name + "!")
    endfunction

    public function getName()
        return name
    endfunction
endclass

print(new Pet("jeff").getName())
new Pet("tofu").sayHello()