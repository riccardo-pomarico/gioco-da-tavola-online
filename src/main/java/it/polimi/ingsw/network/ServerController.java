package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.controllers.GameController;
import it.polimi.ingsw.controller.controllers.SinglePlayerGameController;
import it.polimi.ingsw.task.ClientTask;
import it.polimi.ingsw.task.NetworkTask;
import it.polimi.ingsw.view.VirtualView;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController implements Server {
    private int port;
    private GameController gameController;
    private Map<String, Map.Entry<VirtualView,SocketHandler>> playerMap;
    private List<String> playerUsernames;
    private List<SocketHandler> tempClients;
    private Queue<NetworkTask> operationQueue;
    private Set<String> disconnectedUsers;
    private ExecutorService executor;
    private ActionParser actionParser;
    private int numPlayers;
    private int numClients;
    private int maxNumPlayers;
    private String currentServed;
    private boolean serverStarted = false;
    private boolean numClientsSet = false;
    private boolean isGameStarted = false;

    public ServerController(int port){
        init(port);
    }
    private void restart(){
        init(this.port);
    }
    private void init(int port){
        this.port = port;
        gameController = new GameController();
        playerMap = new HashMap<>();
        disconnectedUsers = new HashSet<>();
        tempClients = new ArrayList<>();
        playerUsernames = new ArrayList<>();
        operationQueue = new LinkedList<>();
        executor = Executors.newCachedThreadPool();
        numPlayers = 0;
        numClients = 0;
        maxNumPlayers = 0;
        currentServed = "";
        serverStarted = false;
        numClientsSet = false;
        isGameStarted = false;
    }

    /**
     * Triggers the server to activate.
     */
    public void startServer(){
        if(serverStarted){
            return;
        }
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e.getMessage()); // porta non disponibile
        }
        serverStarted = true;
        System.out.println("server is ready");
        while(true){
            try{
                //accetta client e incrementa contatore
                Socket socket = serverSocket.accept();
                numClients++;
                //crea nuovo socketHandler, setta un id, lo consegna all'executor
                SocketHandler socketHandler = new SocketHandler(this,socket);
                socketHandler.setId("%"+ numClients);
                executor.submit(socketHandler);
                //aggiunge il client alla lista di client temporanei prima che richieda di fare il login come player
                tempClients.add(socketHandler);
            }catch (IOException e){
                break;
            }
        }
    }

    /**
     * Sets the number of players chosen by the player as the number of players this game will have.
     * @param maxNumPlayers the number chosen by the player.
     */
    public void setNumPlayers(int maxNumPlayers) {
        if (!numClientsSet) {
            this.maxNumPlayers = maxNumPlayers;
            numClientsSet = true;
            if(maxNumPlayers == 1){
                System.out.println("single player");
                String singlePlayerUsername = playerMap.keySet().stream().findFirst().get();
                this.gameController = new SinglePlayerGameController();
                gameController.addPlayer(singlePlayerUsername,playerMap.get(singlePlayerUsername).getKey());
            }else{
                List<SocketHandler> socketHandlers = new ArrayList<>(tempClients);
                socketHandlers.forEach(socketHandler -> {
                    // System.out.println(socketHandler.getId());
                    if(socketHandler.getId().charAt(0) == '%'){
                        currentServed = socketHandler.getId();
                        logIn(socketHandler.getId().substring(1));
                    }
                });
            }
        }
    }

    /**
     * Executes the operation at the top of the queue the server has to process (the queue is always expected to have size 1).
     * @param task the operation to execute.
     */
    @Override
    public void handleTask(NetworkTask task){
        //aggiunge la Operation passata alla coda di operation da eseguire
        operationQueue.add(task);
        //smaltisce la coda
        emptyExecutionQueue();
    }

    /**
     * Actually handles the operation queue.
     */
    private void emptyExecutionQueue(){
        NetworkTask currentOperation;
        while (!operationQueue.isEmpty()) {
            //rimuove la prima prima operation dalla coda e la esegue
            currentOperation = operationQueue.remove();
            currentServed = currentOperation.getSender();
            currentOperation.execute();
            currentServed = "";
        }
    }

    private void closeConnection(SocketHandler socketHandler){

    }

    /**
     * Adds the newly connected player to the map of player usernames and their respective virtual views.
     * @param username the username of the newly connected player.
     */
    @Override
    public synchronized void logIn(String username){
        try {
            //prende il socketHandler relativo al client e aggiorna il suo ID
            SocketHandler playerSocketHandler = getSocketHandler(currentServed);
            if (disconnectedUsers.contains(username)) {
                getSocketHandler(currentServed).sendTask(new ClientTask(Client::reconnectPlayer));
                return;
            }

            if (numPlayers >= maxNumPlayers && numPlayers != 0) {
                if (numClients >= numPlayers) {
                    playerSocketHandler.sendTask(new ClientTask((client -> client.notification("The first player to connect is still choosing how many players the game will have. please wait..."))));
                    playerSocketHandler.setId("%"+username);
                    return;
                }
                playerSocketHandler.sendTask(new ClientTask(Client::maximumPlayersReached));
                return;
            }

            if (gameController.hasStarted() && !disconnectedUsers.contains(username)) {
                playerSocketHandler.sendTask(new ClientTask(Client::gameHasStarted));
            }

            try {
                //prova ad aggiungere un giocatore
                VirtualView view = new VirtualView(playerSocketHandler);
                gameController.addPlayer(username, view);

                playerSocketHandler.setId(username);
                //aggiunge una nuova entry alla mappa dei giocatori
                playerMap.put(username, new AbstractMap.SimpleEntry<>(view, playerSocketHandler));
                playerUsernames.add(username);

                tempClients.remove(playerSocketHandler);
                numPlayers++;
                numClients--;
                if (numPlayers == 1) {
                    send(username, new ClientTask(cc -> cc.getView().numberOfPlayers()));
                } else {
                    StringBuilder usernameBuileder = new StringBuilder();
                    playerUsernames.forEach(usr -> usernameBuileder.append(usr).append(", "));
                    int remainingPlayers = maxNumPlayers - numPlayers;
                    broadcast(new ClientTask(cc -> cc.lobby(usernameBuileder.toString(), remainingPlayers)));
                }
                if (maxNumPlayers == numPlayers) {
                    startGame();
                }

            } catch (IllegalArgumentException e) {
                playerSocketHandler.sendTask(new ClientTask(Client::usernameAlreadyTaken));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method sets off the beginning of the game (both multiplayer and single player modes).
     */
    @Override
    public void startGame(){
        if(!isGameStarted) {
            broadcast(new ClientTask(cc -> cc.getView().startGame()));
            this.actionParser = gameController.getActionParser();
            gameController.start();
            isGameStarted = true;
        }
    }

    @Override
    public void logOut() {
        //controlla se chi ha richiesto la disconnessione è un client temporaneo o un giocatore
        if(tempClients.stream().anyMatch(socketHandler -> socketHandler.getId().equals(currentServed))){
            tempClients.removeIf(socketHandler -> socketHandler.getId().equals(currentServed));
        }else if (playerMap.containsKey(currentServed)){
            gameController.setPlayerInactive(currentServed);
            playerMap.remove(currentServed);
            playerUsernames.remove(currentServed);
            //se non è rimasto più nessun giocatore connesso e la partita è terminata riavvia il server per poterne ospitare un'altra
            if(playerMap.isEmpty() && gameController.hasEnded()){
                restart();
            }
        }
    }

    private String[] getUserList(){
        return playerMap.keySet().toArray(new String[0]);
    }

    /**
     * Sends an operation that a specific client has to execute.
     * @param recipient the client that should execute the operation.
     * @param operation the operation to execute.
     */
    private void send(String recipient, ClientTask operation){
        playerMap.get(recipient).getValue().sendTask(operation);
    }

    /**
     * Broadcasts an operation, so that all clients carry it out.
     * @param operation the operation to carry out.
     */
    private void broadcast(ClientTask operation){
        for (Map.Entry<String, Map.Entry<VirtualView, SocketHandler>> playerMapEntry : playerMap.entrySet()) {
            if(playerMapEntry.getValue().getValue().isOnline()) {
                playerMapEntry.getValue().getValue().sendTask(operation);
            }
        }
    }

    /**
     * Broadcasts an operation, so that all clients except for one carry it out.
     * @param operation the operation to carry out.
     * @param userToAvoid the client who must not execute the operation.
     */
    private void broadcast(ClientTask operation, String userToAvoid){
        for (Map.Entry<String, Map.Entry<VirtualView, SocketHandler>> playerMapEntry : playerMap.entrySet()) {
            if(!playerMapEntry.getKey().equals(userToAvoid) && playerMapEntry.getValue().getValue().isOnline()) {
                playerMapEntry.getValue().getValue().sendTask(operation);
            }
        }
    }

    /**
     * handles every possible game action by using the actionParser to parse the JSONObject request
     * @param request JSONObject containing the request to build and execute a certain action, the request can be obtained with ActionParser.buildRequest()
     */
    public void requestAction(JSONObject request) {
        try{
            Action gameAction = actionParser.buildAction(request);
            gameController.handleAction(gameAction);
        }catch (Exception e){
            e.printStackTrace();
            send(currentServed,new ClientTask(client -> client.actionRejected("error while building the action: " +e.getMessage())));
        }
    }

    @Override
    public void actionFinished() {
        gameController.afterAction();
    }

    @Override
    public void timeForLeaderCard() {
        gameController.requestLeaderCard();
    }

    @Override
    public void changeOfTurn() {
        gameController.changeOfTurn();
    }

    /**
     * Handles the disconnection of a player.
     * @param username the username of the disconnected player.
     */
    @Override
    public void handleDisconnection(String username){
        if(tempClients.stream().map(SocketHandler::getId).anyMatch(d -> d.equals(username))){
            tempClients.removeIf(c -> c.getId().equals(username));
            numClients--;
        }else {
            disconnectedUsers.add(username);
            numPlayers--;
            broadcast(new ClientTask(client -> client.playerDisconnected(username)), username);
            gameController.setPlayerInactive(username);
            System.out.println(username + " disconnected");
        }
    }

    /**
     * Handles the attempt to reconnect the player to the server.
     * @param username the username of the disconnected player.
     */
    public void reconnect(String username){
        if (disconnectedUsers.contains(username)){
            //recupera il nuovo SocketHandler e imposta l'id definitivo
            SocketHandler newSH = getSocketHandler(currentServed);
            System.out.println("currentServed "+currentServed);
            newSH.setId(username);
            tempClients.remove(newSH);

            //imposta il nuovo SocketHandler come SH del giocatore e reimposta il giocatore come attivo
            playerMap.get(username).setValue(newSH);
            gameController.setPlayerActive(username,new VirtualView(newSH));
            numPlayers++;
            broadcast(new ClientTask(cc -> cc.playerReconnected(username)));
            System.out.println(username + " reconnected");
        }
    }

    /**
     * Allows access to the socket of a player.
     * @param id the username of the player whose socket this method allows access to.
     * @return the socket of the player with username id.
     */
    private SocketHandler getSocketHandler(String id){
        if(playerMap.containsKey(id)){
            return playerMap.get(id).getValue();
        } else if(tempClients.stream().anyMatch(s -> s.getId().equals(id))){
            return tempClients.stream().filter(s -> s.getId().equals(id)).findFirst().get();
        }
        throw new IllegalArgumentException();
    }



    public void beginTurn(SocketHandler sh) {
        sh.sendTask(new ClientTask(cc -> cc.getView().chooseAction()));
    }

    public void handleMarketStatus(String username, JSONObject marketStatus) {
        send(username,new ClientTask(cc -> cc.updateMarket(marketStatus)));
    }

    public GameController getGameController() {
        return gameController;
    }
}


