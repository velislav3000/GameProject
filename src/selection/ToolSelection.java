package selection;

import java.util.ArrayList;

import agents.BaseStructureAgent.StructureType;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.heroAgents.BaseHeroAgent.HeroType;
import agents.monsterAgents.BaseMonsterAgent.MonsterTrait;
import agents.monsterAgents.BaseMonsterAgent.MonsterType;
import collectiables.chests.BaseChest.ChestType;
import collectiables.materials.BaseMaterial.MaterialType;
import tiles.Tile.TileType;

public class ToolSelection {
	public enum Tool{
		POINTER,
		TILE,
		HERO,
		MONSTER,
		MATERIAL,
		CHEST,
		STRUCTURE
	}
	
	public static Tool selectedTool = Tool.TILE;

	public static TileType selectedTileType = TileType.GRASS;
	
	public static MaterialType selectedMaterialType = MaterialType.STONE;
	
	public static HeroType selectedHeroType = HeroType.WARRIOR;
	public static HeroPersonalityTrait selectedHeroPersonalityTrait = HeroPersonalityTrait.BLOODTHIRSTY;
	public static ArrayList<HeroAbilityTrait> selectedHeroAbilityTraits = new ArrayList<>();
	
	public static ChestType selectedChestType = ChestType.WOODEN_CHEST;
	
	public static MonsterType selectedMonsterType = MonsterType.SKELETON;
	public static ArrayList<MonsterTrait> selectedMonsterTraits = new ArrayList<>();
	
	public static StructureType selectedStructureType = StructureType.CITY;
}
