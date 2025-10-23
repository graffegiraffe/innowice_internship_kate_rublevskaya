package by.rublevskaya.task_1.node;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node<E> {
    public E value;
    public Node<E> next;

    public Node(E value) {
        this.value = value;
    }
}
