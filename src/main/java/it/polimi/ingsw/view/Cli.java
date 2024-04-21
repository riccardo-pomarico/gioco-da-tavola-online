package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.actions.ActionParser;
import it.polimi.ingsw.controller.actions.Actions;
import it.polimi.ingsw.controller.actions.ActivationAction;
import it.polimi.ingsw.model.cards.Color;
import it.polimi.ingsw.model.cards.DevelopmentCard;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.cards.Level;
import it.polimi.ingsw.model.containers.CardSet;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.task.*;
import it.polimi.ingsw.observer.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Cli extends ViewObservable implements View{
    private String userInput, username;
    private final CardSet genericCardSet = new CardSet();
    private String[][] market = new String[3][4];
    private String excessItem;
    private int numShelves = 3;
    private ActionParser requestBuilder = new ActionParser();
    private final Map<String, String> marbleMap = new HashMap<>();
    private final Map<String, Consumer<Cli>> depositHandlers = new HashMap<>();
    private Map<String, Boolean> playerDevCardDeck = new HashMap<>();
    private List<String> leadCardDeck = new ArrayList<>();
    private Task<View> lastTask;
    private List<String>[] slots = new List[3];
    private List<Resource>[] shelves = new List[3];
    private boolean afterActionAllowed = true;
    // These fields will be filled only in case of storage bonus, otherwise they won't be accessed
    private List<Resource> warehouseExtraShelf;
    private List<Resource> secondWarehouseExtraShelf;
    private Resource extraShelfResource;
    private Resource secondExtraShelfResource;
    private List<String>[][] cardsetMatrix = new List[Level.values().length][Color.values().length];


    @Override
    public void init() {
        System.out.println("Welcome to Masters of Renaissance!");
        genericCardSet.loadCards();
        marbleMap.put("SB", "Black Marble");
        marbleMap.put("SW", "White Marble");
        marbleMap.put("SR", "Faith Point");
        marbleMap.put("RB", "Shield");
        marbleMap.put("RY", "Coin");
        marbleMap.put("RG", "Stone");
        marbleMap.put("RP", "Servant");

        // Li includo nello stesso ciclo, tanto i due array hanno la stessa dimensione
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ArrayList<>();
            shelves[i] = new ArrayList<>();
        }
    }

    @Override
    public void printMessage(String type,String message) {
        System.out.println(message);
    }

    @Override
    public void turnMessage(String turnMessage) {
        System.out.println(turnMessage);
    }

    @Override
    public void notification(String type, String message) {
        System.out.println(message);
    }

    @Override
    public void numberOfPlayers() {
        System.out.println("It seems you are the first one arrived to this server, you get to decide how many players should participate to the game: ");

        int finalNumPlayers = getInt(np -> np>0 && np<5,"the number of players must be between 1 and 4");
        notify(new Task<>(client -> client.setNumberOfPlayers(finalNumPlayers)));
        if (finalNumPlayers != 1) {
            waitingForPlayer(finalNumPlayers-1);
        } else {
            System.out.println("Activating single player mode...");
            notify(new Task<>(ViewObserver::startGame));
        }
    }

    @Override
    public void playerOrder(String[] players) {
        String message = "";
        for(int i = 0; i < players.length; i++){
            switch (i) {
                case 0:
                    message = "- "  + players[i] + " is the first player.";
                    break;
                case 1:
                    message = "- "  + players[i] + " is the second player.\n  They will be given a Resource of their choice. " ;
                    break;
                case 2:
                    message = "- "  + players[i] + " is the third player.\n  They will be given a Resource of their choice and a Faith Point. " ;
                    break;
                case 3:
                    message = "- "  + players[i] + " is the fourth player.\n  They will be given two Resources of their choice and a Faith Point. " ;
                    break;
                default:
                    message = "";
                    break;
            }
            if(players[i].equals(this.username)){
                message = message.replace(players[i],"You");
                message = message.replace("They", "You");
                message = message.replace("is", "are");
                message = message.replace("their", "your");}
            System.out.println(message);
        }

    }

    @Override
    public void waitingForPlayer(int remaining) {
        System.out.println("waiting for other "+ remaining+" players...");
    }

    @Override
    public void startGame() {
        System.out.println("Time to start the game!");
        System.out.println("\n" +
                "   ▄▄▄▄███▄▄▄▄      ▄████████    ▄████████     ███        ▄████████    ▄████████    ▄████████       ▄██████▄     ▄████████         ▄████████    ▄████████ ███▄▄▄▄      ▄████████  ▄█     ▄████████    ▄████████    ▄████████ ███▄▄▄▄    ▄████████    ▄████████ \n" +
                " ▄██▀▀▀███▀▀▀██▄   ███    ███   ███    ███ ▀█████████▄   ███    ███   ███    ███   ███    ███      ███    ███   ███    ███        ███    ███   ███    ███ ███▀▀▀██▄   ███    ███ ███    ███    ███   ███    ███   ███    ███ ███▀▀▀██▄ ███    ███   ███    ███ \n" +
                " ███   ███   ███   ███    ███   ███    █▀     ▀███▀▀██   ███    █▀    ███    ███   ███    █▀       ███    ███   ███    █▀         ███    ███   ███    █▀  ███   ███   ███    ███ ███▌   ███    █▀    ███    █▀    ███    ███ ███   ███ ███    █▀    ███    █▀  \n" +
                " ███   ███   ███   ███    ███   ███            ███   ▀  ▄███▄▄▄      ▄███▄▄▄▄██▀   ███             ███    ███  ▄███▄▄▄           ▄███▄▄▄▄██▀  ▄███▄▄▄     ███   ███   ███    ███ ███▌   ███          ███          ███    ███ ███   ███ ███         ▄███▄▄▄     \n" +
                " ███   ███   ███ ▀███████████ ▀███████████     ███     ▀▀███▀▀▀     ▀▀███▀▀▀▀▀   ▀███████████      ███    ███ ▀▀███▀▀▀          ▀▀███▀▀▀▀▀   ▀▀███▀▀▀     ███   ███ ▀███████████ ███▌ ▀███████████ ▀███████████ ▀███████████ ███   ███ ███        ▀▀███▀▀▀     \n" +
                " ███   ███   ███   ███    ███          ███     ███       ███    █▄  ▀███████████          ███      ███    ███   ███             ▀███████████   ███    █▄  ███   ███   ███    ███ ███           ███          ███   ███    ███ ███   ███ ███    █▄    ███    █▄  \n" +
                " ███   ███   ███   ███    ███    ▄█    ███     ███       ███    ███   ███    ███    ▄█    ███      ███    ███   ███               ███    ███   ███    ███ ███   ███   ███    ███ ███     ▄█    ███    ▄█    ███   ███    ███ ███   ███ ███    ███   ███    ███ \n" +
                "  ▀█   ███   █▀    ███    █▀   ▄████████▀     ▄████▀     ██████████   ███    ███  ▄████████▀        ▀██████▀    ███               ███    ███   ██████████  ▀█   █▀    ███    █▀  █▀    ▄████████▀   ▄████████▀    ███    █▀   ▀█   █▀  ████████▀    ██████████ \n" +
                "                                                                      ███    ███                                                  ███    ███                                                                                                                   \n");
    }

    @Override
    public void askInfo() {
        System.out.println("Enter your nickname");
        this.username = getString(InputHandler::checkUsername,InputHandler.getUsernameError());

        System.out.println("Hi "+ username +", please enter the server address");

        String serverAddress = getString(InputHandler::checkIPAddress,InputHandler.getServerAddressError());
        System.out.println("Now enter the server port");
        String serverPort = Integer.toString(getInt(InputHandler::checkPort, InputHandler.getPortError()));
        notify(new Task<>(cc -> cc.playerInfo(new String[] {username, serverAddress, serverPort})));
    }

    @Override
    public void newUsername() {
        System.out.println("Sorry, "+this.username+" has already been taken by another player, please choose another one:");
        this.username = getString(InputHandler::checkUsername,InputHandler.getUsernameError());
        notify(new Task<>(client -> client.logIn(username)));
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void welcome(){
        System.out.println("Login successful!");
    }

    @Override
    public void showOtherPlayers(int numPlayers,String msg,String[] usernames){
        System.out.println(msg);
        for (String username : usernames){
            System.out.println(username);
        }
    }

    @Override
    public void reconnect(){
        System.out.println("your name matches one of a disconnected player. if it's you, would you like to reconnect? [Y/N]");
        userInput = getString(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"),"answer not recognised, would you like to reconnect? [Y/N]");
        if (userInput.equalsIgnoreCase("Y")){
            notify(new Task<>(ViewObserver::reconnectToServer));
        }else {
            System.out.println("then please change your username, as it matches another");
            this.username = getString(n -> n.length()>1 && n.length()<8,"the length of the username must be between 1 and 8");
            String username = userInput;
            notify(new Task<>(client -> client.logIn(username)));
        }

    }

    @Override
    public void disconnectedPlayer(String username) {
        System.out.println("connection to "+ username+ " lost ");
    }

    @Override
    public Task<View> getLastTask() {
        return this.lastTask;
    }

    @Override
    public void showNewPlayer(String username) {
        System.out.println(username + " joined!");
    }

    @Override
    public void chooseAction() {
        this.lastTask = new Task<>(View::chooseAction);
        System.out.println("It's your turn! Choose what action to perform on your turn:");
        System.out.println("[1] Take Resources from the Market");
        System.out.println("[2] Buy one Development Card");
        System.out.println("[3] Activate the Production");
        System.out.println("[4] Discard a Leader Card");
        int choice = getInt(i -> i > 0 && i < 5,"Incorrect answer: please select one of the three actions by dialing 1, 2 or 3");
        switch (choice) {
            case 1:
                openMarket();
                break;
            case 2:
                openCardSet();
                break;
            case 3:
                handleProduction();
                break;
            case 4:
                discardLeaderCard();
                break;
            default:
                new IllegalArgumentException("It does not match any type.").printStackTrace();
        }
    }

    @Override
    public void afterAction() {
        this.lastTask = new Task<>(View::afterAction);
        System.out.println("Now that you have executed your action, choose how you want to proceed.");
        System.out.println("[1] Move your resources in the warehouse");
        System.out.println("[2] Activate a leader card");
        System.out.println("[3] Finish your turn");
        int choice = getInt(i -> i >= 1 && i <= 3,"Incorrect answer: please select one of the three actions by dialing 1, 2 or 3");
        switch (choice) {
            case 1:
                moveResources();
                break;
            case 2:
                requestLeaderCard();
                break;
            case 3:
                notify(new Task<>(ViewObserver::changeOfTurn));
                break;
            default:
                break;
        }
    }


    private void discardLeaderCard(){
        System.out.println("Do you want to discard a Leader Card? [Y/N]");
        userInput = getString(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"),"incorrect input, Do you want to activate a Leader Card? [Y/N]").toUpperCase();
        if(userInput.equalsIgnoreCase("Y")){
            System.out.println("these are your leader cards: ");
            int j = 0;
            for(int i = 0; i<leadCardDeck.size();i++){
                j = i+1;
                System.out.println("["+ j +"] "+ leadCardDeck.get(i));
            }
            System.out.println("enter the number of the card you want to discard:");
            int cardNumber = getInt(s -> s > 0 && s <= leadCardDeck.size(), "please enter one of the numbers above");
            try {
                JSONObject request = requestBuilder.action(Actions.DISCARDLEADER).targetPlayer(this.username).leaderCard(leadCardDeck.get(cardNumber - 1)).buildRequest();
                notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
                leadCardDeck.remove(cardNumber -1);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }else{
            chooseAction();
        }
    }
    @Override
    public void requestLeaderCard() {
        System.out.println("Do you want to activate a Leader Card? [Y/N]");
        lastTask = new Task<>(View::requestLeaderCard);
        userInput = getString(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"),"incorrect input, Do you want to activate a Leader Card? [Y/N]").toUpperCase();
        switch (userInput) {
            case "Y":
                System.out.println("these are your Leader Cards: ");
                leadCardDeck.forEach(System.out::println);
                System.out.println("Choose which Leader Card to activate.");
                // Apri carte leader possedute dal giocatore
                userInput = getString(card -> leadCardDeck.stream().anyMatch(c -> c.equals(card)),"please enter the id of one of your cards");
                activateLeaderCard(userInput);
                notify(new Task<>(ViewObserver::changeOfTurn));
                break;
            case "N":
                // Prossimo giocatore, fine turno
                notify(new Task<>(ViewObserver::changeOfTurn));
                break;
            default:
                throw new IllegalArgumentException("It does not match any type.");
        }
    }

    @Override
    public void openMarket() {
        // Stampa la matrice del mercato nella condizione attuale
        System.out.println("this is the market: ");
        showMarket();
        System.out.println("Choose the line of marbles you want to purchase from the market.\nFormat: [C/R]-[line index - 1]");
        userInput = getString(Market::checkInputFormat, "This input doesn't correspond to the right format: please insert your choice once again!");
        JSONObject request = requestBuilder.action(Actions.MARKETPURCHASE)
                .targetPlayer(this.username)
                .line(userInput)
                .buildRequest();
        notify(new Task<>(client -> client.handleAction(request)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void showMarket() {
        System.out.println("");
        for (int i = 0; i< 3; i++){
            if(i<2){
                System.out.print("     ");
            }else{
                System.out.print(excessItem + " ");
            }
            for (int j = 0; j<4 ; j++){
                System.out.print(this.market[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

    @Override
    public void openCardSet() {
        DevelopmentCard dc;
        System.out.println("These are all available Development Cards in CardSet:\n");
        int k = 1;
        for(int i = 0; i < Level.values().length; i++){
            for (int j = 0; j < Color.values().length; j++){
                try {
                    dc = (DevelopmentCard) genericCardSet.findCard(this.cardsetMatrix[i][j].get(0));
                    System.out.println("Development Card #" + k);
                    System.out.println("ID = " + dc.generateId());
                    System.out.println("Level = " + dc.getLevel());
                    System.out.println("Color = " + dc.getColor());
                    System.out.println("Price = " + dc.getRequirements().toString());
                    System.out.print("Production input = ");
                    printRule(dc.getProductionRule().getInRule());
                    System.out.print("\nProduction output = ");
                    printRule(dc.getProductionRule().getOutRule());
                    System.out.println("\n---------------------------");
                    k++;
                } catch (IllegalArgumentException ignored) {}
            }
        }

        System.out.println("\nNow choose which development card to buy.\nPlease insert a number between 1 and 12");
        int input = getInt(s -> {
            try {
                int i = s/Level.values().length;
                int j = s%Level.values().length == 0 ? Level.values().length-1 : s%Level.values().length-1;
                DevelopmentCard chosenCard = (DevelopmentCard) this.genericCardSet.findCard(cardsetMatrix[i][j].get(0));
                return true;
            } catch (IllegalArgumentException | ClassCastException ex) {
                return false;
            }
        },"CardSet doesn't contain this card: please choose another one.");
        String chosenCard = cardsetMatrix[input/Level.values().length][input%Level.values().length == 0 ? Level.values().length-1 : input%Level.values().length-1].get(0);
        System.out.println("where do you want to withdraw your resources from? [1 = Strong Box, 2 = warehouse, 3 = auto (both)]");
        int targetDeposit = getInt(i -> i > 0 && i < 4,"please insert the correct value");
        System.out.println("Which slot do you want to store card? [1, 2, 3]");
        int slot = getInt(i -> i >= 1 && i <= 3, "Please insert a valid slot! [1, 2, 3]");
        purchaseDevelopmentCard(chosenCard, targetDeposit,slot-1);

    }

    private boolean feasibleBasicProduction() {
        int notEmptyShelves = 0;
        for (List<Resource> shelf : shelves) {
            if (!shelf.isEmpty()) {
                notEmptyShelves++;
            }
        }
        return notEmptyShelves >= 2;
    }

    @Override
    public void handleProduction() {
        CardSet fullCardSet = new CardSet();
        fullCardSet.loadCards();
        List<String> chosenProductions = new ArrayList<>();
        // Apri carte possedute dal giocatore
        if (playerDevCardDeck.isEmpty() || playerDevCardDeck.values().stream().allMatch(used -> used)) {
            // Se il deck del giocatore è vuoto, allora può attivare alpiù la produzione base.
            if (playerDevCardDeck.isEmpty())
                System.out.println("Your card deck is empty.");
            System.out.println("You can only activate basic production: are you sure you want to do it? [Y/N]");
            String response = getString(s -> s.equalsIgnoreCase("y") || s.equalsIgnoreCase("n"), "Insert a valid answer!");
            if (response.equalsIgnoreCase("n")) {
                lastTask.execute(this);
            } else {
                if (!feasibleBasicProduction()) {
                    System.out.println("You still cannot activate basic production, for you have less than 2 not empty shelves!");
                    lastTask.execute(this);
                } else {
                    handleBasicProduction();
                }
            }
        } else {
            // Caso della produzione non base, cioè attraverso una carta sviluppo
            // Chiedo comunque prima se vuole attivare una produzione base piuttosto che una non base
            System.out.println("You can choose between basic and Development Card production!");
            System.out.println("Which one do you choose? [basic/dc]");
            String answer = getString(response -> response.equalsIgnoreCase("basic") || response.equalsIgnoreCase("dc"), "Please select one of the two answers shown above!");
            if (answer.equalsIgnoreCase("basic")) {
                handleBasicProduction();
            } else {
                playerDevCardDeck.keySet().forEach(cardId -> {
                    if (!playerDevCardDeck.get(cardId)) {
                        System.out.println("ID = " + cardId);
                        if (cardId.charAt(0) == 'D') {
                            DevelopmentCard card = (DevelopmentCard) fullCardSet.findCard(cardId);
                            System.out.print("Production input = ");
                            printRule(card.getProductionRule().getInRule());
                            System.out.print("\nProduction output = ");
                            printRule(card.getProductionRule().getOutRule());
                            System.out.println("\n---------------------------");
                        }
                    }
                });
                do {
                    System.out.println("Choose which production to activate.");
                    userInput = getString(input -> playerDevCardDeck.containsKey(input), "This ID is not contained in your card deck: please select a correct one!");
                    chosenProductions.add(userInput);
                    System.out.println("Do you want to activate another production? [Y/N]");
                    answer = getString(s -> s.equalsIgnoreCase("y") || s.equalsIgnoreCase("n"), "Insert a correct answer.");
                } while (answer.equalsIgnoreCase("y"));
                activateProduction(chosenProductions);
            }
        }
    }

    @Override
    public void showCardDeck() {

    }

    private void printRule(ArrayList<Marble> marbleArrayList) {
        System.out.print("[");
        for (int i = 0; i < marbleArrayList.size(); i++) {
            System.out.print(marbleArrayList.get(i).getType());
            if (i != marbleArrayList.size()-1) {
                System.out.print(", ");
            }
        }
        System.out.print("]");
    }

    @Override
    public void chooseResourcesFromMarket(String chosenLine) {

    }

    @Override
    public void purchaseDevelopmentCard(String cardId, int targetDeposit, int slot) {
        JSONObject request = new JSONObject();
        try {
            request = requestBuilder
                    .action(Actions.DEVCARDPURCHASE)
                    .developmentCard(cardId)
                    .targetPlayer(username)
                    .targetDeposit(targetDeposit)
                    .slot(slot)
                    .buildRequest();
        }catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            System.out.println("Try again...");
            chooseAction();
        }
        JSONObject finalRequest = request;
        notify(new Task<>(c -> c.handleAction(finalRequest)));
    }

    @Override
    public void activateProduction(List<String> chosenProduction) {
        JSONObject request = requestBuilder
                .action(Actions.ACTIVATEPRODUCTION)
                .developmentCardsList(chosenProduction)
                .buildRequest();
        Task<ViewObserver> op1 = new Task<>(viewObserver -> viewObserver.handleAction(request));
        notify(op1);
    }

    private void handleBasicProduction() {
        try {
            System.out.println("Please select the first Resource you want to withdraw from your warehouse");
            String resource1 = getString(r -> {
                    try {
                        Arrays.stream(shelves).anyMatch(shelf -> shelf.contains(Resource.valueOf(r.toUpperCase())));
                        return true;
                    }catch (IllegalArgumentException ignored){
                        return false;
                    }
                }, "Your warehouse doesn't contain this resource at the moment: please select another one");
            System.out.println("Great! now select the second Resource you want to withdraw from your warehouse");
            String resource2 = getString(r -> {
                try {
                    Arrays.stream(shelves).anyMatch(shelf -> shelf.contains(Resource.valueOf(r.toUpperCase())));
                    return true;
                }catch (IllegalArgumentException ignored){
                    return false;
                }
            }, "Your warehouse doesn't contain this resource at the moment: please select another one");
            System.out.println("And now select the kind of resource you want back. [SHIELD, SERVANT, STONE, COIN]");
            String resource3 = getString(r -> r.equalsIgnoreCase("shield") || r.equalsIgnoreCase("servant") || r.equalsIgnoreCase("stone") || r.equalsIgnoreCase("coin"),
                    "Incorrect output: please insert a valid one. [SHIELD, SERVANT, STONE, COIN]");
            Resource[] resources = new Resource[]{Resource.valueOf(resource1.toUpperCase()), Resource.valueOf(resource2.toUpperCase()), Resource.valueOf(resource3.toUpperCase())};
            activateProduction(resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activateProduction(Resource[] chosenProductions) {
        try {
            JSONObject request = requestBuilder
                    .action(Actions.ACTIVATEBASIC)
                    .resources(chosenProductions)
                    .targetPlayer(this.username)
                    .buildRequest();
            notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activateLeaderCard(String cardId) {
        JSONObject request = requestBuilder.action(Actions.ACTIVATELEADER).leaderCard(cardId).buildRequest();
        Task<ViewObserver> op1 = new Task<>(client -> client.handleAction(request));
        notify(op1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updatePersonalDashboard(JSONObject newPDStatus) {
        try {
            // Prendiamo le informazioni del JSONObject riguardante la plancia
            System.out.println("Here is the status of your Personal Dashboard:\n");
            JSONObject PFT = (JSONObject) newPDStatus.get("Pope's Favor Track status");
            JSONObject warehouse = (JSONObject) newPDStatus.get("player's warehouse");
            JSONArray warehouseShelves = (JSONArray) warehouse.get("Warehouse's shelves");
            JSONArray devCards = (JSONArray) newPDStatus.get("player's Development Cards");
            JSONArray strongbox = (JSONArray) newPDStatus.get("player's strongbox");
            devCards.forEach(dc -> {
                if (!playerDevCardDeck.containsKey((String) dc)) {
                    playerDevCardDeck.put((String) dc, false);
                }
            });
            System.out.println("Warehouse's shelves:");
            int position = 0;
            for (Object shelf : warehouseShelves) {
                JSONArray trueShelf = (JSONArray) shelf;
                for (String s : (Iterable<String>) trueShelf) {
                    long occupancies = trueShelf.stream().filter(r -> !r.equals("0")).count();
                    if (!s.equals("0")) {
                        if (shelves[position].stream().filter(Objects::nonNull).count() < occupancies) {
                            shelves[position].add(Resource.valueOf(s));
                        }
                        System.out.print(s + " ");
                    } else {
                        System.out.print("empty ");
                    }
                }
                position++;
                System.out.println();
            }
            // Gestione extra storage
            if (warehouseExtraShelf != null) {
                System.out.println("extra shelves:");
                position = 0;
                JSONObject extraWarehouse = (JSONObject) newPDStatus.get("extra shelves");
                JSONArray extraShelves = (JSONArray) extraWarehouse.get("Warehouse's shelves");
                for (Object shelf : extraShelves) {
                    JSONArray trueShelf = (JSONArray) shelf;
                    for (String s : (Iterable<String>) trueShelf) {
                        long occupancies = trueShelf.stream().filter(r -> !r.equals("0")).count();
                        if (!s.equals("0")) {
                            if (position == 0){
                                if (warehouseExtraShelf.stream().filter(Objects::nonNull).count() < occupancies) {
                                    warehouseExtraShelf.add(Resource.valueOf(s));
                                }
                            } else if (position == 1) {
                                if (secondWarehouseExtraShelf != null) {
                                    if (secondWarehouseExtraShelf.stream().filter(Objects::nonNull).count() < occupancies) {
                                        secondWarehouseExtraShelf.add(Resource.valueOf(s));
                                    }
                                }
                            }
                            System.out.print(s + " ");
                        } else {
                            System.out.print("empty ");
                        }
                    }
                    position++;
                    System.out.println();
                }
            }
            System.out.println("\nStrongbox's content:");
            if (strongbox.isEmpty()) {
                System.out.println("empty");
            } else {
                strongbox.forEach(r -> System.out.print(r + " "));
                System.out.println();
            }
            System.out.println("\nYou now possess the following cards:");
            if (!playerDevCardDeck.isEmpty()) {
                for (int i = 0; i < slots.length; i++) {
                    if (!slots[i].isEmpty()) {
                        System.out.print("Slot " + (i+1) + ": ");
                        slots[i].forEach(card -> {
                            System.out.print(card + " ");
                            if (playerDevCardDeck.get(card)) {
                                System.out.print("(ACTIVATED) ");
                            }
                        });
                    } else {
                        System.out.print("Slot " + (i+1) + ": empty");
                    }
                    System.out.println();
                }
            } else {
                System.out.println("Card deck is currently empty...");
            }
            System.out.println("\nYour position in Faith Track = " + PFT.get("Player's track position") + "\n");
        }catch(Exception e) {
            e.printStackTrace();
            lastTask.execute(this);
        }
    }

    @Override
    public void handleNewLeaderCard(List<String> cards) {
        System.out.println();
        System.out.println("you have received four leader cards: ");
        LeaderCard leaderCard;
        for (String card: cards){
            System.out.println("-------------------");
            leaderCard = (LeaderCard) genericCardSet.findCard(card);
            System.out.println("id: "+leaderCard.generateId());
            System.out.println("bonus: "+leaderCard.getBonusType().toString());
            String[] id = leaderCard.generateId().split("-");
            String resource = id[2];
            switch (leaderCard.getBonusType()) {
                case STORAGE:
                    System.out.println("Extra storage is reserved to " + ActivationAction.fromStringToResource(resource));
                    System.out.print("Requirements: " + leaderCard.getRequirements().toString());
                    break;
                case DISCOUNT:
                    System.out.println("When purchasing a Development card, a discount of one " + ActivationAction.fromStringToResource(resource) + " will be applied to the price of the card");
                    break;
                case BLANKMARBLES:
                    System.out.println("When purchasing from market, all white marbles will be considered as " + ActivationAction.fromStringToResource(resource));
                    break;
                case PRODUCTIONRULE:
                    resource = id[1];
                    System.out.println("One extra production rule will be added: one " + ActivationAction.fromStringToResource(resource) + " can produce one free-choice resource and one faith point");
                    break;
            }
            if (!leaderCard.getRequirementsColor().isEmpty()){
                System.out.print("color requirements: ");
                leaderCard.getRequirementsColor().forEach(color -> System.out.print(color + " "));
            }
            if (leaderCard.getRequirementsInt() != 0){
                System.out.print("\ncard level requirements: one card of level " + leaderCard.getRequirementsInt());
            }
            System.out.println();
        }
        System.out.println("\nYou can choose two of those cards.\nWhich card do you want to discard first? [ 1, 2, 3, 4 ]");
        int chosenCard1 = getInt(c -> c>0 && c<5, "please enter a value between 1 and 4") -1;
        System.out.print("Great! now choose the second card to discard ");
        System.out.print("[ ");
        for(int i = 1; i< 5; i++)
            if(i != chosenCard1+1){
                System.out.print(i);
                if( i != 4)
                    System.out.print(", ");
            }
        System.out.print(" ]\n");

        int chosenCard2 = getInt(c -> c>0 && c<5 && c != chosenCard1+1, "please enter a value between 1 and 4 and different from "+(chosenCard1+1)+", since you already chose it") -1;

        JSONObject request1 = requestBuilder.action(Actions.DISCARDLEADER).targetPlayer(this.username).leaderCard(cards.get(chosenCard1)).buildRequest();
        JSONObject request2 = requestBuilder.action(Actions.DISCARDLEADER).targetPlayer(this.username).leaderCard(cards.get(chosenCard2)).buildRequest();

        notify(new Task<>(viewObserver -> viewObserver.handleAction(request1)));
        notify(new Task<>(viewObserver -> viewObserver.handleAction(request2)));

        cards.remove(chosenCard1);
        if(chosenCard2>chosenCard1){
            chosenCard2--;
        }
        cards.remove(chosenCard2);
        leadCardDeck.addAll(cards);


    }

    @Override
    public void updateCardSet(JSONObject cardSet) {
        try {
            if( cardSet.get("chosen Development card") != null) {
                this.genericCardSet.takeCard((String) cardSet.get("chosen Development card"));
            }
            JSONArray cards = (JSONArray) cardSet.get("remaining Development cards");
            int i =0, j = 0;
            String id;
            for( Level level : Level.values()){
                for( Color color : Color.values() ){
                    cardsetMatrix[i][j] = new ArrayList<>();
                    for(Object o : cards ){
                        try {
                            id = o.toString();
                            DevelopmentCard card = (DevelopmentCard) genericCardSet.findCard(id);
                            if(card.getLevel() == level.ordinal()+1 && card.getColor() == color){
                                this.cardsetMatrix[i][j].add(card.generateId());
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    j++;
                }
                j=0;
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addToSlot(String id, int slot) {
        if (!playerDevCardDeck.containsKey(id)) {
            playerDevCardDeck.put(id, false);
        }
        slots[slot].add(id);
        System.out.println("You updated slot " + (slot+1));
        System.out.print("Slot " + (slot+1) + ": ");
        slots[slot].forEach(s -> System.out.print(s + " "));
        System.out.println();
    }

    @Override
    public void updateMarket(JSONObject market) {
        JSONArray marketStatus = (JSONArray) market.get("market status");
        this.market = new String[][] {{(String) marketStatus.get(0), (String) marketStatus.get(1), (String) marketStatus.get(2), (String) marketStatus.get(3)},
                {(String) marketStatus.get(4), (String) marketStatus.get(5), (String) marketStatus.get(6), (String) marketStatus.get(7)},
                {(String) marketStatus.get(8), (String) marketStatus.get(9), (String) marketStatus.get(10), (String) marketStatus.get(11)}};
        this.excessItem = (String) market.get("new excess item");
    }

    @Override
    public void finishMarketHandling() {
        showMarket();
        notify(new Task<>(ViewObserver::actionFinished));
    }

    @Override
    public void discard(String resource, boolean blackMarble) {
        System.out.println("invoked time for disposal");
        try {
            JSONObject request = requestBuilder
                    .action(Actions.DISCARDRESOURCE)
                    .targetPlayer(this.username)
                    .marble(resource)
                    .blackMarble(blackMarble)
                    .buildRequest();
            notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateServerWarehouse(List<Resource>[] warehouse) {
        try {
            JSONObject request = requestBuilder
                    .action(Actions.UPDATEWAREHOUSE)
                    .targetPlayer(this.username)
                    .warehouse(warehouse)
                    .buildRequest();
            notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
        } catch (Exception e) {
            e.printStackTrace();
            lastTask.execute(this);
        }
    }

    @Override
    public void notifyExtraStorage(String resource) {
        if (warehouseExtraShelf == null) {
            warehouseExtraShelf = new ArrayList<>();
            System.out.println("You have activated an extra shelf");
        } else if (secondWarehouseExtraShelf == null) {
            secondWarehouseExtraShelf = new ArrayList<>();
            System.out.println("You have activated another extra shelf");
        }
        if (extraShelfResource == null){
            extraShelfResource = Resource.valueOf(resource.toUpperCase());
        } else if (secondExtraShelfResource == null) {
            secondExtraShelfResource = Resource.valueOf(resource.toUpperCase());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void takeTwoDevCards(JSONObject cardIds) {
        JSONArray takenCards = (JSONArray) cardIds.get("taken cards");
        for (Object id : takenCards) {
            if (genericCardSet.getCardList().contains(genericCardSet.findCard(id.toString()))){
                genericCardSet.takeCard(id.toString());
            }
        }
        System.out.println("Lorenzo has taken these two cards:");
        takenCards.forEach(System.out::println);
        System.out.println("Remaining number of development cards in cardset = " + genericCardSet.getDevCardList().size());
    }

    @Override
    public void cardActivated(JSONObject cardActivated) {
        String id = (String) cardActivated.get("card");
        playerDevCardDeck.replace(id, false, true);
        System.out.println("You activated the production of card " + id + ": it cannot be used once again, hence won't be shown any further among the possible productions.");
    }

    @Override
    public void vaticanReportMessage(String vaticanReportMessage, boolean isOnTheRightVaticanZone, int popesCardPoints) {
        System.out.println(vaticanReportMessage);
    }

    @Override
    public void rankingPositionMessage(String rankingPosition) {
        System.out.println(rankingPosition);
    }

    @Override
    public void lorenzoTurnActionNotification(String actionType, String message) {
        System.out.println(message);
    }

    @Override
    public void updateLorenzoPFT(int increment) {
        if (increment == 1) {
            System.out.println("Lorenzo has gained one position");
        }
    }

    /**
     * This method asks the player the potential extra shelf they want to store a resource in.
     * @param marble the resource to store.
     * @return the destination to store the resource in.
     */
    private int askForExtraStorage(String marble) {
        // Prima di chiedere in quale scaffale inserire, controlliamo ancora ci sono scaffali extra: essi saranno trattati per primi
        if (extraShelfResource != null && ActivationAction.fromStringToResource(marble) == extraShelfResource) {
            System.out.println("Would you like to store " + extraShelfResource.toString() + " in the first extra shelf? [Y/N]");
            userInput = getString(s -> s.equalsIgnoreCase("y") || s.equalsIgnoreCase("n"), "Invalid answer! Select [Y/N]");
            if (userInput.equalsIgnoreCase("y")) {
                // Deposita nel primo scaffale (destinazione 3 nel metodo addToShelf di Warehouse)
                return 3;
            } else {
                return getShelf();
            }
        } else {
            if (secondWarehouseExtraShelf != null && Resource.valueOf(marble.toUpperCase()) == secondExtraShelfResource) {
                System.out.println("Would you like to store " + marble + " in one of the extra shelves?");
                userInput = getString(s -> s.equalsIgnoreCase("y") || s.equalsIgnoreCase("n"), "Invalid answer! Select [Y/N]");
                if (userInput.equalsIgnoreCase("y")) {
                    // Deposita in uno dei due scaffali
                    System.out.println("Which extra shelf do you want to deposit " + marble + " in?");
                    int extraShelfChoice = getInt(i -> i == 1 || i == 2, "unrecognized extra shelf index: the answer must be either 1 or 2");
                    // Deposita nello scaffale scelto
                    return extraShelfChoice+2;
                } else {
                    return getShelf();
                }
            } else {
                return getShelf();
            }
        }
    }

    /**
     * This method asks the player which shelf they want to choose to store the resource.
     * @return the chosen shelf.
     */
    private int getShelf(){
        System.out.println("which shelf would you like to deposit the resource in?");
        return getInt(s -> s > 0 && s <= numShelves,"please enter a value between 1 and "+numShelves) -1;
    }

    private void depositBlackMarble(int targetDeposit){
        int shelf = 0;
        System.out.println("you have received a black marble! choose a resource to deposit [Shield, Coin, Stone, Servant]");
        String resource = new Marble(Resource.valueOf(getString(res -> {
            try {
                res = new Marble(Resource.valueOf(res.toUpperCase())).generateId();
                return res.charAt(0) == 'R';
            }catch (IllegalArgumentException ignored){
                return false;
            }
        },"please enter the correct value").toUpperCase())).generateId();
        if(targetDeposit==2){
            shelf = getShelf();
        }
        System.out.println("Do you want to discard this resource? [Y/N]");
        String response = getString(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"), "please enter a valid answer [Y/N]");
        if (response.equalsIgnoreCase("y")) {
            // discard(marbleMap.get(resource));
            System.out.println("You have decided or are forced to get rid of a resource of yours. :(");
            try {
                discard(resource, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            deposit(resource, targetDeposit, shelf, true);
        }
    }

    private void depositFaithPoint(){
        System.out.println("you have received a Faith Point, this will increase your position in the Pope's favor track by 1");
        deposit("SR",1,1,false);
    }

    private void depositResource(String marble, int targetDeposit){
        int shelf = 0;
        System.out.println("you have received a "+ new Marble(marble).getType() + "! ");
        if (targetDeposit == 2) {
            if (warehouseExtraShelf == null){
                shelf = getShelf();
            } else {
                shelf = askForExtraStorage(marble);
            }
        }
        deposit(marble, targetDeposit, shelf, false);
    }

    private void depositResource(String marble){
        System.out.println("you have received a "+ new Marble(marble).getType() + "! ");
        System.out.println("where would you like to deposit it? [1 = Strong Box, 2 = Warehouse]");
        int targetDeposit = getInt(t -> t == 1 || t == 2, "please enter either 1 for the Strong Box, 2 for the warehouse");
        int shelf = 0;
        if(targetDeposit==2){
            shelf = getShelf();
        }
        deposit(marble,targetDeposit,shelf,false);
    }

    public void depositMarble(String marble){
        this.lastTask = new Task<>(view -> view.depositMarble(marble));
        if(marble.equals("SB")){
            depositBlackMarble(1);
        }else if(marble.equals("SR")){
            depositFaithPoint();
        }else{
            System.out.println("Do you want to discard this resource? [Y/N]");
            String response = getString(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"), "please enter a valid answer [Y/N]");
            if (response.equalsIgnoreCase("y")) {
                discard(marble,false);
            }else {
                depositResource(marble);
            }
        }
    }
    public void depositMarble(String marble, int targetDeposit){
        this.lastTask = new Task<>(view -> view.depositMarble(marble,targetDeposit));
        if(marble.equals("SB")){
            depositBlackMarble(targetDeposit);
        }else if(marble.equals("SR")){
            depositFaithPoint();
        }else{
            System.out.println("Do you want to discard this resource? [Y/N]");
            String response = getString(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("N"), "please enter a valid answer [Y/N]");
            if (response.equalsIgnoreCase("y")) {
                discard(marble,false);
            }else {
                depositResource(marble, targetDeposit);
            }
        }
    }

    /**
     * Creates the action of depositing the marble to send to ActionParser.
     * @param marble the marble to store.
     * @param targetDeposit the chosen destination of the marble.
     * @param shelf the chosen warehouse's shelf (0 if targetDeposit is the strongbox).
     */
    private void deposit(String marble, int targetDeposit, int shelf, boolean blackMarble){
        try {
            JSONObject request = requestBuilder
                    .action(Actions.DEPOSIT)
                    .targetPlayer(username)
                    .marble(marble)
                    .targetDeposit(targetDeposit)
                    .shelf(shelf)
                    .blackMarble(blackMarble)
                    .buildRequest();
            notify(new Task<>(viewObserver -> viewObserver.handleAction(request)));
        }catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            lastTask.execute(this);
        }

    }

    /**
     * Method handling the action of moving resources within the warehouse.
     * It also checks whether any possible combination of moves does not break the game rules.
     */
    private void moveResources() {
        int fullShelves = 0;
        for (List<Resource> shelf : shelves) {
            if (!shelf.isEmpty()) {
                fullShelves++;
            }
        }
        String resource;
        switch (fullShelves) {
            case 0:
                System.out.println("Your warehouse is empty: what phantom resources do you want to move?? v( ‘.’ )v\n");
                break;
            case 1:
                System.out.println("You have " + (3-fullShelves) + " empty shelves.");
                List<Resource> onlyFullShelf = Arrays.stream(shelves).collect(Collectors.toList()).stream().filter(shelf -> !shelf.isEmpty()).findFirst().get();
                Resource onlyAvailableResource = onlyFullShelf.get(onlyFullShelf.size()-1);
                if (onlyFullShelf.size() == 1) {
                    // Un solo scaffale del magazzino non è vuoto: si può scegliere dove allocare le risorse tra gli altri scaffali liberi
                    System.out.println("Your warehouse only contains " + onlyAvailableResource.toString());
                    System.out.println("Choose where you want to move it, possible choices are:");
                    List<Resource> firstChoice = Arrays.stream(shelves).collect(Collectors.toList()).stream().filter(List::isEmpty).findFirst().orElse(null);
                    List<Resource> secondChoice = Arrays.stream(shelves).collect(Collectors.toList()).stream().filter(List::isEmpty).reduce((first, second) -> second).orElse(null);
                    assert firstChoice != null && secondChoice != null;
                    int firstChoiceIndex = Arrays.asList(shelves).indexOf(firstChoice);
                    int secondChoiceIndex = Arrays.asList(shelves).lastIndexOf(secondChoice);
                    System.out.println("1) Shelf #" + firstChoiceIndex);
                    System.out.println("2) Shelf #" + secondChoiceIndex);
                    int response = getInt(i -> i == firstChoiceIndex || i == secondChoiceIndex, "Your choice doesn't correspond with one of the available shelves: please insert your choice again!");
                    shelves[response].add(onlyAvailableResource);
                    onlyFullShelf.remove(onlyAvailableResource);
                } else {
                    // Se l'unico scaffale completamente pieno è quello da 3, sicuramente non si potrà spostare le risorse
                    if (onlyFullShelf.size() == 3) {
                        System.out.println("You cannot move any resources from this shelf, for you would break the game rules!");
                    } else {
                        System.out.println("Given that you have two " + onlyFullShelf.get(0).toString() + ", you have to move both of them.");
                        System.out.println("Are you sure to do this? [Y/N]");
                        String answer = getString(s -> s.equalsIgnoreCase("y") || s.equalsIgnoreCase("n"), "Incorrect input: insert again!");
                        if (answer.equalsIgnoreCase("n")) {
                            lastTask.execute(this);
                            break;
                        } else {
                            // Se c'è un solo scaffale pieno e questo contiene due risorse, per non violare le regole bisogna spostarle entrambe in un altro scaffale (che sarà di capienza massima 2 o 3 a seconda del caso)
                            List<Resource> onlyChoice = Arrays.stream(shelves).collect(Collectors.toList()).stream().filter(List::isEmpty).reduce((first, second) -> second).orElse(null);
                            int onlyChoiceIndex = Arrays.asList(shelves).lastIndexOf(onlyChoice);
                            System.out.println("You can move " + onlyFullShelf.get(onlyFullShelf.size()-1).toString() + " only to shelf #" + onlyChoiceIndex);
                            System.out.println("This will be done right now...");
                            shelves[onlyChoiceIndex].add(onlyFullShelf.get(onlyFullShelf.size()-1));
                            shelves[onlyChoiceIndex].add(onlyFullShelf.get(onlyFullShelf.size()-1));
                            onlyFullShelf.remove(onlyAvailableResource);
                            onlyFullShelf.remove(onlyAvailableResource);
                        }
                    }
                }
                printWarehouse();
                updateServerWarehouse(this.shelves);
                break;
            case 2:
                System.out.println("You have only " + (3-fullShelves) + " empty shelf: your resource will be moved there.");
                System.out.println("Choose the resource you want to move");
                resource = getString(r -> Arrays.stream(shelves).anyMatch(shelf -> shelf.contains(Resource.valueOf(r.toUpperCase()))), "Your warehouse doesn't contain this resource at the moment: please select another one");
                List<Resource> beginShelf = Arrays.stream(shelves).filter(shelf -> shelf.contains(Resource.valueOf(resource.toUpperCase()))).findFirst().orElse(null);
                assert beginShelf != null;
                if (beginShelf.stream().filter(r -> r.equals(Resource.valueOf(resource.toUpperCase()))).count() != 1) {
                    if (beginShelf.stream().filter(r -> r.equals(Resource.valueOf(resource.toUpperCase()))).count() == 3){
                        System.out.println("By moving " + resource + ", you will definitely break a rule, hence you are not allowed this move!");
                    } else {
                        // Nel caso in cui sia 2, bisogna assicurarsi che entrambe le biglie siano spostate
                        System.out.println("The shelf you chose contains 2 " + resource + "s, so they would both have to be moved in order not to break the rules...");
                        List<Resource> freeShelf = Arrays.stream(shelves).filter(List::isEmpty).findFirst().orElse(null);
                        if (freeShelf != null) {
                            if (Arrays.asList(this.shelves).indexOf(freeShelf) != 0){
                                freeShelf.add(Resource.valueOf(resource.toUpperCase()));
                                freeShelf.add(Resource.valueOf(resource.toUpperCase()));
                                beginShelf.remove(Resource.valueOf(resource.toUpperCase()));
                                beginShelf.remove(Resource.valueOf(resource.toUpperCase()));
                            } else {
                                System.out.println("Unfortunately this is still not possible according to the rules: shelf #" + Arrays.asList(this.shelves).indexOf(freeShelf) + " has only one free space! :(");
                            }
                        }
                    }
                } else {
                    List<Resource> freeShelf = Arrays.stream(shelves).filter(List::isEmpty).findFirst().orElse(null);
                    if (freeShelf != null) {
                        freeShelf.add(Resource.valueOf(resource.toUpperCase()));
                        beginShelf.remove(Resource.valueOf(resource.toUpperCase()));
                    }
                }
                printWarehouse();
                updateServerWarehouse(this.shelves);
                break;
            case 3:
                List<Integer> sizes = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    sizes.add(shelves[i].size());
                }
                Set<Integer> duplicates = sizes.stream().filter(n -> sizes.stream().filter(x -> x.equals(n)).count() > 1).collect(Collectors.toSet());
                // Se il set non presenta duplicati, allora il magazzino è completamente pieno.
                if (duplicates.size() == 0) {
                    System.out.println("All shelves are full: you would surely violate the rules if you moved resources between shelves!");
                    break;
                } else {
                    int dimension = 0;
                    for (Integer duplicate : duplicates) {
                        dimension = duplicate;
                    }
                    int finalDimension = dimension;
                    List<List<Resource>> equallyFullShelves = Arrays.stream(shelves).filter(sh -> sh.size() == finalDimension).collect(Collectors.toList());
                    // Due scaffali egualmente pieni
                    if (equallyFullShelves.size() == 2) {
                        // Due scaffali egualmente pieni con una risorsa a testa
                        if (equallyFullShelves.stream().allMatch(sh -> sh.size() == 1 || sh.size() == 2)) {
                            swap(equallyFullShelves.get(0), equallyFullShelves.get(1));
                        }
                    } else if (equallyFullShelves.size() == 3) {
                        // Tutti gli scaffali sono egualmente pieni (massima dimensione consentita: 1)
                        System.out.println("Which shelves have to be involved in this swap? [1, 2, 3]");
                        int response1 = getInt(i -> i >= 1 && i <= 3, "invalid answer");
                        int response2 = getInt(i -> i >= 1 && i <= 3, "invalid answer");
                        swap(shelves[response1-1], shelves[response2-1]);
                    }
                    printWarehouse();
                    updateServerWarehouse(this.shelves);
                    break;
                }
            default:
                break;
        }
        lastTask.execute(this);
    }


    /**
     * Private method to print the current state of the warehouse when it is being updated whilst moving resources within its shelves.
     */
    private void printWarehouse() {
        for (int i = 0; i < shelves.length; i++) {
            if (shelves[i].isEmpty()) {
                for (int j = 0; j < i+1; j++) {
                    System.out.print("empty ");
                }
            } else {
                shelves[i].forEach(r -> System.out.print(r + " "));
            }
            System.out.println();
        }
    }

    /**
     * This method swaps the content of two shelves.
     * @param shelf1 the first shelf.
     * @param shelf2 the second shelf.
     */
    private void swap(List<Resource> shelf1, List<Resource> shelf2) {
        if (shelf1.size() != shelf2.size()) {
            throw new IllegalArgumentException("The shelves don't have the same size");
        }
        if (shelf1.size() == 1) {
            Resource temp = shelf1.remove(0);
            Resource temp2 = shelf2.remove(0);
            shelf1.add(temp2);
            shelf2.add(temp);
        } else if (shelf1.size() == 2) {
            List<Resource> temp = new ArrayList<>(shelf1);
            List<Resource> temp2 = new ArrayList<>(shelf2);
            for (int i = 1; i >= 0; i--) {
                shelf1.remove(i);
                shelf2.remove(i);
            }
            shelf1.addAll(temp2);
            shelf2.addAll(temp);
        }
    }

    // InputHandler methods
    /**
     * InputHandler method to read the input from terminal if an integer is expected.
     * @param condition the condition allowing the input to be accepted.
     * @param message the message to print in case of error (when the condition is not verified).
     * @return the integer input from terminal.
     */
    private int getInt(Predicate<Integer> condition, String message){
        return InputHandler.getInt(condition, s -> System.out.println(message),this);
    }

    /**
     * InputHandler method to read the input from terminal if a string is expected.
     * @param condition the condition allowing the input to be accepted.
     * @param message the message to print in case of error (when the condition is not verified).
     * @return the string input from terminal.
     */
    private String getString(Predicate<String> condition, String message){
        return InputHandler.getString(condition, s -> System.out.println(message),this);
    }

}