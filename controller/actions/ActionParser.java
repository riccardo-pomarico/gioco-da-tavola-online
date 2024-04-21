package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.function.Supplier;

public class ActionParser {
    //attributi globali action
    private Market market = new Market();
    private CardSet cardSet = new CardSet();
    private List<Player> playerList = new ArrayList<>();

    //attributi action
    private Actions action;
    private String playerUsername;
    private Player targetPlayer;
    private String developmentCard;
    private List<String> developmentCardsToActivate = new ArrayList<>();
    private String leaderCard;
    private int targetDeposit;
    private int shelf;
    private boolean blackMarble;
    private Color color;
    private String line;
    private String marble;
    private Resource[] resources;
    private List<Resource>[] warehouseShelves;
    private int slot;

    //mappa delle action creabili
    private final Map<Actions, Supplier<Action>> actionMap;

    //flag che controlla di che tipo è l'istanza di ActionBuilder creata ( request builder o action builder )
    private boolean isRequestBuilder = false;

    //JSONobject contenente la request da trasformare in action
    private JSONObject request;

    public ActionParser(){
        isRequestBuilder = true;
        this.actionMap = new HashMap<>();
    }
    public ActionParser(List<Player> playerList, Market market, CardSet cardSet){
        this.actionMap = new HashMap<>();
        this.playerList = playerList;
        this.market = market;
        this.cardSet = cardSet;
        isRequestBuilder = false;
        actionMap.put(Actions.MARKETPURCHASE,marketPurchase());
        actionMap.put(Actions.DEVCARDPURCHASE,developmentPurchase());
        actionMap.put(Actions.ACTIVATELEADER,activateLeader());
        actionMap.put(Actions.ACTIVATEPRODUCTION,activateProduction());
        actionMap.put(Actions.ACTIVATEBASIC,activateBasicProduction());
        actionMap.put(Actions.DISCARDRESOURCE,discardResource());
        actionMap.put(Actions.DISCARDLEADER,discardLeader());
        actionMap.put(Actions.DEPOSIT,deposit());
        actionMap.put(Actions.UPDATEWAREHOUSE,updateWarehouse());
        actionMap.put(Actions.ACTIVATELEADERPRODUCTION,activateLeaderProduction());
    }

    /**
    * Builder part defining the action to perform.
    * @param action the kind of action.
    * @return the action parser with the action field defined.
     * */
    public ActionParser action(Actions action){
        this.action = action;
        return this;
    }

    /**
    * Builder part defining the player involved in the action to perform.
    * @param username the player targeted by the action itself.
    * @return the action parser with the player field defined.
     */
    public ActionParser targetPlayer(String username){
        this.playerUsername = username;
        return this;
    }

    /**
    * Builder part defining the development card to purchase.
    * @param card the ID of the development card to purchase.
    * @return the action parser with the development card field defined.
     */
    public ActionParser developmentCard(String card){
        if(CardSet.isDevelopmentCard(card)){
            this.developmentCard = card;
            return this;
        }else{
            throw new IllegalArgumentException("["+card+"] is not a development card");
        }
    }

    /**
     * Builder part defining the development cards to purchase.
     * @param cards the list of IDs of the development cards.
     * @return the action parser with the development card list field defined.
     */
    public ActionParser developmentCardsList(List<String> cards) {
        for (String card : cards){
            if (CardSet.isDevelopmentCard(card)) {
                this.developmentCardsToActivate.add(card);
            } else {
                throw new IllegalArgumentException("["+card+"] is not a development card");
            }
        }
        return this;
    }

    /**
     * Builder part defining the development card to activate.
     * @param card the ID of the leader card.
     * @return the action parser with the leader card field defined.
     */
    public ActionParser leaderCard(String card){
        if(CardSet.isLeaderCard(card)){
            this.leaderCard = card;
            return this;
        }else{
            throw new IllegalArgumentException("["+card+"] is not a leader card");
        }
    }

    /**
     * Builder part defining the line of marbles the player wants to purchase from the market.
     * @param line the chosen line of marbles.
     * @return the action parser with the marble line field defined.
     */
    public ActionParser line(String line){
        if(Market.checkInputFormat(line.toUpperCase())){
            this.line = line;
            return this;
        }else{
            throw new IllegalArgumentException("line is not in the correct format");
        }
    }

    /**
     * Builder part defining the slot in which a purchased development card shall be stored.
     * @param slot the chosen slot.
     * @return the action parser with the slot field defined.
     * @throws IllegalArgumentException when an invalid slot (not a number between 0 and 2) is passed to the action parser.
     */
    public ActionParser slot(int slot) {
        if (slot >= 0 && slot <= 2){
            this.slot = slot;
            return this;
        } else {
            throw new IllegalArgumentException("invalid slot");
        }
    }

    /**
     * Builder part defining the stock from which resources are withdrawn.
     * @param targetDeposit the chosen stock.
     * @return the action parser with the target stock field defined.
     * @throws IllegalArgumentException if an incorrect format (a number not between 1 and 3) is passed.
     */
    public ActionParser targetDeposit(int targetDeposit){
        if(targetDeposit == 1 || targetDeposit == 2 || targetDeposit == 3){
            this.targetDeposit = targetDeposit;
            return this;
        }else{
            throw new IllegalArgumentException("targetDeposit is not in the correct format");
        }
    }

    /**
     * Builder part defining the shelf in which a resource shall be stored.
     * @param shelf the chosen shelf in the warehouse.
     * A number between 0 and 2 indicates a normal warehouse's shelf.
     * 3 indicates the first extra shelf deriving from a leader card bonus.
     * 4 indicates the second potential extra shelf deriving from a leader card bonus (this case happens only when a player possesses two leader cards with both storage bonus).
     * @return the action parser with the development card field defined.
     * @throws IllegalArgumentException if an incorrect format (a number not between 0 and 4) is passed.
     */
    public ActionParser shelf(int shelf){
        if(shelf>=0 && shelf < 4){
            this.shelf = shelf;
            return this;
        }else{
            throw new IllegalArgumentException("shelf is not in the correct format");
        }
    }

    /**
     * Builder part defining whether a black marble is being handled.
     * @param blackMarble a flag indicating whether a black marble is involved in this action.
     * @return the action parser with the black marble field defined.
     */
    public ActionParser blackMarble(boolean blackMarble){
        this.blackMarble = blackMarble;
        return this;
    }

    /**
    * Builder part defining the kind of marble involved in the action.
    * @param marble the marble kind characters.
    * @return the action parser with the marble field defined.
    * @throws IllegalArgumentException if an incorrect format is passed.
     */
    public ActionParser marble(String marble) throws IllegalArgumentException{
        try {
            Marble marble1 = new Marble(marble);
            this.marble = marble;
            return this;
        }catch (IllegalArgumentException ignored){
            throw new IllegalArgumentException("marble is not in the correct format");
        }
    }

    /**
    * Builder part defining the marble color involved in the action.
    * @param color the marble color.
    * @return the action parser with the color field defined.
     */
    public ActionParser color(Color color){
        this.color = color;
        return this;
    }

    /**
     * Builder part defining the list of resources involved in the action.
     * @param resources the resources involved in the action.
     * @return the action parser with the resources field defined.
     */
    public ActionParser resources(Resource[] resources){
        this.resources = resources;
        return this;
    }

    /**
    * Builder part defining the warehouse update to pass to server (only UpdateAction!).
    * @param warehouseShelves the warehouse status.
    * @return the action parser with the warehouse field defined.
     */
    public ActionParser warehouse(List<Resource>[] warehouseShelves) {
        this.warehouseShelves = warehouseShelves;
        return this;
    }

    /**
    * This method builds the action request to analyse.
    * @return the action request.
     */
    @SuppressWarnings("unchecked")
    public JSONObject buildRequest(){
        JSONObject actionRequest = new JSONObject();
        actionRequest.put("action",action);
        actionRequest.put("targetPlayer",playerUsername);
        actionRequest.put("developmentCard",developmentCard);
        if (developmentCardsToActivate != null) {
            JSONArray dCards = new JSONArray();
            dCards.addAll(developmentCardsToActivate);
            actionRequest.put("developmentCardsToActivate", dCards);
        }
        actionRequest.put("leaderCard",leaderCard);
        actionRequest.put("targetDeposit",targetDeposit);
        actionRequest.put("shelf",shelf);
        actionRequest.put("blackMarble",blackMarble);
        actionRequest.put("marble",marble);
        actionRequest.put("color",color);
        actionRequest.put("line",line);
        if (resources != null) {
            JSONArray productionResources = new JSONArray();
            productionResources.addAll(java.util.Arrays.asList(resources));
            actionRequest.put("resources", productionResources);
        }
        actionRequest.put("slot",slot);
        if (warehouseShelves != null) {
            JSONArray shelves = new JSONArray();
            JSONArray shelf1 = new JSONArray();
            JSONArray shelf2 = new JSONArray();
            JSONArray shelf3 = new JSONArray();
            shelf1.addAll(warehouseShelves[0]);
            shelf2.addAll(warehouseShelves[1]);
            shelf3.addAll(warehouseShelves[2]);
            shelves.add(shelf1);
            shelves.add(shelf2);
            shelves.add(shelf3);
            actionRequest.put("warehouse shelves", shelves);
        }
        return actionRequest;
    }

    /**
    * This method builds the action to perform based on the request created in buildRequest() method.
    * @param actionRequest the action request to analyse.
    * @return the action to perform.
    * @throws IllegalArgumentException if a field misses.
     */
    public Action buildAction(JSONObject actionRequest) throws IllegalArgumentException{
        if(isRequestBuilder){
            return null;
        }
        try {
            this.targetPlayer = getPlayer(actionRequest.get("targetPlayer").toString());
        }catch (Exception ignored){
            throw new IllegalArgumentException("player field missing or player not in playerList");
        }
        this.request = actionRequest;
        //ritorna la action richiesta
        try {
            return actionMap.get(Actions.valueOf(actionRequest.get("action").toString())).get();
        }catch (Exception e){
            //throw new IllegalArgumentException("missing fields for the requested action");
            e.printStackTrace();
            return null;
        }
    }

    /**
    * @return the market purchase action.
     */
    private Supplier<Action> marketPurchase(){
        return () -> new PurchaseAction(targetPlayer, request.get("line").toString(),market);
    }

    /**
    * @return the development card purchase action.
    * @throws IllegalArgumentException if something goes wrong while performing the action (the error is specified in the exception's message).
     */
    private Supplier<Action> developmentPurchase() throws IllegalArgumentException{
        return () -> new PurchaseAction(this.targetPlayer,(DevelopmentCard) cardSet.findCard(this.request.get("developmentCard").toString()),(int) request.get("targetDeposit"),cardSet,(int) request.get("slot"));
    }

    /**
    * @return the leader card activation action.
     */
    private Supplier<Action> activateLeader(){
        return () -> new ActivationAction(this.targetPlayer,(LeaderCard) cardSet.findCard(this.request.get("leaderCard").toString()));
    }

    /**
     * @return the production activation action through a list of development cards.
     */
    private Supplier<Action> activateProduction(){
        return () -> {
            List<DevelopmentCard> devCards = new ArrayList<>();
            JSONArray developmentCardsToActivate = (JSONArray) request.get("developmentCardsToActivate");
            for (Object o : developmentCardsToActivate) {
                String id = o.toString();
                if (CardSet.isDevelopmentCard(id)) {
                    devCards.add(targetPlayer.getPersonalDashboard().getDevCardsDeck().stream().filter(card -> card.generateId().equals(id)).findFirst().get());
                }
            }
            return new ActivationAction(this.targetPlayer, devCards, (int) request.get("targetDeposit"));
        };
    }

    /**
     * @return the basic production activation action.
     */
    private Supplier<Action> activateBasicProduction(){
        return () -> new ActivationAction(this.targetPlayer, Resource.valueOf(request.get("resources").toString().split(",")[0].substring(1).toUpperCase()), Resource.valueOf(request.get("resources").toString().split(",")[1].toUpperCase()), Resource.valueOf(removeLastChar(request.get("resources").toString().split(",")[2].toUpperCase())));
    }
    private Supplier<Action> activateLeaderProduction(){
        return () -> new ActivationAction(this.targetPlayer, Resource.valueOf(request.get("resources").toString().split(",")[0].substring(1).toUpperCase()), null, Resource.valueOf(removeLastChar(request.get("resources").toString().split(",")[2].toUpperCase())));
    }

    /**
     * @return the resource discard action.
     */
    private Supplier<Action> discardResource(){
        return () -> new DiscardAction(this.playerList, this.targetPlayer, (String) request.get("marble"), (boolean) request.get("blackMarble"));
    }

    /**
     * @return the leader card discard action.
     */
    private Supplier<Action> discardLeader(){
        return () -> new DiscardAction(this.targetPlayer,(LeaderCard) this.cardSet.findCard(request.get("leaderCard").toString()));
    }

    /**
     * @return the resource deposit action.
     */
    private Supplier<Action> deposit(){
        return () -> new DepositAction(this.targetPlayer,(String) request.get("marble"),(int) request.get("targetDeposit"),(int) request.get("shelf"),(boolean) request.get("blackMarble"));
    }

    /**
     * @return the action of updating the warehouse status in server.
     */
    private Supplier<Action> updateWarehouse() {
        return () -> {
            JSONArray shelves = (JSONArray) request.get("warehouse shelves");
            JSONArray shelf;
            this.warehouseShelves = new List[3];
            for (int i = 0; i < 3; i++) {
                shelf = (JSONArray) shelves.get(i);
                this.warehouseShelves[i] = new ArrayList<>();
                if (shelf.isEmpty()) {
                    // Scaffale vuoto
                    for(int j = 0; j < i+1; j++){
                        this.warehouseShelves[i].add(null);
                    }
                } else {
                    // Scaffale pieno interamente o parzialmente
                    for (int j = 0; j < i+1; j++) {
                        long occupancies = shelf.stream().filter(Objects::nonNull).count();
                        if (j < occupancies) {
                            this.warehouseShelves[i].add(Resource.valueOf(shelf.get(j).toString().toUpperCase()));
                        } else {
                            this.warehouseShelves[i].add(null);
                        }
                    }
                }
            }
            return new UpdateAction(this.targetPlayer, this.warehouseShelves);
        };
    }

    private Player getPlayer(String player){
        Optional<Player> p = playerList.stream().filter(pl -> pl.getUsername().equals(player)).findFirst();
        if(p.isEmpty()){
            throw new IllegalArgumentException("no player matches the username: "+player);
        }
        return p.get();
    }

    private String removeLastChar(String s) {
        return s.substring(0, s.length()-1);
    }

}
