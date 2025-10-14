package by.rublevskaya.task3.factory;

import by.rublevskaya.task3.parts.Robot;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResourceStorage {
    private final Queue<Robot> storage = new ConcurrentLinkedQueue<>();

    public void addPart(Robot part) {
        if (part != null) {
            storage.add(part);
        }
    }

    public Robot retrievePart() {
        return storage.poll();
    }

    public int getSize() {
        return storage.size();
    }
}