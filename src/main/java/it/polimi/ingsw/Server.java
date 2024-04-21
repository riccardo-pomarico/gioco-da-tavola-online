package it.polimi.ingsw;
import it.polimi.ingsw.network.ServerController;

public class Server {
    public static void main(String[] args){
        ServerController serverController = new ServerController(1330);
        serverController.startServer();
    }
}
