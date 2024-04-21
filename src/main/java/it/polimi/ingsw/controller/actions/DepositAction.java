package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.exceptions.ImpossibleStorageException;
import it.polimi.ingsw.model.exceptions.WarehouseException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;

public class DepositAction extends Action{
    private Marble marble;
    private int destination;
    private int shelf;
    private boolean blackMarble;
    public DepositAction(Player targetPlayer, String marble , int destination, int shelf, boolean blackMarble){
        super(targetPlayer);
        this.marble = new Marble(marble);
        this.destination = destination;
        this.shelf = shelf;
        this.blackMarble = blackMarble;
    }


    @Override
    public Actions getActionType() {
        return Actions.DEPOSIT;
    }

    /**
     * Action execution method.
     * @throws ActionException if something goes berserk with the deposit.
     */
    @Override
    public void execute() throws ActionException {
            targetPlayer.getPersonalDashboard().getWarehouse().getWarehouse().forEach(shelf -> {
            });

            //se il vassoio contiene una biglia del tipo contenuto nella action:
            if (targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(t -> t.generateId().equals(marble.generateId()))) {
                deposit(marble);
            } else if (targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(t -> t.generateId().equals("SB")) && blackMarble) {
                if (marble.generateId().charAt(0) == 'R') {
                    deposit(marble);
                } else {
                    throw new ActionException("you cannot deposit this marble");
                }
            } else {
                throw new ActionException("this marble is not present in the tray");
            }
    }

    @Override
    public boolean multipleExecutions() {
        return true;
    }

    /**
     * Method handling the deposit of a marble in the warehouse or the strongbox.
     * @param marble the marble to deposit.
     * @throws ActionException if something goes berserk with the deposit.
     */
    private void deposit(Marble marble) throws ActionException{
            //se il primo carattere dell'id è R, la biglia contine una risorsa
            if (marble.generateId().charAt(0) == 'R') {
                try {
                    targetPlayer.getPersonalDashboard().depositResource(destination, shelf, Resource.valueOf(marble.getType()));
                } catch (WarehouseException | ImpossibleStorageException e) {
                    throw new ActionException(e.getMessage());
                }
            }else {
                throw new ActionException("you cannot deposit a marble of this type");
            }

            try{
                targetPlayer.getPersonalDashboard().removeFromTray(marble);
            }catch (IllegalArgumentException ex){
                targetPlayer.getPersonalDashboard().getTray()
                        .stream()
                        .filter(m -> m.generateId().equals("SB"))
                        .findFirst()
                        .ifPresent(blackMarble -> targetPlayer.getPersonalDashboard().removeFromTray(blackMarble));
            }

    }


}
