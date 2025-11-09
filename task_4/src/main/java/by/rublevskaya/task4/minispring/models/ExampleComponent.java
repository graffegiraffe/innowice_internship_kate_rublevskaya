package by.rublevskaya.task4.minispring.models;

import by.rublevskaya.task4.minispring.annotations.Autowired;
import by.rublevskaya.task4.minispring.annotations.Component;

@Component
public class ExampleComponent {
    @Autowired
    private DependencyComponent dependency;

    public void doSomething() {
        System.out.println("ExampleComponent is working with " + dependency.getMessage());
    }
}
