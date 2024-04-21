package it.polimi.ingsw.controller.exceptions;

/**
 * exception thrown by Action classes
 */
public class ActionException extends RuntimeException{
    public ActionException(String msg){
        super(msg);
    }

}
