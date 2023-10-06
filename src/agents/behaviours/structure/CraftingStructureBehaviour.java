package agents.behaviours.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import agents.items.CityInventory;
import agents.items.CraftingRecipe;
import agents.items.CraftingRecipe.RecipeType;
import agents.items.equipment.Equipment;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.introspection.RemovedBehaviour;

public class CraftingStructureBehaviour extends OneShotBehaviour{

	private transient CityInventory inventory;
	private transient HashMap<CraftingRecipe, Integer> recipePriority;
	
	public CraftingStructureBehaviour(CityInventory inventory) {
		this.inventory = inventory;
		recipePriority = new HashMap<>();
	}
	
	@Override
	public void action() {
		ArrayList<CraftingRecipe> craftingRecipies = inventory.getUsableCraftingRecipies();
		setRecipePriority(craftingRecipies);
		
		CraftingRecipe recipe = getHighestPriorityRecipe();
		recipe.use(inventory);
		
		recipe = getHighestPriorityRecipe();
		if(recipe == null) {
			return;
		}
		recipe.use(inventory);
		
		recipe = getHighestPriorityRecipe();
		if(recipe == null) {
			return;
		}
		recipe.use(inventory);
	}
	
	private void setRecipePriority(ArrayList<CraftingRecipe> craftingRecipies) {
		for (CraftingRecipe craftingRecipe : craftingRecipies) {
			recipePriority.put(craftingRecipe, 1);
			
			if(craftingRecipe.getType() == RecipeType.EQUIPMENT_RECIPE) {
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
		
		for (CraftingRecipe craftingRecipe : recipePriority.keySet()) {
			if(highestPriority < recipePriority.get(craftingRecipe)) {
				highestPriority = recipePriority.get(craftingRecipe);
			}
		}
		
		ArrayList<CraftingRecipe> highestPriorityRecipies = new ArrayList<>();
		
		for (CraftingRecipe craftingRecipe : recipePriority.keySet()) {
			if(highestPriority == recipePriority.get(craftingRecipe)) {
				highestPriorityRecipies.add(craftingRecipe);
			}
		}
		
		if(highestPriorityRecipies.isEmpty()) {
			return null;
		}
		
		Random random = new Random();
		int index = random.nextInt(highestPriorityRecipies.size());
				
		recipePriority.remove(highestPriorityRecipies.get(index));
		
		return highestPriorityRecipies.get(index);
	}

}
