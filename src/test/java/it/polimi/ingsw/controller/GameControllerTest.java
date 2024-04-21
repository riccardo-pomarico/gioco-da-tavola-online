package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.actions.*;
import it.polimi.ingsw.controller.controllers.GameController;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.view.VirtualView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GameControllerTest {

    @Test
    public void GameControllerTest() {
        GameController gc = new GameController();

        // Test dell'inizializzazione
        gc.addPlayer("pimar116", new VirtualView());
        try {
            gc.addPlayer("pimar116", new VirtualView());
        } catch (IllegalArgumentException e) {
            assertEquals("username already taken", e.getMessage());
        }
        gc.addPlayer("ricky", new VirtualView());
        gc.addPlayer("luca zaia", new VirtualView());
        try {
            gc.start();
        } catch (Exception ignored) {
        }
        assertEquals(48, gc.getCards().getDevCardList().size());
        assertEquals(16, gc.getCards().getLeadCardList().size());
        gc.setCurrentPlayer(gc.getPlayer("pimar116"));
        assertEquals("pimar116", gc.getCurrentPlayer().getUsername());

        // Test di una UpdateAction
        List<Resource>[] warehouse1 = new List[3];
        for (int i = 0; i < 3; i++) {
            warehouse1[i] = new ArrayList<>();
        }
        warehouse1[0].add(Resource.COIN);
        warehouse1[1].add(Resource.STONE);
        warehouse1[2].add(Resource.SHIELD);
        gc.handleAction(new UpdateAction(gc.getCurrentPlayer(), warehouse1));
        assertTrue(Arrays.asList(gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().getShelves().get(0)).contains(Resource.COIN));
        assertTrue(Arrays.asList(gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().getShelves().get(1)).contains(Resource.STONE));
        assertTrue(Arrays.asList(gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().getShelves().get(2)).contains(Resource.SHIELD));

        // Test del cambio turno
        gc.setPlayerOrder();
        assertEquals(0, gc.getCurrentPlayer().getPosition());
        gc.changeOfTurn();
        assertEquals(1, gc.getCurrentPlayer().getPosition());

        gc = new GameController();

        try {
            gc.start();
        } catch (Exception ignored) {
        }
        gc.addPlayer("fede", new VirtualView());
        try {
            gc.addPlayer("fede", new VirtualView());
        } catch (IllegalArgumentException e) {
            assertEquals("username already taken", e.getMessage());
        }
        gc.addPlayer("ric", new VirtualView());
        gc.addPlayer("michele emiliano", new VirtualView());
        gc.setPlayerOrder();

        // Test di una deposit (sta giocando il primo del turno)
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.COIN));
        try {
            gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RG", 1, 0, false));
        } catch (ActionException e) {
            assertEquals("this marble is not present in the tray", e.getMessage());
        }
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getStrongBox().size());
        gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RY", 1, 0, false));
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getStrongBox().size());

        gc.changeOfTurn();

        assertEquals(1, gc.getCurrentPlayer().getPosition());   // Sta giocando il secondo del turno
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.STONE));
        gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RG", 2, 1, false));
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().count(Resource.STONE));
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.STONE));
        gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RG", 2, 1, false));
        assertEquals(2, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().count(Resource.STONE));
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.STONE));
        try {
            gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RG", 2, 1, false));
        } catch (ActionException e) {
            assertEquals("There's no possible way to store this resource in the warehouse...", e.getMessage());
        }
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.SHIELD));
        try {
            gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RB", 2, 1, false));
        } catch (ActionException e) {
            assertEquals("this shelf contains a different type of resource!", e.getMessage());
        }
        gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RB", 2, 2, false));
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().count(Resource.SHIELD));
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.SHIELD));
        try {
            gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RB", 2, 0, false));
        } catch (ActionException e) {
            assertEquals("another shelf contains SHIELD!", e.getMessage());
        }
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().count(Resource.SHIELD));
        gc.getCurrentPlayer().increaseTrackPosition(5);

        gc.changeOfTurn();      // Sta giocando il terzo del turno
        gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().addToShelf(2, Resource.SHIELD);
        gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().addToShelf(2, Resource.SHIELD);
        gc.handleAction(new PurchaseAction(gc.getCurrentPlayer(), (DevelopmentCard) gc.getCards().findCard("DC-1-GREEN-RY-SR-1"), 2, gc.getCards(), 1));
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getDevCardsDeck().size());
        try {
            gc.handleAction(new PurchaseAction(gc.getCurrentPlayer(), (DevelopmentCard) gc.getCards().findCard("DC-1-GREEN-RY-SR-1"), 2, gc.getCards(), 1));
        } catch (IllegalArgumentException e) {
            assertEquals("no card id matches [DC-1-GREEN-RY-SR-1]", e.getMessage());
        }
        try {
            gc.handleAction(new PurchaseAction(gc.getCurrentPlayer(), (DevelopmentCard) gc.getCards().findCard("DC-1-PURPLE-RG-SR-1"), 2, gc.getCards(), 2));
        } catch (ActionException e) {
            assertEquals("not enough resources from warehouse", e.getMessage());
        }
        try {
            gc.handleAction(new PurchaseAction(gc.getCurrentPlayer(), (DevelopmentCard) gc.getCards().findCard("DC-2-PURPLE-RY-SRSR-5"), 2, gc.getCards(), 1));
        } catch (ActionException e) {
            assertEquals("not enough resources from warehouse", e.getMessage());
        }
        try {
            gc.handleAction(new PurchaseAction(gc.getCurrentPlayer(), (DevelopmentCard) gc.getCards().findCard("DC-2-PURPLE-RY-SRSR-5"), 2, gc.getCards(), 2));
        } catch (ActionException e) {
            assertEquals("you must put in slot 3 a card of level 1 first! ", e.getMessage());
        }
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getDevCardsDeck().size());

        // Attivazione della sua produzione
        gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().addToShelf(0, Resource.COIN);
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().count(Resource.COIN));
        List<DevelopmentCard> devCard = new ArrayList<>();
        devCard.add(gc.getCurrentPlayer().getPersonalDashboard().getDevCardsDeck().get(0));
        gc.handleAction(new ActivationAction(gc.getCurrentPlayer(), devCard, 2));
        assertEquals(1, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().count(Resource.COIN));

        gc.getCurrentPlayer().increaseTrackPosition(7);
        assertEquals(8, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(2, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(2, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().obtainPopesCardPoints(8));
        assertEquals(5, gc.getPlayer(gc.getPlayer(1)).getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(1, gc.getPlayer(gc.getPlayer(1)).getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(2, gc.getPlayer(gc.getPlayer(1)).getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());

        gc.changeOfTurn();      // Ora gioca di nuovo il primo del turno
        gc.getCurrentPlayer().increaseTrackPosition(23);
        assertEquals(23, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(16, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(3, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());

        gc.changeOfTurn();
        assertNull(gc.getFinalTurnCallingPlayer());
        gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.SERVANT));
        gc.handleAction(new DiscardAction(gc.getPlayerList(), gc.getCurrentPlayer(), "RP", false));
        assertTrue(gc.isFinalTurn());
        assertNotNull(gc.getFinalTurnCallingPlayer());

        gc.changeOfTurn();
        gc.changeOfTurn();
        assertNull(gc.getRanking());
        gc.changeOfTurn();
        assertEquals(gc.getPlayer(0), gc.getPlayer(gc.getRanking().get(0)[0]));
        assertEquals(gc.getPlayer(1), gc.getPlayer(gc.getRanking().get(2)[0]));
        assertEquals(gc.getPlayer(2), gc.getPlayer(gc.getRanking().get(1)[0]));
    }

    @Test
    public void handleDraw() {
        GameController gc = new GameController();

        // Test dell'inizializzazione
        gc.addPlayer("jack", new VirtualView());
        try {
            gc.addPlayer("jack", new VirtualView());
        } catch (IllegalArgumentException e) {
            assertEquals("username already taken", e.getMessage());
        }
        gc.addPlayer("nino", new VirtualView());
        gc.addPlayer("francesca", new VirtualView());
        try {
            gc.start();
        } catch (Exception ignored) {
        }
        assertEquals(48, gc.getCards().getDevCardList().size());
        assertEquals(16, gc.getCards().getLeadCardList().size());
        gc.setPlayerOrder();

        gc.getCurrentPlayer().increaseTrackPosition(8);
        gc.getCurrentPlayer().increaseTrackPosition(15);
        assertEquals(16, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(5, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());
        assertEquals(0, gc.getPlayer(gc.getPlayer(1)).getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(0, gc.getPlayer(gc.getPlayer(1)).getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());
        assertEquals(0, gc.getPlayer(gc.getPlayer(2)).getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(0, gc.getPlayer(gc.getPlayer(2)).getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());

        gc.changeOfTurn();
        for (int i = 0; i < 25; i++) {
            gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.STONE));
            gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RG", 1, 0, false));
        }
        assertEquals(25, gc.getCurrentPlayer().countStrongBoxItems());
        assertEquals(0, gc.getCurrentPlayer().countAllWarehouseItems());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());

        gc.changeOfTurn();
        for (int i = 0; i < 26; i++) {
            gc.getCurrentPlayer().getPersonalDashboard().addToTray(new Marble(Resource.STONE));
            gc.handleAction(new DepositAction(gc.getCurrentPlayer(), "RG", 1, 0, false));
        }
        assertEquals(26, gc.getCurrentPlayer().countStrongBoxItems());
        assertEquals(0, gc.getCurrentPlayer().countAllWarehouseItems());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());

        gc.changeOfTurn();
        gc.getCurrentPlayer().increaseTrackPosition(1);

        gc.changeOfTurn();
        assertEquals(25, gc.getCurrentPlayer().countStrongBoxItems());
        assertEquals(0, gc.getCurrentPlayer().countAllWarehouseItems());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());
        gc.changeOfTurn();
        assertEquals(26, gc.getCurrentPlayer().countStrongBoxItems());
        assertEquals(0, gc.getCurrentPlayer().countAllWarehouseItems());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getGainedVictoryPoints());
        assertEquals(0, gc.getCurrentPlayer().getPersonalDashboard().getPopesFavorTrack().getPopesCardPoints());
        gc.changeOfTurn();
        GameController finalGc = gc;
    }

    @Test
    public void createExtraStorage() {
        GameController gc = new GameController();
        gc.addPlayer("jack", new VirtualView());
        gc.addPlayer("nino", new VirtualView());
        gc.addPlayer("francesca", new VirtualView());
        try {
            gc.start();
        } catch (Exception ignored) {
        }
        gc.setPlayerOrder();
        for (int i = 0; i < 5; i++) {
            gc.getCurrentPlayer().getPersonalDashboard().depositResource(1, 0, Resource.COIN);
        }
        assertEquals(5, gc.getCurrentPlayer().countStrongBoxItems());
        LeaderCard leaderCard = (LeaderCard) gc.getCards().takeCard("LC-0-RG-S");
        gc.getCurrentPlayer().getPersonalDashboard().getLeadCardsDeck().add(leaderCard);
        assertTrue(gc.getCurrentPlayer().getPersonalDashboard().getLeadCardsDeck().size() >= 1 && gc.getCurrentPlayer().getPersonalDashboard().getLeadCardsDeck().size() <= 2);
        gc.getCurrentPlayer().getPersonalDashboard().getLeadCardsDeck().stream().filter(lc -> lc.getId().equals("LC-0-RG-S")).findFirst().get().activate();
        assertTrue(gc.getCurrentPlayer().getPersonalDashboard().getLeadCardsDeck().stream().filter(lc -> lc.getId().equals("LC-0-RG-S")).findFirst().get().hasBeenActivated());
        assertNull(gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().getExtraStorage());
        assertNull(gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().getExtraStorageResource());
        gc.handleAction(new ActivationAction(gc.getCurrentPlayer(), leaderCard));
        assertEquals(Resource.STONE, gc.getCurrentPlayer().getPersonalDashboard().getWarehouse().getExtraStorageResource());
    }
}