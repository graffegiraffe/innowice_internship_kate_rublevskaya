package by.rublevskaya.task4.minispring.models;

import by.rublevskaya.task4.minispring.annotations.Component;
import by.rublevskaya.task4.minispring.annotations.Scope;
import by.rublevskaya.task4.minispring.annotations.ScopeType;

@Component
@Scope(ScopeType.PROTOTYPE)
public class PrototypeComponent {
    private final long id = System.nanoTime();//по ID узнаем, что создаются разные экземпляры

    public long getId() {
        return id;
    }
}
