package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.exceptions.ImpossibleStorageException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import org.junit.Test;

import static org.junit.Assert.*;

public class DepositActionTest {
    @Test
    public void multipleExecutionTest(){
        Player testPlayer = new Player("test");
        Action depositAction = new DepositAction(testPlayer,"RB",1,0,false);
        assertEquals(Actions.DEPOSIT,depositAction.getActionType());
        assertTrue(depositAction.multipleExecutions());
    }

    @Test
    public void executeTest() {
        Player testPlayer = new Player("test");
        //test con ogni tipo di biglia
        Action depositAction1 = new DepositAction(testPlayer,"RB",1,0,false);
        Action depositAction2 = new DepositAction(testPlayer,"RY",1,0,false);
        Action depositAction5 = new DepositAction(testPlayer,"SR",1,0,false);
        //all'inizio il tray del giocatore è vuoto
        assertEquals(0,testPlayer.getPersonalDashboard().getTray().size());
        //prova a rimuovere qualcosa dal tray quando è vuoto;
        try{
        depositAction1.execute();
        }catch(ActionException e){
            assertEquals("this marble is not present in the tray",e.getMessage());
        }
        //prova a depositare una biglia nel tray ed eseguire una deposit diversa da quella necessaria
        testPlayer.getPersonalDashboard().addToTray(new Marble(Resource.COIN));
        try{
            depositAction1.execute();
        }catch(ActionException e){
            assertEquals("this marble is not present in the tray",e.getMessage());
        }
        //prova con un input corretto
        depositAction2.execute();
        assertEquals(0,testPlayer.getPersonalDashboard().getTray().size());
        //aggiunge più biglie dello stesso tipo e ne preleva una
        testPlayer.getPersonalDashboard().addToTray(new Marble(Resource.COIN));
        testPlayer.getPersonalDashboard().addToTray(new Marble(Resource.COIN));
        testPlayer.getPersonalDashboard().addToTray(new Marble(Resource.COIN));
        depositAction2.execute();
        assertEquals(2,testPlayer.getPersonalDashboard().getTray().size());
        //prova a depositare una risorsa in un luogo non concesso
        Action depositAction7 = new DepositAction(testPlayer,"RY",2,0,false);
        depositAction7.execute();
        try{
            depositAction7.execute();
        }catch (ActionException e){
            assertEquals("There's no possible way to store this resource in the warehouse...",e.getMessage());
        }catch (ImpossibleStorageException ex) {
            assertEquals("There's no possible way to store this resource in the warehouse...", ex.getMessage());
            assertEquals(Resource.COIN, ex.getResourceToDiscard());
        }
        //gestione della biglia rossa
        testPlayer.getPersonalDashboard().addToTray(new Marble(SpecialType.FAITHPOINT));
        assertEquals(0,testPlayer.getPersonalDashboard().getPopesFavorTrack().getTrackPosition());
        try {
            depositAction5.execute();
        }catch (ActionException e){
            assertEquals("you cannot deposit a marble of this type",e.getMessage());
        }

        //test su biglia nera
        assertEquals(0,testPlayer.getPersonalDashboard().countResources(Resource.SERVANT)[0]);
        testPlayer.getPersonalDashboard().addToTray(new Marble(SpecialType.BLACK));
        Action blackDeposit = new DepositAction(testPlayer,"RP",1,0,true);
        assertEquals(1,testPlayer.getPersonalDashboard().getTray().stream().filter(m -> m.generateId().equals("SB")).count());
        blackDeposit.execute();
        assertEquals(1,testPlayer.getPersonalDashboard().countResources(Resource.SERVANT)[0]);
        //prova a depositare un punto fede mediante biglia nera
        testPlayer.getPersonalDashboard().addToTray(new Marble(SpecialType.BLACK));
        try{
            blackDeposit = new DepositAction(testPlayer,"SR",1,0,true);
            blackDeposit.execute();
        }catch (ActionException e){
            assertEquals("you cannot deposit a marble of this type",e.getMessage());
        }
        //prova a depositare un mediante biglia nera
        testPlayer.getPersonalDashboard().addToTray(new Marble(SpecialType.BLACK));
        try{
            blackDeposit = new DepositAction(testPlayer,"SW",1,0,true);
            blackDeposit.execute();
        }catch (ActionException e){
            assertEquals("you cannot deposit this marble",e.getMessage());
        }

        //prova con impossibleStorageException
        testPlayer = new Player("test");
        testPlayer.getPersonalDashboard().addToTray(new Marble("RB"));
        testPlayer.getPersonalDashboard().addToTray(new Marble("RG"));
        testPlayer.getPersonalDashboard().addToTray(new Marble("RY"));
        testPlayer.getPersonalDashboard().addToTray(new Marble("RY"));
        Action depositAction = new DepositAction(testPlayer,"RB",2,0,false);
        depositAction.execute();
        depositAction = new DepositAction(testPlayer,"RG",2,1,false);
        depositAction.execute();
        depositAction = new DepositAction(testPlayer,"RY",2,2,false);
        depositAction.execute();
        try{
            depositAction.execute();
        }catch (ActionException e ){
            assertEquals("There's no possible way to store this resource in the warehouse...",e.getMessage());
        }


    }
}