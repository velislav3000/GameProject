package ui.infoDialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import agents.BaseEntityAgent;
import agents.BaseEntityAgent.StatusEffect;
import agents.heroAgents.BaseHeroAgent.HeroAbilityTrait;
import agents.heroAgents.BaseHeroAgent.HeroPersonalityTrait;
import agents.monsterAgents.BaseMonsterAgent.MonsterTrait;

public class TraitStatusLabel extends JLabel{
	
	private int size;
	
	private TraitStatusLabel(int size) {
		super();
		this.size = size;
	}
	
	public TraitStatusLabel(HeroPersonalityTrait trait, int size) {
		this(size);
		ImageIcon icon = getHeroPersonalityTraitIcon(trait);
		setDisplayIcon(icon);
		setTraitTooltip(trait.toString());
	}
	
	public TraitStatusLabel(HeroAbilityTrait trait, int size) {
		this(size);
		ImageIcon icon = getHeroAbilityTraitIcon(trait);
		setDisplayIcon(icon);
		setTraitTooltip(trait.toString());
	}
	
	public TraitStatusLabel(MonsterTrait trait, int size) {
		this(size);
		ImageIcon icon = getMonsterTraitIcon(trait);
		setDisplayIcon(icon);
		setTraitTooltip(trait.toString());
	}
	
	public TraitStatusLabel(StatusEffect status, int size, BaseEntityAgent agent) {
		this(size);
		ImageIcon icon = getStatusEffectIcon(status);
		setDisplayIcon(icon);
		setStatusEffectTooltip(status, agent);
	}
	
	private void setDisplayIcon(ImageIcon icon) {
		Image img = icon.getImage();
		Image newImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		icon.setImage(newImg);
		setIcon(icon);
	}
	
	private void setTraitTooltip(String traitName) {
		setToolTipText(traitName.replace("_", " "));
	}
	
	private void setStatusEffectTooltip(StatusEffect status, BaseEntityAgent agent) {

		setFont(new Font("VCR OSD MONO", 1, 10));
		setForeground(new Color(253,231,111,255));
		setText(agent.getStatusEffectDuration(status) + "");
		setHorizontalTextPosition(JLabel.CENTER);
		
		switch (status) {
		case BURNING: {
			setToolTipText("<html>" + status.toString().replace("_", " ") + "<br><br>"
						+ "Take 6% of Max Health as damage every turn. Removes FROZEN effect");
			break;
		}
		case FROZEN: {
			setToolTipText("<html>" + status.toString().replace("_", " ") + "<br><br>"
						+ "Can't do anything.");
			break;
		}
		case POISON: {
			setToolTipText("<html>" + status.toString().replace("_", " ") + "<br><br>"
						+ "Take 4% of Max Health as damage every turn.");
			break;
		}
		case QUICK: {
			setToolTipText("<html>" + status.toString().replace("_", " ") + "<br><br>"
						+ "Move twice every turn.");
			break;
		}
		case REGENARATION: {
			setToolTipText("<html>" + status.toString().replace("_", " ") + "<br><br>"
						+ "Heal 10% of Max Health every turn.");
			break;
		}
		}
	}
	
	private ImageIcon getHeroPersonalityTraitIcon(HeroPersonalityTrait trait) {
		String iconPath = "./res/agentTraitIcons/";
		String iconName = "trait_" + trait.toString().toLowerCase() + ".png";
		
		return new ImageIcon(iconPath + iconName);
	}
	
	private ImageIcon getHeroAbilityTraitIcon(HeroAbilityTrait trait) {
		String iconPath = "./res/agentTraitIcons/";
		String iconName = "trait_" + trait.toString().toLowerCase() + ".png";
		
		return new ImageIcon(iconPath + iconName);
	}
	
	private ImageIcon getMonsterTraitIcon(MonsterTrait trait) {
		String iconPath = "./res/agentTraitIcons/";
		String iconName = "trait_" + trait.toString().toLowerCase() + ".png";
		
		return new ImageIcon(iconPath + iconName);
	}
	
	private ImageIcon getStatusEffectIcon(StatusEffect status) {
		String iconPath = "./res/statusEffectIcons/";
		String iconName = "status_" + status.toString().toLowerCase() + ".png";
		
		return new ImageIcon(iconPath + iconName);
	}

	public void updateStatusEffectDuration(int statusEffectDuration) {
		setText(statusEffectDuration + "");
	}
}
