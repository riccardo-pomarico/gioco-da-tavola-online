package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * Class DevelopmentCard
 */

public class DevelopmentCard extends Card {
    private Level level;
    private Color color;
    private Rule productionRule;
    private int cardLevel;
    private int slot;

    /**
     * @param l It converts the card level into an integer
     */
    public DevelopmentCard(Level l, Color c, Rule productionRule, int victoryPoints) {
        this.level = l;
        super.setVictoryPoints(victoryPoints);
        switch (l) {
            case LEVEL1 : cardLevel = 1; break;
            case LEVEL2 : cardLevel = 2; break;
            case LEVEL3 : cardLevel = 3; break;
            default:
                throw new IllegalArgumentException("It does not match any type.");
        }
        this.color = c;
        this.productionRule = productionRule;
        setId(generateId());
    }

    /**
     * @param slot the slot in which the card will be allocated
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getSlot() { return slot; }

    /**
     * @return Int which contains the card level
     */
    public int getLevel() {
        return cardLevel;
    }

    /**
     * @return Color which contains the color of the card
     */
    public Color getColor() {
        return color;
    }

    public Rule getProductionRule() {
        return this.productionRule;
    }

    /**
     * This method generates the ID of a Development card.
     * @return this card's ID.
     */
    @Override
    public String generateId() {
        return (productionRule != null) ? "DC-"+this.cardLevel+"-"+this.color.toString()+"-"+productionRule.generateId()+"-"+getVictoryPoints()
                : "DC-"+this.cardLevel+"-"+this.color.toString()+"-"+getVictoryPoints();
    }

    /**
     * This is a private auxiliary method to fill the JSONArray (which has to be inserted in the JSONObject then) with the list of marbles contained in the input/output production rule or in the list of requirements.
     * @param source the list of marbles or resources associated to each of them.
     * @param inputArray the JSONArray to be filled.
     */
    private void ioContents(List<Marble> source, JSONArray inputArray) {
        int blue = 0, yellow = 0, purple = 0, grey = 0, red = 0;
        for (Marble m : source) {
            switch (m.getType().toUpperCase()) {
                case "SERVANT": purple++; break;
                case "COIN": yellow++; break;
                case "STONE": grey++; break;
                case "SHIELD": blue++; break;
                case "FAITHPOINT": red++; break;
            }
        }
        for (Marble m : source) {
            switch (m.getType().toUpperCase()) {
                case "SERVANT": inputArray.add(purple + "SERVANT"); break;
                case "COIN": inputArray.add(yellow + "COIN"); break;
                case "STONE": inputArray.add(grey + "STONE"); break;
                case "SHIELD": inputArray.add(blue + "SHIELD"); break;
                case "FAITHPOINT": inputArray.add(red + "FAITHPOINT"); break;
            }
        }
    }

    /**
     * This method creates the JSONObject associated to this development card.
     * @return the JSONObject representing this development card.
     */
    @SuppressWarnings("unchecked")
    public JSONObject createJSONObject() {
        JSONObject result = new JSONObject();
        result.put("id", super.getId());
        result.put("level", cardLevel);
        result.put("color", color);
        JSONArray ruleIn = new JSONArray();
        JSONArray ruleOut = new JSONArray();
        ioContents(productionRule.getInRule(), ruleIn);
        ioContents(productionRule.getOutRule(), ruleOut);
        result.put("productionIn", ruleIn);
        result.put("productionOut", ruleOut);
        result.put("victoryPoints", getVictoryPoints());
        JSONArray requirements = new JSONArray();
        ioContents(Marble.toMarbles(super.getRequirements()), requirements);
        result.put("requirements", requirements);
        return result;
    }
}
