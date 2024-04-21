package it.polimi.ingsw.model.player;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.exceptions.ImpossibleStorageException;
import it.polimi.ingsw.model.exceptions.WarehouseException;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.exceptions.PersonalDashboardException;
import it.polimi.ingsw.observer.ModelObservable;
import it.polimi.ingsw.observer.ModelObserver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Represents the Personal Dashboard game item.
 * Contains:
 *  - all the player's assets,
 *   including Resources (stored in Warehouse and Strongbox), the player's purchased development cards and activated leader cards.
 *  - the players's Pope's farvor track
 *  - all the necessary methods to interact with the above-mentioned elements
 */
public class PersonalDashboard extends ModelObservable {
    private final Warehouse warehouse;
    private final PopesFavorTrack popesFavorTrack;
    private final List<Resource> strongBox;
    private final List<DevelopmentCard> devCardsDeck;
    private final List<LeaderCard> leadCardsDeck;
    private final List<Marble> tray;
    private final String playerUsername;
    
    public PersonalDashboard(String playerUsername) {
         warehouse = new Warehouse(3);
         popesFavorTrack = new PopesFavorTrack();
         strongBox = new ArrayList<>();
         devCardsDeck = new ArrayList<>();
         leadCardsDeck = new ArrayList<>();
         tray = new ArrayList<>();
         this.playerUsername = playerUsername;
    }
    
    /**
     * Counts the quantity of the provided resource in the player's strongbox and warehouse
     * @param resource resource to count
     * @return two positions int array, 0 is the quantity in the strongbox, 1 in the warehouse
     */
    public int[] countResources(Resource resource){
       int strongBoxQuan = (int) strongBox.stream().filter(a -> a == resource).count();
       int wareHouseQuan;
       try{
           wareHouseQuan = warehouse.count(resource);
           if (warehouse.getExtraStorage() != null) {
               wareHouseQuan += warehouse.getExtraStorage().count(resource);
           }
       }catch (WarehouseException e){
           wareHouseQuan = 0;
       }
       return new int[] {strongBoxQuan,wareHouseQuan};
    }

    /**
     * * Checks if the provided resources are possessed by the player, in both the strongbox and the warehouse
     * @param resources the list of resources to check
     * @return two positions boolean array, 0 is the result of the check on the strongbox, 1 on the warehouse
     */
    public boolean[] checkResources(List<Resource> resources){
        boolean[] availability = new boolean[] { true, true };
        int[] resourceCount;
        int currentQuan;
        for(Resource r : resources){
            resourceCount = countResources(r);
            currentQuan = (int) resources.stream().filter(b -> b == r).count();
            if(resourceCount[0] < currentQuan){
                availability[0] = false;
            }
            if(resourceCount[1] < currentQuan){
                availability[1] = false;
            }
        }
        return availability;
    }

    /**
     * withdraws resources from the warehouse or strongbox
     * @param source 1 to withdraw from Strongbox, 2 to withdraw from warehouse, 3 from both
     * @param items list of items to withdraw
     * @throws PersonalDashboardException if there are not enough resources to be withdrawn
     * @throws IllegalArgumentException the first parameter is not either 1, 2 or 3
     */
    public void withdrawResources(int source, List<Resource> items) throws PersonalDashboardException, IllegalArgumentException {
        boolean[] availability = checkResources(items);
        if (source == 1) {
            if (availability[0]) {
                for (Resource r : items) {
                    strongBox.remove(strongBox.stream().filter(a -> a == r).findFirst().get());
                }
            } else {
                throw new PersonalDashboardException("not enough resources from strongbox");
            }
        } else if (source == 2) {
            if (availability[1]) {
                warehouse.remove(items);
            } else {
                throw new PersonalDashboardException("not enough resources from warehouse");
            }
        } else if (source == 3) {
            Map<Resource,int[]> requirementsMap = new HashMap<>();
            Arrays.asList(Resource.values()).forEach( resource ->  requirementsMap.put(resource,countResources(resource)));

            //divisione della lista di risorse richieste in risorse da prelevare dal magazzino e risorse da prelevare dal forziere
            List<Resource> withdrawFromWarehouse = new ArrayList<>(),withdrawFromStrongbox = new ArrayList<>();
            for(Resource resource : items){
                if(requirementsMap.get(resource)[1]>0){
                    withdrawFromWarehouse.add(resource);
                    requirementsMap.get(resource)[1]--;
                }else{
                    if(requirementsMap.get(resource)[0]>0){
                        withdrawFromStrongbox.add(resource);
                        requirementsMap.get(resource)[0]--;
                    }else {
                        throw new ActionException("you don't have enough resources!");
                    }
                }
            }

            try{
                withdrawResources(2,withdrawFromWarehouse);
                withdrawResources(1,withdrawFromStrongbox);
            }catch (WarehouseException e){
                throw new ActionException("you don't have enough resources!");
            }
        }
        notify(createJSONObject());
    }

    /**
     * This method is responsible for depositing a resource in the chosen destination.
     * @param destination 1 = personal dashboard's strongbox, 2 = personal dashboard's warehouse.
     * @param shelf the shelf in the warehouse (this value is set to 0 by default if destination 1 is chosen instead).
     * @param item the resource to deposit.
     */
    public void depositResource(int destination, int shelf, Resource item) throws WarehouseException, ImpossibleStorageException {
        if (destination == 1) {
            this.strongBox.add(item);
        } else if (destination == 2) {
            if (warehouse.getShelves().stream().anyMatch(sh -> Arrays.asList(sh).contains(item) && Arrays.stream(sh).noneMatch(Objects::isNull)) || warehouse.isAllFull()) {
                if(warehouse.getExtraStorage() == null || warehouse.getExtraStorage().count(item) == 2 ) {
                    throw new ImpossibleStorageException(item, "There's no possible way to store this resource in the warehouse...");
                }
            }
            warehouse.addToShelf(shelf, item);
        }
        notify(createJSONObject());
    }

    /**
     * adds a development card to the deck
     * @param card DevelopmentCard to add to the deck
     * @throws PersonalDashboardException if @param card is already present in the player's card deck.
     */
    public void addCard(Card card) throws PersonalDashboardException{
        //controlla se la carta è di tipo development o di tipo leader
        if(card.generateId().charAt(0) == 'D'){
            //se non trova già quella carta nella lista la aggiunge
            if( devCardsDeck.stream().noneMatch(o -> o.generateId().equals(card.generateId()))){
                devCardsDeck.add((DevelopmentCard) card);
            }else{
                throw new PersonalDashboardException("["+card.generateId()+"] card is already in the deck");
            }
        } else {
            //se non trova già quella carta nella lista la aggiunge
            if( leadCardsDeck.stream().noneMatch(o -> o.generateId().equals(card.generateId()))){
                leadCardsDeck.add((LeaderCard) card);
                return;
            }else{
                throw new PersonalDashboardException("["+card.generateId()+"] card is already in the deck");
            }
        }
        notify(createJSONObject());
        if (devCardsDeck.size() == 7) {
            notify(announceFinalTurn());
        }
    }

    /**
     * This method removes a card from the player's card deck when they want to discard it (to activate a production or to obtain a Faith point if it's a leader card).
     * @param card the card to discard.
     */
    public void removeCard(Card card){
        //controlla se la carta è di tipo development o di tipo leader
        if(card.generateId().charAt(0) == 'D'){
            //rimuove la carta con lo stesso id
           devCardsDeck.removeIf(c -> c.generateId().equals(card.generateId()));

        } else {
            //rimuove la carta con lo stesso id
            leadCardsDeck.removeIf(c -> c.generateId().equals(card.generateId()));
        }
        notify(createJSONObject());
    }

    /**
     * Adds a marble to the personal dashboard's tray, so that it will be appropriately processed.
     * @param marble the marble to process.
     */
    public void addToTray(Marble marble){
        if(!marble.generateId().equals("SW")){
            tray.add(marble);
        }else{
            throw new IllegalArgumentException();
        }
    }

    /**
     * This method triggers the Observer pattern ModelObserver-ModelObservable to notify the client that a marble has been added to the tray.
     * @param marble the marble inserted in the tray.
     * @param player the player whose tray is being handled.
     */
    public void notifyTray(Marble marble, String player) {
        JSONObject trayNotify = new JSONObject();
        trayNotify.put("operation","tray");
        trayNotify.put("marble",marble.generateId());
        trayNotify.put("player", player);
        notify(trayNotify);
    }

    /**
     * Removes the marble from the tray when it has already been taken care of.
     * @param marble the processed marble.
     */
    public void removeFromTray(Marble marble){
        Optional<Marble> toRemove = tray.stream().filter(a -> a.generateId().equals(marble.generateId())).findFirst();
        toRemove.ifPresentOrElse(tray::remove,() -> {throw new IllegalArgumentException("marble not present in the tray");});
    }

    public List<Marble> getTray(){
        return new ArrayList<>(tray);
    }

    public PopesFavorTrack getPopesFavorTrack() {
        return popesFavorTrack;
    }

    public List<DevelopmentCard> getDevCardsDeck() {
        return devCardsDeck;
    }

    public List<LeaderCard> getLeadCardsDeck() {
        return leadCardsDeck;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public List<Resource> getStrongBox() {
        return strongBox;
    }

    /**
     * Counts how many resources of type r are already present in the strongbox.
     * @param r the kind of resources whose quantity in the strongbox the player wants to check.
     * @return the number of resources of type r present in the strongbox.
     */
    public long countStrongBox(Resource r) {
        return strongBox.stream().filter(res -> res == r).count();
    }

    /**
     * This method creates the JSONObject associated to the player's personal dashboard, so the data structure in the client's view can be accordingly updated.
     * It is also useful if other players want to peek into it.
     * @return the JSONObject representing the player's personal dashboard.
     */
    @SuppressWarnings("unchecked")
    public JSONObject createJSONObject() {
        JSONObject result = new JSONObject();
        result.put("operation", "Personal Dashboard's update");
        result.put("player", playerUsername);
        result.put("Pope's Favor Track status", popesFavorTrack.createJSONObject());
        JSONArray devCardsDeckJSONArray = new JSONArray();
        for (DevelopmentCard dc : devCardsDeck) {
            devCardsDeckJSONArray.add(dc.getId());
        }
        result.put("player's Development Cards", devCardsDeckJSONArray);
        JSONArray leadCardsDeckJSONArray = new JSONArray();
        for (LeaderCard lc : leadCardsDeck) {
            leadCardsDeckJSONArray.add(lc.getId());
        }
        result.put("player's Leader Cards", leadCardsDeckJSONArray);
        JSONArray strongBoxJSONArray = new JSONArray();
        strongBox.forEach(resource -> strongBoxJSONArray.add(resource.toString()));
        result.put("player's strongbox", strongBoxJSONArray);
        result.put("player's warehouse", warehouse.createJSONObject(3));
        if (warehouse.getExtraStorage() != null) {
            result.put("extra shelves", warehouse.getExtraStorage().createJSONObject(2));
        }
        return result;
    }

    /**
     * This method creates the JSONObject which will trigger the Observer pattern ModelObserver-ModelObservable to notify the client that the personal dashboard's update has finished.
     * @return the JSONObject with the operation field "end of Personal Dashboard's update".
     */
    @SuppressWarnings("unchecked")
    public JSONObject notifyEndUpdate() {
        JSONObject result = new JSONObject();
        result.put("operation", "end of Personal Dashboard's update");
        return result;
    }

    /**
     * This method creates the JSONObject which will trigger the Observer pattern ModelObserver-ModelObservable to notify the client that the personal dashboard's update has finished.
     * @param cardIds the IDs of the development cards Lorenzo has taken.
     * @return the JSONObject with the operation field "Lorenzo takes two cards".
     */
    @SuppressWarnings("unchecked")
    public JSONObject notifyLorenzoTakingTwoCards(List<String> cardIds) {
        JSONObject result = new JSONObject();
        result.put("operation", "Lorenzo takes two cards");
        JSONArray cards = new JSONArray();
        cards.addAll(cardIds);
        result.put("taken cards", cards);
        return result;
    }

    @Override
    public void addModelObserver(ModelObserver observer){
        super.addModelObserver(observer);
        popesFavorTrack.addModelObserver(observer);
    }

    /**
     * This method creates the JSONObject which will trigger the Observer pattern ModelObserver-ModelObservable to notify the production of a development card has been activated.
     * @param card the card whose production has been activated.
     * @return the JSONObject with the operation field "card was used".
     */
    @SuppressWarnings("unchecked")
    public JSONObject setCardActivated(Card card) {
        card.used();
        JSONObject result = new JSONObject();
        result.put("operation", "card was used");
        result.put("card", card.getId());
        return result;
    }

    /**
     * This method creates the JSONObject which will trigger the Observer pattern ModelObserver-ModelObservable to notify the client that someone has reached the game termination condition.
     * The final turn will begin with the invocation of this method.
     * @return the JSONObject announcing the final turn.
     */
    public JSONObject announceFinalTurn() {
        JSONObject result = new JSONObject();
        result.put("operation", "final turn");
        return result;
    }
}
