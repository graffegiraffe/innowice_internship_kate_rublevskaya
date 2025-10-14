package by.rublevskaya.task_1;

import by.rublevskaya.task_1.exception.CustomGlobalExceptionHandler;
import by.rublevskaya.task_1.list.MyLinkedList;

public class LinkedListApplication {
    public static void main(String[] args) {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        try {
            list.addFirst(14);
            list.addLast(22);
            list.add(2, 15);

            System.out.println("First element: " + list.getFirst());
            System.out.println("Last element: " + list.getLast());
            System.out.println("Element at index 2: " + list.get(2));

            list.remove(2);
            System.out.println("After removal, size: " + list.size());

        } catch (Exception e) {
            CustomGlobalExceptionHandler.handle(e);
        }
    }
}