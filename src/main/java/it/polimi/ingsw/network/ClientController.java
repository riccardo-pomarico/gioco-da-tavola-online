package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.task.NetworkTask;
import it.polimi.ingsw.task.ServerTask;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.view.View;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the main controller of the client part of the game. It handles both Cli and Gui of the player.
 */
public class ClientController implements Client, ViewObserver {
    private final View view;
    // Ci servono due thread: uno per la view, uno per SocketHandler. Ergo, noi creiamo un pool di due thread.
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Queue<NetworkTask> taskQueue;
    private SocketHandler clientSocketHandler;
    private final String LOCALHOST = "127.0.0.1";
    private final int DEFAULT_PORT = 1330;
    private String actualIp;
    private int actualPort;
    private String username;
    private boolean turn;
    private Actions lastAction;
    private Task<View> lastTask;
    private List<String> leaderCards = new ArrayList<>();
    private Map<Actions,String> actionMessageMap = new HashMap();
    private String currentAction;
    private int actionCounter =0;
    private Queue<JSONObject> actionRequestQueue = new LinkedList<>();
    private boolean taskInSemaphore = true;
    private boolean taskOutSemaphore = true;
    private boolean firstConnected = false;

    public ClientController(View view){
        this.view = view;
        taskQueue = new LinkedList<>();
        actionMessageMap.put(Actions.MARKETPURCHASE," has chosen to withdraw resources from the Market! ");
        actionMessageMap.put(Actions.ACTIVATEBASIC," has activated the basic production! ");
        actionMessageMap.put(Actions.ACTIVATELEADER," has decided to use a LeaderCard! ");
        actionMessageMap.put(Actions.DEVCARDPURCHASE, " has purcased a Development Card! ");
        actionMessageMap.put(Actions.DISCARDLEADER, " has decided to discard a Leader Card!");
        actionMessageMap.put(Actions.DISCARDRESOURCE," has decided to discard a Resource!");
    }

    /**
     * Method to ask the player their credentials by terminal input.
     */
    public void init(){
        view.init();
        view.askInfo();
    }

    /**
     * Method to autogenerate a player's credentials. It makes testing far less painful.
     */
    public void autoInit(){
        view.init();
        this.actualIp = LOCALHOST;
        this.actualPort = DEFAULT_PORT;
        String rndUsername = "P"+new Random().nextInt(10000);
        view.setUsername(rndUsername);
        this.username = rndUsername;
        view.printMessage("","auto generated username: "+this.username);
        connect(LOCALHOST,DEFAULT_PORT);
        registerPlayer(rndUsername);
    }

    public void singlePlayerInit(){
        this.actualIp = LOCALHOST;
        this.actualPort = DEFAULT_PORT;
        view.setUsername("SinglePlayer");
        view.init();
        this.username = "SinglePlayer";
        connect(LOCALHOST,DEFAULT_PORT);
        registerPlayer("SinglePlayer");
        setNumberOfPlayers(1);
        startGame();
    }

    /**
     * This method connects the player's socket to IP address defined by @param ip and to the port defined by @param port.
     * @param ip IP address.
     * @param port port to connect to.
     */
    private void connect(String ip, int port) {
        boolean connected = false;
        //cerca di stabilire la connessione
        long startTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        do{
            try {
                this.clientSocketHandler = new SocketHandler(this, ip, port);
                executor.submit(clientSocketHandler);
                //se la connessione va a buon fine, chiede al server di aggiungere un nuovo giocatore
                connected = true;

            } catch (IOException ignored) { }
        } while (!connected && new Timestamp(System.currentTimeMillis()).getTime() - startTimestamp < 10000);
        if(!connected){
            view.notification("error","sorry, could not connect to the server, please try with different values");
            view.askInfo();
        }
    }

    /**
     * Registers a connected player to the list of players in server.
     * @param usr the username of the connected player.
     */
    private void registerPlayer(String usr){
        NetworkTask op = new ServerTask(sc -> sc.logIn(usr));
        clientSocketHandler.sendTask(op);
    }

    @Override
    public void startGame(){
        clientSocketHandler.sendTask(new ServerTask(Server::startGame));
    }

    public void usernameNotValid(String username){

    }

    /**
     * Executes the operation at the top of the queue the client has to be process (the queue is always expected to have size 1).
     * @param task the operation to execute.
     */
    @Override
    public void handleTask(NetworkTask task){
        if(task.isPriority()){
            task.execute();
            return;
        }
        taskQueue.add(task);
        emptytaskQueue();
    }
    private void emptytaskQueue(){
        NetworkTask currentTask;
        while (!(taskQueue.isEmpty()) && taskInSemaphore){
            currentTask = taskQueue.remove();
            currentTask.execute();
        }
    }

    /**
     * Processes the player's credentials input (nickname, IP address, server port).
     * @param info the player's credentials.
     */
    @Override
    public void playerInfo(String[] info) {
        this.actualIp = info[1];
        this.actualPort = Integer.parseInt(info[2]);
        this.username = info[0];
        view.setUsername(username);
        connect(actualIp, actualPort);
        registerPlayer(username);
    }



    public View getView(){ return view; }

    @Override
    public void notification(String msg) {
        view.notification("info",msg);
    }

    /**
     * The update() method invoked by notify() in the Observer pattern with Cli.
     * @param op the task the target (parameter of execute()) must carry out.
     */
    @Override
    public void update(Task<ViewObserver> op) {
        op.execute(this);
    }

    /**
     * Indicates to a player that they can't connect to the game because the maximum admissible number has been reached.
     */
    public void maximumPlayersReached(){
        view.notification("error", "sorry, maximum number of players already reached");
    }
    /**
     * Indicates to a player that they can't connect to the game because the maximum admissible number has been reached.
     */
    public void gameHasStarted(){
        view.notification("error", "the game has already started!");
    }

    @Override
    public void usernameAlreadyTaken() {
        view.newUsername();
    }


    /**
     * Notifies to the player that they have lost connection to the server and tries to reconnect
     * @param subject
     */
    @Override
    public void handleDisconnection(String subject){
        view.notification("error","connection to server lost\nattempting to reconnect...");
        connect(actualIp,actualPort);
    }

    /**
     * Handles the potential disconnection of a player from the server.
     */
    @Override
    public void reconnectPlayer(){
        view.reconnect();
    }
    @Override
    public void playerDisconnected(String player){
        view.disconnectedPlayer(player);
    }
    public void playerReconnected(String player){
        if(player.equals(this.username)) {
            view.notification("success", "connection re-established!");
            //lastTask.execute(view);
        }
        else
            view.notification("success",player+" reconnected! ");
    }



    /**
     * Shows all players already waiting in the lobby.
     * @param players
     * @param remaining
     */
    public void lobby(String players, int remaining){
        String[] playersArray = players.substring(0,players.length()-2).split(", ");
        if((playersArray.length > 2 || firstConnected) && !playersArray[playersArray.length - 1].equals(this.username)) {
            view.showNewPlayer(playersArray[playersArray.length - 1]);
        }
        view.showOtherPlayers(remaining,"lobby: ",playersArray);
    }

    public void playerOrder(String[] players){
        view.playerOrder(players);
    }

    private String[] splitString(String string){
        string = string.substring(1,string.length()-2);
        return string.split(", ");
    }

    public void changeUsername(String username){
        this.username = username;
    }


    /**
     * Notifies the player whether it's their turn or someone else's.
     * @param username the player to notify.
     */
    @Override
    public void turnMessage(String username) {
        if (!this.username.equals(username)) {
            view.turnMessage("It's " + username + "'s turn!");
        }
    }

    /**
     * Method displaying the action choice message at the beginning of a player's turn.
     */
    public void startTurn() {
        this.turn = true;
        view.chooseAction();
    }

    /**
     * Method to ask a player if they want to activate a leader card.
     */
    @Override
    public void requestLeaderCard() {
        view.requestLeaderCard();
    }

    @Override
    public void newLeaderCard(String card){
        try {
            leaderCards.add(card);
            if (leaderCards.size() == 4) {
                view.handleNewLeaderCard(leaderCards);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void timeForDisposal(String resource, boolean blackMarble) {
        view.discard(resource, blackMarble);
    }

    /**
     * Method invoked after the market data structure in Cli/Gui has been updated.
     * It triggers the server to call the sequence of methods that will invoke requestLeaderCard() in this class.
     */
    @Override
    public void timeForLeaderCard() {
        clientSocketHandler.sendTask(new ServerTask(Server::timeForLeaderCard));
    }

    /**
     * Notifies the server of the end of the current player's turn.
     */
    @Override
    public void changeOfTurn() {
        clientSocketHandler.sendTask(new ServerTask(Server::changeOfTurn));
    }



    @Override
    public void forwardMessage(String message) {
        view.notification("info", message);
        if (message.equals("\nLORENZO HAS WON, YOU HAVE LOST!\nThank you for playing!")) {
            logOut();
        }
    }

    /**
     * Notifies the server of the number of players the game will have.
     * @param finalNumPlayers the chosen number of players.
     */
    @Override
    public void setNumberOfPlayers(int finalNumPlayers) {
        firstConnected = true;
        clientSocketHandler.sendTask(new ServerTask(sc -> sc.setNumPlayers(finalNumPlayers)));
    }

    /**
     * Used to ask the server to add a new Player
     * @param usr the username of the new client.
     */
    @Override
    public void logIn(String usr) {
        clientSocketHandler.sendTask(new ServerTask(sc -> sc.logIn(usr)));
    }

    /**
     * Used to ask the server to log out
     */
    @Override
    public void logOut() {
        clientSocketHandler.sendTask(new ServerTask(Server::logOut));
    }

    public void reconnectToServer(){
        String usr = this.username;
        clientSocketHandler.sendTask(new ServerTask(sc -> sc.reconnect(usr)));
    }

    /**
     * Notifies the server of the necessity to process a new action from the client.
     * @param request the action to process through ActionParser.
     */
    @Override
    public void handleAction(JSONObject request) {
        actionRequestQueue.add(request);
        // System.out.println("added "+request.get("action")+"action to actionQueue");
        if(taskOutSemaphore){
            emptyActionRequestQueue();
        }
    }

    @Override
    public void blockIncomingTasks() {
        this.taskInSemaphore = false;
    }

    @Override
    public void unblockIncomingTasks(){
        this.taskInSemaphore = true;
        emptytaskQueue();
    }

    private void emptyActionRequestQueue(){
        JSONObject currentRequest;
        while (!actionRequestQueue.isEmpty() && taskOutSemaphore) {
            //rimuove la prima prima request dalla coda e la invia
            currentRequest = actionRequestQueue.remove();
            this.lastTask = view.getLastTask();
            if (!currentRequest.get("action").toString().equals(Actions.DEPOSIT)) {
                lastAction = Actions.valueOf(currentRequest.get("action").toString());
            }
            taskOutSemaphore = false;
            taskInSemaphore = false;

            currentAction = currentRequest.get("action").toString() + actionCounter;
            actionCounter++;
            // System.out.println("action " + currentAction + " request. actionAck = false");

            currentRequest.put("targetPlayer", this.username);
            JSONObject finalCurrentRequest = currentRequest;
            clientSocketHandler.sendTask(new ServerTask(sc -> sc.requestAction(finalCurrentRequest)));
        }
    }
    @Override
    public void actionMessage(Actions action, String username){
            String message = actionMessageMap.get(action);
            if(message != null)
                view.notification("info",username + actionMessageMap.get(action));
    }
    /**
     * Notifies the player that a certain action could not be processed.
     * @param error the error message to send.
     */
    public void actionRejected(String error){
        this.view.notification("error",error);
        taskOutSemaphore = true;
        taskInSemaphore = false;
        lastTask.execute(view);
    }

    public void actionAccepted(){
        // System.out.println("action "+ currentAction+ " accepted. actionAck = true");
        taskOutSemaphore = true;
        taskInSemaphore = true;

        emptyActionRequestQueue();
        emptytaskQueue();
    }
    /**
     * Updates market data structure in Cli/Gui with the new status received by the server.
     * @param market the new status of the market.
     */
    @Override
    public void updateMarket(JSONObject market) {
        view.updateMarket(market);
    }


    /**
     * Shows the new market status to the player and then triggers marketNotify() in this class.
     */
    @Override
    public void finishMarketHandling() {
        view.finishMarketHandling();
    }

    /**
     * Updates card set data structure in Cli/Gui with the new status received by the server.
     * @param cardSet the new status of the card set.
     */
    @Override
    public void updateCardSet(JSONObject cardSet) {
        view.updateCardSet(cardSet);
    }

    @Override
    public void addToSlot(String id, int slot) {
        view.addToSlot(id, slot);
    }

    @Override
    public void actionFinished() {
        clientSocketHandler.sendTask(new ServerTask(Server::actionFinished));
    }

    /**
     * Handles the tray of the player's personal dashboard.
     * @param marble the marble to process.
     */
    public void handleTray(String marble){
        if(lastAction == Actions.MARKETPURCHASE){
            view.depositMarble(marble,2);
        } else if( lastAction == Actions.ACTIVATEBASIC || lastAction == Actions.ACTIVATEPRODUCTION){
            view.depositMarble(marble,1);
        }else {
            view.depositMarble(marble, 2);
        }
    }

    @Override
    public void updatePersonalDashboard(JSONObject newPDStatus) {
        view.updatePersonalDashboard(newPDStatus);
    }

    @Override
    public void updatePFT(int newPosition, String username) {
        String message;
        String type;
            if (username.equals(this.username)) {
                message = "you have advanced in Pope's Favor Track and you are now in position #" + newPosition;
                type = "success";
            } else {
                message = username + " has advanced in Pope's Favor Track and is now in position #" + newPosition;
                type = "info";
            }

        view.notification(type,message);
    }

    @Override
    public void afterAction() {
        view.afterAction();
    }

    @Override
    public void notifyExtraStorage(String resource) {
        //System.out.println("You are entitled to an extra shelf which will contain " + resource);
        view.notifyExtraStorage(resource);
    }
    @Override
    public void incrementLorenzoPFT(int increment){
        view.updateLorenzoPFT(increment);
    }

    @Override
    public void takeTwoCards(JSONObject cardIds) {
        view.takeTwoDevCards(cardIds);
    }

    @Override
    public void cardActivated(JSONObject cardActivated) {
        view.cardActivated(cardActivated);
    }

    @Override
    public void vaticanReportMessage(String vaticanReportMessage, boolean isOnTheRightVaticanZone, int popesCardPoints) {
        view.vaticanReportMessage(vaticanReportMessage, isOnTheRightVaticanZone, popesCardPoints);
    }

    @Override
    public void rankingPositionMessage(String rankingPosition) {
        view.rankingPositionMessage(rankingPosition);
    }

    @Override
    public void lorenzoTurnActionNotification(String actionType, String message) {
        view.lorenzoTurnActionNotification(actionType, message);
    }

}

