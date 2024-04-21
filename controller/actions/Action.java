package it.polimi.ingsw.controller.actions;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.player.Player;

import java.io.Serializable;

/**
 * interface for the action controller, which implements a Strategy Pattern design
 */
public abstract class Action {
    protected Player targetPlayer;

    public abstract Actions getActionType();

    /**
     * This method indicates whether an action can be executed multiple times.
     * For example, purchasing a development card isn't allowed more than once per turn.
     * @return the flag indicating if the action can be executed once or more times.
     */
    public abstract boolean multipleExecutions();

    /**
     * This method handles the execution of an action.
     * @throws ActionException when an action can't be carried out until the end because of a problem.
     */
    public abstract void execute() throws ActionException;

    public Action(Player targetPlayer){
        this.targetPlayer = targetPlayer;
    }

    public Player getPlayer(){
        return targetPlayer;
    }
}