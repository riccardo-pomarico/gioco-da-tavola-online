package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.view.gui.SceneController;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the scene in which the market is shown.
 */
public class MarketSceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Button backButton;
    @FXML
    private Button c0Button;
    @FXML
    private Button c1Button;
    @FXML
    private Button c2Button;
    @FXML
    private Button c3Button;
    @FXML
    private Button r0Button;
    @FXML
    private Button r1Button;
    @FXML
    private Button r2Button;
    @FXML
    private ImageView blue1;
    @FXML
    private ImageView blue2;
    @FXML
    private ImageView yellow1;
    @FXML
    private ImageView yellow2;
    @FXML
    private ImageView grey1;
    @FXML
    private ImageView grey2;
    @FXML
    private ImageView white1;
    @FXML
    private ImageView white2;
    @FXML
    private ImageView white3;
    @FXML
    private ImageView white4;
    @FXML
    private ImageView purple1;
    @FXML
    private ImageView purple2;
    @FXML
    private ImageView red1;

    private double[][] marbleMatrix = new double[13][2];
    private boolean initialized = false;
    private ActionParser requestBuilder = new ActionParser();

    @FXML
    public void initialize() {
        super.setTitle("Market");
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onBackBtnClick);
        c0Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onC0BtnClick);
        c1Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onC1BtnClick);
        c2Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onC2BtnClick);
        c3Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onC3BtnClick);
        r0Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onR0BtnClick);
        r1Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onR1BtnClick);
        r2Button.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onR2BtnClick);
        if (initialized) {

            purple1.setLayoutX(marbleMatrix[0][0]);
            purple1.setLayoutY(marbleMatrix[0][1]);
            purple2.setLayoutX(marbleMatrix[1][0]);
            purple2.setLayoutY(marbleMatrix[1][1]);
            grey1.setLayoutX(marbleMatrix[2][0]);
            grey1.setLayoutY(marbleMatrix[2][1]);
            grey2.setLayoutX(marbleMatrix[3][0]);
            grey2.setLayoutY(marbleMatrix[3][1]);
            yellow1.setLayoutX(marbleMatrix[4][0]);
            yellow1.setLayoutY(marbleMatrix[4][1]);
            yellow2.setLayoutX(marbleMatrix[5][0]);
            yellow2.setLayoutY(marbleMatrix[5][1]);
            blue1.setLayoutX(marbleMatrix[6][0]);
            blue1.setLayoutY(marbleMatrix[6][1]);
            blue2.setLayoutX(marbleMatrix[7][0]);
            blue2.setLayoutY(marbleMatrix[7][1]);
            red1.setLayoutX(marbleMatrix[8][0]);
            red1.setLayoutY(marbleMatrix[8][1]);
            white1.setLayoutX(marbleMatrix[9][0]);
            white1.setLayoutY(marbleMatrix[9][1]);
            white2.setLayoutX(marbleMatrix[10][0]);
            white2.setLayoutY(marbleMatrix[10][1]);
            white3.setLayoutX(marbleMatrix[11][0]);
            white3.setLayoutY(marbleMatrix[11][1]);
            white4.setLayoutX(marbleMatrix[12][0]);
            white4.setLayoutY(marbleMatrix[12][1]);

        } else {
            initialized = true;
        }
    }

    public MarketSceneController(){
        super("market");
    }

    /**
     * This method builds the market request to be sent
     * @param line indicates the row or column chosen by the player
     */
    private void sendPurchaseAction(String line) {
        try {
            JSONObject request = requestBuilder.action(Actions.MARKETPURCHASE)
                    .line(line)
                    .buildRequest();
            new Thread(() -> notify(new Task<>(client -> client.handleAction(request)))).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method updates the market matrix
     * @param market has all the information of the market
     */
    public void updateMarket(JSONObject market) {
        try {
            JSONArray marketStatus = (JSONArray) market.get("market status");
            // Aggiungo l'excess item alla lista di biglie del mercato
            marketStatus.add((String) market.get("new excess item"));

            int i = 0;
            int purple = 0;
            int grey = 0;
            int blue = 0;
            int white = 0;
            int yellow = 0;
            double x = 0;
            double y = 0;

            /*
                [0] position C0 R0 layoutX="230.0" layoutY="85.0"
                [1] position C1 R0 layoutX="265.0" layoutY="85.0"
                [2] position C2 R0 layoutX="300.0" layoutY="85.0"
                [3] position C3 R0 layoutX="335.0" layoutY="85.0"
                [4] position C0 R1 layoutX="230.0" layoutY="119.0"
                [5] position C1 R1 layoutX="265.0" layoutY="119.0"
                [6] position C2 R1 layoutX="300.0" layoutY="119.0"
                [7] position C3 R1 layoutX="335.0" layoutY="119.0"
                [8] position C0 R2 layoutX="230.0" layoutY="151.0"
                [9] position C1 R2 layoutX="265.0" layoutY="151.0"
                [10] position C2 R2 layoutX="300.0" layoutY="151.0"
                [11] position C3 R2 layoutX="335.0" layoutY="151.0"
                [12] position excess item layoutX="363.0" layoutY="46.0"
            */

            double[][] indexMatrix = new double[][] {
                    {230.0, 85.0}, {265.0, 85.0}, {300.0, 85.0}, {335.0, 85.0},
                    {230.0, 119.0}, {265.0, 119.0}, {300.0, 119.0}, {335.0, 119.0},
                    {230.0, 151.0}, {265.0, 151.0}, {300.0, 151.0}, {335.0, 151.0},
                    {363.0, 46.0}
            };

            while (i < 13) {

                x = indexMatrix[i][0];
                y = indexMatrix[i][1];

                switch (((String) marketStatus.get(i)).toUpperCase()) {
                    case "SERV":
                        if (purple == 0) {
                            marbleMatrix[0][0] = x;
                            marbleMatrix[0][1] = y;
                            purple = 1;
                        } else {
                            marbleMatrix[1][0] = x;
                            marbleMatrix[1][1] = y;
                        }
                        break;
                    case "STON":
                        if (grey == 0) {
                            marbleMatrix[2][0] = x;
                            marbleMatrix[2][1] = y;
                            grey = 1;
                        } else {
                            marbleMatrix[3][0] = x;
                            marbleMatrix[3][1] = y;
                        }
                        break;
                    case "COIN":
                        if (yellow == 0) {
                            marbleMatrix[4][0] = x;
                            marbleMatrix[4][1] = y;
                            yellow = 1;
                        } else {
                            marbleMatrix[5][0] = x;
                            marbleMatrix[5][1] = y;
                        }
                        break;
                    case "SHIE":
                        if (blue == 0) {
                            marbleMatrix[6][0] = x;
                            marbleMatrix[6][1] = y;
                            blue = 1;
                        } else {
                            marbleMatrix[7][0] = x;
                            marbleMatrix[7][1] = y;
                        }
                        break;
                    case "FAIT":
                        marbleMatrix[8][0] = x;
                        marbleMatrix[8][1] = y;
                        break;
                    case "BLAN":
                        if (white == 0) {
                            marbleMatrix[9][0] = x;
                            marbleMatrix[9][1] = y;
                            white = 1;
                        } else if (white == 1) {
                            marbleMatrix[10][0] = x;
                            marbleMatrix[10][1] = y;
                            white = 2;
                        } else if (white == 2) {
                            marbleMatrix[11][0] = x;
                            marbleMatrix[11][1] = y;
                            white = 3;
                        } else if (white == 3) {
                            marbleMatrix[12][0] = x;
                            marbleMatrix[12][1] = y;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("It does not match any type.");
                }
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the click on the back button.
     * @param event the mouse click event.
     */
    private void onBackBtnClick(Event event) {
        Platform.runLater(() -> {
            ((PersonalDashboardSceneController) getScene("personalDashboard")).turnOnButtons();
        });
        changeScene("personalDashboard");
    }

    /**
     * Handle the click on the C-0 button.
     * @param event the mouse click event.
     */
    private void onC0BtnClick(Event event) {
        sendPurchaseAction("C-0");
    }

    /**
     * Handle the click on the C-1 button.
     * @param event the mouse click event.
     */
    private void onC1BtnClick(Event event) {
        sendPurchaseAction("C-1");
    }

    /**
     * Handle the click on the C-2 button.
     * @param event the mouse click event.
     */
    private void onC2BtnClick(Event event) {
        sendPurchaseAction("C-2");
    }

    /**
     * Handle the click on the C-3 button.
     * @param event the mouse click event.
     */
    private void onC3BtnClick(Event event) {
        sendPurchaseAction("C-3");
    }

    /**
     * Handle the click on the R-0 button.
     * @param event the mouse click event.
     */
    private void onR0BtnClick(Event event) {
        sendPurchaseAction("R-0");
    }

    /**
     * Handle the click on the R-1 button.
     * @param event the mouse click event.
     */
    private void onR1BtnClick(Event event) {
        sendPurchaseAction("R-1");
    }

    /**
     * Handle the click on the R-2 button.
     * @param event the mouse click event.
     */
    private void onR2BtnClick(Event event) {
        sendPurchaseAction("R-2");
    }

}
