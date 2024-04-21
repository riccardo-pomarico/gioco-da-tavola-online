package it.polimi.ingsw.task;

import it.polimi.ingsw.network.Server;

import java.io.Serializable;

/**
 * Class of Consumers of all Server implementations (ServerController).
 */
public class ServerTask extends NetworkTask implements Serializable{
    private NetworkConsumer<Server> operation;
    public ServerTask(NetworkConsumer<Server> operation){
        this.operation = operation;
    }
    public void execute(){
        operation.accept((Server) getController());
    }
}
