package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.resources.*;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class tests the ActivationAction class
 */

public class ActivationActionTest {

    @Test
    public void execute() {

        //Il player 1 attiva la produzione di una carta sviluppo
        Player p1 = new Player("1");

        List<Marble> in1 = new ArrayList<>();
        in1.add(new Marble(Resource.SERVANT));

        p1.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);

        List<Marble> out1 = new ArrayList<>();
        out1.add(new Marble(Resource.STONE));

        Rule firstProduction = new Rule(in1, out1);

        DevelopmentCard card1 = new DevelopmentCard(Level.LEVEL1, Color.BLUE, firstProduction, 2);
        List<DevelopmentCard> dc = new ArrayList<>();
        dc.add(card1);

        ActivationAction activationAction1 = new ActivationAction(p1, dc, 2);

        try {
            activationAction1.execute();
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }

        Resource r1;

        r1 = p1.getPersonalDashboard().getStrongBox().get(0);

        assertEquals(Resource.STONE, r1);



        //Il player 2 attiva la produzione base
        Player p2 = new Player("2");

        p2.getPersonalDashboard().depositResource(2, 1, Resource.COIN);
        p2.getPersonalDashboard().depositResource(2, 0, Resource.SERVANT);

        ActivationAction activationAction2 = new ActivationAction(p2, Resource.COIN, Resource.SERVANT, Resource.SHIELD);

        try {
            activationAction2.execute();
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }

        Resource r2 = p2.getPersonalDashboard().getStrongBox().get(0);

        assertEquals(Resource.SHIELD, r2);




        //Il player 3 gioca la carta leader con risorse come requisiti
        Player p3 = new Player("3");

        List<Resource> in3 = new ArrayList<>();
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);

        p3.getPersonalDashboard().depositResource(1, 0, Resource.STONE);
        p3.getPersonalDashboard().depositResource(1, 0, Resource.STONE);
        p3.getPersonalDashboard().depositResource(1, 0, Resource.STONE);
        p3.getPersonalDashboard().depositResource(1, 0, Resource.STONE);
        p3.getPersonalDashboard().depositResource(1, 0, Resource.STONE);

        List<Marble> n = new ArrayList<>();

        BonusType b = BonusType.STORAGE;

        List<Marble> in4 = new ArrayList<>();
        in4.add(new Marble(Resource.SERVANT));
        in4.add(new Marble(Resource.SERVANT));

        Rule bonusRule = new Rule(in4, n);

        List<Color> reqC = new ArrayList<>();

        LeaderCard card2 = new LeaderCard(bonusRule, b, reqC, 0, 3);

        p3.getPersonalDashboard().addCard(card2);
        card2.setRequirements(in3);

        ActivationAction activationAction3 = new ActivationAction(p3, card2);

        try {
            activationAction3.execute();
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }

        assertEquals(true, card2.hasBeenActivated());




        //Il player 4 gioca la carta leader con carte sviluppo come requisiti
        Player p4 = new Player("4");

        List<Color> reqC1 = new ArrayList<>();
        reqC1.add(Color.GREEN);
        reqC1.add(Color.YELLOW);

        p4.getPersonalDashboard().addCard(new DevelopmentCard(Level.LEVEL1, Color.GREEN, null, 1));
        p4.getPersonalDashboard().addCard(new DevelopmentCard(Level.LEVEL1, Color.YELLOW, null, 1));

        BonusType b1 = BonusType.DISCOUNT;

        List<Marble> in5 = new ArrayList<>();
        in5.add(new Marble(Resource.SERVANT));

        Rule bonusRule1 = new Rule(in5, n);

        LeaderCard card3 = new LeaderCard(bonusRule1, b1, reqC1, 0, 2);

        card3.setRequirements(null);
        p4.getPersonalDashboard().addCard(card3);
        ActivationAction activationAction4 = new ActivationAction(p4, card3);

        try {
            activationAction4.execute();
        }  catch (IllegalArgumentException e) {
            e.getMessage();
        }

        assertEquals(true, card3.hasBeenActivated());





        // Il player 5 sperimenta un po' di casi di produzione con carte sviluppo multiple.
        Player p5 = new Player("5");

        List<Marble> firstIn = new ArrayList<>();
        List<Marble> secondIn = new ArrayList<>();
        List<Marble> thirdIn = new ArrayList<>();

        firstIn.add(new Marble(Resource.COIN));
        firstIn.add(new Marble(Resource.SERVANT));
        secondIn.add(new Marble(Resource.STONE));
        secondIn.add(new Marble(Resource.STONE));
        secondIn.add(new Marble(Resource.SERVANT));
        thirdIn.add(new Marble(Resource.STONE));
        thirdIn.add(new Marble(Resource.STONE));

        List<Marble> firstOut = new ArrayList<>();
        List<Marble> secondOut = new ArrayList<>();
        List<Marble> thirdOut = new ArrayList<>();

        firstOut.add(new Marble(SpecialType.FAITHPOINT));
        firstOut.add(new Marble(SpecialType.FAITHPOINT));
        secondOut.add(new Marble(Resource.COIN));
        secondOut.add(new Marble(Resource.SERVANT));
        thirdOut.add(new Marble(Resource.SHIELD));

        // Il giocatore 5 soddisfa i requisiti della carta 1, ma non della 2.
        p5.getPersonalDashboard().depositResource(2, 0, Resource.COIN);
        p5.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);

        Rule production1 = new Rule(firstIn, firstOut);
        Rule production2 = new Rule(secondIn, secondOut);
        Rule production3 = new Rule(thirdIn, thirdOut);

        DevelopmentCard firstCard = new DevelopmentCard(Level.LEVEL1, Color.GREEN, production1, 1);
        DevelopmentCard secondCard = new DevelopmentCard(Level.LEVEL1, Color.YELLOW, production2, 2);
        DevelopmentCard thirdCard = new DevelopmentCard(Level.LEVEL1, Color.YELLOW, production3, 2);

        List<DevelopmentCard> dCards = new ArrayList<>();
        dCards.add(firstCard);
        dCards.add(secondCard);
        dCards.add(thirdCard);

        Action action5 = new ActivationAction(p5, dCards, 2);

        try {
            action5.execute();
        } catch (ActionException ex) {
            assertEquals(ex.getMessage(), "you don't have enough resources!");
        }

        // Adesso il giocatore 5 soddisfa anche i requisiti della carta 2.
        p5.getPersonalDashboard().depositResource(2, 2, Resource.STONE);
        p5.getPersonalDashboard().depositResource(2, 2, Resource.STONE);
        p5.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);

        // E adesso vediamo le risorse nel forziere.
        p5.getPersonalDashboard().depositResource(1, 0, Resource.STONE);

        try {
            action5.execute();
        } catch (ActionException ex) {
            assertEquals(ex.getMessage(), "you don't have enough resources!");
        }

        p5.getPersonalDashboard().depositResource(1, 0, Resource.STONE);

        action5.execute();

        assertEquals(p5.getPersonalDashboard().getStrongBox().get(0), Resource.COIN);
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(1), Resource.SERVANT);
        assertEquals(2, p5.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(2), Resource.SHIELD);
        assertEquals(3, p5.getPersonalDashboard().getStrongBox().size());
        for (int i = 0; i < p5.getPersonalDashboard().getWarehouse().getShelves().size(); i++){
            assertTrue(Arrays.stream(p5.getPersonalDashboard().getWarehouse().getShelves().get(i)).allMatch(Objects::isNull));
        }

        dCards.remove(thirdCard);

        p5.getPersonalDashboard().depositResource(2, 0, Resource.COIN);
        p5.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);
        p5.getPersonalDashboard().depositResource(2, 2, Resource.STONE);
        p5.getPersonalDashboard().depositResource(2, 2, Resource.STONE);
        p5.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);

        action5.execute();

        assertEquals(5, p5.getPersonalDashboard().getStrongBox().size());
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(3), Resource.COIN);
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(4), Resource.SERVANT);
        for (int i = 0; i < p5.getPersonalDashboard().getWarehouse().getShelves().size(); i++){
            assertTrue(Arrays.stream(p5.getPersonalDashboard().getWarehouse().getShelves().get(i)).allMatch(Objects::isNull));
        }

        // Adesso vediamo il caso di risorse smezzate tra i due contenitori
        p5.getPersonalDashboard().depositResource(2, 0, Resource.COIN);
        p5.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);
        p5.getPersonalDashboard().depositResource(2, 2, Resource.STONE);
        p5.getPersonalDashboard().depositResource(2, 2, Resource.STONE);
        p5.getPersonalDashboard().depositResource(1, 0, Resource.STONE);
        p5.getPersonalDashboard().depositResource(2, 1, Resource.SERVANT);
        p5.getPersonalDashboard().depositResource(1, 0, Resource.STONE);
        assertEquals(7, p5.getPersonalDashboard().getStrongBox().size());
        assertEquals(5, p5.getPersonalDashboard().getStrongBox().stream().filter(r -> r != Resource.STONE).count());

        dCards.add(thirdCard);

        action5.execute();

        assertEquals(8, p5.getPersonalDashboard().getStrongBox().size());
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(5), Resource.COIN);
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(6), Resource.SERVANT);
        assertEquals(6, p5.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        assertEquals(p5.getPersonalDashboard().getStrongBox().get(7), Resource.SHIELD);
        assertEquals(0, p5.getPersonalDashboard().countStrongBox(Resource.STONE));
        assertEquals(2, p5.getPersonalDashboard().countStrongBox(Resource.SHIELD));
        assertEquals(3, p5.getPersonalDashboard().countStrongBox(Resource.COIN));
        assertEquals(3, p5.getPersonalDashboard().countStrongBox(Resource.SERVANT));


        //testa i colori delle leaderCard
        Player player = new Player("test");
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        LeaderCard l = (LeaderCard) cardSet.findCard("LC-RP-SBSR-P");
        DevelopmentCard d = (DevelopmentCard) cardSet.findCard("DC-1-BLUE-RP-RG-2");
        player.getPersonalDashboard().addCard(l);
        player.getPersonalDashboard().addCard(d);
        Action activate = new ActivationAction(player,l);

        try {
            activate.execute();
        }catch (ActionException e){
            assertEquals("Cannot activate leader: you don't have enough cards with the required level",e.getMessage());
        }
        d = (DevelopmentCard) cardSet.findCard("DC-2-BLUE-RYRG-RPRPRP-6");
        player.getPersonalDashboard().addCard(d);
        activate.execute();

        assertEquals(1,player.getPersonalDashboard().getLeadCardsDeck().stream().filter(Card::hasBeenActivated).count());
    }
}
