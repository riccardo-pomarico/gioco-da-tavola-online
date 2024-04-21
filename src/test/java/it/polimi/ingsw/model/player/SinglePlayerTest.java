package it.polimi.ingsw.model.player;

import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.containers.CardSet;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class SinglePlayerTest {

    @Test
    public void singlePlayerTest() {
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        SinglePlayer sp = new SinglePlayer("pimar116", cardSet);
        assertEquals(7, sp.getSoloActionTokens().size());
        Action firstAction = sp.getSoloActionTokens().get(0);
        sp.actionTokensShiftLeft();
        assertEquals(firstAction, sp.getSoloActionTokens().get(6));
        sp.lorenzoMovesForward(2);
        assertEquals(2, sp.getBlackCrossToken());
        sp.increaseTrackPosition(6);
        sp.setBlackCrossToken(8);
        assertEquals(8, sp.getBlackCrossToken());
        assertEquals(2, sp.getPopesFavorTrackPoints());
        try {
            sp.setBlackCrossToken(24);
        } catch (ActionException e) {
            assertEquals("end of the game", e.getMessage());
        }
    }
}