package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.exceptions.WarehouseException;
import it.polimi.ingsw.model.resources.Resource;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class WarehouseTest {

    @Test
    public void getShelvesTest() {
        Warehouse warehouse = new Warehouse(3);
        List<Resource[]> shelves = new ArrayList<>();

        //getShelves su warehouse vuota
        shelves = warehouse.getShelves();
        assertEquals(3,shelves.size());
        for(int i = 0; i < 3; i++){
            for(int j = 0; j <= i; j++){
                assertNull(shelves.get(i)[j]);
            }
        }

        //input standard
        warehouse.addToShelf(0,Resource.STONE);
        warehouse.addToShelf(1,Resource.SHIELD);
        warehouse.addToShelf(1,Resource.SHIELD);
        warehouse.addToShelf(2,Resource.COIN);
        //caso standard
        shelves = warehouse.getShelves();
        assertEquals(Resource.STONE,shelves.get(0)[0]);
        assertEquals(Resource.SHIELD,shelves.get(1)[0]);
        assertEquals(Resource.SHIELD,shelves.get(1)[1]);
        assertEquals(Resource.COIN,shelves.get(2)[0]);

    }

    @Test
    public void getShelfTest() {
        Warehouse warehouse = new Warehouse(3);
        //prova a prendere una shelf da una Warehouse vuota
        try{
            warehouse.getShelf(Resource.SHIELD);
        }catch (WarehouseException e){
            assertEquals("no shelf found with SHIELD in it", e.getMessage());
        }

        //input standard
        warehouse.addToShelf(0,Resource.STONE);
        warehouse.addToShelf(1,Resource.SHIELD);
        warehouse.addToShelf(1,Resource.SHIELD);
        warehouse.addToShelf(2,Resource.COIN);
        Resource[] shelf = new Resource[2];

        //caso standard
        try{
            shelf = warehouse.getShelf(Resource.SHIELD);
        }catch (WarehouseException e){
            assertEquals(Resource.STONE, shelf[0]);
        }
    }

    @Test
    public void getExtraShelfTest(){
        Warehouse warehouse = new Warehouse(3);
        warehouse.createExtraStorage(Resource.COIN);
        warehouse.addToExtraShelf(1,Resource.COIN);
        warehouse.remove(Resource.COIN);
        assertEquals(0,warehouse.count(Resource.COIN));
    }

    @Test
    public void setShelfTest() {
        Warehouse warehouse = new Warehouse(3);
        //configurazione normale
        Resource[] resources = new Resource[] {
                Resource.COIN,
                Resource.SERVANT,
                Resource.SERVANT,
                Resource.SHIELD,
                Resource.SHIELD,
        };
        warehouse.addToShelf(0, resources[0]);
        warehouse.addToShelf(1, resources[1]);
        warehouse.addToShelf(1, resources[2]);
        warehouse.addToShelf(2,resources[3]);
        warehouse.addToShelf(2,resources[4]);
        List<Resource[]> shelves = warehouse.getShelves();
        List<Resource> shelfResources = new ArrayList<>();
        shelfResources.addAll(Arrays.asList(shelves.get(0)));
        shelfResources.addAll(Arrays.asList(shelves.get(1)));
        shelfResources.addAll(Arrays.asList(shelves.get(2)));
        for (int i = 0; i < resources.length; i++){
            assertEquals(resources[i],shelfResources.get(i));
        }
        //prova ad inserire una risorsa presente in uno scaffale in un altro scaffale
        try{
            warehouse.addToShelf(2,resources[0]);
        }catch (WarehouseException e){
            assertEquals("another shelf contains " + resources[0] + "!",e.getMessage());
        }
        //prova ad inserire una risorsa in uno scaffale in cui sono presenti risorse di un altro tipo
        try{
            warehouse.addToShelf(2,Resource.STONE);
        }catch (WarehouseException e){
            assertEquals("this shelf contains a different type of resource!",e.getMessage());
        }
        //prova ad inserire una risorsa in uno scaffale pieno
        try{
            warehouse.addToShelf(2,Resource.SHIELD);
            warehouse.addToShelf(2,Resource.SHIELD);
        }catch (WarehouseException e){
            assertEquals("this shelf is full!",e.getMessage());
        }
    }

    @Test
    public void countTest() {
        Warehouse warehouse = new Warehouse(3);
        //prova a contare in una Warehouse vuota
        assertEquals(0, warehouse.count(Resource.STONE));

        //input standard
        warehouse.addToShelf(0, Resource.STONE);
        warehouse.addToShelf(1, Resource.SHIELD);
        warehouse.addToShelf(2, Resource.COIN);
        warehouse.addToShelf(2, Resource.COIN);
        assertEquals(1, warehouse.count(Resource.STONE));
        assertEquals(1, warehouse.count(Resource.SHIELD));
        assertEquals(2, warehouse.count(Resource.COIN));

        //prova a contare una risorsa non presente
        assertEquals(0, warehouse.count(Resource.SERVANT));
        warehouse.createExtraStorage(Resource.SERVANT);
        assertNotNull(warehouse.getExtraStorageResource());
        warehouse.addToShelf(3, Resource.SERVANT);
        assertEquals(1, warehouse.count(Resource.SERVANT));
        warehouse.addToShelf(3, Resource.SERVANT);
        assertEquals(2, warehouse.count(Resource.SERVANT));

        warehouse.createExtraStorage(Resource.COIN);
        assertNotNull(warehouse.getExtraStorageResource2());
        try {
            warehouse.addToShelf(3, Resource.COIN);
        } catch (WarehouseException e) {
            assertEquals("You're trying to store COIN, whilst SERVANT is expected here!", e.getMessage());

            //conta con una risorsa nell'extra storage
            warehouse = new Warehouse(3);
            warehouse.createExtraStorage(Resource.STONE);
            warehouse.addToShelf(1, Resource.STONE);
            warehouse.addToExtraShelf(1, Resource.STONE);
            assertEquals(2, warehouse.count(Resource.STONE));

        }
    }

    @Test
    public void removeTest() {
        Warehouse warehouse = new Warehouse(3);

        //prova ad eliminare da una Warehouse vuota
        warehouse.remove(new Resource[] {Resource.SHIELD, Resource.COIN});
        assertEquals(0,warehouse.count(Resource.SHIELD));
        assertEquals(0,warehouse.count(Resource.COIN));

        //input standard
        warehouse.addToShelf(0,Resource.STONE);
        warehouse.addToShelf(1,Resource.SHIELD);
        warehouse.addToShelf(2,Resource.COIN);
        warehouse.addToShelf(2,Resource.COIN);
        warehouse.remove(new Resource[] {Resource.SHIELD, Resource.COIN});
        assertEquals(0,warehouse.count(Resource.SHIELD));
        assertEquals(1,warehouse.count(Resource.COIN));

        //provo a rimuovere più risorse di quante ce ne siano
        warehouse.remove(new Resource[] {Resource.STONE, Resource.STONE});
        assertEquals(0,warehouse.count(Resource.STONE));

        //prova a rimuovere dall'extraStorage
        warehouse = new Warehouse(3);
        warehouse.createExtraStorage(Resource.STONE);
        warehouse.addToExtraShelf(1,Resource.STONE);
        warehouse.addToExtraShelf(1,Resource.STONE);
        warehouse.addToShelf(1,Resource.STONE);
        warehouse.remove(Resource.STONE);
        assertEquals(2,warehouse.count(Resource.STONE));

    }

    @Test
    public void addShelfTest() {
        Warehouse warehouse = new Warehouse(3);
        warehouse.addShelf(1);
        assertEquals(4,warehouse.getShelves().size());
        try{
            warehouse.addShelf(-1);
        }catch (IllegalArgumentException e){
            assertEquals("size of shelf must be > 0",e.getMessage());
        }
        try{
            warehouse.addShelf(0);
        }catch (IllegalArgumentException e){
            assertEquals("size of shelf must be > 0",e.getMessage());
        }
    }

    @Test
    public void shelvesReusabilityTest(){
        //testa la riutilizzabilità degli scaffali
        Warehouse warehouse = new Warehouse(3);
        warehouse.addToShelf(0,Resource.COIN);
        warehouse.addToShelf(1,Resource.SHIELD);
        warehouse.addToShelf(2,Resource.SERVANT);
        warehouse.remove(Resource.COIN);
        warehouse.addToShelf(0,Resource.STONE);
        assertEquals(Resource.STONE,warehouse.getShelf(Resource.STONE)[0]);
    }

    @Test
    public void updateWarehouseTest() {
        Warehouse warehouse = new Warehouse(3);
        warehouse.addToShelf(0,Resource.COIN);
        warehouse.addToShelf(1,Resource.SHIELD);
        List<Resource[]> newWarehouse = new ArrayList<>();
        newWarehouse.add(new Resource[] {Resource.COIN});
        newWarehouse.add(null);
        newWarehouse.add(new Resource[] {Resource.SHIELD});
        warehouse.updateWarehouse(newWarehouse);
        assertEquals(warehouse.getShelves().get(2)[0], Resource.SHIELD);
        assertNull(warehouse.getShelves().get(1));
    }

    @Test
    public void createExtraStorage() {
        Warehouse w = new Warehouse(3);

        // Iniziamo col primo scaffale extra.
        w.createExtraStorage(Resource.COIN);
        for (int i = 0; i < 3; i++) {
            try {
                assertEquals(2, w.getExtraStorage().getShelves().get(i).length);
            } catch (IndexOutOfBoundsException e) {
                // Testiamo che gli scaffali siano effettivamente al massimo 2
                assertEquals(e.getMessage(), "Index 2 out of bounds for length 2");
            }
        }
        assertEquals(5, w.getShelves().size() + w.getExtraStorage().getShelves().size());
        w.addToExtraShelf(1, Resource.COIN);
        // Mi aspetto che il numero di risorse non nulle nel primo scaffale sia 1
        assertEquals(1, w.getExtraStorage().getShelves().stream().filter(r -> Arrays.stream(r).anyMatch(Objects::nonNull)).count());
        try {
            w.addToExtraShelf(1, Resource.STONE);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "You're trying to store STONE, whilst COIN is expected here!");
        }
        w.addToExtraShelf(1, Resource.COIN);
        try {
            w.addToExtraShelf(1, Resource.COIN);
        } catch (WarehouseException ex) {
            assertEquals(ex.getMessage(), "this shelf is full!");
        }

        // Proviamo con il secondo scaffale extra.
        w.createExtraStorage(Resource.SERVANT);
        // Per sicurezza ritestiamo che le dimensioni degli scaffali non siano cambiate così natüre.
        for (int i = 0; i < 3; i++) {
            try {
                assertEquals(2, w.getExtraStorage().getShelves().get(i).length);
            } catch (IndexOutOfBoundsException e) {
                assertEquals(e.getMessage(), "Index 2 out of bounds for length 2");
            }
        }
        assertEquals(5, w.getShelves().size() + w.getExtraStorage().getShelves().size());
        w.addToExtraShelf(2, Resource.SERVANT);
        w.addToExtraShelf(2, Resource.SERVANT);
        assertEquals(2, w.getExtraStorage().getShelves().stream().filter(r -> Arrays.stream(r).anyMatch(Objects::nonNull)).count());
        try {
            w.addToExtraShelf(2, Resource.COIN);
        } catch (IllegalArgumentException ex1) {
            assertEquals(ex1.getMessage(), "You're trying to store COIN, whilst SERVANT is expected here!");
        }
        try {
            w.addToExtraShelf(2, Resource.SERVANT);
        } catch (WarehouseException ex2) {
            assertEquals(ex2.getMessage(), "this shelf is full!");
        }
    }
}