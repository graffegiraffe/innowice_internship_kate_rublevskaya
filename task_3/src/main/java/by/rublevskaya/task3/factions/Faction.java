package by.rublevskaya.task3.factions;

import by.rublevskaya.task3.factory.ResourceStorage;
import by.rublevskaya.task3.parts.Robot;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static by.rublevskaya.task3.constants.Constants.DAYS;
import static by.rublevskaya.task3.constants.Constants.MAX_PARTS_TO_TAKE;

@Slf4j
public abstract class Faction implements Runnable {
    private volatile boolean isRunning = true;

    @Getter
    private final Map<Robot, Integer> inventory = new HashMap<>();
    private final ResourceStorage factoryStorage;
    @Getter
    private int robotsAssembled = 0;

    public Faction(ResourceStorage factoryStorage) {
        this.factoryStorage = factoryStorage;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        for (int day = 1; day <= DAYS && isRunning; day++) {
            collectParts();
            robotsAssembled += assembleRobots();
            log.info("Day {}: {} collected {} parts. Total robots assembled: {}", day, this.getClass().getSimpleName(),
                                       inventory.values().stream().mapToInt(Integer::intValue).sum(), robotsAssembled);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Faction thread was interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void collectParts() {
        synchronized (factoryStorage) {
            for (int i = 0; i < MAX_PARTS_TO_TAKE; i++) {
                Robot part = factoryStorage.retrievePart();
                if (part != null) {
                    inventory.put(part, inventory.getOrDefault(part, 0) + 1);
                }
            }
        }
    }

    protected abstract int assembleRobots();
}