package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.resources.Resource;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UpdateActionTest {
    @Test
    public void createUpdateAction() {
        Player p = new Player("pimar116");
        List<Resource>[] warehouse = new List[3];
        for (int i = 0; i < 3; i++) {
            warehouse[i] = new ArrayList<>();
        }
        warehouse[0].add(Resource.COIN);
        warehouse[2].add(Resource.SHIELD);
        warehouse[2].add(Resource.SHIELD);
        UpdateAction action = new UpdateAction(p, warehouse);
        Assert.assertEquals(action.warehouse.get(0)[0], Resource.COIN);
        Assert.assertNull(action.warehouse.get(1)[0]);
        Assert.assertEquals(action.warehouse.get(2)[0], Resource.SHIELD);
        Assert.assertEquals(action.warehouse.get(2)[1], Resource.SHIELD);
        warehouse = new List[3];
        for (int i = 0; i < 3; i++) {
            warehouse[i] = new ArrayList<>();
        }
        warehouse[1].add(Resource.STONE);
        warehouse[1].add(Resource.STONE);
        warehouse[2].add(Resource.SERVANT);
        action = new UpdateAction(p, warehouse);
        Assert.assertNull(action.warehouse.get(0)[0]);
        Assert.assertEquals(action.warehouse.get(1)[0], Resource.STONE);
        Assert.assertEquals(action.warehouse.get(1)[1], Resource.STONE);
        Assert.assertEquals(action.warehouse.get(2)[0], Resource.SERVANT);
        warehouse[0].add(Resource.SHIELD);
        warehouse[2].add(Resource.SERVANT);
        warehouse[2].add(Resource.SERVANT);
        action = new UpdateAction(p, warehouse);
        Assert.assertEquals(action.warehouse.get(2)[1], Resource.SERVANT);
        Assert.assertEquals(action.warehouse.get(2)[2], Resource.SERVANT);
        Assert.assertEquals(action.warehouse.get(0)[0], Resource.SHIELD);
    }

    @Test
    public void execute() {
        Player p = new Player("pimar116");
        List<Resource>[] warehouse = new List[3];
        for (int i = 0; i < 3; i++) {
            warehouse[i] = new ArrayList<>();
        }
        warehouse[0].add(Resource.COIN);
        warehouse[2].add(Resource.SHIELD);
        warehouse[2].add(Resource.SHIELD);
        Action action = new UpdateAction(p, warehouse);
        p.getPersonalDashboard().getWarehouse().addToShelf(0, Resource.COIN);
        p.getPersonalDashboard().getWarehouse().addToShelf(1, Resource.SHIELD);
        p.getPersonalDashboard().getWarehouse().addToShelf(1, Resource.SHIELD);
        action.execute();
        Assert.assertEquals(p.getPersonalDashboard().getWarehouse().getShelves().get(0)[0], Resource.COIN);
        for (int i = 0; i < 2; i++){
            Assert.assertNull(p.getPersonalDashboard().getWarehouse().getShelves().get(1)[i]);
        }
        Assert.assertEquals(p.getPersonalDashboard().getWarehouse().getShelves().get(2)[0], Resource.SHIELD);
        Assert.assertEquals(p.getPersonalDashboard().getWarehouse().getShelves().get(2)[1], Resource.SHIELD);
    }
}