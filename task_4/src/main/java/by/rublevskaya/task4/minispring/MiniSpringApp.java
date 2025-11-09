package by.rublevskaya.task4.minispring;

import by.rublevskaya.task4.minispring.context.MiniAppContext;
import by.rublevskaya.task4.minispring.models.DependencyComponent;
import by.rublevskaya.task4.minispring.models.ExampleComponent;
import by.rublevskaya.task4.minispring.models.PrototypeComponent;

public class MiniSpringApp {
    public static void main(String[] args) {
        MiniAppContext context = new MiniAppContext("by.rublevskaya.task4.minispring");

        ExampleComponent exampleComponent = context.getBean(ExampleComponent.class);
        exampleComponent.doSomething();

        PrototypeComponent prototype1 = context.getBean(PrototypeComponent.class);
        PrototypeComponent prototype2 = context.getBean(PrototypeComponent.class);
        System.out.println("Prototype instance 1 ID: " + prototype1.getId());
        System.out.println("Prototype instance 2 ID: " + prototype2.getId());

        DependencyComponent dependency = context.getBean(DependencyComponent.class);
        System.out.println("DependencyComponent created via @Bean: " + dependency.getMessage());
    }
}