package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.*;

/**
 * This class implements the scene showing the resources earned and manages their placement within the warehouse.
 */
public class DepositResourceSceneController extends GenericScene {

    @FXML
    protected AnchorPane rootpane;
    @FXML
    protected Label labelField;
    @FXML
    protected TextField shelfField;
    @FXML
    protected Button confirmButton, s0, s1, s2, s3, s4;
    @FXML
    protected ImageView res0_0, res1_0, res1_1, res2_0, res2_1, res2_2, res3_0, res3_1, res4_0, res4_1, extra1, extra2, tray;

    private Stack<String> resources = new Stack<>();
    private List<Resource>[] warehouse;
    private Resource extraStorage1, extraStorage2;
    protected ActionParser requestBuilder;
    @FXML
    public void initialize() {
        super.setTitle("Deposit resource");
        confirmButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBtnClick);
        //s0.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> deposit(0));
        s0.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.ANY));
        s0.setOnDragDropped(dragEvent -> deposit(0));
        s1.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.ANY));
        s1.setOnDragDropped(dragEvent -> deposit(1));
        s1.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> deposit(1));
        s2.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.ANY));
        s2.setOnDragDropped(dragEvent -> deposit(2));
        s3.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.ANY));
        s3.setOnDragDropped(dragEvent -> deposit(3));
        s4.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.ANY));
        s4.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> deposit(4));
        s3.setDisable(true);
        s4.setDisable(true);
        setDisable(false);
        renderWarehouse();

        if(!resources.empty()){
            labelField.setText("You have received a "+ new Marble(resources.peek()).getType() + "!");

            String pathname = "/images/resource_" + new Marble(resources.peek()).getType().toLowerCase() + ".png";
            Image image = new Image(pathname);
            tray.setImage(image);
            tray.setOnDragDetected(event -> {
                Dragboard db = tray.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("");
                db.setContent(content);
                event.consume();
            });


        }else{
            changeScene("personalDashboard");
        }
    }

    protected void renderWarehouse(){
        ImageView[][] resourceImages = new ImageView[][] {{res0_0}, {res1_0, res1_1}, {res2_0, res2_1, res2_2}, {res3_0, res3_1}, {res4_0, res4_1}};
        this. warehouse = ((PersonalDashboardSceneController) getScene("personalDashboard")).getWarehouse();
        String pathname;
        for(int i = 0; i< warehouse.length; i++){
            if( warehouse[i] != null && !warehouse[i].isEmpty() ){
                for(int j = 0; j<warehouse[i].size(); j++){
                    pathname = "/images/resource_" + warehouse[i].get(j).toString().toLowerCase() + ".png";
                    Image image = new Image(pathname);
                    resourceImages[i][j].setImage(image);
                }
            }
            if( i == 3 && extraStorage1 != null){
                Image image = new Image("/images/resource_" + extraStorage1.toString().toLowerCase() + ".png");
                extra1.setImage(image);
                s3.setDisable(false);
            }
            if( i == 4 && extraStorage2 != null){
                Image image = new Image("/images/resource_" + extraStorage2.toString().toLowerCase() + ".png");
                extra1.setImage(image);
                s4.setDisable(false);
            }
        }
    }


    public DepositResourceSceneController() {
        super("depositResource");
        requestBuilder = new ActionParser();

    }

    /**
     * Adds a resource to the queue of resources to deposit
     * @param resource the resource to add to the queue
     */
    public void setResource(String resource,Resource extrashelf1,Resource extrashelf2) {
        extraStorage1 = extrashelf1;
        extraStorage2 = extrashelf2;
        this.resources.push(resource);
        changeScene(getSceneId());
    }

    private void deposit(int shelf){
        try {
            JSONObject request = requestBuilder
                    .action(Actions.DEPOSIT)
                    .marble(resources.pop())
                    .targetDeposit(2)
                    .shelf(shelf)
                    .blackMarble(false)
                    .buildRequest();
            new Thread(() ->  notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        handleSceneChange();
    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBtnClick(Event event) {
        JSONObject request = requestBuilder
                .action(Actions.DISCARDRESOURCE)
                .marble(resources.pop())
                .blackMarble(false)
                .buildRequest();
        new Thread(() ->  notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
        handleSceneChange();
    }

    private void handleSceneChange(){
        setDisable(true);
        if(!resources.empty()){
            changeScene(getSceneId());
        }else{
            new Thread(() ->  notify(new Task<>(ViewObserver::blockIncomingTasks))).start();
            changeScene("personalDashboard");
        }
    }

    private void setDisable(boolean onOff){
        confirmButton.setDisable(onOff);
        s0.setDisable(onOff);
        s1.setDisable(onOff);
        s2.setDisable(onOff);
        s3.setDisable(onOff);
        s4.setDisable(onOff);
    }


}
