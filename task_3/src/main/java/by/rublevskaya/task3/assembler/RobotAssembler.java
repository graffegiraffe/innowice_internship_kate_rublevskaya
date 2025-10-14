package by.rublevskaya.task3.assembler;

import by.rublevskaya.task3.parts.Robot;

import java.util.Map;

public class RobotAssembler {
    public static int assembleRobots(Map<Robot, Integer> parts) {
        int robots = 0;
        while (canAssemble(parts)) {
            removeParts(parts);
            robots++;
        }
        return robots;
    }

    private static boolean canAssemble(Map<Robot, Integer> parts) {
        return parts.getOrDefault(Robot.HEAD, 0) >= 1 &&
                parts.getOrDefault(Robot.TORSO, 0) >= 1 &&
                parts.getOrDefault(Robot.HAND, 0) >= 2 &&
                parts.getOrDefault(Robot.FOOT, 0) >= 2;
    }

    private static void removeParts(Map<Robot, Integer> parts) {
        parts.put(Robot.HEAD, parts.get(Robot.HEAD) - 1);
        parts.put(Robot.TORSO, parts.get(Robot.TORSO) - 1);
        parts.put(Robot.HAND, parts.get(Robot.HAND) - 2);
        parts.put(Robot.FOOT, parts.get(Robot.FOOT) - 2);
    }
}