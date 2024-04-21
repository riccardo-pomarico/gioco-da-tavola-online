package it.polimi.ingsw.model.cards;
import it.polimi.ingsw.model.IdGenerable;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * Class Card
 */

public abstract class Card implements IdGenerable {
    private String id;
    private int victoryPoints;
    private List<Resource> requirements;
    private boolean activated = false;
    private boolean used = false;

    public void activate() {
        activated = true;
    }

    /**
     * Setting a card as used when it has been activated during the game but now is covered by a new card
     */
    public void used() {
        used = true;
        activated = false;
    }

    public void setRequirements(List<Resource> requirements) {
        this.requirements = requirements;
    }

    public List<Resource> getRequirements() {
        return requirements;
    }

    /**
     * Returns ID string of the card
     * @return ID string
     */
    // Things to do: handle the card requirements
    public String getId() { return this.id; }
    /**
     * Sets ID string of the card
     */
    protected void setId(String id) { this.id = id; }

    /**
     * @return Int which contains the card victory points
     */
    public int getVictoryPoints() {
        return victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) { this.victoryPoints = victoryPoints; }

    /**
     * @return Boolean which contains the status of the Card (it tells if the card is active or not)
     */
    public boolean hasBeenActivated() {
        return activated;
    }

    public boolean hasBeenUsed() { return used; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * This method generates the second char of a card's ID string.
     * @return the second char of a card's ID.
     */
    @Override
    public String generateId() {
        return "C";
    }

    /**
     * This method generates the JSONObject representing a card, which is identified only through its ID.
     * The card's ID will be processed when necessary.
     * @return the JSONObject representing this card.
     */
    @SuppressWarnings("unchecked")
    public JSONObject createJSONObject() {
        JSONObject result = new JSONObject();
        result.put("card id", id);
        return result;
    }
}
