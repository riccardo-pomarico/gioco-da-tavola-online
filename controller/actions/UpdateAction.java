package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Warehouse;
import it.polimi.ingsw.model.resources.Resource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateAction extends Action {
    protected List<Resource[]> warehouse;
    private Actions actionType;

    /**
    * Class constructor. It also translates the warehouse from Cli (List<Resource>[]) to server format (List<Resource[]>).
    * @param targetPlayer the player whose warehouse must be updated.
    * @param warehouse the warehouse new update.
     */
    public UpdateAction(Player targetPlayer, List<Resource>[] warehouse) {
        super(targetPlayer);
        actionType = Actions.UPDATEWAREHOUSE;
        this.warehouse = new ArrayList<>();
        // Traduzione della warehouse da array di liste (formato Cli, parametro) in lista di array (formato Warehouse, risultato finale)
        for (int i = 0; i < 3; i++) {
            this.warehouse.add(new Resource[i+1]);
            Iterator<Resource> cliIterator = warehouse[i].iterator();
            while (cliIterator.hasNext()) {
                if (!warehouse[i].isEmpty()) {
                    AtomicInteger j = new AtomicInteger(0);
                    int finalI = i;
                    cliIterator.forEachRemaining(resource -> {
                        this.warehouse.get(finalI)[j.get()] = resource;
                        j.getAndIncrement();
                    });
                }
            }
        }
    }

    @Override
    public Actions getActionType() {
        return actionType;
    }

    @Override
    public boolean multipleExecutions() {
        return false;
    }

    /**
    * Action execution method.
    * @throws ActionException if something goes wrong with the update, so that action parser can handle it.
     */
    @Override
    public void execute() throws ActionException {
        Warehouse w = targetPlayer.getPersonalDashboard().getWarehouse();
        w.updateWarehouse(this.warehouse);
        for (int i = 0; i < w.getShelves().size(); i++) {
            for (int j = 0; j < w.getShelves().get(i).length; j++) {
                if (w.getShelves().get(i)[j] != null) {
                    // System.out.print(w.getShelves().get(i)[j] + " ");
                } else {
                    // System.out.print("empty ");
                }
            }
            // System.out.println();
        }
    }
}
