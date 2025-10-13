package by.rublevskaya.task3.factory;

import by.rublevskaya.task3.parts.Robot;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

import static by.rublevskaya.task3.constants.Constants.DAYS;
import static by.rublevskaya.task3.constants.Constants.DAY_DURATION_MS;
import static by.rublevskaya.task3.constants.Constants.MAX_PARTS;

@Slf4j
public class Factory implements Runnable {
    private final ResourceStorage storage;
    private final Random random = new Random();

    public Factory(ResourceStorage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        for (int day = 1; day <= DAYS; day++) { 
            int partsProduced = random.nextInt(MAX_PARTS) + 1;

            for (int i = 0; i < partsProduced; i++) {
                Robot part = Robot.values()[random.nextInt(Robot.values().length)];
                storage.addPart(part);
            }

            log.info("Day {}: Factory produced {} parts. Total in storage: {}", day, partsProduced, storage.getSize());

            try {
                Thread.sleep(DAY_DURATION_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Factory thread was interrupted", e);
                break;
            }
        }
    }
}