package by.rublevskaya.task3.factions;

import by.rublevskaya.task3.assembler.RobotAssembler;
import by.rublevskaya.task3.factory.ResourceStorage;

public class WorldFaction extends Faction {

    public WorldFaction(ResourceStorage factoryStorage) {
        super(factoryStorage);
    }

    @Override
    protected int assembleRobots() {
        return RobotAssembler.assembleRobots(getInventory());
    }
}