package agents.behaviours;

import agents.AgentUtils;
import agents.monsterAgents.GolemAgent;
import agents.monsterAgents.GolemAgent.GolemStage;
import collectiables.materials.AdamantiteMaterial;
import collectiables.materials.BaseMaterial;
import collectiables.materials.CobaltMaterial;
import collectiables.materials.IronMaterial;
import collectiables.materials.MythrilMaterial;
import collectiables.materials.StoneMaterial;
import jade.core.behaviours.OneShotBehaviour;
import tiles.Tile;

public class SpawnMaterialGolemBehaviour extends OneShotBehaviour{

	private transient GolemAgent agentRef;
	
	public SpawnMaterialGolemBehaviour(GolemAgent agent) {
		agentRef = agent;
	}
	
	@Override
	public void action() {
		Tile currentTileRef = agentRef.getCurrentTile();
		BaseMaterial material = null;
			
		GolemStage stage = agentRef.getStage();
		switch (stage) {
		case STONE: {
			material = new StoneMaterial();
			break;
		}
		case IRON: {
			material = new IronMaterial();
			break;
		}
		case COBALT: {
			material = new CobaltMaterial();
			break;
		}
		case MYTHRIL: {
			material = new MythrilMaterial();
			break;
		}
		case ADAMANTITE: {
			material = new AdamantiteMaterial();
			break;
		}
		}
		
		currentTileRef.setCollectable(material);
		AgentUtils.materialsArray.add(material);
		
		agentRef.removeBehaviour(this);
	}

}
