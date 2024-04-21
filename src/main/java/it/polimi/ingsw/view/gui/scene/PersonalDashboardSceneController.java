package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.model.cards.Rule;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.task.Task;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements the scene in which the personal dashboard is shown.
 */
public class PersonalDashboardSceneController extends GenericScene {

    @FXML
    private AnchorPane rootpane;
    @FXML
    private Button production1, production2, production3, basicProduction, marketButton, buyDCButton, activateProd, bonus1, bonus2;
    @FXML
    private ImageView faithMarker, firstShelf1, secondShelf1, secondShelf2, thirdShelf1, thirdShelf2, thirdShelf3;
    @FXML
    private ImageView devCard1, devCard2, devCard3, blackCross, extraShelf1, extraShelf2, extraShelf3, extraShelf4;
    @FXML
    private ImageView popesFavorTilesBack2, popesFavorTilesBack3, popesFavorTilesBack4, popesFavorTilesFront2, popesFavorTilesFront3, popesFavorTilesFront4, lorenzoToken;
    @FXML
    private Label numCoin, numStone, numServant, numShield;

    private Button confirmBtn;
    private Button confirmBonus1Btn;
    private Button confirmBonus2Btn;
    private Marble b1In;
    private Marble b2In;
    private TextField res1In;
    private TextField res2In;
    private TextField resOut;
    private TextField b1Out;
    private TextField b2Out;
    private Stage dialog;
    private Map<String, Boolean> playerDevCardDeck = new HashMap<>();
    private List<Resource>[] shelves = new List[5];
    private List<String>[] slots = new List[3];
    private double[][] playerPosition = new double[25][2];
    private JSONArray strongbox;
    private JSONObject requestBasicProd;
    private JSONObject requestBonus1;
    private JSONObject requestBonus2;
    private ActionParser requestBuilder = new ActionParser();
    private boolean initialized = false;
    private int faithMarkerPos, blackFaithMarkerPos = 0;
    private boolean singlePlayer = false;
    private boolean basicProd = false;
    private boolean bonusLeader1 = false;
    private boolean bonusLeader2 = false;
    private boolean popesFavorTiles2 = false;
    private boolean popesFavorTiles3 = false;
    private boolean popesFavorTiles4 = false;
    private boolean prod = false;
    private List<String> devCards = new ArrayList<>();
    private Rule card1 = new Rule();
    private Rule card2 = new Rule();
    private boolean bonus1On = false;
    private boolean bonus2On = false;
    private Image lorenzoImage;

    @FXML
    public void initialize() {
        super.setTitle("Personal Dashboard");

        production1.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onProduction1BtnClick);
        production2.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onProduction2BtnClick);
        production3.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onProduction3BtnClick);
        basicProduction.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onBasicProductionBtnClick);
        activateProd.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onActivateProdBtnClick);
        marketButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMarketBtnClick);
        buyDCButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onBuyDCBtnClick);
        bonus1.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onBonus1BtnClick);
        bonus2.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onBonus2BtnClick);

        lorenzoToken.setVisible(false);
        int position = 0;

        for (List<Resource> shelf : shelves) {
            if (shelf != null) {
                if (!shelf.isEmpty()) {
                    setWarehouse();
                }
            }
        }

        if (strongbox != null) {
            if (!strongbox.isEmpty()) {
                setStrongBox();
            }
        }

        if (playerDevCardDeck != null) {
            if (playerDevCardDeck.keySet().size() != 0) {
                setDevCard();
            }
        }

        if (popesFavorTiles2) {
            String pathname;
            pathname = "/images/2_popesfavortiles_front.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            popesFavorTilesFront2.setFitWidth(49);
            popesFavorTilesFront2.setFitHeight(50.2);
            popesFavorTilesFront2.setLayoutX(143);
            popesFavorTilesFront2.setLayoutY(48);
            popesFavorTilesBack2.setLayoutX(-143);
            popesFavorTilesBack2.setLayoutY(48);
        }

        if (popesFavorTiles3) {
            String pathname;
            pathname = "/images/3_popesfavortiles_front.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            popesFavorTilesFront3.setFitWidth(54);
            popesFavorTilesFront3.setFitHeight(52);
            popesFavorTilesFront3.setLayoutX(290);
            popesFavorTilesFront3.setLayoutY(21);
            popesFavorTilesBack3.setLayoutX(-143);
            popesFavorTilesBack3.setLayoutY(48);
        }

        if (popesFavorTiles4) {
            String pathname;
            pathname = "/images/4_popesfavortiles_front.png";
            /* File file = new File(pathname);
            Image image = new Image(file.toURI().toString()); */
            Image image = new Image(pathname);
            popesFavorTilesFront4.setFitWidth(54);
            popesFavorTilesFront4.setFitHeight(51.3);
            popesFavorTilesFront4.setLayoutX(470);
            popesFavorTilesFront4.setLayoutY(48);
            popesFavorTilesBack4.setLayoutX(-143);
            popesFavorTilesBack4.setLayoutY(48);
        }

        if (singlePlayer) {
            blackCross.setLayoutX(playerPosition[blackFaithMarkerPos][0]);
            blackCross.setLayoutY(playerPosition[blackFaithMarkerPos][1]);
            lorenzoToken.setImage(lorenzoImage);
            lorenzoToken.setVisible(true);
        }

        if (!initialized) {
            playerPosition[0][0] = 6;
            playerPosition[0][1] = 65;

            playerPosition[1][0] = 40;
            playerPosition[1][1] = 65;

            playerPosition[2][0] = 69;
            playerPosition[2][1] = 65;

            playerPosition[3][0] = 70;
            playerPosition[3][1] = 37;

            playerPosition[4][0] = 67;
            playerPosition[4][1] = 7;

            playerPosition[5][0] = 98;
            playerPosition[5][1] = 7;

            playerPosition[6][0] = 129;
            playerPosition[6][1] = 7;

            playerPosition[7][0] = 158;
            playerPosition[7][1] = 7;

            playerPosition[8][0] = 183;
            playerPosition[8][1] = 7;

            playerPosition[9][0] = 220;
            playerPosition[9][1] = 7;

            playerPosition[10][0] = 214;
            playerPosition[10][1] = 35;

            playerPosition[11][0] = 214;
            playerPosition[11][1] = 65;

            playerPosition[12][0] = 245;
            playerPosition[12][1] = 65;

            playerPosition[13][0] = 276;
            playerPosition[13][1] = 65;

            playerPosition[14][0] = 308;
            playerPosition[14][1] = 65;

            playerPosition[15][0] = 334;
            playerPosition[15][1] = 65;

            playerPosition[16][0] = 365;
            playerPosition[16][1] = 65;

            playerPosition[17][0] = 365;
            playerPosition[17][1] = 30;

            playerPosition[18][0] = 365;
            playerPosition[18][1] = 7;

            playerPosition[19][0] = 393;
            playerPosition[19][1] = 7;

            playerPosition[20][0] = 425;
            playerPosition[20][1] = 7;

            playerPosition[21][0] = 454;
            playerPosition[21][1] = 7;

            playerPosition[22][0] = 485;
            playerPosition[22][1] = 7;

            playerPosition[23][0] = 513;
            playerPosition[23][1] = 7;

            playerPosition[24][0] = 544;
            playerPosition[24][1] = 7;

            initialized = true;
        } else {
            if (faithMarkerPos >= 0) {
                faithMarker.setLayoutX(playerPosition[faithMarkerPos][0]);
                faithMarker.setLayoutY(playerPosition[faithMarkerPos][1]);
            }
        }
    }

    public void singlePlayerMode() {
        singlePlayer = true;
    }

    public void addToSlot(String id, int slot) {
        if (!playerDevCardDeck.containsKey(id)) {
            playerDevCardDeck.put(id, false);
        }
        slots[slot].add(id);
    }

    public void setPopesFavorTilesBack2() {
        popesFavorTiles2 = true;
    }

    public void setPopesFavorTilesBack3()  {
        popesFavorTiles3 = true;
    }

    public void setPopesFavorTilesBack4() {
        popesFavorTiles4 = true;
    }
    /**
     * Method used to show on the personal dashboard the Lorenzo Action Token in Single Player Mode
     * @param type String conaining the type of the action [LORENZO/DEVCARDWITHDRAW]_[Color/PFTpoints]
     */
    public void lorenzoAction(String type){
        // System.out.println(type);
        /* File file = new File("/images/lorenzo_tokens/" + type.toLowerCase() + ".png");
        Image image = new Image(file.toURI().toString()); */
        Image image = new Image("/images/lorenzo_tokens/" + type.toLowerCase() + ".png");
        lorenzoToken.setImage(image);
        lorenzoImage = image;
    }
    public void updateLorenzoPFT(int increment){
        blackFaithMarkerPos += increment;
    }
    /**
     * This method updates the resources on the shelves of the warehouse
     */
    private void setWarehouse() {
        int x = -80;
        int y = 300;

        firstShelf1.setLayoutX(x);
        firstShelf1.setLayoutY(y);
        secondShelf1.setLayoutX(x);
        secondShelf1.setLayoutY(y);
        secondShelf2.setLayoutX(x);
        secondShelf2.setLayoutY(y);
        thirdShelf1.setLayoutX(x);
        thirdShelf1.setLayoutY(y);
        thirdShelf2.setLayoutX(x);
        thirdShelf2.setLayoutY(y);
        thirdShelf3.setLayoutX(x);
        thirdShelf3.setLayoutY(y);


        for (int i = 0; i < shelves.length; i++) {
            if (!shelves[i].isEmpty()) {
                for (int j = 0; j < shelves[i].size(); j++) {
                    Resource res = shelves[i].get(j);
                    String s = res.toString();
                    String pathname;
                    pathname = "/images/resource_" + s.toLowerCase() + ".png";
                    /* File file = new File(pathname);
                    Image image = new Image(file.toURI().toString()); */
                    Image image = new Image(pathname);
                    ImageView resource;

                    int count = i + j;

                    if (i == 2) {
                        count++;
                    }

                    if (i == 3) {
                        count += 3;
                    }

                    if (i == 4){
                        count += 5;
                    }

                    switch (count) {
                        case 0:
                            resource = firstShelf1;
                            firstShelf1.setImage(image);
                            firstShelf1.setLayoutX(49);
                            firstShelf1.setLayoutY(158);
                            break;
                        case 1:
                            resource = secondShelf1;
                            secondShelf1.setImage(image);
                            secondShelf1.setLayoutX(31);
                            secondShelf1.setLayoutY(199);
                            break;
                        case 2:
                            resource = secondShelf2;
                            secondShelf2.setImage(image);
                            secondShelf2.setLayoutX(74);
                            secondShelf2.setLayoutY(199);
                            break;
                        case 3:
                            resource = thirdShelf1;
                            thirdShelf1.setImage(image);
                            thirdShelf1.setLayoutX(14);
                            thirdShelf1.setLayoutY(235);
                            break;
                        case 4:
                            resource = thirdShelf2;
                            thirdShelf2.setImage(image);
                            thirdShelf2.setLayoutX(48);
                            thirdShelf2.setLayoutY(235);
                            break;
                        case 5:
                            resource = thirdShelf3;
                            thirdShelf3.setImage(image);
                            thirdShelf3.setLayoutX(86);
                            thirdShelf3.setLayoutY(235);
                            break;
                        case 6:
                            resource = extraShelf1;
                            extraShelf1.setImage(image);
                            extraShelf1.setLayoutX(6);
                            extraShelf1.setLayoutY(266);
                            break;
                        case 7:
                            resource = extraShelf2;
                            extraShelf2.setImage(image);
                            extraShelf2.setLayoutX(37);
                            extraShelf2.setLayoutY(266);
                            break;
                        case 8:
                            resource = extraShelf3;
                            extraShelf3.setImage(image);
                            extraShelf3.setLayoutX(65);
                            extraShelf3.setLayoutY(266);
                            break;
                        case 9:
                            resource = extraShelf4;
                            extraShelf4.setImage(image);
                            extraShelf4.setLayoutX(90);
                            extraShelf4.setLayoutY(266);
                            break;
                        default:
                            resource = new ImageView();
                            resource.setImage(image);
                            break;
                    }

                    switch (s.toUpperCase()) {
                        case "COIN":
                            resource.setFitWidth(50);
                            resource.setFitHeight(41.25);
                            break;
                        case "SERVANT":
                            resource.setFitWidth(50);
                            resource.setFitHeight(41.75);
                            break;
                        case "SHIELD":
                            resource.setFitWidth(47);
                            resource.setFitHeight(47);
                            break;
                        case "STONE":
                            resource.setFitWidth(50);
                            resource.setFitHeight(41.26);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * This method updates the number of resources in the strongbox
     */
    private void setStrongBox() {
        AtomicInteger servant = new AtomicInteger();
        AtomicInteger coin = new AtomicInteger();
        AtomicInteger stone = new AtomicInteger();
        AtomicInteger shield = new AtomicInteger();

        strongbox.forEach(r -> {
            switch(r.toString()) {
                case "COIN":
                    coin.getAndIncrement(); break;
                case "SERVANT":
                    servant.getAndIncrement(); break;
                case "SHIELD":
                    shield.getAndIncrement(); break;
                case "STONE":
                    stone.getAndIncrement(); break;
                default:
                    break;
            }
        });

        numCoin.setText("" + coin);
        numServant.setText("" + servant);
        numShield.setText("" + shield);
        numStone.setText("" + stone);
    }


    /**
     * This method updates the player's development cards in the personal dashboard
     */
    private void setDevCard() {

        for (int i = 0; i < slots.length; i++) {

            if (slots[i].size() != 0) {

                for (int j = 0; j < slots[i].size(); j++) {

                    if (i == 0) {

                        String pathname;
                        pathname = "/images/cards/front/" + slots[i].get(j) + ".png";
                        /* File file = new File(pathname);
                        Image image = new Image(file.toURI().toString()); */
                        Image image = new Image(pathname);
                        devCard1.setImage(image);
                        devCard1.setLayoutX(224.0);
                        devCard1.setLayoutY(174.0);
                        devCard1.setFitHeight(181.0);
                        devCard1.setFitWidth(120.0);

                    } else if (i == 1) {

                        String pathname;
                        pathname = "/images/cards/front/" + slots[i].get(j) + ".png";
                        /* File file = new File(pathname);
                        Image image = new Image(file.toURI().toString()); */
                        Image image = new Image(pathname);
                        devCard2.setImage(image);
                        devCard2.setLayoutX(344.0);
                        devCard2.setLayoutY(174.0);
                        devCard2.setFitHeight(181.0);
                        devCard2.setFitWidth(120.0);

                    } else if (i == 2) {

                        String pathname;
                        pathname = "/images/cards/front/" + slots[i].get(j) + ".png";
                        /* File file = new File(pathname);
                        Image image = new Image(file.toURI().toString()); */
                        Image image = new Image(pathname);
                        devCard3.setImage(image);
                        devCard3.setLayoutX(464.0);
                        devCard3.setLayoutY(174.0);
                        devCard3.setFitHeight(181.0);
                        devCard3.setFitWidth(120.0);

                    }
                }
            }
        }
    }

    public PersonalDashboardSceneController(){
        super("personalDashboard");
        // lorenzoImage = new Image(new File("src/main/resources/images/lorenzo_tokens/lorenzo_back.png").toURI().toString());
        lorenzoImage = new Image("/images/lorenzo_tokens/lorenzo_back.png");
    }

    public void setBonus1(Rule card1) {
        this.card1 = card1;
        //bonus1.setLayoutX(130);
        //bonus1.setLayoutY(155);
        bonus1.setDisable(false);
        bonus1On = true;
    }

    public void setBonus2(Rule card2) {
        this.card2 = card2;
        //bonus1.setLayoutX(130);
        //bonus1.setLayoutY(187);
        bonus2.setDisable(false);
        bonus2On = true;
    }

    public void turnOffButtons() {
        Platform.runLater(() -> {
            production1.setDisable(true);
            production2.setDisable(true);
            production3.setDisable(true);
            basicProduction.setDisable(true);
            marketButton.setDisable(true);
            buyDCButton.setDisable(true);
            activateProd.setDisable(true);
            bonus1.setDisable(true);
            bonus2.setDisable(true);
        });
    }

    public void turnOnButtons() {
        Platform.runLater(() -> {
            production1.setDisable(false);
            production2.setDisable(false);
            production3.setDisable(false);
            basicProduction.setDisable(false);
            marketButton.setDisable(false);
            buyDCButton.setDisable(false);
            activateProd.setDisable(true);
            if (bonus1On) {
                bonus1.setDisable(false);
            } else {
                bonus1.setDisable(true);
            }
            if (bonus2On) {
                bonus2.setDisable(false);
            } else {
                bonus2.setDisable(true);
            }
        });
    }

    /**
     * This method forwards requests to activate productions.
     */
    public void sendActivationProductions(List<String> devCards, JSONObject requestBasicProd, JSONObject requestBonus1, JSONObject requestBonus2) {
        // Se è stata richiesta l'attivazione di una o più produzioni
        if (prod) {
            JSONObject request = requestBuilder
                    .action(Actions.ACTIVATEPRODUCTION)
                    .developmentCardsList(devCards)
                    .buildRequest();
            prod = false;
            new Thread(() -> notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
        }

        // Se è stata richiesta l'attivazione della produzione base
        if (basicProd) {
            try {
                basicProd = false;
                new Thread(() -> notify(new Task<>(client -> client.handleAction(requestBasicProd)))).start();
            } catch (Exception e) {
            e.printStackTrace();
            }
        }

        if (bonusLeader1) {
            try {
                bonusLeader1 = false;
                JSONObject request = requestBuilder
                        .action(Actions.DEPOSIT)
                        .marble("SR")
                        .targetDeposit(1)
                        .shelf(1)
                        .blackMarble(false)
                        .buildRequest();
                new Thread(() -> notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
                new Thread(() -> notify(new Task<>(client -> client.handleAction(requestBonus1)))).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (bonusLeader2) {
            try {
                bonusLeader2 = false;
                JSONObject request = requestBuilder
                        .action(Actions.DEPOSIT)
                        .marble("SR")
                        .targetDeposit(1)
                        .shelf(1)
                        .blackMarble(false)
                        .buildRequest();
                new Thread(() -> notify(new Task<>(viewObserver -> viewObserver.handleAction(request)))).start();
                new Thread(() -> notify(new Task<>(client -> client.handleAction(requestBonus2)))).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This method updates the personal dashboard
     * @param PFT indicates the position of the faith marker
     * @param warehouse indicates the resources in the warehouse
     * @param warehouseShelves indicates the shelves occupied
     * @param devCards indicates the player's development cards
     * @param strongbox indicates the resources in the strongbox
     * @param slots saves the occupied slots
     */
    @SuppressWarnings("unchecked")
    public void updatePersonalDashboard(JSONObject PFT, JSONObject warehouse, JSONArray warehouseShelves, JSONArray devCards, JSONArray strongbox, JSONObject extraShelvesJson, List<String>[] slots) {
        this.slots = slots;

        for (int i = 0; i < shelves.length; i++) {
            shelves[i] = new ArrayList<>();
        }

        // Sistemo le carte sviluppo nei vari slot
        devCards.forEach(dc -> {
            if (!playerDevCardDeck.containsKey((String) dc)) {
                playerDevCardDeck.put((String) dc, false);
            }
        });

        // Sistemo le risorse nel magazzino
        int position = 0;

        for (Object shelf : warehouseShelves) {

            JSONArray trueShelf = (JSONArray) shelf;

                for (String s : (Iterable<String>) trueShelf) {

                    long occupancies = trueShelf.stream().filter(r -> !r.equals("0")).count();

                    if (!s.equals("0")) {

                        if (shelves[position].stream().filter(Objects::nonNull).count() < occupancies) {

                            shelves[position].add(Resource.valueOf(s));

                        }

                    }

                }

            position++;
        }

        if(extraShelvesJson != null) {
            JSONArray extraShelvesJsonArray = (JSONArray) extraShelvesJson.get("Warehouse's shelves");
            for (Object shelf : extraShelvesJsonArray) {
                JSONArray trueShelf = (JSONArray) shelf;
                // In caso di extra shelf
                for (String s : (Iterable<String>) trueShelf) {

                    long occupancies = trueShelf.stream().filter(r -> !r.equals("0")).count();

                    if (!s.equals("0")) {

                        if (shelves[position].stream().filter(Objects::nonNull).count() < occupancies) {
                            shelves[position].add(Resource.valueOf(s));
                        }

                    }

                }
            }
        }

        // Salvo gli elementi della strongbox
        this.strongbox = strongbox;

        // Sistemo la posizione del faith marker
        if (PFT.get("Player's track position").toString() != null) {
            faithMarkerPos = Integer.parseInt(PFT.get("Player's track position").toString());
        }

    }

    /**
     * This method is triggered if a player wants to activate the production of the first slot
     * @param event the mouse click event.
     */
    private void onProduction1BtnClick(Event event) {
        try {
            devCards.add(slots[0].get(0));
            production1.setDisable(true);
            activateProd.setDisable(false);
            prod = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is triggered if a player wants to activate the production of the second slot
     * @param event the mouse click event.
     */
    private void onProduction2BtnClick(Event event) {
        try {
            devCards.add(slots[1].get(0));
            production2.setDisable(true);
            activateProd.setDisable(false);
            prod = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is triggered if a player wants to activate the production of the third slot
     * @param event the mouse click event.
     */
    private void onProduction3BtnClick(Event event) {
        try {
            devCards.add(slots[2].get(0));
            production3.setDisable(true);
            activateProd.setDisable(false);
            prod = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is triggered if a player wants to activate the basic production
     * @param event the mouse click event.
     */
    private void onBasicProductionBtnClick(Event event) {
        dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);

        dialogVbox.getChildren().add(new Text("Please select two kinds of resources you want to withdraw from your warehouse"));
        res1In = new TextField();
        dialogVbox.getChildren().add(res1In);
        res2In = new TextField();
        dialogVbox.getChildren().add(res2In);

        dialogVbox.getChildren().add(new Text("Select the kind of resource you want back. [SHIELD, SERVANT, STONE, COIN]"));
        resOut = new TextField();
        dialogVbox.getChildren().add(resOut);

        confirmBtn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBtn);
        Scene dialogScene = new Scene(dialogVbox, 500, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBtnClick);
    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBtnClick(Event event) {

        Resource resource1 = Resource.valueOf(res1In.getText().toUpperCase());
        Resource resource2 = Resource.valueOf(res2In.getText().toUpperCase());
        Resource resource3 = Resource.valueOf(resOut.getText().toUpperCase());

        Resource[] resources = new Resource[] {resource1, resource2, resource3};

        requestBasicProd = requestBuilder
                .action(Actions.ACTIVATEBASIC)
                .resources(resources)
                .buildRequest();

        basicProd = true;
        basicProduction.setDisable(true);
        activateProd.setDisable(false);

        dialog.close();
    }

    /**
     * This method is activated if a player has chosen all the productions to activate
     * @param event the mouse click event.
     */
    private void onActivateProdBtnClick(Event event) {
        sendActivationProductions(devCards, requestBasicProd, requestBonus1, requestBonus2);
    }

    /**
     * This method is triggered if a player wants to go to the market
     * @param event the mouse click event.
     */
    private void onMarketBtnClick(Event event) {
        changeScene("market");
    }

    /**
     * This method is triggered if a player wants to buy a development card
     * @param event the mouse click event.
     */
    private void onBuyDCBtnClick(Event event) {
        changeScene("buyDC");
    }

    /**
     * This method is activated if the player has an active leader card which has a production as a bonus
     * @param event the mouse click event.
     */
    private void onBonus1BtnClick(Event event) {
        b1In = card1.getInRule().get(0);

        dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);

        dialogVbox.getChildren().add(new Text("This production activated by the leader card withdraws a " + b1In.toString().toLowerCase() + " resource from your warehouse."));
        dialogVbox.getChildren().add(new Text("You will earn a faith point!"));
        dialogVbox.getChildren().add(new Text("Select the kind of resource you want back. [SHIELD, SERVANT, STONE, COIN]"));
        b1Out = new TextField();
        dialogVbox.getChildren().add(b1Out);

        confirmBonus1Btn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBonus1Btn);
        Scene dialogScene = new Scene(dialogVbox, 500, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBonus1Btn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBonus1BtnClick);
    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBonus1BtnClick(Event event) {

        Resource resource1 = Resource.valueOf(b1In.getType());
        Resource resource3 = Resource.valueOf(b1Out.getText().toUpperCase());

        Resource[] resources = new Resource[] {resource1, null, resource3};

        requestBonus1 = requestBuilder
                .action(Actions.ACTIVATELEADERPRODUCTION)
                .resources(resources)
                .buildRequest();

        bonusLeader1 = true;
        bonus1.setDisable(true);
        activateProd.setDisable(false);

        dialog.close();
    }

    /**
     * This method is activated if the player has an active leader card which has a production as a bonus
     * @param event the mouse click event.
     */
    private void onBonus2BtnClick(Event event) {
        b2In = card2.getInRule().get(0);

        dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);

        dialogVbox.getChildren().add(new Text("This production activated by the leader card withdraws a " + b1In.toString().toLowerCase() + " resource from your warehouse."));
        dialogVbox.getChildren().add(new Text("You will earn a faith point!"));
        dialogVbox.getChildren().add(new Text("Select the kind of resource you want back. [SHIELD, SERVANT, STONE, COIN]"));
        b2Out = new TextField();
        dialogVbox.getChildren().add(b2Out);

        confirmBonus1Btn = new Button("CONFIRM");
        dialogVbox.getChildren().add(confirmBonus1Btn);
        Scene dialogScene = new Scene(dialogVbox, 500, 300);
        dialog.setScene(dialogScene);
        dialog.show();

        confirmBonus2Btn.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onConfirmBonus2BtnClick);
    }

    /**
     * Handle the click on the confirm button.
     * @param event the mouse click event.
     */
    private void onConfirmBonus2BtnClick(Event event) {

        Resource resource1 = Resource.valueOf(b2In.getType());
        Resource resource3 = Resource.valueOf(b2Out.getText().toUpperCase());

        Resource[] resources = new Resource[] {resource1, null, resource3};

        requestBonus2 = requestBuilder
                .action(Actions.ACTIVATELEADERPRODUCTION)
                .resources(resources)
                .buildRequest();

        bonusLeader2 = true;
        bonus2.setDisable(true);
        activateProd.setDisable(false);

        dialog.close();
    }

    public List<Resource>[] getWarehouse(){
        return shelves;
    }
}
