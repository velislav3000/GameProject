package agents;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import tiles.Tile;
import tiles.TileUtils;

public class UpdaterAgent extends Agent{
	
	private int spawnShroomersCooldown = 10;
	
	@Override
	protected void setup() {
		AgentUtils.updater = this;
	}
	
	public void update(boolean isPaused) {
		if(isPaused) {
			ArrayList<BaseAgent> agents = AgentUtils.agentsArray;
			
			for(int i = agents.size()-1; i>=0; i--) {
				BaseAgent agent = agents.get(i);
				if(agent.shouldBeRemoved()) {
					agents.remove(agent);
				}
			}
		}
		else {
			requestUpdateFromOtherAgents();
			
			spawnShroomersCooldown--;
			if(spawnShroomersCooldown == 0) {
				spawnShroomersCooldown = 10;
				spawnShroomers();
			}
		}
	}
	
	public void requestUpdateFromOtherAgents() {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setConversationId("update");
		
		ArrayList<BaseAgent> agents = AgentUtils.agentsArray;
		
		for(int i = agents.size()-1; i>=0; i--) {
			BaseAgent agent = agents.get(i);
			if(agent.shouldBeRemoved()) {
				agents.remove(agent);
			}
			else {
				msg.addReceiver(agent.getAID());
			}
		}
		
		send(msg);
	}
	
	private void spawnShroomers() {
		int numOfShroomers = TileUtils.getMushroomTiles().size()/10;
		if(numOfShroomers < 1) {
			numOfShroomers = 1;
		}
		else if(numOfShroomers > 10) {
			numOfShroomers = 10;
		}
		
		ArrayList<Tile> mushroomsTilesTemp = new ArrayList<>();
		for (Tile tile : TileUtils.getMushroomTiles()) {
			if(tile.getAgent() == null) {
				mushroomsTilesTemp.add(tile);
			}
		}

		if(mushroomsTilesTemp.size() < 1) {
			return;
		}
			
		Random random = new Random();
		
		for(int i = 0; i < numOfShroomers; i++) {
			if(mushroomsTilesTemp.size() < 0) {
				break;
			}
			
			int index = random.nextInt(mushroomsTilesTemp.size());
			Tile tile = mushroomsTilesTemp.get(index);
			AgentUtils.spawnAgent("Shroomer", false, tile, tile.toString());
			mushroomsTilesTemp.remove(tile);
		}
	}

	public int getSpawnShroomersCooldown() {
		return spawnShroomersCooldown;
	}

	public void setSpawnShroomersCooldown(int spawnShroomersCooldown) {
		this.spawnShroomersCooldown = spawnShroomersCooldown;
	}
}
