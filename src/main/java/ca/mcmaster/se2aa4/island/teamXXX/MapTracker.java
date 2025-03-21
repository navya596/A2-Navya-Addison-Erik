package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.HashMap;
import java.util.Map;

public class MapTracker {
    private Map<Position, TileValue> map;

    public MapTracker() {
        map = new HashMap<>();
    }

    public void updateTile(Position p, TileValue value) {
        map.put(new Position(p), value);
    }

    public TileValue getTile(Position p, TileValue value) {
        return map.get(p);
    }
}
