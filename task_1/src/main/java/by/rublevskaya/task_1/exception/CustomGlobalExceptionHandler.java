package by.rublevskaya.task_1.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomGlobalExceptionHandler {
    private static final Logger logger = Logger.getLogger(CustomGlobalExceptionHandler.class.getName());

    public static void handle(Exception e) {
        logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
        throw new RuntimeException("A handled exception was thrown: " + e.getMessage(), e);
    }
}