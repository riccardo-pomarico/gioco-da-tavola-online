package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.SinglePlayer;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DiscardActionTest {
    @Test
    public void getActionTypeTest(){
        Player p1 = new Player("pimar116"), p2 = new Player("fede"), p3 = new Player("riccardo");
        List<Player> playerList = new ArrayList<>();
        playerList.add(p1);
        playerList.add(p2);
        playerList.add(p3);
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        Action discardAction = new DiscardAction(playerList,p1,"RB",false);
        assertEquals(Actions.DISCARDRESOURCE,discardAction.getActionType());
        assertTrue(discardAction.multipleExecutions());
        discardAction = new DiscardAction(playerList,p1,"RB",true);
        assertEquals(Actions.DISCARDRESOURCE,discardAction.getActionType());
        assertTrue(discardAction.multipleExecutions());
        discardAction = new DiscardAction(p1,(LeaderCard) cardSet.findCard("LC-0-RB-D"));
        assertEquals(Actions.DISCARDLEADER,discardAction.getActionType());
    }


    @Test
    public void execute(){
        Player p1 = new Player("pimar116"), p2 = new Player("fede"), p3 = new Player("riccardo");
        List<Player> playerList = new ArrayList<>();
        playerList.add(p1);
        playerList.add(p2);
        playerList.add(p3);

        //prova a scartare una risorsa
        p1.getPersonalDashboard().addToTray(new Marble(Resource.COIN));
        assertEquals(1,p1.getPersonalDashboard().getTray().size());
        Action discardAction = new DiscardAction(playerList,p1,"RY",false);
        discardAction.execute();
        assertEquals(0,p1.getPersonalDashboard().getTray().size());
        assertEquals(0,p1.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(1,p2.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(1,p3.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());

        //prova a scartare una biglia nera
        p2.getPersonalDashboard().addToTray(new Marble(SpecialType.BLACK));
        discardAction = new DiscardAction(playerList,p2,"RY",true);
        discardAction.execute();
        assertEquals(0,p2.getPersonalDashboard().getTray().size());
        assertEquals(1,p1.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(1,p2.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(2,p3.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());

        //prova a scartare una risorsa non presente nel tray
        discardAction = new DiscardAction(playerList,p2,"RY",false);
        try {
            discardAction.execute();
        }catch (ActionException e){
            assertEquals("marble not present in the tray",e.getMessage());
        }

        //prova a scartare una biglia nera non presente nel tray
        discardAction = new DiscardAction(playerList,p2,"RY",true);
        try {
            discardAction.execute();
        }catch (ActionException e){
            assertEquals("marble not present in the tray",e.getMessage());
        }


        //prova a scartare una carta leader
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        p1.getPersonalDashboard().addCard(cardSet.findCard("LC-0-RB-D"));
        discardAction = new DiscardAction(p1,(LeaderCard) cardSet.findCard("LC-0-RB-D"));
        discardAction.execute();
        assertEquals(0,p1.getPersonalDashboard().getLeadCardsDeck().size());
        assertEquals(2,p1.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(1,p2.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(2,p3.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());

        //prova a scartare una carta leader non posseduta
        try{
            discardAction = new DiscardAction(p1,(LeaderCard) cardSet.findCard("LC-0-RB-D"));
            discardAction.execute();
        }catch (ActionException e){
            assertEquals("leader card not present in the deck",e.getMessage());
        }

        //prova a scartare una carta nulla
        try{
            discardAction = new DiscardAction(p1,null);
            discardAction.execute();
        }catch (ActionException e){
            assertEquals("leader card not present in the deck",e.getMessage());
        }

        //caso giocatore singolo
        SinglePlayer singlePlayer = new SinglePlayer("test",cardSet);
        discardAction = new DiscardAction(singlePlayer,cardSet,Color.BLUE);
        int cardsNumber = cardSet.getDevCardList().size();
        discardAction.execute();
        assertEquals(cardsNumber-2,cardSet.getDevCardList().size());
        boolean cardsEnded = false;

        //prova a scartare carte blu finché non ce ne sono più
        for(Color color : Color.values()){
            while (!cardsEnded){
                try {
                    new DiscardAction(singlePlayer,cardSet,color).execute();
                }catch (ActionException e){
                    assertEquals("end of the game",e.getMessage());
                    cardsEnded = true;
                }
            }
        }


    }
  /*   @Test
    public void execute() {
        GameController gc = new GameController();
        Player p1 = new Player("pimar116"), p2 = new Player("fede"), p3 = new Player("riccardo");
        List<Player> playerList = new ArrayList<>();
        playerList.add(p1);
        playerList.add(p2);
        playerList.add(p3);
        try {
            p1.increaseTrackPosition(7);
        } catch (VaticanReportException e) {
            // This Vatican Report shouldn't be called according to p1's position increment
            //gc.vaticanReport(8);
        }
        assertEquals(2, p1.getPopesFavorTrackPoints());

        try {
            (new DiscardAction(playerList, p2)).execute();
        } catch (VaticanReportException ex) {
            //gc.vaticanReport(8);
        }

        assertEquals(4, p1.getPopesFavorTrackPoints());
        assertEquals(0, p2.getPopesFavorTrackPoints());
        assertEquals(0, p3.getPopesFavorTrackPoints());
        DiscardAction discardAction = new DiscardAction(playerList, p3);
        for (int i = 0; i < 8; i++) {
            try {
                discardAction.execute();
            } catch (VaticanReportException ex1) {
                //gc.vaticanReport(16);
            }
        }
        assertEquals(14, p1.getPopesFavorTrackPoints());
        assertEquals(2, p2.getPopesFavorTrackPoints());
        assertEquals(0, p3.getPopesFavorTrackPoints());
    }
*/
    /**
     * This method tests the case in which Lorenzo takes 2 Development Cards, hence they have to be discarded from CardSet
     */
    /* @Test
    public void LorenzoTakes2DevCards() {
        SinglePlayerGameController spgc = new SinglePlayerGameController("pimar116");
        assertEquals(48, spgc.getCards().getDevCardList().size());
        assertEquals(64, spgc.getCards().getCardList().size());
        // Test "-2 Carte Sviluppo viola"
        DiscardAction discardAction = new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.PURPLE);
        try {
            discardAction.execute();
        } catch (VaticanReportException ignored) {}
        assertEquals(46, spgc.getCards().getDevCardList().size());
        assertEquals(62, spgc.getCards().getCardList().size());
        // Test "-2 Carte Sviluppo viola", ma una è di livello 1, l'altra di livello 2
        try {
            (new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.PURPLE)).execute();
        } catch (VaticanReportException ignored) {}
        assertEquals(44, spgc.getCards().getDevCardList().size());
        assertEquals(60, spgc.getCards().getCardList().size());
        // Esauriamo le carte viola dal card set
        try {
            for (int i = 0; i < 4; i++) {
                (new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.PURPLE)).execute();
            }
        } catch (VaticanReportException ignored) {}
        assertEquals(36, spgc.getCards().getDevCardList().size());
        assertEquals(52, spgc.getCards().getCardList().size());
        // E testiamo l'eccezione del colore
        try {
            (new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.PURPLE)).execute();
        } catch (VaticanReportException ignored) {
        } catch (NoSuchElementException ex) {
            assertEquals("There are no other cards with color PURPLE to discard...", ex.getMessage());
        }
        // Proviamo un altro colore
        try {
            for (int i = 0; i < 6; i++) {
                (new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.GREEN)).execute();
            }
        } catch (VaticanReportException ignored) {}
        assertEquals(24, spgc.getCards().getDevCardList().size());
        assertEquals(40, spgc.getCards().getCardList().size());
        // Ed esauriamo il cardset
        try {
            for (int i = 0; i < 6; i++) {
                (new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.YELLOW)).execute();
                (new DiscardAction(spgc.getSinglePlayer(), spgc.getCards(), Color.BLUE)).execute();
            }
        } catch (VaticanReportException ignored) {}
        assertEquals(0, spgc.getCards().getDevCardList().size());
        assertEquals(16, spgc.getCards().getCardList().size());
    } */


}