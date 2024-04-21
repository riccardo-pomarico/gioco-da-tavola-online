package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.exceptions.PersonalDashboardException;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PersonalDashboardTest {



    @Test
    public void checkResourcesTest(){
        PersonalDashboard pd = new PersonalDashboard("pimar116");
        //il forziere è vuoto
        assertFalse(pd.checkResources(Arrays.asList(Resource.SHIELD, Resource.SHIELD))[0]);
        //il magazzino è vuoto
        assertFalse(pd.checkResources(Arrays.asList(Resource.SHIELD, Resource.SHIELD))[1]);

        //deposita risorse sia nel forziere che nel magazzino
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.SHIELD);
        pd.depositResource(2,0,Resource.COIN);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,1,Resource.SHIELD);
        //il forziere contiene le risorse richeste
        assertTrue(pd.checkResources(Arrays.asList(Resource.COIN, Resource.SHIELD))[0]);
        //il magazzino contiene le risorse richeste
        assertTrue(pd.checkResources(Arrays.asList(Resource.COIN, Resource.SHIELD))[1]);
        //il forziere non contiene le risorse richeste
        assertFalse(pd.checkResources(Arrays.asList(Resource.COIN, Resource.STONE))[0]);
        //il magazzino non contiene le risorse richeste
        assertFalse(pd.checkResources(Arrays.asList(Resource.COIN, Resource.STONE))[1]);
        //il forziere contiene meno risorse di quelle richeste
        assertFalse(pd.checkResources(Arrays.asList(Resource.SHIELD, Resource.SHIELD))[0]);
        //il magazzino contiene meno risorse di quelle richeste
        assertFalse(pd.checkResources(Arrays.asList(Resource.COIN, Resource.COIN))[1]);
    }

    @Test
    public void withdrawResourcesTest() {
        PersonalDashboard pd = new PersonalDashboard("pimar116");
        //deposita risorse sia nel forziere che nel magazzino
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.SHIELD);
        pd.depositResource(2,0,Resource.COIN);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,2,Resource.SERVANT);

        //prova a prelevare delle risosrse non possedute dal forziere
        try{
            pd.withdrawResources(1,Arrays.asList(Resource.SERVANT, Resource.STONE));
        }catch (PersonalDashboardException e){
            assertEquals("not enough resources from strongbox",e.getMessage());
            assertEquals(3,pd.countResources(Resource.COIN)[0]);
            assertEquals(1,pd.countResources(Resource.SHIELD)[0]);
        }
        //prova a prelevare una risorsa posseduta e una no dal forziere
        try{
            pd.withdrawResources(1,Arrays.asList(Resource.SHIELD, Resource.STONE));
        }catch (PersonalDashboardException e){
            assertEquals("not enough resources from strongbox",e.getMessage());
            assertEquals(3,pd.countResources(Resource.COIN)[0]);
            assertEquals(1,pd.countResources(Resource.SHIELD)[0]);
        }
        //prova a prelevare più risorse di un tipo di quante ne siano possedute
        try{
            pd.withdrawResources(1,Arrays.asList(Resource.STONE, Resource.STONE));
        }catch (PersonalDashboardException e){
            assertEquals("not enough resources from strongbox",e.getMessage());
            assertEquals(3,pd.countResources(Resource.COIN)[0]);
            assertEquals(1,pd.countResources(Resource.SHIELD)[0]);
        }
        //esegue un prelievo corretto sul forziere
        pd.withdrawResources(1,Arrays.asList(Resource.COIN, Resource.SHIELD));
        assertEquals(2,pd.countResources(Resource.COIN)[0]);
        assertEquals(0,pd.countResources(Resource.SHIELD)[0]);
        //esegue un prelievo corretto sul magazzino
        pd.withdrawResources(2,Arrays.asList(Resource.COIN, Resource.SHIELD));
        assertEquals(0,pd.countResources(Resource.COIN)[1]);
        assertEquals(1,pd.countResources(Resource.SHIELD)[1]);

        pd = new PersonalDashboard("vincenzo de luca");
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.SHIELD);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,2,Resource.SERVANT);
        List<Resource> resourcesToWithdraw = new ArrayList<>();
        resourcesToWithdraw.add(Resource.COIN);
        resourcesToWithdraw.add(Resource.SERVANT);
        pd.withdrawResources(3, resourcesToWithdraw);
        assertEquals(0, pd.countResources(Resource.COIN)[0]);
        assertEquals(3, pd.countResources(Resource.COIN)[0]+pd.countResources(Resource.SHIELD)[0]+pd.countResources(Resource.SHIELD)[1]+pd.countResources(Resource.SERVANT)[0]);
    }

    @Test
    public void depositResourceTest() {
        PersonalDashboard pd = new PersonalDashboard("pimar116");
        //prova a depositare risorse nel forziere
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.SHIELD);
        assertEquals(3,pd.countResources(Resource.COIN)[0]);
        assertEquals(1,pd.countResources(Resource.SHIELD)[0]);

        //prova a depositare risorse nel magazzino
        pd.depositResource(2,0,Resource.COIN);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,2,Resource.SERVANT);
        assertEquals(1,pd.countResources(Resource.COIN)[1]);
        assertEquals(2,pd.countResources(Resource.SHIELD)[1]);
        assertEquals(1,pd.countResources(Resource.SERVANT)[1]);
        assertEquals(0,pd.countResources(Resource.STONE)[1]);

    }

    @Test
    public void addCardTest() {
        PersonalDashboard pd = new PersonalDashboard("pimar116");
        //prova ad aggiungere due Development card uguali
        Card card1 = new DevelopmentCard(Level.LEVEL1, Color.BLUE,new Rule(),3);
        try{
            pd.addCard(card1);
            pd.addCard(card1);
        }catch (PersonalDashboardException e){
            assertEquals("[DC-1-BLUE---3] card is already in the deck",e.getMessage());
        }
        //prova ad aggiungere due Leader Card uguali
        List<Color>colorList = new ArrayList<Color>(Arrays.asList(Color.BLUE,Color.GREEN));
        Card card2 = new LeaderCard(new Rule(), BonusType.BLANKMARBLES, colorList,4,4);
        try{
            pd.addCard(card2);
            pd.addCard(card2);
        }catch (PersonalDashboardException e){
            assertEquals("[LC---B] card is already in the deck",e.getMessage());
        }

        //input standard
        Card card3 =  new DevelopmentCard(Level.LEVEL2, Color.GREEN,new Rule(),3);
        Card card4 = new LeaderCard(new Rule(), BonusType.DISCOUNT, colorList,4,4);
        pd.addCard(card3);
        pd.addCard(card4);
        List<LeaderCard> leaderCardList = pd.getLeadCardsDeck();
        List<DevelopmentCard> developmentCardList = pd.getDevCardsDeck();
        assertEquals("LC---B",leaderCardList.get(0).generateId());
        assertEquals("LC---D",leaderCardList.get(1).generateId());
        assertEquals("DC-1-BLUE---3",developmentCardList.get(0).generateId());
        assertEquals("DC-2-GREEN---3",developmentCardList.get(1).generateId());


    }

    @Test
    public void removeCardTest() {
        PersonalDashboard pd = new PersonalDashboard("pimar116");
        List<Color>colorList = new ArrayList<Color>(Arrays.asList(Color.BLUE,Color.GREEN));
        Card card1 = new DevelopmentCard(Level.LEVEL1, Color.BLUE,new Rule(),3);
        //rimuove da un deck vuoto
        pd.removeCard(card1);
        assertEquals(0,pd.getDevCardsDeck().size());
        assertEquals(0,pd.getLeadCardsDeck().size());

        //rimuove una carta non presente
        pd.addCard(card1);
        Card card2 = new LeaderCard(new Rule(), BonusType.BLANKMARBLES, colorList,4,4);
        pd.removeCard(card2);
        assertEquals(1,pd.getDevCardsDeck().size());
        assertEquals(0,pd.getLeadCardsDeck().size());
        assertEquals(card1.generateId(),pd.getDevCardsDeck().get(0).generateId());

        //rimozione standard
        Card card3 =  new DevelopmentCard(Level.LEVEL2, Color.GREEN,new Rule(),3);
        Card card4 = new LeaderCard(new Rule(), BonusType.DISCOUNT, colorList,4,4);
        pd.addCard(card2);
        pd.addCard(card3);
        pd.addCard(card4);
        pd.removeCard(card1);
        pd.removeCard(card4);
        assertEquals(1,pd.getDevCardsDeck().size());
        assertEquals(1,pd.getLeadCardsDeck().size());
        assertEquals(card2.generateId(),pd.getLeadCardsDeck().get(0).generateId());
        assertEquals(card3.generateId(),pd.getDevCardsDeck().get(0).generateId());
    }

    @Test
    public void createJSONObjectTest() {
        PersonalDashboard pd = new PersonalDashboard("pimar116");
        //deposita risorse sia nel forziere che nel magazzino
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.COIN);
        pd.depositResource(1,0, Resource.SHIELD);
        pd.depositResource(2,0,Resource.COIN);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,1,Resource.SHIELD);
        pd.depositResource(2,2,Resource.SERVANT);
        JSONObject jsonPD = pd.createJSONObject();
        JSONObject warehouse = (JSONObject) jsonPD.get("player's warehouse");
        JSONArray shelves = (JSONArray) warehouse.get("Warehouse's shelves");
        assertEquals("[\"COIN\"]", shelves.get(0).toString());
        assertEquals("[\"SHIELD\",\"SHIELD\"]", shelves.get(1).toString());
        assertEquals("[\"SERVANT\",\"0\",\"0\"]", shelves.get(2).toString());
    }
}