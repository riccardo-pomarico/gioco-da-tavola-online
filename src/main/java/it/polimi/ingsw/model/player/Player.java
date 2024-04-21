package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.IdGenerable;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * class Player
 */
public class Player implements IdGenerable, Cloneable {
    private final String username;
    private int position;
    private final PersonalDashboard personalDashboard;
    private int victoryPoints;
    private final boolean[] hasBeenCalled = new boolean[] {false, false, false};
    private boolean active;
    private int resourcesInStock = 0;

    /**
     * Constructor for Player class
     * @param username used as identifier for the player, must be unique
     */
    public Player(String username){
        this.username = username;
        this.active = true;
        personalDashboard = new PersonalDashboard(username);
    }

    /**
     * Returns the player's username
     * @return String containing the username
     */
    @Override
    public String generateId() {
        return username;
    }

    @Override
    protected boolean[] clone() throws CloneNotSupportedException {
        boolean[] clone = new boolean[3];
        Collections.addAll(Arrays.asList(clone), hasBeenCalled);
        return clone;
    }

    /**
     * Setter for the player's position
     * @param position Integer of position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * getter for the player's position
     * @return the player position relative to the other players
     */
    public int getPosition() {
        return position;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public PersonalDashboard getPersonalDashboard() { return personalDashboard; }

    public int countStrongBoxItems() {
        return personalDashboard.getStrongBox().size();
    }

    /**
     * This method counts all items present in the warehouse and the extra storage.
     * @return the number of resources in the warehouse and the extra shelves.
     */
    public int countAllWarehouseItems() {
        int count = 0;
        for (Resource r : Resource.values()) {
            count += personalDashboard.getWarehouse().count(r);
            if (personalDashboard.getWarehouse().getExtraStorage() != null) {
                count += personalDashboard.getWarehouse().getExtraStorage().count(r);
            }
        }
        return count;
    }

    public boolean[] getHasBeenCalled() {
        return hasBeenCalled.clone();
    }

    /**
     * This is to mark that a Vatican Report corresponding to Pope's box #pos has been called and cannot be recalled from other players in the future.
     * @param pos the Pope's box corresponding to a Vatican Report
     */
    public void vaticanReportHasBeenCalled(int pos) {
        hasBeenCalled[(pos/8)-1] = true;
    }

    /**
     * This method collects all victory points from popesFavorTrack and cards.
     * @return the final number of victory points a player has obtained throughout the game
     */
    public int getAllVictoryPoints() {
        int popesFavorTrackPoints = personalDashboard.getPopesFavorTrack().getGainedVictoryPoints() + personalDashboard.getPopesFavorTrack().getPopesCardPoints();
        int leaderCardPoints = 0;
        // Punti vittoria derivanti dalle carte leader possedute e non scartate
        for (LeaderCard lc : this.personalDashboard.getLeadCardsDeck()) {
            leaderCardPoints += lc.getVictoryPoints();
        }
        int developmentCardPoints = 0;
        // Punti vittoria derivanti dalle carte sviluppo comprate
        for (DevelopmentCard dc : this.personalDashboard.getDevCardsDeck()) {
            developmentCardPoints += dc.getVictoryPoints();
        }
        int resourcePoints = 0;
        /* for (Resource[] shelf : personalDashboard.getWarehouse().getShelves()) {
            if (Arrays.stream(shelf).anyMatch(Objects::nonNull)) {
                for (Resource r : shelf) {
                    resourcePoints += personalDashboard.getWarehouse().count(r);
                }
            }
        } */
        // Aggiunta del numero di risorse nel forziere e nel magazzino
        resourcePoints = (countStrongBoxItems() + countAllWarehouseItems());
        resourcePoints /= 5;
        setVictoryPoints(popesFavorTrackPoints + leaderCardPoints + developmentCardPoints + resourcePoints);
        return victoryPoints;
    }

    /**
     * This method updates a player's position.
     * It's useful to avoid needless repetitions of setPosition(getPosition()).
     * @param increment the number of boxes to go forward
     */
    @SuppressWarnings("unchecked")
    public void increaseTrackPosition(int increment) {
        PopesFavorTrack playerPFT = personalDashboard.getPopesFavorTrack();
        int oldPosition = playerPFT.getTrackPosition();
        int newPosition = oldPosition + increment;
        if (newPosition > 24) {
            newPosition = 24;
        }
        playerPFT.setTrackPosition(newPosition);
        int positionIncrementPoints = playerPFT.obtainPositionPoints(oldPosition, newPosition);
        if (positionIncrementPoints != 0) {
            playerPFT.setGainedVictoryPoints(positionIncrementPoints);
        }
        JSONObject notifyJson = personalDashboard.getPopesFavorTrack().createJSONObject();
        notifyJson.put("player",this.username);
        personalDashboard.getPopesFavorTrack().notify(notifyJson);
        notifyVaticanReport(oldPosition,newPosition);
        if (newPosition == 24) {
            personalDashboard.notify(personalDashboard.announceFinalTurn());
        }
    }

    /**
     * This method creates the JSONObject which will trigger the Observer pattern ModelObserver-ModelObservable to notify the client whether a potential Vatican report is being called.
     * @param oldPosition old track position of the player (or Lorenzo).
     * @param newPosition new track position of the player (or Lorenzo).
     */
    @SuppressWarnings("unchecked")
    protected void notifyVaticanReport(int oldPosition, int newPosition){
        for (int i = 8; i <= 24; i += 8) {
            if (i > oldPosition && i <= newPosition && !hasBeenCalled[(i/8)-1]) {
                JSONObject notifyJson = new JSONObject();
                notifyJson.put("operation","vatican Report");
                notifyJson.put("popesBox",i);
                notifyJson.put("player",this.username);
                personalDashboard.notify(notifyJson);
            }
        }
    }

    /**
     * This method creates the JSONObject which will trigger the Observer pattern ModelObserver-ModelObservable to notify the client a leader card is being activated.
     * @param card the activated leader card.
     */
    public void activateLeader(String card){
        try{
            personalDashboard.getLeadCardsDeck().stream().filter(leaderCard -> leaderCard.generateId().equals(card)).findFirst().get().activate();
            JSONObject notifyJson = new JSONObject();
            notifyJson.put("operation","leader activation");
            personalDashboard.notify(notifyJson);
        }catch (NoSuchElementException e){
           throw new IllegalArgumentException("card not in the deck");
        }
    }

    /**
     * This method will trigger the end of the single-time action.
     */
    public void notifyActionFinished(){
        personalDashboard.notify(personalDashboard.notifyEndUpdate());
    }

    public String getUsername() {
        return username;
    }

    public int getPopesFavorTrackPoints() { return personalDashboard.getPopesFavorTrack().getGainedVictoryPoints() + personalDashboard.getPopesFavorTrack().getPopesCardPoints(); }

    public void setResourcesInStock(int resourcesInStock) {
        this.resourcesInStock = resourcesInStock;
    }

    public int getResourcesInStock() {
        return resourcesInStock;
    }

    /**
     * This method checks whether a player is still connected to the game.
     * @param active the flag indicating if a player is connected or not.
     */
    public void isActive(boolean active){
        this.active = active;
    }
    public boolean isActive(){
        return active;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }
}

