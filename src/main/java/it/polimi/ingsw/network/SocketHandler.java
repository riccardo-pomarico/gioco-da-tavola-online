package it.polimi.ingsw.network;
import it.polimi.ingsw.task.NetworkTask;

import java.io.*;
import java.net.Socket;

public class SocketHandler implements Runnable, Serializable {
    private Socket socket;
    private NetworkController networkController;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connection = true;
    private String id;
    private boolean online = false;

    public SocketHandler(NetworkController networkController, Socket socket){
        this.networkController = networkController;
        this.socket = socket;
        setOnline();
        resetStreams();
    }

    public SocketHandler(NetworkController networkController, String ip, int port) throws IOException {
        this(networkController, new Socket(ip,port));
    }

    /**
     * Sends an operation to client or server.
     * @param operation the operation to send.
     */
    public void sendTask(NetworkTask operation){
        if(isOnline()) {
            try {
                out.writeObject(operation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(){
        try{
            while(isOnline()){
                NetworkTask receivedMessage = (NetworkTask)in.readObject();
                receivedMessage.setSender(id);
                receivedMessage.setController(networkController);
                networkController.handleTask(receivedMessage);
            }

        }catch (Exception e){
            e.printStackTrace();
            setOffline();
            networkController.handleDisconnection(this.id);
        }

    }

    /**
     * Resets the streams of the socket when a player has disconnected from the server.
     */
    private void resetStreams(){
        try {
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void updateSocket(Socket socket){
        this.socket = socket;
        //resetStreams();
    }
    public void updateSocket(String ip, int port) throws IOException {
        updateSocket(new Socket(ip,port));
    }
    public Socket getSocket(){
        return socket;
    }
    public void setOnline(){
        this.online = true;
    }
    public void setOffline(){
        this.online = false;
    }
    public boolean isOnline(){
        return this.online;
    }
}
