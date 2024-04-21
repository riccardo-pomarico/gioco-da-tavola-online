package it.polimi.ingsw.model;


/**
 * interface, all the classes that implement it can generate an identifier
 */
public interface IdGenerable {
    public String generateId();

    //LE BIGLIE NERE DEVONO SALVARE N SULL'ID PERCHE' ALTRIMENTI SI SOVRAPPONGO CON LE BLUE
}
