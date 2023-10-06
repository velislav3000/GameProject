package ontology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.h2.mvstore.tx.TransactionStore.Change;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.AddAxiomData;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener.LoadingFinishedEvent;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener.LoadingStartedEvent;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import com.sun.tools.javac.Main;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.RelationshipType;
import agents.BaseStructureAgent;
import agents.heroAgents.BaseHeroAgent;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.items.BaseItem;
import agents.items.CraftingRecipe;
import agents.items.equipment.Equipment;
import agents.items.equipment.Equipment.EquipmentType;
import agents.items.potions.BasePotion;
import agents.items.potions.BasePotion.PotionType;
import agents.monsterAgents.BaseMonsterAgent;
import agents.monsterAgents.BaseMonsterAgent.MonsterTrait;
import collectiables.chests.BaseChest;
import collectiables.materials.BaseMaterial.MaterialType;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GameOntology {
	private static OWLOntologyManager ontoManager;
	private static OWLOntology gameOntology;
	private static OWLDataFactory dataFactory;
	private static ReasonerFactory reasonerFactory = new ReasonerFactory();
	
	private static String ontologyIRIStr;
	
	public static void initOntology() {
		loadOntologyFromFile();
		
		ontologyIRIStr = gameOntology.getOntologyID().getOntologyIRI().toString() + "#";
	}

	private static void loadOntologyFromFile() {
		ontoManager = OWLManager.createOWLOntologyManager();
		dataFactory = ontoManager.getOWLDataFactory();
		
		try {
				File ontoFile = new File("src/ontology/gameOntologyOriginal.owl");
				File ontoCopyFile = new File("src/ontology/gameOntology.owl");
				
				System.out.println("Created Ontology!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				if(ontoCopyFile.exists()) {
					ontoCopyFile.delete();
					System.out.println("deleted");
				}
				Files.copy(ontoFile.toPath(), ontoCopyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				gameOntology = ontoManager.loadOntologyFromOntologyDocument(ontoCopyFile);
				
		}  catch (IOException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	private static void resetOntoManagerAndResaveAxioms(List<OWLOntologyChange> list) {
		ontoManager = OWLManager.createOWLOntologyManager();
		dataFactory = ontoManager.getOWLDataFactory();
		
		File ontoCopyFile = new File("src/ontology/gameOntology.owl");
		try {
			gameOntology = ontoManager.loadOntologyFromOntologyDocument(ontoCopyFile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		List<OWLOntologyChange> newList = new ArrayList<>();
		
		for(OWLOntologyChange change : list) {
			OWLOntologyChange recreatedChange = null;
			if(change.isAddAxiom()) {
				recreatedChange = new AddAxiom(gameOntology, change.getAxiom());
			}
			else if(change.isRemoveAxiom()) {
				recreatedChange = new RemoveAxiom(gameOntology, change.getAxiom());
			}
			newList.add(recreatedChange);
			
		}
		
		saveOntology(newList);
	}
	
	private synchronized static void saveOntology(List<OWLOntologyChange> list) {
		
		synchronized(OWLOntologyManager.class) {
			//System.out.println(Thread.currentThread().getName() + " is saving");
			
			/*String changes = "";
			for (OWLOntologyChange owlOntologyChange : list) {
				changes += owlOntologyChange.toString() + "\n";
			}
			System.out.println(changes);*/
			
			try {
				ontoManager.applyChanges(list);
			}catch (ConcurrentModificationException e) {
				System.out.println("Reset------------------------------------------------");
				resetOntoManagerAndResaveAxioms(list);
				return;
			}
			
			try {
				ontoManager.saveOntology(gameOntology);
			} catch (OWLOntologyStorageException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addHeroAgent(BaseHeroAgent agent) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLClass agentClass = null;
		switch (((BaseHeroAgent) agent).getHeroType()) {
		case WARRIOR: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Warrior"));
			break;
		}
		case ROGUE: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Rogue"));
			break;
		}
		case ARCHER: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Archer"));
			break;
		}
		}
		
		OWLNamedIndividual newAgent = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(agentClass, newAgent);
		axioms.add(new AddAxiom(gameOntology, axiom));
		
		String personalityTrait = agent.getPersonalityTrait().toString();
		personalityTrait = personalityTrait.substring(0, 1) + personalityTrait.substring(1).toLowerCase();
		OWLNamedIndividual personalityTraitInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + personalityTrait));
		OWLObjectProperty hasPersonalityTrait = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasPersonalityTrait"));
		OWLObjectPropertyAssertionAxiom personalityAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasPersonalityTrait, newAgent, personalityTraitInd);
		axioms.add(new AddAxiom(gameOntology, personalityAxiom));
		
		for (HeroAbilityTrait abilityTrait : agent.getAbilityTraits()) {
			
			String abilityTraitStr = abilityTrait.toString();
			abilityTraitStr = abilityTraitStr.substring(0, 1) + abilityTraitStr.substring(1).toLowerCase();
			OWLNamedIndividual abilityTraitInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + abilityTraitStr));
			OWLObjectProperty hasAbilityTrait = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasAbilityTrait"));
			OWLObjectPropertyAssertionAxiom abilityAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasAbilityTrait, newAgent, abilityTraitInd);
			axioms.add(new AddAxiom(gameOntology, abilityAxiom));
		}
	
		
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "health"));
		OWLDataPropertyAssertionAxiom healthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, newAgent, agent.getMaxHealth());
		axioms.add(new AddAxiom(gameOntology, healthAxiom));
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "attack"));
		OWLDataPropertyAssertionAxiom attackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, newAgent, agent.getBaseAttack());
		axioms.add(new AddAxiom(gameOntology, attackAxiom));
		
		OWLDataProperty critProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "critChance"));
		OWLDataPropertyAssertionAxiom critAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(critProperty, newAgent, agent.getCritChance());
		axioms.add(new AddAxiom(gameOntology, critAxiom));
		
		OWLDataProperty evasionProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "evasionChance"));
		OWLDataPropertyAssertionAxiom evasionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(evasionProperty, newAgent, agent.getEvasionChance());
		axioms.add(new AddAxiom(gameOntology, evasionAxiom));
		
		OWLDataProperty levelProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "level"));
		OWLDataPropertyAssertionAxiom levelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, newAgent, agent.getLevel());
		axioms.add(new AddAxiom(gameOntology, levelAxiom));
		
		if(agent.getSpawnStructure() != null) {
			OWLNamedIndividual structureInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getSpawnStructure().getLocalName()));
			OWLObjectProperty isSpawnedByStructure = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isSpawnedByStructure"));
			OWLObjectPropertyAssertionAxiom traitAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(isSpawnedByStructure, newAgent, structureInd);
			axioms.add(new AddAxiom(gameOntology, traitAxiom));
		}
		
		saveOntology(axioms);
	}
	
	public static void addMonsterAgent(BaseMonsterAgent agent) {

		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLClass agentClass = null;
		switch (((BaseMonsterAgent) agent).getMonsterType()) {
		case SKELETON: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "SkeletonMonster"));
			break;
		}
		case SPIDER:
		case SPIDER_EGG: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "SpiderMonster"));
			break;
		}
		case SHROOMER: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "ShroomerMonster"));
			break;
		}
		case GOLEM: {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "GolemMonster"));
			break;
		}
		}
		
		OWLNamedIndividual newAgent = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(agentClass, newAgent);
		axioms.add(new AddAxiom(gameOntology, axiom));
		
		for (MonsterTrait trait : agent.getTraits()) {
			
			String traitStr = trait.toString();
			traitStr = traitStr.substring(0, 1) + traitStr.substring(1).toLowerCase();
			OWLNamedIndividual traitInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + traitStr));
			OWLObjectProperty hasMonsterTrait = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasMonsterTrait"));
			OWLObjectPropertyAssertionAxiom traitAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasMonsterTrait, newAgent, traitInd);
			axioms.add(new AddAxiom(gameOntology, traitAxiom));
		}
		
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "health"));
		OWLDataPropertyAssertionAxiom healthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, newAgent, agent.getMaxHealth());
		axioms.add(new AddAxiom(gameOntology, healthAxiom));
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "attack"));
		OWLDataPropertyAssertionAxiom attackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, newAgent, agent.getBaseAttack());
		axioms.add(new AddAxiom(gameOntology, attackAxiom));
		
		OWLDataProperty critProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "critChance"));
		OWLDataPropertyAssertionAxiom critAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(critProperty, newAgent, agent.getCritChance());
		axioms.add(new AddAxiom(gameOntology, critAxiom));
		
		OWLDataProperty evasionProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "evasionChance"));
		OWLDataPropertyAssertionAxiom evasionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(evasionProperty, newAgent, agent.getEvasionChance());
		axioms.add(new AddAxiom(gameOntology, evasionAxiom));
		
		OWLDataProperty levelProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "level"));
		OWLDataPropertyAssertionAxiom levelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, newAgent, agent.getLevel());
		axioms.add(new AddAxiom(gameOntology, levelAxiom));
		
		if(agent.getSpawnStructure() != null) {
			OWLNamedIndividual structureInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getSpawnStructure().getLocalName()));
			OWLObjectProperty isSpawnedByStructure = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isSpawnedByStructure"));
			OWLObjectPropertyAssertionAxiom traitAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(isSpawnedByStructure, newAgent, structureInd);
			axioms.add(new AddAxiom(gameOntology, traitAxiom));
		}
		
		saveOntology(axioms);
	
	}
	
	public static void addStructureAgent(BaseStructureAgent agent) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLClass agentClass = null;
		if(agent.isHeroStructureAgent()) {
			agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "City"));
		}
		else {
			switch (agent.getSpawnedUnitClassName()) {
			case "Skeleton": {
				agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Graveyard"));
				break;
			}
			case "Spider": {
				agentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "SpiderNest"));
				break;
			}
			}
		}
		
		OWLNamedIndividual newAgent = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(agentClass, newAgent);
		axioms.add(new AddAxiom(gameOntology, axiom));
		
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "structureHealth"));
		OWLDataPropertyAssertionAxiom healthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, newAgent, agent.getMaxHealth());
		axioms.add(new AddAxiom(gameOntology, healthAxiom));
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "structureAttack"));
		OWLDataPropertyAssertionAxiom attackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, newAgent, agent.getAttack());
		axioms.add(new AddAxiom(gameOntology, attackAxiom));
		
		OWLDataProperty levelProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "structureLevel"));
		OWLDataPropertyAssertionAxiom levelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, newAgent, agent.getLevel());
		axioms.add(new AddAxiom(gameOntology, levelAxiom));
		
		if(agent.getOwner() != null) {
			OWLNamedIndividual ownerInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getOwner().getLocalName()));
			
			OWLObjectProperty isOwnedByProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isOwnedBy"));
			OWLObjectPropertyAssertionAxiom isOwnedByAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(isOwnedByProperty, newAgent, ownerInd);
			axioms.add(new AddAxiom(gameOntology, isOwnedByAxiom));
		}
		
		saveOntology(axioms);
	}
	
	public static void updateEntityAgentStats(BaseEntityAgent agent, int oldLevel, int oldHealth, int oldAttack) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "health"));
		OWLDataPropertyAssertionAxiom oldHealthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, agentInd, oldHealth);
		OWLDataPropertyAssertionAxiom healthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, agentInd, agent.getBaseMaxHealth());
		RemoveAxiom removeHealthAxiom = new RemoveAxiom(gameOntology, oldHealthAxiom);
		axioms.add(removeHealthAxiom);
		AddAxiom addHealthAxiom = new AddAxiom(gameOntology, healthAxiom);
		axioms.add(addHealthAxiom);
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "attack"));
		OWLDataPropertyAssertionAxiom oldAttackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, agentInd, oldAttack);
		OWLDataPropertyAssertionAxiom attackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, agentInd, agent.getBaseAttack());
		RemoveAxiom removeAttackAxiom = new RemoveAxiom(gameOntology, oldAttackAxiom);
		axioms.add(removeAttackAxiom);
		AddAxiom addAttackAxiom = new AddAxiom(gameOntology, attackAxiom);
		axioms.add(addAttackAxiom);
		
		OWLDataProperty levelProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "level"));
		OWLDataPropertyAssertionAxiom oldLevelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, agentInd, oldLevel);
		OWLDataPropertyAssertionAxiom levelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, agentInd, agent.getLevel());
		RemoveAxiom removeLevelAxiom = new RemoveAxiom(gameOntology, oldLevelAxiom);
		axioms.add(removeLevelAxiom);
		AddAxiom addLevelAxiom = new AddAxiom(gameOntology, levelAxiom);
		axioms.add(addLevelAxiom);
		
		saveOntology(axioms);
		
	}
	
	public static void updateStructureAgentStats(BaseStructureAgent agent, int oldLevel, int oldHealth, int oldAttack, BaseEntityAgent oldOwner) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		ArrayList<OWLOntologyChange> removeAxioms = new ArrayList<>();
		ArrayList<OWLOntologyChange> addAxioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "structureHealth"));
		OWLDataPropertyAssertionAxiom oldHealthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, agentInd, oldHealth);
		OWLDataPropertyAssertionAxiom healthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, agentInd, agent.getMaxHealth());
		removeAxioms.add(new RemoveAxiom(gameOntology, oldHealthAxiom));
		addAxioms.add(new AddAxiom(gameOntology, healthAxiom));
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "structureAttack"));
		OWLDataPropertyAssertionAxiom oldAttackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, agentInd, oldAttack);
		OWLDataPropertyAssertionAxiom attackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, agentInd, agent.getAttack());
		removeAxioms.add(new RemoveAxiom(gameOntology, oldAttackAxiom));
		addAxioms.add(new AddAxiom(gameOntology, attackAxiom));
		
		OWLDataProperty levelProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "structureLevel"));
		OWLDataPropertyAssertionAxiom oldLevelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, agentInd, oldLevel);
		OWLDataPropertyAssertionAxiom levelAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(levelProperty, agentInd, agent.getLevel());
		removeAxioms.add(new RemoveAxiom(gameOntology, oldLevelAxiom));
		addAxioms.add(new AddAxiom(gameOntology, levelAxiom));
		
		if(agent.getOwner() != null && !agent.getOwner().equals(oldOwner)) {
			OWLObjectProperty isOwnedByProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isOwnedBy"));
			
			if(oldOwner != null) {
				OWLNamedIndividual oldOwnerInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + oldOwner.getLocalName()));
				OWLObjectPropertyAssertionAxiom oldIsOwnedByAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(isOwnedByProperty, agentInd, oldOwnerInd);
				removeAxioms.add(new RemoveAxiom(gameOntology, oldIsOwnedByAxiom));
			}
			
			OWLNamedIndividual ownerInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getOwner().getLocalName()));
			OWLObjectPropertyAssertionAxiom isOwnedByAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(isOwnedByProperty, agentInd, ownerInd);
			addAxioms.add(new AddAxiom(gameOntology, isOwnedByAxiom));
		}
		
		axioms.addAll(removeAxioms);
		axioms.addAll(addAxioms);
		
		saveOntology(axioms);
	}
	
	public static void addEquipment(Equipment equipment) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		String equipmentType = equipment.getType().toString();
		equipmentType = equipmentType.substring(0, 1) + equipmentType.substring(1).toLowerCase();
		
		OWLClass equipmentClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + equipmentType));
		OWLNamedIndividual equipmentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + equipment.getName()));
		OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(equipmentClass, equipmentInd);
		axioms.add(new AddAxiom(gameOntology, axiom));
		
		if(equipment.getType() == EquipmentType.BOW || equipment.getType() == EquipmentType.SWORD || equipment.getType() == EquipmentType.DAGGER || equipment.getType() == EquipmentType.HAMMER) {
			OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "bonusAttack"));
			OWLDataPropertyAssertionAxiom attackAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(attackProperty, equipmentInd, equipment.getAttackBonus());
			axioms.add(new AddAxiom(gameOntology, attackAxiom));
		}
		else {
			OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "bonusHealth"));
			OWLDataPropertyAssertionAxiom healthAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(healthProperty, equipmentInd, equipment.getHealthBonus());
			axioms.add(new AddAxiom(gameOntology, healthAxiom));
		}
		
		OWLDataProperty priceProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "itemPrice"));
		OWLDataPropertyAssertionAxiom priceAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(priceProperty, equipmentInd, equipment.getPrice());
		axioms.add(new AddAxiom(gameOntology, priceAxiom));
		
		String materialName = equipment.getMaterial().toString();
		materialName = materialName.substring(0, 1) + materialName.substring(1).toLowerCase();
		OWLNamedIndividual materialInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + materialName));
		OWLObjectProperty madeFrom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "madeFrom"));
		OWLObjectPropertyAssertionAxiom madeFromAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(madeFrom, equipmentInd, materialInd);
		axioms.add(new AddAxiom(gameOntology, madeFromAxiom));
		
		String qualityName = equipment.getQuality().toString();
		qualityName = qualityName.substring(0, 1) + qualityName.substring(1).toLowerCase();
		OWLNamedIndividual qualityInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + qualityName));
		OWLObjectProperty hasQuality = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasQuality"));
		OWLObjectPropertyAssertionAxiom hasQualityAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasQuality, equipmentInd, qualityInd);
		axioms.add(new AddAxiom(gameOntology, hasQualityAxiom));
		
		saveOntology(axioms);
	}
	
	public static void addPotion(BasePotion potion) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		String potionType = potion.getType().toString();
		potionType = potionType.substring(0, 1) + potionType.substring(1).toLowerCase();
		potionType = potionType.replace("health", "Health");
		
		OWLClass potionClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + potionType + "Potion"));
		OWLNamedIndividual potionInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + potion.getName()));
		OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(potionClass, potionInd);
		axioms.add(new AddAxiom(gameOntology, axiom));
		
		OWLDataProperty priceProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "itemPrice"));
		OWLDataPropertyAssertionAxiom priceAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(priceProperty, potionInd, potion.getPrice());
		axioms.add(new AddAxiom(gameOntology, priceAxiom));
		
		String qualityName = potion.getQuality().toString();
		qualityName = qualityName.substring(0, 1) + qualityName.substring(1).toLowerCase();
		OWLNamedIndividual qualityInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + qualityName));
		OWLObjectProperty hasQuality = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasQuality"));
		OWLObjectPropertyAssertionAxiom hasQualityAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasQuality, potionInd, qualityInd);
		axioms.add(new AddAxiom(gameOntology, hasQualityAxiom));
		
		saveOntology(axioms);
	}
	
	public static void addOwnedRelationshipBetweenAgentAndItem(BaseAgent agent, String itemName) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		OWLNamedIndividual ItemInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + itemName));
		
		OWLObjectPropertyAssertionAxiom axiom = null;
		if(agent.isEntityAgent()) {
			OWLObjectProperty hasItem = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasItem"));
			axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasItem, agentInd, ItemInd);
		}
		else {
			OWLObjectProperty stores = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "stores"));
			axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(stores, agentInd, ItemInd);
		}
		
		AddAxiom addAxiom = new AddAxiom(gameOntology, axiom);
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		axioms.add(addAxiom);
		saveOntology(axioms);
	}
	
	public static void addOwnedRelationshipBetweenAgentAndItem(BaseAgent agent, ArrayList<String> itemNames) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		
		for (String itemName : itemNames) {
			OWLNamedIndividual ItemInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + itemName));
			
			OWLObjectPropertyAssertionAxiom axiom = null;
			if(agent.isEntityAgent()) {
				OWLObjectProperty hasItem = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasItem"));
				axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasItem, agentInd, ItemInd);
			}
			else {
				OWLObjectProperty stores = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "stores"));
				axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(stores, agentInd, ItemInd);
			}
			
			AddAxiom addAxiom = new AddAxiom(gameOntology, axiom);
			axioms.add(addAxiom);
		}
		
		saveOntology(axioms);
	}
	
	public static void removeOwnedRelationshipBetweenAgentAndItem(BaseAgent agent, String itemName) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		OWLNamedIndividual ItemInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + itemName));
		
		OWLObjectPropertyAssertionAxiom axiom = null;
		if(agent.isEntityAgent()) {
			OWLObjectProperty hasItem = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasItem"));
			axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasItem, agentInd, ItemInd);
		}
		else {
			OWLObjectProperty stores = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "stores"));
			axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(stores, agentInd, ItemInd);
		}
		
		RemoveAxiom removeAxiom = new RemoveAxiom(gameOntology, axiom);
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		axioms.add(removeAxiom);
		saveOntology(axioms);
	}
	
	public static void removeOwnedRelationshipBetweenAgentAndItem(BaseAgent agent, ArrayList<String> itemNames) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agent.getLocalName()));
		
		for (String itemName : itemNames) {
			OWLNamedIndividual ItemInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + itemName));
			
			OWLObjectPropertyAssertionAxiom axiom = null;
			if(agent.isEntityAgent()) {
				OWLObjectProperty hasItem = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasItem"));
				axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(hasItem, agentInd, ItemInd);
			}
			else {
				OWLObjectProperty stores = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "stores"));
				axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(stores, agentInd, ItemInd);
			}
			
			RemoveAxiom removeAxiom = new RemoveAxiom(gameOntology, axiom);

			axioms.add(removeAxiom);
		}
		saveOntology(axioms);
	}
	
	public static void addEquipedRelationshipBetweenAgentAndEquipment(String agentName, String equipmentName, String equipmentType) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLNamedIndividual equipmentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + equipmentName));
		
		OWLObjectProperty equipedPropery = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "equiped" + equipmentType));
		
		OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(equipedPropery, agentInd, equipmentInd);
		AddAxiom addAxiom = new AddAxiom(gameOntology, axiom);
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		axioms.add(addAxiom);
		saveOntology(axioms);
	}
	
	public static void addEquipedRelationshipBetweenAgentAndEquipment(String agentName, HashMap<String, String> equipedEquipmentNamesAndTypes) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		
		for (String equipmentName : equipedEquipmentNamesAndTypes.keySet()) {
			OWLNamedIndividual equipmentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + equipmentName));
			
			OWLObjectProperty equipedPropery = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "equiped" + equipedEquipmentNamesAndTypes.get(equipmentName)));
			
			OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(equipedPropery, agentInd, equipmentInd);
			AddAxiom addAxiom = new AddAxiom(gameOntology, axiom);
			axioms.add(addAxiom);
		}
		
		saveOntology(axioms);
	}
	
	public static void removeEquipedRelationshipBetweenAgentAndEquipment(String agentName, String equipmentName, String equipmentType) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLNamedIndividual equipmentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + equipmentName));
		
		OWLObjectProperty equipedPropery = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "equiped" + equipmentType));
		
		OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(equipedPropery, agentInd, equipmentInd);
		RemoveAxiom removeAxiom = new RemoveAxiom(gameOntology, axiom);
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		axioms.add(removeAxiom);
		saveOntology(axioms);
	}
	
	public static void removeEquipedRelationshipBetweenAgentAndEquipment(String agentName, HashMap<String, String> equipmentNameAndType) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		
		for (String equipmentName : equipmentNameAndType.keySet()) {
			OWLNamedIndividual equipmentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + equipmentName));
			
			OWLObjectProperty equipedPropery = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "equiped" + equipmentNameAndType.get(equipmentName)));
			
			OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(equipedPropery, agentInd, equipmentInd);
			RemoveAxiom removeAxiom = new RemoveAxiom(gameOntology, axiom);
			axioms.add(removeAxiom);
		}
		
		saveOntology(axioms);
	}
	
	public static int getTotalEquipmentBonuses(String agentName) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		
		OWLObjectProperty equipedPropery = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "equipedEquipment"));
		
		NodeSet<OWLNamedIndividual> equipmentSet = reasoner.getObjectPropertyValues(agentInd, equipedPropery);
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "bonusAttack"));
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "bonusHealth"));
		
		int bonus = 0;
		for (OWLNamedIndividual equipmentInd : equipmentSet.getFlattened()) {
			Set<OWLLiteral> literalSet = equipmentInd.getDataPropertyValues(healthProperty, gameOntology);
			
			if(!literalSet.iterator().hasNext()) {
				literalSet = equipmentInd.getDataPropertyValues(attackProperty, gameOntology);
			}
			
			if(literalSet.iterator().hasNext()) {
				OWLLiteral lit = literalSet.iterator().next();
				
				bonus += Integer.parseInt(lit.getLiteral());
			}
		}
		
		return bonus;
	}
	
	public static ArrayList<Integer> getTotalEquipmentBonuses(String agentName, String enemyName) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLNamedIndividual enemyInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + enemyName));
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		
		OWLDataProperty attackProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "bonusAttack"));
		OWLDataProperty healthProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "bonusHealth"));
		
		ArrayList<Integer> bonuses = new ArrayList<>();
		
		OWLObjectProperty equipedPropery = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "equipedEquipment"));
		
		NodeSet<OWLNamedIndividual> equipmentSet = reasoner.getObjectPropertyValues(agentInd, equipedPropery);
		
		int bonus = 0;
		for (OWLNamedIndividual equipmentInd : equipmentSet.getFlattened()) {
			Set<OWLLiteral> literalSet = equipmentInd.getDataPropertyValues(healthProperty, gameOntology);
			
			if(!literalSet.iterator().hasNext()) {
				literalSet = equipmentInd.getDataPropertyValues(attackProperty, gameOntology);
			}
			
			if(literalSet.iterator().hasNext()) {
				OWLLiteral lit = literalSet.iterator().next();
				
				bonus += Integer.parseInt(lit.getLiteral());
			}
		}
		bonuses.add(bonus);
		
		equipmentSet = reasoner.getObjectPropertyValues(enemyInd, equipedPropery);
		
		bonus = 0;
		for (OWLNamedIndividual equipmentInd : equipmentSet.getFlattened()) {
			Set<OWLLiteral> literalSet = equipmentInd.getDataPropertyValues(healthProperty, gameOntology);
			
			if(!literalSet.iterator().hasNext()) {
				literalSet = equipmentInd.getDataPropertyValues(attackProperty, gameOntology);
			}
			
			if(literalSet.iterator().hasNext()) {
				OWLLiteral lit = literalSet.iterator().next();
				
				bonus += Integer.parseInt(lit.getLiteral());
			}
		}
		bonuses.add(bonus);
		
		return bonuses;
	}
	
	public static ArrayList<CraftingRecipe> getAllCraftingRecipes(){
		OWLClass craftingRecipeClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "CraftingRecipe"));
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		
		NodeSet<OWLNamedIndividual> recipeSet = reasoner.getInstances(craftingRecipeClass, false);
		
		OWLObjectProperty isIngrediantOf = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isIngredientOf"));
		OWLObjectProperty isProducedBy = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isProducedBy"));
		
		ArrayList<CraftingRecipe> usableRecipes = new ArrayList<>();
		for (OWLNamedIndividual ind : recipeSet.getFlattened()) {
			int counter = 1;
			
			HashMap<MaterialType, Integer> ingredients = new HashMap<>();
			
			OWLClassExpression classExpression = dataFactory.getOWLObjectHasValue(isIngrediantOf, ind);
			
			for (OWLNamedIndividual materialInd : reasoner.getInstances(classExpression, false).getFlattened()) {
				String ingredientIRI = materialInd.getIRI().toString();
				MaterialType ingredient = MaterialType.valueOf(ingredientIRI.replace(ontologyIRIStr, "").toUpperCase());
				
				OWLDataProperty ingredientAmountProperty = dataFactory.getOWLDataProperty(IRI.create(ontologyIRIStr + "ingredientAmount" + counter));
				int amount = Integer.parseInt(ind.getDataPropertyValues(ingredientAmountProperty, gameOntology).iterator().next().getLiteral());
				
				ingredients.put(ingredient, amount);
				counter++;
			}
			
			classExpression = dataFactory.getOWLObjectHasValue(isProducedBy, ind);
			OWLNamedIndividual itemInd = reasoner.getInstances(classExpression, false).getFlattened().iterator().next();
			String item = itemInd.getIRI().toString().replace(ontologyIRIStr, "").replace("Class", "").toUpperCase();
			
			try {
				usableRecipes.add(new CraftingRecipe(ingredients, EquipmentType.valueOf(item)));
				
			} catch (IllegalArgumentException e) {
				item = item.replace("POTION", "");
				usableRecipes.add(new CraftingRecipe(ingredients, PotionType.valueOf(item)));
			}
			
		}
		
		return usableRecipes;
	}
	
	public static RelationshipType getRelationshipWithAgent(String agentName, String otherAgentName) {OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLNamedIndividual otherAgentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + otherAgentName));
		
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		
		OWLObjectProperty isAlliedWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isAlliedWith"));
		OWLObjectProperty isEnemiesWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isEnemiesWith"));
		OWLObjectProperty isFriendlyWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isFriendlyWith"));
		OWLObjectProperty hasNoRelationsWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNoRelationsWith"));
		OWLObjectProperty isUnfriendlyWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isUnfriendlyWith"));
		
		if(reasoner.getObjectPropertyValues(agentInd, hasNoRelationsWith).containsEntity(otherAgentInd)){
			return RelationshipType.NEUTRAL;
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isEnemiesWith).containsEntity(otherAgentInd)){
			return RelationshipType.ENEMIES;
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isFriendlyWith).containsEntity(otherAgentInd)){
			return RelationshipType.FRIENDLY;
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isUnfriendlyWith).containsEntity(otherAgentInd)){
			return RelationshipType.UNFRIENDLY;
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isAlliedWith).containsEntity(otherAgentInd)){
			return RelationshipType.ALLIES;
		}
		return null;
	}
	
	public static void setRelationshipBetweenAgents(String agentName, String otherAgentName, RelationshipType relation) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLNamedIndividual otherAgentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + otherAgentName));
		
		OWLObjectProperty relationProperty = null;
		switch (relation) {
		case ALLIES: {
			relationProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isAlliedWith"));
			break;
		}
		case FRIENDLY: {
			relationProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isFriendlyWith"));
			break;
		}
		case NEUTRAL: {
			relationProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNoRelationsWith"));
			break;
		}
		case UNFRIENDLY: {
			relationProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isUnfriendlyWith"));
			break;
		}
		case ENEMIES: {
			relationProperty = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isEnemiesWith"));
			break;
		}
		}

		/*OWLReasoner reasoner = new Reasoner(gameOntology);
		if(reasoner.getObjectPropertyValues(otherAgentInd, relationProperty).containsEntity(agentInd)){
			return;
		}
		else if(reasoner.getObjectPropertyValues(agentInd, relationProperty).containsEntity(otherAgentInd)) {
			return;
		}*/
		
		ArrayList<OWLOntologyChange> removeAxioms = getRemoveRelationshipBetweenAgentsAxioms(agentName, otherAgentName);
		if(removeAxioms != null) {
			axioms.addAll(removeAxioms);
		}
		
		OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(relationProperty, agentInd, otherAgentInd);
		AddAxiom addAxiom = new AddAxiom(gameOntology, axiom);
		axioms.add(addAxiom);
		
		saveOntology(axioms);
		
		/*if(relation == RelationshipType.ENEMIES) {
			UpdateAgentAlliesOnNewEnemy(agentName, otherAgentName);
		}
		else if(relation == RelationshipType.ALLIES) {
			UpdateAgentNewAllyOnEnemies(agentName, otherAgentName);
		}*/
	}
	
	private static void UpdateAgentAlliesOnNewEnemy(String agentName, String enemyName) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		
		OWLObjectProperty isAlliedWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isAlliedWith"));
		
		OWLClassExpression classExpression = dataFactory.getOWLObjectHasValue(isAlliedWith, agentInd);
		
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		NodeSet<OWLNamedIndividual> allySet = reasoner.getInstances(classExpression, false);
		
		for (OWLNamedIndividual allyInd : allySet.getFlattened()) {
			
			String allyName = allyInd.getIRI().toString().split("#")[1];
			
			if(getRelationshipWithAgent(allyName, enemyName) == RelationshipType.ENEMIES) {
				continue;
			}
			
			removeRelationshipBetweenAgents(allyName, enemyName);
			setRelationshipBetweenAgents(allyName, enemyName, RelationshipType.ENEMIES);
		}
	}
	
	private static void UpdateAgentNewAllyOnEnemies(String agentName, String allyName) {
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		
		OWLObjectProperty isEnemiesWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isEnemiesWith"));
		
		OWLClassExpression classExpression = dataFactory.getOWLObjectHasValue(isEnemiesWith, agentInd);
		
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		NodeSet<OWLNamedIndividual> enemySet = reasoner.getInstances(classExpression, false);
		
		for (OWLNamedIndividual enemyInd : enemySet.getFlattened()) {
			
			String enemyName = enemyInd.getIRI().toString().split("#")[1];
			
			if(getRelationshipWithAgent(allyName, enemyName) == RelationshipType.ENEMIES) {
				continue;
			}
			
			removeRelationshipBetweenAgents(allyName, enemyName);
			setRelationshipBetweenAgents(allyName, enemyName, RelationshipType.ENEMIES);
		}
	}
	
	public static void removeRelationshipBetweenAgents(String agentName, String otherAgentName) {
		ArrayList<OWLOntologyChange> removeAxioms = getRemoveRelationshipBetweenAgentsAxioms(agentName, otherAgentName);
		saveOntology(removeAxioms);
	}
	
	private static ArrayList<OWLOntologyChange> getRemoveRelationshipBetweenAgentsAxioms(String agentName, String otherAgentName){
		OWLNamedIndividual agentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + agentName));
		OWLNamedIndividual otherAgentInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + otherAgentName));
		
		OWLReasoner reasoner = reasonerFactory.createReasoner(gameOntology);
		
		OWLObjectProperty isAlliedWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isAlliedWith"));
		OWLObjectProperty isEnemiesWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isEnemiesWith"));
		OWLObjectProperty isFriendlyWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isFriendlyWith"));
		OWLObjectProperty hasNoRelationsWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNoRelationsWith"));
		OWLObjectProperty isUnfriendlyWith = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "isUnfriendlyWith"));
		
		OWLObjectPropertyAssertionAxiom axiom1 = null;
		OWLObjectPropertyAssertionAxiom axiom2 = null;
		
		if(reasoner.getObjectPropertyValues(agentInd, hasNoRelationsWith).containsEntity(otherAgentInd)){
			axiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(hasNoRelationsWith, agentInd, otherAgentInd);
			axiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(hasNoRelationsWith, otherAgentInd, agentInd);
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isFriendlyWith).containsEntity(otherAgentInd)){
			axiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(isFriendlyWith, agentInd, otherAgentInd);
			axiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(isFriendlyWith, otherAgentInd, agentInd);
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isUnfriendlyWith).containsEntity(otherAgentInd)){
			axiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(isUnfriendlyWith, agentInd, otherAgentInd);
			axiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(isUnfriendlyWith, otherAgentInd, agentInd);
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isEnemiesWith).containsEntity(otherAgentInd)){
			axiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(isEnemiesWith, agentInd, otherAgentInd);
			axiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(isEnemiesWith, otherAgentInd, agentInd);
		}
		else if(reasoner.getObjectPropertyValues(agentInd, isAlliedWith).containsEntity(otherAgentInd)){
			axiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(isAlliedWith, agentInd, otherAgentInd);
			axiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(isAlliedWith, otherAgentInd, agentInd);
		}
		
		if(axiom1 == null) {
			return null;
		}
		
		ArrayList<OWLOntologyChange> removeAxioms = new ArrayList<>();
		removeAxioms.add(new RemoveAxiom(gameOntology, axiom1));
		removeAxioms.add(new RemoveAxiom(gameOntology, axiom2));
		
		return removeAxioms;
	}
	
	public static void addChest(BaseChest chest, ArrayList<BaseItem> items) {
		ArrayList<OWLOntologyChange> axioms = new ArrayList<>();
		
		String chestType = chest.getName().replaceAll("\\d", "");
		
		OWLClass chestClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + chestType));
		OWLNamedIndividual chestInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + chest.getName()));
		OWLClassAssertionAxiom axiom = dataFactory.getOWLClassAssertionAxiom(chestClass, chestInd);
		axioms.add(new AddAxiom(gameOntology, axiom));
		
		OWLObjectProperty contains = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "contains"));
		
		for (BaseItem item : items) {
			OWLNamedIndividual itemInd = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + item.getName()));
			OWLObjectPropertyAssertionAxiom containsAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(contains, chestInd, itemInd);
			axioms.add(new AddAxiom(gameOntology, containsAxiom));
		}
		
		saveOntology(axioms);
	}
	
	public synchronized static void removeObjectFromOntology(String name) {
		OWLNamedIndividual removedObject = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + name));
		OWLEntityRemover remover = new OWLEntityRemover(ontoManager, Collections.singleton(gameOntology));
		remover.visit(removedObject);
		saveOntology(remover.getChanges());
	}

	public synchronized static void removeObjectsFromOntology(ArrayList<String> names) {
		OWLEntityRemover remover = new OWLEntityRemover(ontoManager, Collections.singleton(gameOntology));
		
		for (String name : names) {
			OWLNamedIndividual removedObject = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + name));
			remover.visit(removedObject);
		}
		
		saveOntology(remover.getChanges());
	}

	public static void reloadOntology() {
		try {
			ontoManager = OWLManager.createOWLOntologyManager();
			dataFactory = ontoManager.getOWLDataFactory();
			
			File ontoCopyFile = new File("src/ontology/gameOntology.owl");
			
			gameOntology = ontoManager.loadOntologyFromOntologyDocument(ontoCopyFile);

			ontologyIRIStr = gameOntology.getOntologyID().getOntologyIRI().toString() + "#";
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
	}

	public static void tempInitOntology() {
		initOntology();
	}
}
