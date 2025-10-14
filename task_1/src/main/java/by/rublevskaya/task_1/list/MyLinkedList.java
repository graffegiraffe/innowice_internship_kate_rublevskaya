package by.rublevskaya.task_1.list;

import java.util.NoSuchElementException;
import by.rublevskaya.task_1.node.Node;

public class MyLinkedList<E> implements ListOperations<E> {
    private Node<E> head;
    private Node<E> tail;
    private int size;

    @Override
    public int size() {
        return size;
    }

    @Override
    public void addFirst(E elem) {
        Node<E> newNode = new Node<>(elem);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    @Override
    public void addLast(E elem) {
        Node<E> newNode = new Node<>(elem);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    @Override
    public void add(int index, E elem) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        if (index == 0) {
            addFirst(elem);
        } else if (index == size) {
            addLast(elem);
        } else {
            Node<E> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            Node<E> newNode = new Node<>(elem);
            newNode.next = current.next;
            current.next = newNode;
            size++;
        }
    }

    @Override
    public E getFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        return head.value;
    }

    @Override
    public E getLast() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.value;
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        Node<E> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    @Override
    public E removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        E value = head.value;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return value;
    }

    @Override
    public E removeLast() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        if (size == 1) {
            E value = head.value;
            head = tail = null;
            size = 0;
            return value;
        }
        Node<E> current = head;
        while (current.next != tail) {
            current = current.next;
        }
        E value = tail.value;
        tail = current;
        tail.next = null;
        size--;
        return value;
    }

    @Override
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        if (index == 0) {
            return removeFirst();
        }
        if (index == size - 1) {
            return removeLast();
        }
        Node<E> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }
        E value = current.next.value;
        current.next = current.next.next;
        size--;
        return value;
    }
}