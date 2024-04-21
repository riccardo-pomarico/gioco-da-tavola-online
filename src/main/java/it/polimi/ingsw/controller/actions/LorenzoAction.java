package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.model.player.SinglePlayer;

import java.util.Collections;

/**
 * Strategy pattern: this is the action carried out when Lorenzo Il Magnifico makes a move
 */
public class LorenzoAction extends Action {
    private SinglePlayer singlePlayer;
    private int boxesForward;

    public LorenzoAction(SinglePlayer singlePlayer, int boxesForward) {
        super(singlePlayer);
        this.singlePlayer = (SinglePlayer) getPlayer();
        this.boxesForward = boxesForward;
    }

    @Override
    public Actions getActionType() {
        return Actions.LORENZO;
    }

    @Override
    public void execute() {
        singlePlayer.lorenzoMovesForward(boxesForward);
        if (boxesForward == 1) {
            Collections.shuffle(singlePlayer.getSoloActionTokens());
        }
    }

    @Override
    public boolean multipleExecutions() {
        return true;
    }

    public int getBoxesForward() {
        return boxesForward;
    }
}
