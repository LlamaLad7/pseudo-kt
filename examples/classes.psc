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

class Dog inherits Pet
    private breed

    public function new(givenName, givenBreed)
        super.new(givenName)
        breed = givenBreed
    endfunction

    public function getBreed()
        return breed
    endfunction
endclass

pet = new Pet("Scruffy")
pet.sayHello()
print(pet.getName())
dog = new Dog("Fido", "Bull terrier")
dog.sayHello()
print(dog.getName())
print(dog.getBreed())