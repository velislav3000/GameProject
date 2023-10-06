package agents.behaviours;

import agents.BaseAgent;
import agents.BaseEntityAgent;
import agents.BaseEntityAgent.BehaviourType;
import agents.monsterAgents.SkeletonAgent;
import jade.lang.acl.ACLMessage;

public class MessageProcessingSkeletonBehaviour extends MessageProcessingBehaviour{

	private transient SkeletonAgent agentRef;
	
	public MessageProcessingSkeletonBehaviour(SkeletonAgent agent) {
		super(agent);
		agentRef = agent;
	}
	
	@Override
	public void action() {
		if(!agentRef.isAlive() && agentRef.getBehaviour() != BehaviourType.SPECIAL) {
			agentRef.removeBehaviour(this);
			return;
		}
		
		if(agentRef.getPaused()) {
			return;
		}
		
		ACLMessage msg = agentRef.receive(mtUpdate);
		if(msg != null) {
			agentRef.update();
		}
		
		msg = agentRef.receive(mtSharingIntent);
		if(msg != null) {
			evaluateIntent(msg);
		}
		
		msg = agentRef.receive(mtCallForHelp);
		if(msg != null) {
			respondToCallForHelp(msg);
		}
	}

	@Override
	public void loadOwner(BaseAgent agent) {
		super.loadOwner(agent);
		agentRef = (SkeletonAgent) agent;
	}
}
