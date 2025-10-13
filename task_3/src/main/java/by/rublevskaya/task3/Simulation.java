package by.rublevskaya.task3;

import by.rublevskaya.task3.factory.Factory;
import by.rublevskaya.task3.factory.ResourceStorage;
import by.rublevskaya.task3.factions.WorldFaction;
import by.rublevskaya.task3.factions.WednesdayFaction;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static by.rublevskaya.task3.constants.Constants.DAYS;
import static by.rublevskaya.task3.constants.Constants.THREAD_COUNT;

@Slf4j
public class Simulation {
    private final ResourceStorage factoryStorage = new ResourceStorage();
    private final Factory factory = new Factory(factoryStorage);
    private final WorldFaction worldFaction = new WorldFaction(factoryStorage);
    private final WednesdayFaction wednesdayFaction = new WednesdayFaction(factoryStorage);

    public void start() {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            executor.execute(factory);
            executor.execute(worldFaction);
            executor.execute(wednesdayFaction);

            executor.shutdown();
            if (!executor.awaitTermination(DAYS + 10, TimeUnit.SECONDS)) {
                log.error("Threads did not terminate");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Simulation was interrupted", e);
            Thread.currentThread().interrupt();
        }
        logResults();
    }

    private void logResults() {
        System.out.printf("WorldFaction robots: %d%n", worldFaction.getRobotsAssembled());
        System.out.printf("WednesdayFaction robots: %d%n", wednesdayFaction.getRobotsAssembled());
    }
}