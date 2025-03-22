package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class MapTracker {
    private Map<Position, TileValue> map;
    private ArrayList<Position> foundCreeks;

    public MapTracker() {
        map = new HashMap<>();
        foundCreeks = new ArrayList<>();
    }

    public void updateTile(Position p, TileValue value) {
        map.put(new Position(p), value);
    }

    public TileValue getTile(Position p, TileValue value) {
        return map.get(p);
    }
}
