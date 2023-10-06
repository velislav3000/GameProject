package agents.behaviours;

import agents.BaseEntityAgent;
import agents.monsterAgents.SpiderEggAgent;
import jade.lang.acl.ACLMessage;

public class MessageProcessingSpiderEggBehaviour extends MessageProcessingBehaviour{

	private transient SpiderEggAgent spiderEggAgentRef;
	
	public MessageProcessingSpiderEggBehaviour(SpiderEggAgent agent) {
		super(agent);
		spiderEggAgentRef = agent;
	}
	
	@Override
	public void action() {
		if(!spiderEggAgentRef.isAlive()) {
			spiderEggAgentRef.removeBehaviour(this);
			return;
		}
		
		if(spiderEggAgentRef.getPaused()) {
			return;
		}
		
		ACLMessage msg = spiderEggAgentRef.receive(mtUpdate);
		if(msg != null) {
			spiderEggAgentRef.update();
		}
	}
}
