package it.polimi.ingsw.model.containers;

import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.junit.Assert.*;

/**
 * Test class fot Model.CardSet.java
 * @author Federico Garzia
 */
public class CardSetTest {

    @Test
    public void addCardTest(){
        CardSet set = new CardSet();
        List<Marble> in = new ArrayList<>();
        in.add(new Marble(Resource.SHIELD));
        List<Marble> out = new ArrayList<>();
        out.add(new Marble(Resource.COIN));
        Rule rule = new Rule(in,out);
        Card card1 = new DevelopmentCard(Level.LEVEL1, Color.BLUE, rule, 1);
        Card card2 = new DevelopmentCard(Level.LEVEL1, Color.BLUE, rule, 1);
        try {
            set.addCard(card1);
            set.addCard(card2);
        }catch (IllegalArgumentException e){
            assertEquals(e.getMessage(),"Card already in the deck");
        }

    }
    @Test
    public void takeCardTest(){
        CardSet set = new CardSet();
        //provo a togliere una carta dal set vuoto
        try{
            set.takeCard("123");
        }catch (IllegalArgumentException e){
            assertEquals(e.getMessage(),"no card id matches [123]");
        }

    }

    @Test
    public void loadCardsTest() {
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        assertEquals(64, cardSet.getCardList().size());
        assertEquals(48, cardSet.getDevCardList().size());
        JSONParser jsonParser = new JSONParser();
        List<String> cardIds = null;
        // try (FileReader reader = new FileReader("src/main/resources/config/cards.json")) {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/config/cards.json");
            Object obj = jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            JSONObject readObject = (JSONObject) obj;
            JSONArray cardsList = (JSONArray) readObject.get("cards");
            cardIds = new ArrayList<>();
            for (Object o : cardsList) {
                cardIds.add((String) ((JSONObject) o).get("id"));
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        for (Card card : cardSet.getCardList()) {
            assertTrue(cardIds.contains(card.getId()));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createJSONObjectTest() {
        List<Marble> in1 = new ArrayList<>();
        in1.add(new Marble(Resource.SERVANT));

        List<Marble> out1 = new ArrayList<>();
        out1.add(new Marble(Resource.STONE));

        Rule firstProduction = new Rule(in1, out1);

        DevelopmentCard card1 = new DevelopmentCard(Level.LEVEL1, Color.BLUE, firstProduction, 2);

        card1.setRequirements(Arrays.asList(Resource.COIN, Resource.SERVANT, Resource.STONE));

        List<Resource> in3 = new ArrayList<>();
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);
        in3.add(Resource.STONE);

        List<Marble> n = new ArrayList<>();

        BonusType b = BonusType.STORAGE;

        List<Marble> in4 = new ArrayList<>();
        in4.add(new Marble(Resource.SERVANT));
        in4.add(new Marble(Resource.SERVANT));

        Rule bonusRule = new Rule(in4, n);

        List<Color> reqC = new ArrayList<>();

        LeaderCard card2 = new LeaderCard(bonusRule, b, reqC, 0, 3);

        card2.setRequirements(in3);

        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        cardSet.takeCard(card1.getId());

        JSONArray remainingCards = (JSONArray) cardSet.createJSONObject(card1).get("remaining Development cards");

        assertTrue(remainingCards.stream().noneMatch(id -> id.equals(card1.getId())));
    }
}