package it.polimi.ingsw.task;

import it.polimi.ingsw.network.NetworkController;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Abstract class representing all operations involved in the network part of the code.
 * It also implements a NetworkConsumer interface with the same functions of Consumer, but with the extra feature of being serializable (essential characteristics to send operations in the network).
 */
public abstract class NetworkTask implements Serializable {
    private String sender;
    private transient NetworkController networkController;
    private boolean priority = false;

    public boolean isPriority() {
        return priority;
    }
    public void setPriority() {
        priority = true;
    }
    public String getSender() { return sender; }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public void setController(NetworkController networkController){
        this.networkController = networkController;
    }
    public NetworkController getController() { return networkController; }
    public interface NetworkConsumer<T> extends Consumer<T>, Serializable{ }
    public abstract void execute();


}
