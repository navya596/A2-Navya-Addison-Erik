package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class MapTracker {
    private Map<Coordinate, TileValue> map;
    private ArrayList<Coordinate> foundCreeks;

    public MapTracker() {
        map = new HashMap<>();
        foundCreeks = new ArrayList<>();
    }

    public void updateTile(Position p, TileValue value) {
        map.put(new Coordinate(p), value);
    }

    public TileValue getTile(Coordinate c, TileValue value) {
        return map.get(c);
    }
}
