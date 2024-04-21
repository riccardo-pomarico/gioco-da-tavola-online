package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PurchaseActionTest {


    @Test
    public void executeTest() {
        Player targetPlayer = new Player("test");
        Market market = new Market();
        market.init();
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        // test sulla funzione "prelievo risorse dal mercato"
        //prova inserire stringhe errate
        String[] wrongPositions = new String[] {"","R-","R-4","C-5","R--1","C--1","--","CC","RR","ABCDE","012345"};
        Action purchaseAction;
        for(int i = 0; i< wrongPositions.length; i++){
             purchaseAction = new PurchaseAction(targetPlayer,wrongPositions[i], market);
            try{
                purchaseAction.execute();
            }catch (ActionException e){
                assertEquals("vector is not in the correct format",e.getMessage());
            }
        }
        //prova a prelevare da tutte le righe e colonne, targetPlayer non ha bonus
            int PFTPostition = targetPlayer.getPersonalDashboard().getPopesFavorTrack().getTrackPosition();
            for (int j = 0; j < 3; j++) {
                Marble[][] marketMatrix = market.getMarketMatrix();
                purchaseAction = new PurchaseAction(targetPlayer, "R-" + j, market);
                purchaseAction.execute();
                for (int i = 0; i < 4; i++) {
                    Marble m = marketMatrix[j][i];
                    if (Marble.isType(SpecialType.FAITHPOINT,m) || Marble.isType(SpecialType.BLANK,m)) {
                        assertFalse(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                    } else {
                        assertTrue(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                        targetPlayer.getPersonalDashboard().removeFromTray(m);
                    }
                }
            }
            for (int j = 0; j < 4; j++) {
                Marble[][] marketMatrix = market.getMarketMatrix();
                purchaseAction = new PurchaseAction(targetPlayer, "C-" + j, market);
                purchaseAction.execute();
                for (int i = 0; i < 3; i++) {
                    Marble m = marketMatrix[i][j];
                    if (Marble.isType(SpecialType.FAITHPOINT,m) || Marble.isType(SpecialType.BLANK,m)) {
                        assertFalse(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                    } else {
                        assertTrue(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                        targetPlayer.getPersonalDashboard().removeFromTray(m);

                    }
                }
            }
        //prova a prelevare da tutte le righe e colonne, targetPlayer ha bonus di tipo whiteMarble, prova con ogni tipo di leader card con bonus White Marble
        LeaderCard[] cards = new LeaderCard[] {
                (LeaderCard) cardSet.findCard("LC-0-RY-B"),
                (LeaderCard) cardSet.findCard("LC-0-RB-B"),
                (LeaderCard) cardSet.findCard("LC-0-RG-B"),
                (LeaderCard) cardSet.findCard("LC-0-RY-B")
            };
        for(LeaderCard card : cards) {
            card.activate();
            String bonusOut = card.getBonusRule().getOutRule().get(0).generateId();
            targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(card);
            for (int j = 0; j < 3; j++) {
                Marble[][] marketMatrix = market.getMarketMatrix();
                purchaseAction = new PurchaseAction(targetPlayer, "R-" + j, market);
                purchaseAction.execute();
                for (int i = 0; i < 4; i++) {
                    Marble m = marketMatrix[j][i];
                    if (Marble.isType(SpecialType.BLANK,m)) {
                        assertTrue(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(bonusOut)));
                        targetPlayer.getPersonalDashboard().removeFromTray(new Marble(bonusOut));
                    } else if(Marble.isType(SpecialType.FAITHPOINT,m)){
                        assertFalse(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                    }else {
                        assertTrue(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                        targetPlayer.getPersonalDashboard().removeFromTray(m);
                    }
                }
            }
            for (int j = 0; j < 4; j++) {
                Marble[][] marketMatrix = market.getMarketMatrix();
                purchaseAction = new PurchaseAction(targetPlayer, "C-" + j, market);
                purchaseAction.execute();
                for (int i = 0; i < 3; i++) {
                    Marble m = marketMatrix[i][j];
                    if (m.generateId().equals("SW")) {
                        assertTrue(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(bonusOut)));
                        targetPlayer.getPersonalDashboard().removeFromTray(new Marble(bonusOut));

                    } else if(Marble.isType(SpecialType.FAITHPOINT,m)){
                        assertFalse(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                    }else {
                        assertTrue(targetPlayer.getPersonalDashboard().getTray().stream().anyMatch(a -> a.generateId().equals(m.generateId())));
                        targetPlayer.getPersonalDashboard().removeFromTray(m);

                    }
                }
            }
        }
        //rimuove tutte le carte dal deck delle leadercards
        LeaderCard[] finalCards = cards;
        targetPlayer.getPersonalDashboard().getLeadCardsDeck().removeIf(a -> Arrays.stream(finalCards).anyMatch(c -> c.generateId().equals(a.generateId())));

        //test sulla funzione "acquisto carta sviluppo"
        //prova a comprare ogni carta sviluppo nonostante manchino le risorse
        cardSet = new CardSet();
        cardSet.loadCards();
        //prova con le carte di livello 1
        for(DevelopmentCard card : cardSet.getDevCardList().stream().filter(card -> card.getLevel() == 1).collect(Collectors.toList())) {
            //prova prima col forziere
            try {
                purchaseAction = new PurchaseAction(targetPlayer, card, 1, cardSet, 1);
                purchaseAction.execute();
            } catch (ActionException e) {
                assertEquals("not enough resources from strongbox", e.getMessage());
            }
            //poi col magazzino
            try{
                purchaseAction = new PurchaseAction(targetPlayer,card,2,cardSet, 2);
                purchaseAction.execute();
            }catch (ActionException e){
                assertEquals("not enough resources from warehouse",e.getMessage());
            }
            targetPlayer = new Player("test");
        }
        //prova a comprare ogni carta avendo i requisiti per comprarla
        List<Resource> requirements;
        for(DevelopmentCard card : cardSet.getDevCardList().stream().filter(card -> card.getLevel() == 1).collect(Collectors.toList())) {
            requirements = card.getRequirements();
            for(Resource r : requirements){
                targetPlayer.getPersonalDashboard().depositResource(1,0,r);
            }
            purchaseAction = new PurchaseAction(targetPlayer, card, 1, cardSet, 3);
            purchaseAction.execute();
            assertTrue(targetPlayer.getPersonalDashboard().getDevCardsDeck().stream().anyMatch(c -> c.generateId().equals(card.generateId())));
            assertEquals(1,targetPlayer.getPersonalDashboard().getDevCardsDeck().size());
            targetPlayer = new Player("test");
        }
        cardSet = new CardSet();
        cardSet.loadCards();
        assertEquals(0,targetPlayer.getPersonalDashboard().getStrongBox().size());
        //prova a comprare carte sviluppo usando il bonus sconto
        List<Resource> discount;
        List<Resource> strongBoxAfterDiscount = new ArrayList<>();
        cards = new LeaderCard[] {
                (LeaderCard) cardSet.findCard("LC-0-RP-D"),
                (LeaderCard) cardSet.findCard("LC-0-RB-D"),
                (LeaderCard) cardSet.findCard("LC-0-RG-D"),
                (LeaderCard) cardSet.findCard("LC-0-RY-D")
        };
        //prova prima con un bonus attivo alla volta

        for(int i = 0; i< cards.length; i++) {
            for (DevelopmentCard card : cardSet.getDevCardList().stream().filter(card -> card.getLevel() == 1).collect(Collectors.toList())) {
                targetPlayer = new Player("test");
                cards[i].activate();
                targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(cards[i]);
                requirements = card.getRequirements();
                for (Resource r : requirements) {
                    targetPlayer.getPersonalDashboard().depositResource(1, 0, r);
                }
                discount = Marble.toResources(cards[i].getBonusRule().apply(Marble.toMarbles(requirements)));
                strongBoxAfterDiscount = targetPlayer.getPersonalDashboard().getStrongBox();
                List<Resource> finalStrongBoxAfterDiscount = new ArrayList<>(strongBoxAfterDiscount);
                discount.forEach(a -> finalStrongBoxAfterDiscount.remove(finalStrongBoxAfterDiscount.stream().filter(b-> b == a).findFirst().get()));
                purchaseAction = new PurchaseAction(targetPlayer,card,1,cardSet,1);
                purchaseAction.execute();
                assertArrayEquals(strongBoxAfterDiscount.toArray(),targetPlayer.getPersonalDashboard().getStrongBox().toArray());
            }
        }
        //prova con due bonus attivi contemporaneamente
        for(int i = 0; i< cards.length-1; i++) {
            for (DevelopmentCard card : cardSet.getDevCardList().stream().filter(card -> card.getLevel() == 1).collect(Collectors.toList())) {
                targetPlayer = new Player("test");
                cards[i].activate();
                cards[i+1].activate();
                targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(cards[i]);
                targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(cards[i+1]);
                requirements = card.getRequirements();
                for (Resource r : requirements) {
                    targetPlayer.getPersonalDashboard().depositResource(1, 0, r);
                }
                discount = Marble.toResources(cards[i].getBonusRule().apply(Marble.toMarbles(requirements)));
                discount = Marble.toResources(cards[i+1].getBonusRule().apply(Marble.toMarbles(discount)));
                strongBoxAfterDiscount = targetPlayer.getPersonalDashboard().getStrongBox();
                List<Resource> finalStrongBoxAfterDiscount = new ArrayList<>(strongBoxAfterDiscount);
                discount.forEach(a -> finalStrongBoxAfterDiscount.remove(finalStrongBoxAfterDiscount.stream().filter(b-> b == a).findFirst().get()));
                purchaseAction = new PurchaseAction(targetPlayer,card,1,cardSet,3);
                purchaseAction.execute();
                assertArrayEquals(strongBoxAfterDiscount.toArray(),targetPlayer.getPersonalDashboard().getStrongBox().toArray());

            }
        }
        //prova ad usare tre bonus
        targetPlayer = new Player("test");
        targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(cards[0]);
        targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(cards[1]);
        targetPlayer.getPersonalDashboard().getLeadCardsDeck().add(cards[2]);
        cardSet = new CardSet();
        cardSet.loadCards();
        purchaseAction = new PurchaseAction(targetPlayer,(DevelopmentCard) cardSet.getCardList().stream().filter(card -> card.generateId().charAt(0) == 'D').collect(Collectors.toList()).get(0),1,cardSet,3);
        /*try{
            purchaseAction.execute();
        }catch (ActionException e){
            assertEquals("there cannot be more than two leader card activated",e.getMessage());
        } */

        //test sul prelievo da warehouse e forziere
        cardSet = new CardSet();
        cardSet.loadCards();
        DevelopmentCard card = (DevelopmentCard) cardSet.findCard("DC-1-GREEN-RPRP-RYRBRG-3");
        targetPlayer = new Player("test");
        targetPlayer.getPersonalDashboard().depositResource(1,2,Resource.SHIELD);
        targetPlayer.getPersonalDashboard().depositResource(2,2,Resource.SHIELD);
        targetPlayer.getPersonalDashboard().depositResource(2,2,Resource.SHIELD);
        purchaseAction = new PurchaseAction(targetPlayer,card,3,cardSet,1);
        try{
            purchaseAction.execute();
            assertEquals(1,targetPlayer.getPersonalDashboard().getDevCardsDeck().size());
        }catch (Exception e){
            e.printStackTrace();
        }
        cardSet = new CardSet();
        cardSet.loadCards();
        card = (DevelopmentCard) cardSet.findCard("DC-1-GREEN-RPRP-RYRBRG-3");
        targetPlayer = new Player("test");
        targetPlayer.getPersonalDashboard().depositResource(1,2,Resource.SHIELD);
        targetPlayer.getPersonalDashboard().depositResource(2,2,Resource.SHIELD);
        purchaseAction = new PurchaseAction(targetPlayer,card,3,cardSet,1);
        try{
            purchaseAction.execute();
        }catch (ActionException e){
            assertEquals("you don't have enough resources!",e.getMessage());

        }
    }
}