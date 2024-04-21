package it.polimi.ingsw.network;

import it.polimi.ingsw.task.NetworkTask;

import java.io.Serializable;

public interface NetworkController extends Serializable {
    void handleTask(NetworkTask task);
    void handleDisconnection(String subject);
}
