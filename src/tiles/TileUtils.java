package tiles;

import java.util.ArrayList;

public class TileUtils {
	private static ArrayList<Tile> mushroomTiles = new ArrayList<>();
	
	public static void addMushroomTile(Tile tile) {
		if(tile != null && !mushroomTiles.contains(tile)) {
			mushroomTiles.add(tile);
		}
	}
	
	public static void removeMushroomTile(Tile tile) {
		mushroomTiles.remove(tile);
	}
	
	public static ArrayList<Tile> getMushroomTiles(){
		return mushroomTiles;
	}
}
