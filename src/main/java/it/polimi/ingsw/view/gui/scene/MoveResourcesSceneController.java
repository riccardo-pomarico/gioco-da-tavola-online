package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;

import org.json.simple.JSONObject;

import java.util.List;

public class MoveResourcesSceneController extends DepositResourceSceneController{

    @FXML
    private ImageView res0_0, res1_0, res1_1, res2_0, res2_1, res2_2, res3_0, res3_1, res4_0, res4_1, extra1, extra2;
    @FXML
    private Button swap1_2, swap2_3, exit, s3,s4;
    @FXML
    private Label error;

    private List<Resource>[] warehouse;
    private List<Resource> extraStorage1, extraStorage2;

    @FXML
    public void initialize(){
        error.setVisible(false);
        setDragActions(s0,0);
        setDragActions(s1,1);
        setDragActions(s2,2);

        s3.setDisable(true);
        s4.setDisable(true);
        this.warehouse = ((PersonalDashboardSceneController) getScene("personalDashboard")).getWarehouse();
        renderWarehouse();
        if(extraStorage1 != null){
            setDragActions(s3,3);
        }
        if(extraStorage2 != null){
            setDragActions(s4,4);
        }
        exit.addEventHandler(MouseEvent.MOUSE_CLICKED,mouseEvent -> exit());
    }
    //imposta sul bottone le varie onDrag events
    private void setDragActions(Button button, int shelf){
        button.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.ANY));
        button.setOnDragDetected(dragEvent -> onShelfDrag(button,shelf));
        button.setOnDragDropped(dragEvent -> onShelfDrop(dragEvent,shelf));
    }
    private void onShelfDrag(Node node, int a){
        Dragboard db = node.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(Integer.toString(a));
        db.setContent(content);
    }
    private void onShelfDrop(DragEvent event, int b){
        String a = event.getDragboard().getString();
        swap(Integer.parseInt(a),b);
        event.consume();
    }

    //torna alla personalDashboard
    private void exit(){
        ((PersonalDashboardSceneController) getScene("personalDashboard")).turnOffButtons();
        changeScene("personalDashboard");
        new Thread(() -> notify(new Task<>(ViewObserver::afterAction))).start();

    }

    //scambia i due scaffali e invia una updateAction al server
    private void swap(int shelf1, int shelf2){
        if(warehouse[shelf1].size() > shelf2+1 || warehouse[shelf2].size() > shelf1+1 ){
            error.setVisible(true);
        }else {
            List<Resource>[] newWarehouse = warehouse;
            List<Resource> tmp = newWarehouse[shelf1];
            newWarehouse[shelf1] = newWarehouse[shelf2];
            newWarehouse[shelf2] = tmp;
            JSONObject request = requestBuilder.action(Actions.UPDATEWAREHOUSE).warehouse(newWarehouse).buildRequest();
            new Thread(() -> notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
            changeScene("moveResources");
        }
    }
}
