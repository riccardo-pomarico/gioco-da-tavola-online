package it.polimi.ingsw.model.player;

import it.polimi.ingsw.observer.ModelObservable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * class PopesFavorTrack
 */
public class PopesFavorTrack extends ModelObservable {
    private int trackPosition = 0;
    private final boolean[] vaticanZone = new boolean[] {false, false, false};
    private HashMap<Integer, Integer> goldenBoxPoints = new HashMap<>();
    private int gainedVictoryPoints = 0;
    private int popesCardPoints = 0;

    /**
     * Class constructor, it initialises all attributes accordingly to the initial conditions of the game.
     * Golden box victory points are set to default values, if not chosen by the player.
     */
    public PopesFavorTrack() {
        int goldenBox = 3, positionPoints = 1, increment = 1;
        while (goldenBoxPoints.size() < 8) {
            goldenBoxPoints.put(goldenBox, positionPoints);
            // Apart from the beginning, the difference between each couple of golden boxes increments of 1 every two times
            if (goldenBoxPoints.size() % 2 == 0) {
                increment++;
            }
            goldenBox = goldenBox + 3;
            positionPoints = positionPoints + increment;
        }
    }

    /**
     * Class constructor with the difference that golden box points are set by the player
     * @param values the list of golden box points the player chooses to assign to the track
     */
    public PopesFavorTrack(ArrayList<Integer> values) {
        int goldenBox = 3;
        for (int i = 0; i < 8; i++) {
            goldenBoxPoints.put(goldenBox, values.get(i));
            goldenBox = goldenBox + 3;
        }
    }

    /**
     * This method is called when a player moves along the Faith Track.
     * It adds further victory points if such player steps on a golden box of the track.
     * @param pos1 the initial position of the player in a move along the Faith Track.
     * @param pos2 the final position of the player in a move along the Faith Track.
     * @return potential further victory points deriving from stepping on a new golden box.
     * @throws IllegalStateException when the initial position is further than the final one.
     * @throws NoSuchElementException when a player stands still or, when moving, doesn't step on a new golden box alongside the track.
     */
    public int obtainPositionPoints(int pos1, int pos2) throws IllegalStateException {
        if (pos1 > pos2) {
            throw new IllegalStateException("Illegal call of this function: the final position has to succeed the initial one.");
        }
        try {
            // To be assigned (old_points = new_points)
            return goldenBoxPoints.get(goldenBoxPoints.keySet().stream().sorted().filter(i -> i > pos1 && i <= pos2).reduce((a, b) -> b).orElseThrow(NoSuchElementException::new));
            // return goldenBoxPoints.keySet().stream().sorted().filter(i -> i > pos1 && i <= pos2).reduce((a, b) -> b).stream().findFirst().get();
        } catch (NoSuchElementException e) {
            // To be incremented (old_points += new_points)
            return 0;
        }
    }

    /**
     * This method checks whether a player is in the same Vatican zone as the player first reaching its Pope's box.
     * It should be called for all players except the one stepping on the Pope's box in the first place.
     * @param popesBox the involved Pope's box which has been reached first by a player
     * @return potential points from being in that Vatican zone
     */
    public int obtainPopesCardPoints(int popesBox) throws IllegalArgumentException {
        if (!(popesBox == 8 || popesBox == 16 || popesBox == 24)) {
            throw new IllegalArgumentException();
        }
        hasReachedPopesZone(popesBox);
        return vaticanZone[(popesBox/8)-1] ? (popesBox/8)+1 : 0;
    }

    /**
     * Checks whether a player's faith marker is in a position where they can claim a popesFavorCard
     * This method is called when another player has reached or passed a Pope's box
     * @param pos the position of the player
     */
    public void hasReachedPopesZone(int pos) throws IllegalArgumentException {
        // Each boolean vaticanZone[i] represents whether a player is in a Vatican zone at the time of a check
        // (when another player steps on a Pope's box). The check will attribute points to those in a vaticanZone[i]
        // correspondent to popesBox[i]
        vaticanZone[0] = vaticanZone[1] = vaticanZone[2] = false;
        if (pos >= 0 && pos <= 24) {
            if (pos >= 5 && pos <= 8) {
                vaticanZone[0] = true;
            } else if (pos >= 12 && pos <= 16) {
                vaticanZone[1] = true;
            } else if (pos >= 19) {
                vaticanZone[2] = true;
            }
        } else {
            throw new IllegalArgumentException("You're out of the track's boundaries!");
        }
    }

    private boolean undefinedBorders(ArrayList<Integer> chosenVaticanZones) {
        return chosenVaticanZones.get(0) > 8 || chosenVaticanZones.get(1) > 16 || chosenVaticanZones.get(2) > 24;
    }

    private boolean illegalList(ArrayList<Integer> chosenVaticanZones) {
        return chosenVaticanZones.size() != 3 || chosenVaticanZones.stream().anyMatch(i -> (i >= 0 && i <= 2) && chosenVaticanZones.get(i).equals(chosenVaticanZones.get(i + 1))) || undefinedBorders(chosenVaticanZones);
    }

    /**
     * Same purpose of hasReachedPopesZone(), but the player can choose where the vatican zones are
     * The arraylist chosenVaticanZones contains the number of the boxes where each of the three vatican zones begins
     * @param pos the position of the player
     * @param chosenVaticanZones the position of the vatican zones according to the player's will
     * @throws IllegalArgumentException when the parameters aren't six or if the vatican zones overlap each other
     */
    public void hasReachedPopesZone(int pos, ArrayList<Integer> chosenVaticanZones) throws IllegalArgumentException {
        if (illegalList(chosenVaticanZones)) {
            throw new IllegalArgumentException("Illegal call!");
        }
        vaticanZone[0] = vaticanZone[1] = vaticanZone[2] = false;
        if (pos >= 0 && pos <= 24) {
            if (pos >= chosenVaticanZones.get(0) && pos <= 8) {
                vaticanZone[0] = true;
            } else if (pos >= chosenVaticanZones.get(1) && pos <= 16) {
                vaticanZone[1] = true;
            } else if (pos >= chosenVaticanZones.get(2)) {
                vaticanZone[2] = true;
            }
        } else {
            throw new IllegalArgumentException("You're out of the track's boundaries!");
        }
    }

    /**
     * Getter method of the hashmap goldenBoxPoints
     * @return the hashmap associating each golden box to a certain amount of victory points
     */
    public HashMap<Integer, Integer> getGoldenBoxPoints() {
        return goldenBoxPoints;
    }

    /**
     * Getter method of vaticanZone[]
     * @return the flags that check whether a player can obtain a PopesCard
     */
    public boolean[] getVaticanZone() {
        return vaticanZone;
    }

    /**
     * Getter method of the player's victory points
     * @return the victory points of the player
     */
    public int getGainedVictoryPoints() {
        return gainedVictoryPoints;
    }

    /**
     * Getter method to obtain the current position.
     * @return the current position of the player's faith marker
     */
    public int getTrackPosition() {
        return trackPosition;
    }

    /**
     * Setter method to update the player's position
     * @param position the position of the player
     */
    public void setTrackPosition(int position) {
        this.trackPosition = position;
    }

    /**
     * Setter method to update the player's victory points
     * @param gainedVictoryPoints the current position victory points of the player
     */
    public void setGainedVictoryPoints(int gainedVictoryPoints) {
        this.gainedVictoryPoints = gainedVictoryPoints;
    }

    public int getPopesCardPoints() {
        return popesCardPoints;
    }

    public void setPopesCardPoints(int popesCardPoints) {
        if (popesCardPoints != 0){
            this.popesCardPoints = popesCardPoints;
        }
    }

    /**
     * This method creates the JSONObject associated to the player's PopesFavorTrack attached to the personal dashboard's update.
     * @return the JSONObject representing the player's PopesFavorTrack status.
     */
    @SuppressWarnings("unchecked")
    public JSONObject createJSONObject() {
        JSONObject result = new JSONObject();
        result.put("operation", "PFT update");
        result.put("Player's track position", trackPosition);
        JSONArray vaticanZoneFlags = new JSONArray();
        for (boolean b : vaticanZone) {
            vaticanZoneFlags.add(b);
        }
        result.put("Vatican Zone flags", vaticanZoneFlags);
        // Non è necessario esportare la tabella dei punti delle caselle dorate, perché è fissa.
        result.put("Position points", gainedVictoryPoints);
        result.put("Pope's Card points", popesCardPoints);
        return result;
    }
}
