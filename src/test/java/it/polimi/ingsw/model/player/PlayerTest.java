package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.exceptions.ImpossibleStorageException;
import it.polimi.ingsw.model.resources.Marble;
import java.util.List;
import java.util.ArrayList;

import it.polimi.ingsw.model.resources.Resource;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {

    // TODO: ricordarsi che ho fatto modifiche nel costruttore delle carte: mi servono i victoryPoints per testare questo metodo
    @Test
    public void getFinalVictoryPoints() {
        Player p = new Player("pimar116");
        List<Marble> in = new ArrayList<>();
        List<Marble> out = new ArrayList<>();
        in.add(new Marble(Resource.COIN));
        out.add(new Marble(Resource.SHIELD));
        Rule rule = new Rule(in, out);
        p.getPersonalDashboard().getPopesFavorTrack().setGainedVictoryPoints(29);
        p.getPersonalDashboard().getDevCardsDeck().add(new DevelopmentCard(Level.LEVEL1, Color.YELLOW, rule, 1));
        for (int i = 1; i <= 3; i++) {
            p.getPersonalDashboard().getWarehouse().addShelf(i);
        }
        p.getPersonalDashboard().getWarehouse().addToShelf(0, Resource.SERVANT);
        for (int i = 0; i < 1; i++) {
            p.getPersonalDashboard().getWarehouse().addToShelf(1, Resource.STONE);
        }
        for (int i = 0; i < 2; i++) {
            p.getPersonalDashboard().getWarehouse().addToShelf(2, Resource.COIN);
        }
        for (int i = 0; i < 5; i++) {
            p.getPersonalDashboard().getStrongBox().add(Resource.STONE);
        }
        assertEquals(31, p.getAllVictoryPoints());
    }

    @Test
    public void storage() {
        Player p = new Player("emma stone");
        for (Resource r : Resource.values()) {
            assertEquals(0, p.getPersonalDashboard().getWarehouse().count(r));
        }
        for (int i = 0; i < 2; i++) {
            p.getPersonalDashboard().depositResource(2, 1, Resource.COIN);
        }
        assertEquals(2, p.getPersonalDashboard().getWarehouse().count(Resource.COIN));
        try {
            p.getPersonalDashboard().depositResource(2, 1, Resource.COIN);
        } catch (ImpossibleStorageException e) {
            assertEquals(e.getResourceToDiscard(), Resource.COIN);
            assertEquals("There's no possible way to store this resource in the warehouse...", e.getMessage());
        }
        List<Marble> marbleList = new ArrayList<>();
        marbleList.add(new Marble("RG"));
        marbleList.add(new Marble("RG"));
        marbleList.add(new Marble("RG"));
        assertEquals(3, marbleList.stream().filter(m -> m.getType().equalsIgnoreCase("stone")).count());
        List<Resource> resourceList = new ArrayList<>();
        for (Marble m : marbleList) {
            resourceList.add(Marble.resourceFromID(m.generateId()));
        }
        assertEquals(3, resourceList.size());
        assertTrue(resourceList.stream().allMatch(r -> r == Resource.STONE));
        marbleList = Marble.toMarbles(resourceList);
        assertEquals(3, marbleList.stream().filter(m -> m.getType().equalsIgnoreCase("stone")).count());
        resourceList = Marble.toResources(marbleList);
        assertEquals(3, resourceList.stream().filter(r -> r == Resource.STONE).count());
        marbleList = new ArrayList<>();
        marbleList.add(new Marble("RP"));
        marbleList.add(new Marble("RY"));
        marbleList.add(new Marble("RB"));
        for (Resource res : Resource.values()) {
            if (res != Resource.STONE) {
                assertEquals(1, marbleList.stream().filter(m -> Marble.resourceFromID(m.generateId()) == res).count());
            }
        }
        marbleList = new ArrayList<>();
        marbleList.add(new Marble("SR"));
        marbleList.add(new Marble("SB"));
        marbleList.add(new Marble("SW"));
        assertEquals(1, marbleList.stream().filter(m -> m.getType().equalsIgnoreCase("black")).count());
        assertEquals(1, marbleList.stream().filter(m -> m.getType().equalsIgnoreCase("blank")).count());
        assertEquals(1, marbleList.stream().filter(m -> m.getType().equalsIgnoreCase("faithpoint")).count());
        try {
            marbleList.add(new Marble("SC"));
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "cannot create a marble from [SC]");
        }
    }
}