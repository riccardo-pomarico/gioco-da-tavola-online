package it.polimi.ingsw.model.exceptions;

/**

 * Custom exception for Warehouse Class, Handles all the possible exceptions in this class.
 */
public class WarehouseException extends RuntimeException{
    /**
     * Constructor for WarehouseException
     * @param errorMessage
     */
    public WarehouseException(String errorMessage) {
        super(errorMessage);
    }
}
