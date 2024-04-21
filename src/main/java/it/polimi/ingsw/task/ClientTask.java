package it.polimi.ingsw.task;

import it.polimi.ingsw.network.Client;

import java.io.Serializable;

/**
 * Class of Consumers of all Client implementations (ClientController).
 */
public class ClientTask extends NetworkTask implements Serializable {
    private NetworkConsumer<Client> operation;
    public ClientTask(NetworkConsumer<Client> operation){
        this.operation = operation;
    }
    public void execute(){
        operation.accept((Client) getController());
    }
}
