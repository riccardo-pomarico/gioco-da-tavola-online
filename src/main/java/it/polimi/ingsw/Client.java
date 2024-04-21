package it.polimi.ingsw;


import it.polimi.ingsw.network.ClientController;
import it.polimi.ingsw.view.Cli;
import it.polimi.ingsw.view.gui.GuiController;
import javafx.application.Application;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        ClientController clientController;
        int mode = 1;
        if(mode == 1){
            System.out.println("Initialization...\nSelect the type of UI you would like to use [cli/gui]");
            while(true){
                String ui = in.nextLine();
                if (ui.equalsIgnoreCase("cli")){
                    Cli cli = new Cli();
                    clientController= new ClientController(cli);
                    cli.addViewObserver(clientController);
                    clientController.init();
                    break;
                }else if (ui.equalsIgnoreCase("gui")){
                    Application.launch(GuiController.class);
                }else{
                    System.out.println("Input not recognised, please select the type of UI you would like to use [cli/gui]");
                }
            }
        }else if (mode == 2){
            System.out.println("Auto initialization mode.. (CLI)");
            Cli cli = new Cli();
            clientController= new ClientController(cli);
            cli.addViewObserver(clientController);
            clientController.autoInit();

        } else if (mode == 3){
            Cli cli = new Cli();
            clientController= new ClientController(cli);
            cli.addViewObserver(clientController);
            clientController.init();
        } else if (mode == 4){
            Application.launch(GuiController.class);
        }

    }
}

