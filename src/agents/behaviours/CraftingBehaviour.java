package agents.behaviours;

import java.util.ArrayList;
import java.util.HashMap;

import agents.items.HeroInventory;
import agents.items.CraftingRecipe;
import agents.items.CraftingRecipe.RecipeType;
import agents.items.equipment.Equipment;
import jade.core.behaviours.OneShotBehaviour;

public class CraftingBehaviour extends OneShotBehaviour{

	private transient HeroInventory inventory;
	private transient HashMap<CraftingRecipe, Integer> recipePriority;
	
	public CraftingBehaviour(HeroInventory inventory) {
		this.inventory = inventory;
		recipePriority = new HashMap<>();
	}
	
	@Override
	public void action() {
		ArrayList<CraftingRecipe> craftingRecipies = inventory.getUsefullCraftingRecipes();
		setRecipePriority(craftingRecipies);
		
		CraftingRecipe recipe = getHighestPriorityRecipe();
		recipe.use(inventory);
	}
	
	private void setRecipePriority(ArrayList<CraftingRecipe> craftingRecipies) {
		for (CraftingRecipe craftingRecipe : craftingRecipies) {
			recipePriority.put(craftingRecipe, 1);
			
			if(craftingRecipe.getType() == RecipeType.EQUIPMENT_RECIPE) {
				Equipment equipment = inventory.getEquipedEquipment(craftingRecipe.getProducedEquipmentType());
				
				if(equipment == null) {
					recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 10);
					
					switch (craftingRecipe.getMainIngredient()) {
					case ADAMANTITE: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 4);
						break;
					}
					case MYTHRIL: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 3);
						break;
					}
					case COBALT: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 2);
						break;
					}
					case IRON: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 1);
						break;
					}
					}
				}
				else if(equipment.getMaterial().ordinal() < craftingRecipe.getMainIngredient().ordinal()) {
					int bonusPriority = (craftingRecipe.getMainIngredient().ordinal() - equipment.getMaterial().ordinal())*2;
					recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + bonusPriority);
					
					switch (craftingRecipe.getMainIngredient()) {
					case ADAMANTITE: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 4);
						break;
					}
					case MYTHRIL: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 3);
						break;
					}
					case COBALT: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 2);
						break;
					}
					case IRON: {
						recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 1);
						break;
					}
					}
				}
			}
			else {
				recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 1);
				
				switch (craftingRecipe.getProducedPotionType()) {
				case SUPERHEALTH: {
					recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 3);
					break;
				}
				case FLEEING: {
					recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 2);
					break;
				}
				case REGENARATION: {
					recipePriority.put(craftingRecipe, recipePriority.get(craftingRecipe) + 1);
					break;
				}
				}
			}
		}
	}
	
	private CraftingRecipe getHighestPriorityRecipe() {
		int highestPriority = 0;
		CraftingRecipe highestPriorityRecipe = null;
		
		for (CraftingRecipe craftingRecipe : recipePriority.keySet()) {
			if(highestPriority < recipePriority.get(craftingRecipe)) {
				highestPriority = recipePriority.get(craftingRecipe);
				highestPriorityRecipe = craftingRecipe;
			}
		}
		
		return highestPriorityRecipe;
	}
}
