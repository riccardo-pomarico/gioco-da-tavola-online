package it.polimi.ingsw.model.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Class LeaderCard
 */

public class LeaderCard extends Card {
    private Rule bonusRule;
    private BonusType bonusType;
    private List<Color> requirementsColor;
    private int requirementsInt;

    /**
     * @param rule Constructor which contains the Card bonus
     */
    public LeaderCard(Rule rule, BonusType type, List<Color> requirementsColor, int requirementsInt, int victoryPoints) {
        super.setVictoryPoints(victoryPoints);
        this.bonusRule = rule;
        bonusType = type;
        this.requirementsColor = new ArrayList<>(requirementsColor);
        this.requirementsInt = requirementsInt;
        setId(generateId());
    }

    public List<Color> getRequirementsColor() {
        return requirementsColor;
    }

    public int getRequirementsInt() {
        return requirementsInt;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public Rule getBonusRule() {
        return bonusRule;
    }

    /**
     * This method generates the ID of a Leader card.
     * @return this card's ID.
     */
    @Override
    public String generateId() {
        return "LC-"+bonusRule.generateId()+"-"+bonusType.toString().charAt(0);
    }
}
