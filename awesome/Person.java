package awesome;

public class Person {
    private String name;

    public Person(String _name) {
        name = _name;
    }

    public void sleep() {
        System.out.printf("%s is sleeping.", name);
    }
}
