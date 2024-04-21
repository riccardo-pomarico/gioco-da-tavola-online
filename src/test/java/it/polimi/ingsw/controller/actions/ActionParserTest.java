package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ActionParserTest {

    @Test
    public void actionTest() {
    }

    @Test
    public void targetPlayerTest() {

    }

    @Test
    public void developmentCardTest() {
        ActionParser actionParser = new ActionParser();
        try{
            JSONObject request = actionParser.developmentCard("LC-0-RP-S").buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[LC-0-RP-S] is not a development card",e.getMessage());
        }
        try{
            JSONObject request = actionParser.developmentCard("").buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[] is not a development card",e.getMessage());
        }
        try{
            JSONObject request = actionParser.developmentCard("123").buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[123] is not a development card",e.getMessage());
        }
        //prova con liste di carte
        List<String> cards = new ArrayList<>();
        cards.add("");
        cards.add("");
        try{
            JSONObject request = actionParser.developmentCardsList(cards).buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[] is not a development card",e.getMessage());
        }
        cards = new ArrayList<>();
        cards.add("ABCD");
        cards.add("DC-3-YELLOW-RB-RPSRSRSR-11");
        try{
            JSONObject request = actionParser.developmentCardsList(cards).buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[ABCD] is not a development card",e.getMessage());
        }
    }

    @Test
    public void leaderCardTest() {
        ActionParser actionParser = new ActionParser();
        try{
            JSONObject request = actionParser.leaderCard("DC-3-YELLOW-RB-RPSRSRSR-11").buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[DC-3-YELLOW-RB-RPSRSRSR-11] is not a leader card",e.getMessage());
        }
        try{
            JSONObject request = actionParser.leaderCard("").buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[] is not a leader card",e.getMessage());
        }
        try{
            JSONObject request = actionParser.leaderCard("123").buildRequest();
        }catch (IllegalArgumentException e){
            assertEquals("[123] is not a leader card",e.getMessage());
        }
        //input corretto
        JSONObject request = actionParser.leaderCard("LC-0-RP-S").buildRequest();
        assertEquals("LC-0-RP-S",request.get("leaderCard").toString());

    }

    @Test
    public void lineTest() {
        ActionParser actionParser = new ActionParser();
        String[] wrongInputs = new String[] {"","qwertytytrhgfhhhhghghhghhghjj","123","ABC","0","R0","R-5","C-8","C3"};
        for(String input : wrongInputs){
            try{
                JSONObject request = actionParser.line(input).buildRequest();
            }catch (IllegalArgumentException e){
                assertEquals("line is not in the correct format",e.getMessage());
            }
        }

    }
    @Test
    public void slotTest(){
        ActionParser actionParser = new ActionParser();
        int[] wrongInputs = new int[] {5,7,-666,-12345667,1234546566};
        for(int input : wrongInputs){
            try{
                JSONObject request = actionParser.slot(input).buildRequest();
            }catch (IllegalArgumentException e){
                assertEquals("invalid slot",e.getMessage());
            }
        }
        JSONObject request = actionParser.slot(0).buildRequest();
        assertEquals(0,(int)request.get("slot"));
        request = actionParser.slot(1).buildRequest();
        assertEquals(1,(int)request.get("slot"));
        request = actionParser.slot(2).buildRequest();
        assertEquals(2,(int)request.get("slot"));
    }

    @Test
    public void targetDepositTest() {
        ActionParser actionParser = new ActionParser();
        int[] wrongInputs = new int[] {0,5,7,-666,-12345667,1234546566};
        for(int input : wrongInputs){
            try{
                JSONObject request = actionParser.targetDeposit(input).buildRequest();
            }catch (IllegalArgumentException e){
                assertEquals("targetDeposit is not in the correct format",e.getMessage());
            }
        }
        JSONObject request = actionParser.targetDeposit(1).buildRequest();
        assertEquals(1,(int)request.get("targetDeposit"));
        request = actionParser.targetDeposit(2).buildRequest();
        assertEquals(2,(int)request.get("targetDeposit"));

    }
    @Test
    public void shelfTest(){
        ActionParser actionParser = new ActionParser();
        int[] wrongInputs = new int[] {0,5,7,-666,-12345667,1234546566};
        for(int input : wrongInputs){
            try{
                JSONObject request = actionParser.shelf(input).buildRequest();
            }catch (IllegalArgumentException e){
                assertEquals("shelf is not in the correct format",e.getMessage());
            }
        }
        JSONObject request = actionParser.shelf(0).buildRequest();
        assertEquals(0,(int)request.get("shelf"));
        request = actionParser.shelf(1).buildRequest();
        assertEquals(1,(int)request.get("shelf"));
        request = actionParser.shelf(2).buildRequest();
        assertEquals(2,(int)request.get("shelf"));
        request = actionParser.shelf(3).buildRequest();
        assertEquals(3,(int)request.get("shelf"));
    }
    @Test
    public void blackMarbleTest(){
        ActionParser actionParser = new ActionParser();
        JSONObject request = actionParser.blackMarble(true).buildRequest();
        assertTrue((boolean) request.get("blackMarble"));
        request = actionParser.blackMarble(false).buildRequest();
        assertFalse((boolean) request.get("blackMarble"));
    }
    @Test
    public void marbleTest(){
        ActionParser actionParser = new ActionParser();
        String[] wrongInputs = new String[] {"","qwertytytrhgfhhhhghghhghhghjj","123","ABC","0","S1","R23","RQ","ds£$"};
        for(String input : wrongInputs){
            try{
                JSONObject request = actionParser.marble(input).buildRequest();
            }catch (IllegalArgumentException e){
                assertEquals("marble is not in the correct format",e.getMessage());
            }
        }
        String[] correctInputs = new String[] {"SB","SR","SW","RP","RB","RY","RG"};
        for(String input : correctInputs){
            JSONObject request = actionParser.marble(input).buildRequest();
            assertEquals(input,request.get("marble"));
        }

    }
    @Test
    public void colorTest(){
        ActionParser actionParser = new ActionParser();
        Arrays.asList(Color.values()).forEach(color -> {
            JSONObject request = actionParser.color(color).buildRequest();
            assertEquals(color,Color.valueOf(request.get("color").toString()));
        });
    }
    @Test
    public void resourcesTest(){
        ActionParser actionParser = new ActionParser();
        Resource[] resources = new Resource[] {Resource.SHIELD,Resource.COIN,Resource.SERVANT,Resource.STONE};
        JSONObject request = actionParser.resources(resources).buildRequest();
        assertEquals(Resource.SHIELD,Resource.valueOf(request.get("resources").toString().split(",")[0].substring(1)));
        assertEquals(Resource.COIN,Resource.valueOf(request.get("resources").toString().split(",")[1]));
        assertEquals(Resource.SERVANT,Resource.valueOf(request.get("resources").toString().split(",")[2]));
    }

    @Test
    public void buildRequestTest() {
        ActionParser requestBuilder = new ActionParser();
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        //prova a fare una request per tipo di action
        JSONObject request = requestBuilder
                .targetPlayer("test")
                .action(Actions.MARKETPURCHASE)
                .line("R-1")
                .buildRequest();
        assertEquals("{\"marble\":null,\"targetPlayer\":\"test\",\"developmentCardsToActivate\":[],\"color\":null,\"leaderCard\":null,\"line\":\"R-1\",\"targetDeposit\":0,\"action\":MARKETPURCHASE,\"developmentCard\":null,\"blackMarble\":false,\"slot\":0,\"shelf\":0}",request.toString());
        requestBuilder = new ActionParser();
        request = requestBuilder
                .targetPlayer("test")
                .action(Actions.DEVCARDPURCHASE)
                .developmentCard("DC-1-YELLOW-RB-RY-2")
                .buildRequest();
        assertEquals("{\"marble\":null,\"targetPlayer\":\"test\",\"developmentCardsToActivate\":[],\"color\":null,\"leaderCard\":null,\"line\":null,\"targetDeposit\":0,\"action\":DEVCARDPURCHASE,\"developmentCard\":\"DC-1-YELLOW-RB-RY-2\",\"blackMarble\":false,\"slot\":0,\"shelf\":0}",request.toString());
        List<String> dCards = new ArrayList<>();
        dCards.add(cardSet.findCard("DC-1-GREEN-RY-SR-1").generateId());
        dCards.add(cardSet.findCard("DC-1-PURPLE-RG-SR-1").generateId());
        requestBuilder = new ActionParser();
        request = requestBuilder
                .targetPlayer("test")
                .action(Actions.DEVCARDPURCHASE)
                .developmentCardsList(dCards)
                .buildRequest();
        assertEquals("{\"marble\":null,\"targetPlayer\":\"test\",\"developmentCardsToActivate\":[\"DC-1-GREEN-RY-SR-1\",\"DC-1-PURPLE-RG-SR-1\"],\"color\":null,\"leaderCard\":null,\"line\":null,\"targetDeposit\":0,\"action\":DEVCARDPURCHASE,\"developmentCard\":null,\"blackMarble\":false,\"slot\":0,\"shelf\":0}", request.toString());

    }

    @Test
    public void buildActionTest() {
        ActionParser requestBuilder = new ActionParser();
        JSONObject request = new JSONObject();
        assertNull(requestBuilder.buildAction(request));


        List<Player> playerList = new ArrayList<>();
        Player player = new Player("test");
        playerList.add(player);
        Market market = new Market();
        market.init();
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        ActionParser actionParser = new ActionParser(playerList,market,cardSet);

        try {
            request = requestBuilder.action(Actions.MARKETPURCHASE).buildRequest();
            Action action = actionParser.buildAction(request);
        }catch (IllegalArgumentException e){
            assertEquals("player field missing or player not in playerList",e.getMessage());
        }

        try {
            request = requestBuilder.action(Actions.MARKETPURCHASE).targetPlayer("test1").buildRequest();
            Action action = actionParser.buildAction(request);
        }catch (IllegalArgumentException e){
            assertEquals("player field missing or player not in playerList",e.getMessage());
        }

        try {
            request = requestBuilder.action(Actions.MARKETPURCHASE).line("r-0").targetPlayer(player.getUsername()).buildRequest();
            Action action = actionParser.buildAction(request);
        }catch (IllegalArgumentException e){
            assertEquals("missing fields for the requested action",e.getMessage());
        }



        //Test su marketPurchase
        request = requestBuilder.action(Actions.MARKETPURCHASE).targetPlayer(player.getUsername()).line("R-0").buildRequest();
        Action action = actionParser.buildAction(request);
        assertEquals(Actions.MARKETPURCHASE,action.getActionType());
        assertEquals(player,action.getPlayer());
        assertFalse(action.multipleExecutions());

        //Test su activateProduction
        List<String> cards = new ArrayList<>();
        cards.add("DC-3-YELLOW-RB-RPSRSRSR-11");
        cards.add("DC-3-GREEN-RG-RYRYRYRB-12");
        cards.add("DC-3-BLUE-RG-RBSRSRSR-11");
        request = requestBuilder.action(Actions.ACTIVATEPRODUCTION).targetPlayer(player.getUsername()).targetDeposit(1).buildRequest();
        action = actionParser.buildAction(request);
        assertEquals(Actions.ACTIVATEPRODUCTION,action.getActionType());
        assertEquals(player,action.getPlayer());
        assertFalse(action.multipleExecutions());

        //Test su activateBasicProduction
        request = requestBuilder.action(Actions.ACTIVATEBASIC).targetPlayer(player.getUsername()).resources(new Resource[] {Resource.STONE,Resource.SERVANT,Resource.SHIELD}).buildRequest();
        action = actionParser.buildAction(request);
        assertEquals(Actions.ACTIVATEBASIC,action.getActionType());
        assertEquals(player,action.getPlayer());
        assertFalse(action.multipleExecutions());


       /*
        Player testPlayer = new Player("test");
        assertNull(purchaseAction);
        Market market = new Market();
        CardSet cardSet = new CardSet();
        cardSet.loadCards();
        market.init();
        ActionParser actionParser = new ActionParser(new ArrayList<>(Arrays.asList(testPlayer)),market,cardSet);
        purchaseAction = actionParser.buildAction(request);
        purchaseAction.execute();
         */
    }
}