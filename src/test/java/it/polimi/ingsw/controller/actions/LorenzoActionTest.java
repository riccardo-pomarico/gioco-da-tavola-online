package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.controllers.SinglePlayerGameController;
import org.junit.Test;

import static org.junit.Assert.*;

public class LorenzoActionTest {
    @Test
    public void shuffleSoloTokenActions() {
        SinglePlayerGameController spgc = new SinglePlayerGameController();
        spgc.addThisPlayer("pimar116");
        assertEquals(7, spgc.getSinglePlayer().getSoloActionTokens().size());
        LorenzoAction lorenzoAction = new LorenzoAction(spgc.getSinglePlayer(), 1);
        for (int i = 0; i <= 8; i++) {
            lorenzoAction.execute();
        }
        assertEquals(9, spgc.getSinglePlayer().getBlackCrossToken());
        assertEquals(0, spgc.getSinglePlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
    }
}