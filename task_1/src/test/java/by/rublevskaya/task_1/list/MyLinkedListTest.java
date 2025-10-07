package by.rublevskaya.task_1.list;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyLinkedListTest {

    @Test
    void size() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        assertEquals(0, list.size());
        list.addFirst(1);
        assertEquals(1, list.size());
        list.addLast(2);
        assertEquals(2, list.size());
        list.removeFirst();
        assertEquals(1, list.size());
    }

    @Test
    void addFirst() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addFirst(19);
        list.addFirst(20);
        assertEquals(20, list.getFirst());
        assertEquals(2, list.size());
    }

    @Test
    void addLast() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(25);
        list.addLast(11);
        assertEquals(25, list.getFirst());
        assertEquals(11, list.getLast());
        assertEquals(2, list.size());
    }

    @Test
    void add() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(11);
        list.addLast(25);
        list.add(1, 19);
        assertEquals(3, list.size());
        assertEquals(11, list.getFirst());
        assertEquals(19, list.get(1));
        assertEquals(25, list.getLast());
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(5, 30));
    }

    @Test
    void getFirst() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(25);
        assertEquals(25, list.getFirst());
        list.addFirst(11);
        assertEquals(11, list.getFirst());
    }

    @Test
    void getLast() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addFirst(11);
        assertEquals(11, list.getLast());
        list.addLast(25);
        assertEquals(25, list.getLast());
    }

    @Test
    void get() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(11);
        list.addLast(25);
        list.addLast(19);
        assertEquals(11, list.get(0));
        assertEquals(25, list.get(1));
        assertEquals(19, list.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
    }

    @Test
    void removeFirst() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addFirst(25);
        list.addFirst(11);
        assertEquals(11, list.removeFirst());
        assertEquals(1, list.size());
        assertEquals(25, list.getFirst());
    }

    @Test
    void removeLast() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(25);
        list.addLast(11);
        assertEquals(11, list.removeLast());
        assertEquals(1, list.size());
        assertEquals(25, list.getLast());
    }

    @Test
    void remove() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(11);
        list.addLast(25);
        list.addLast(19);

        assertEquals(25, list.remove(1));
        assertEquals(2, list.size());
        assertEquals(19, list.get(1));

        list.remove(0);
        assertEquals(1, list.size());
        assertEquals(19, list.getFirst());

        list.remove(0);
        assertEquals(0, list.size());
    }

    @Test
    void testExceptions() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        assertThrows(NoSuchElementException.class, list::getFirst);
        assertThrows(NoSuchElementException.class, list::getLast);
        assertThrows(NoSuchElementException.class, list::removeFirst);
        assertThrows(NoSuchElementException.class, list::removeLast);
        list.addLast(11);
        list.addLast(25);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(5));
    }

    @Test
    void complexScenario() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        list.addLast(11);
        list.addLast(25);
        list.addFirst(19);
        list.add(1, 20);

        assertEquals(19, list.getFirst());
        assertEquals(25, list.getLast());
        assertEquals(20, list.get(1));
        assertEquals(4, list.size());

        assertEquals(20, list.remove(1));
        assertEquals(19, list.removeFirst());
        assertEquals(25, list.removeLast());
        assertEquals(1, list.size());
    }
}