package it.polimi.ingsw.model.player;

import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.actions.DiscardAction;
import it.polimi.ingsw.controller.actions.LorenzoAction;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.IdGenerable;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.containers.CardSet;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class SinglePlayer, particular case of Player.
 */
public class SinglePlayer extends Player implements IdGenerable {
    private int blackCrossToken = 0;
    private List<Action> soloActionTokens;

    /**
     * Class constructor initializing the single player and all action tokens.
     * @param username the player's username.
     */
    public SinglePlayer(String username, CardSet cardSet) {
        super(username);
        soloActionTokens = new ArrayList<>();
        // Istanziazione dei segnalini Azione
        soloActionTokens.add(new LorenzoAction(this, 2));
        soloActionTokens.add(new LorenzoAction(this, 2));
        soloActionTokens.add(new LorenzoAction(this, 1));
        soloActionTokens.add(new DiscardAction(this, cardSet, Color.PURPLE));
        soloActionTokens.add(new DiscardAction(this, cardSet, Color.GREEN));
        soloActionTokens.add(new DiscardAction(this, cardSet, Color.BLUE));
        soloActionTokens.add(new DiscardAction(this, cardSet, Color.YELLOW));
        Collections.shuffle(soloActionTokens);
    }
    
    public int getBlackCrossToken() { return blackCrossToken; }

    /**
     * Updates Lorenzo's position in PopesFavorTrack.
     * @param newLorenzoPosition new Lorenzo's position.
     */
    public void setBlackCrossToken(int newLorenzoPosition){
        int oldLorenzoPosition = blackCrossToken;
        blackCrossToken = newLorenzoPosition;
        if (blackCrossToken > 24) {
            blackCrossToken = 24;
        }
        notifyVaticanReport(oldLorenzoPosition,newLorenzoPosition);
        if (blackCrossToken == 24) {
            throw new ActionException("end of the game");
        }
    }

    /**
     * Updates Lorenzo's position in PopesFavorTrack.
     * This method is used rather than setBlackCrossToken() especially in LorenzoActions.
     * @param increment the increment of positions of Lorenzo.
     */
    public void lorenzoMovesForward(int increment)  {
        setBlackCrossToken(blackCrossToken + increment);
        JSONObject notifyJson = new JSONObject();
        notifyJson.put("operation","LORENZO PFT INCREMENT");
        notifyJson.put("increment",increment);
        getPersonalDashboard().notify(notifyJson);
    }

    public List<Action> getSoloActionTokens() {
        return soloActionTokens;
    }

    /**
     * Updates the stack of solo action tokens when Lorenzo has made his move.
     */
    public void actionTokensShiftLeft() {
        Action temp = soloActionTokens.get(0);
        for (int i = 0; i < soloActionTokens.size()-1; i++) {
            soloActionTokens.set(i, soloActionTokens.get(i+1));
        }
        soloActionTokens.set(soloActionTokens.size()-1, temp);
    }
}
