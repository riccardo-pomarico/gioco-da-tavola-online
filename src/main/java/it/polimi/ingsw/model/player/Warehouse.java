package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.exceptions.WarehouseException;
import it.polimi.ingsw.observer.ModelObservable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * class Warehouse
 */
public class Warehouse extends ModelObservable {
    private List<Resource[]> shelves = new ArrayList<>();
    // Additional fields in case of storage bonus
    private Warehouse extraStorage;
    private Resource extraStorageResource;
    private Resource extraStorageResource2;
    private final int extraStorageDimension = 2;

    /**
     * Class constructor initializing all warehouse's shelves.
     */
    public Warehouse(int numShelves){
        for(int i = 1; i<= numShelves; i++)
            addShelf(i);
    }

    /**
     * This method creates the data structure containing the extra shelf guaranteed as a leader card bonus.
     */
    public void createExtraStorage(Resource chosenResource) {
        // La nuova warehouse per le risorse extra dev'essere istanziata una sola volta.
        if (extraStorage == null) {
            extraStorage = new Warehouse(extraStorageDimension);
            // Per com'è concepito il costruttore della Warehouse, new Warehouse(2) istanzierebbe uno scaffale da un posto e uno da due, quando in questo caso ne servono due da due.
            // Bisogna rendere allora il primo scaffale da due posti.
            extraStorage.getShelves().set(0, new Resource[extraStorageDimension]);
        }
        // Se il primo scaffale extra è ancora vuoto.
        if (extraStorageResource == null){
            extraStorageResource = chosenResource;
            notify(notifyExtraStorage(extraStorageResource.toString()));
        } else if (extraStorageResource2 == null) {
            // Se il secondo scaffale extra è ancora vuoto.
            extraStorageResource2 = chosenResource;
            notify(notifyExtraStorage(extraStorageResource2.toString()));
        }
    }

    /**
     * Getter method of the group of the warehouse's shelves
     * @return the warehouse's shelves
     */
    public List<Resource[]> getShelves() {
        return shelves;
    }

    public Warehouse getExtraStorage() {
        return extraStorage;
    }

    /**
     * Returns a copy of the Warehouse
     * @return List of Resource[] containing all the resources in the player's warehouse, organized by shelf
     */
    public List<Resource[]> getWarehouse(){
        return new ArrayList<>(shelves);
    }
    /**
     * This method allows access to a shelf containing the resource r.
     * @param r the sought resource.
     * @return any shelf containing r.
     */
    public Resource[] getShelf(Resource r){
        for(Resource[] shelf : shelves){
            if(Arrays.stream(shelf).anyMatch(a -> a == r)){
                return shelf;
            }
        }
        throw new WarehouseException("no shelf found with " + r + " in it");
    }
    public Resource[] getExtraShelf(Resource r){
        if(r == extraStorageResource){
            return extraStorage.shelves.get(0);
        }else if(r == extraStorageResource2){
            return extraStorage.shelves.get(1);
        }else{
            return null;
        }
    }

    /**
     * Setter method to update the content of the warehouse's shelves.
     * @param shelfNumber the shelf in which to insert the resource r.
     * @param r the resource to insert.
     */
    public void addToShelf(int shelfNumber, Resource r) throws WarehouseException {
        if (shelfNumber <= 2) {
            Resource[] tmp = this.shelves.get(shelfNumber);
            for (Resource[] shelf : this.shelves) {
                List<Resource> shelfList = Arrays.asList(shelf.clone());
                if (shelfList.isEmpty()) {
                    continue;
                } else if (this.shelves.indexOf(shelf) != shelfNumber) {
                    if (shelfList.contains(r)) {
                        throw new WarehouseException("another shelf contains " + r.toString() + "!");
                    }
                } else {
                    if (!shelfList.contains(r) && shelf[0] != null) {
                        throw new WarehouseException("this shelf contains a different type of resource!");
                    } else if (shelf[shelf.length - 1] != null) {
                        throw new WarehouseException("this shelf is full!");
                    }
                }
            }
            int positionInShelf = 0;
            for (Resource resourceInShelf : this.shelves.get(shelfNumber)) {
                if (resourceInShelf == null) {
                    this.shelves.get(shelfNumber)[positionInShelf] = r;
                    break;
                }
                positionInShelf++;
            }
        } else if (shelfNumber == 3 || shelfNumber == 4) {
            // Deposito in uno scaffale extra a seconda di quello selezionato
            try {
                addToExtraShelf(shelfNumber-2, r);
            } catch (IllegalArgumentException e) {
                throw new WarehouseException(e.getMessage());
            }
        }
    }


    /**
     * Counts the number of resources r present in all shelves containing it.
     * @param r the sought resource.
     * @return the number of resources r in all shelves (0 if it isn't present at all).
     */
    public int count(Resource r) {
        int quantity = 0;
        try {
            for (Resource resource : getShelf(r)) {
                if (resource == r) {
                    quantity++;
                }
            }
            if(extraStorage != null){
                try {
                    quantity += extraStorage.count(r);
                }catch (WarehouseException f){
                    return 0;
                }
            }
            return quantity;
        }catch(WarehouseException e){
            if(extraStorage != null){
                try {
                    return extraStorage.count(r);
                }catch (WarehouseException f){
                    return 0;
                }
            }
            return 0;
        }
    }

    /**
     * Removes an array of resources from the warehouse.
     * @param resources the array of resources to remove.
     */
    public void remove(Resource[] resources){
        for (Resource r : resources) {
            remove(r);
        }
    }

    /**
     * Removes a resource from the warehouse.
     * @param resource the resource to remove.
     */
    public void remove(Resource resource){
        int numResources = count(resource);
        try {
            Resource[] shelf = getShelf(resource);
            if(numResources > shelf.length){
                numResources -= shelf.length;
                throw new WarehouseException("");
            }
            shelf[numResources-1] = null;
        }catch (WarehouseException | IndexOutOfBoundsException e){
            try{
                if(extraStorage!= null) {
                    getExtraShelf(resource)[numResources- 1] = null;
                }
            }catch (WarehouseException ignored){ }
        }
    }

    public void remove(List<Resource> resources){
        for (Resource r : resources){
            remove(r);
        }
    }

    /**
     * Creates a shelf with a determined size defined by @param size.
     * @param size the size of the shelf to generate.
     * @throws IllegalArgumentException if a negative or null size is passed.
     */
    public void addShelf(int size) throws IllegalArgumentException{
        if( size>0 ) {
            this.shelves.add(new Resource[size]);
        }else{
            throw new IllegalArgumentException("size of shelf must be > 0");
        }
    }

    /**
     * This method summarizes the controls to make in order to check whether the warehouse is completely full.
     * @return the status of the emptiness of the warehouse: is it totally full?
     */
    public boolean isAllFull() {
        if(extraStorage == null) {
            return shelves.stream().allMatch(shelf -> Arrays.stream(shelf).noneMatch(Objects::isNull));
        }else{
            if(!shelves.stream().allMatch(shelf -> Arrays.stream(shelf).noneMatch(Objects::isNull))){
                return false;
            }else{
                return extraStorage.getShelves().stream().allMatch(shelf -> Arrays.stream(shelf).noneMatch(Objects::isNull));
            }
        }
    }

    /**
     * This method updates the server data structure representing the warehouse.
     * @param warehouse the new status of the warehouse updated from the client (e.g. after moving the resources within it).
     */
    public void updateWarehouse(List<Resource[]> warehouse) {
        // La nuova warehouse (aggiornata in Cli) deve sovrascrivere quella memorizzata nel model
        shelves = new ArrayList<>();
        for (int i = 1; i<= 3; i++) {
            addShelf(i);
            if (warehouse.get(i-1) == null) {
                shelves.set(i-1, null);
            } else {
                shelves.set(i-1, warehouse.get(i-1));
            }
        }
    }

    /**
     * Creates the JSONObject associated to the player's warehouse.
     * This will also be included in the personal dashboard's update.
     * @return the JSONObject representing the player's warehouse.
     */
    @SuppressWarnings("unchecked")
    public JSONObject createJSONObject(int numShelves) {
        JSONObject result = new JSONObject();
        JSONArray warehouse1 = new JSONArray();
        JSONArray warehouse2 = new JSONArray();
        JSONArray warehouse3 = new JSONArray();
        JSONArray warehouse = new JSONArray();
        for (int i = 0; i < numShelves; i++) {
            for (Resource r : shelves.get(i)) {
                switch (i) {
                    case 0:
                        if (r != null) {
                            warehouse1.add(r.toString());
                        } else {
                            warehouse1.add("0");
                        }
                        break;
                    case 1:
                        if (r != null) {
                            warehouse2.add(r.toString());
                        } else {
                            warehouse2.add("0");
                        }
                        break;
                    case 2:
                        if (r != null) {
                            warehouse3.add(r.toString());
                        } else {
                            warehouse3.add("0");
                        }
                        break;
                }
            }
        }
        warehouse.add(warehouse1);
        warehouse.add(warehouse2);
        warehouse.add(warehouse3);
        result.put("Warehouse's shelves", warehouse);
        return result;
    }

    /**
     * This method sets off a chain of methods to initialize the extra storage data structures in client's view.
     * @param resource the resource destined to the newly created extra shelf.
     * @return the JSONObject representing the necessity to initialize extra storage in view and that that data structures will be reserved to resources of the parameter's kind.
     */
    @SuppressWarnings("unchecked")
    public JSONObject notifyExtraStorage(String resource) {
        JSONObject result = new JSONObject();
        result.put("operation", "add extra storage");
        result.put("resource", resource);
        return result;
    }

    /**
     * This method adds a resource to an extra shelf given by a leader card bonus.
     * @param choice the chosen extra shelf (there may be two of them rather than just one).
     * @param resourceToStore the resource to store.
     */
    public void addToExtraShelf(int choice, Resource resourceToStore) {
        if (matchResources(choice, resourceToStore)) {
            extraStorage.addToShelf(choice-1, resourceToStore);
        } else {
            switch (choice) {
                case 1:
                    throw new IllegalArgumentException("You're trying to store " + resourceToStore.toString() + ", whilst " + extraStorageResource.toString() + " is expected here!");
                case 2:
                    throw new IllegalArgumentException("You're trying to store " + resourceToStore.toString() + ", whilst " + extraStorageResource2.toString() + " is expected here!");
            }
        }
    }

    /**
     * Private method to check whether the resource to store matches the one expected in the chosen extra shelf.
     * @param choice the index (1 or 2) of the chosen extra shelf.
     * @param resourceToStore the resource to store in the extra shelf and to check.
     * @return if the resource to store corresponds to the one expected in that shelf.
     */
    private boolean matchResources(int choice, Resource resourceToStore) {
        return (choice == 1 && resourceToStore == extraStorageResource) || (choice == 2 && resourceToStore == extraStorageResource2);
    }

    public Resource getExtraStorageResource() {
        return extraStorageResource;
    }

    public Resource getExtraStorageResource2() {
        return extraStorageResource2;
    }
}
