package it.polimi.ingsw.model.containers;

import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import it.polimi.ingsw.observer.ModelObservable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class CardSet extends ModelObservable implements Cloneable, Serializable {
    private final List<Card> cardList = new ArrayList<>();
    private final List<DevelopmentCard> devCardList = new ArrayList<>();

    @Override
    public List<Card> clone() {
        return new ArrayList<>(cardList);
    }

    /**
     * returns the requested card
     * @param id unique id of the card
     * @return card requested
     * @throws IllegalArgumentException if the id does not match any card in the deck
     */
    public Card findCard(String id) throws IllegalArgumentException{
        Optional<Card> card = cardList.stream().filter(c -> c.generateId().equals(id)).findFirst();
        if (card.isPresent()){
            return card.get();
        }else{
            throw new IllegalArgumentException("no card id matches ["+id+"]");

        }
    }

    /**
     * returns the requested card and removes it from the deck
     * @param id unique id of the card
     * @return card requested
     */
    public Card takeCard(String id){
        Card returnCard = findCard(id);
        cardList.remove(returnCard);
        if (CardSet.isDevelopmentCard(id)) {
            devCardList.remove((DevelopmentCard) returnCard);
            notify(createJSONObject(returnCard));
        }
        return returnCard;
    }

    public List<Card> getCardList() {
        for (Card c : cardList) {
            if (c.generateId().charAt(0) == 'D' && !devCardList.contains(c)) {
                devCardList.add((DevelopmentCard) c);
            }
        }
        return clone();
    }

    public List<DevelopmentCard> getDevCardList() { return new ArrayList<>(devCardList); }
    public List<LeaderCard> getLeadCardList() {
    List<LeaderCard> returnList = new ArrayList<>();
        cardList.stream().filter(card -> CardSet.isLeaderCard(card.generateId())).collect(Collectors.toList()).forEach(card -> {
            returnList.add((LeaderCard) card);
        });
     return returnList;
    }

    /**
     * @param card card to add
     * @throws IllegalArgumentException thrown if the card is already in the deck
     */
    public void addCard(Card card) throws IllegalArgumentException{
        for (Card value : cardList) {
            if (value.getId().equals(card.getId())) {
                throw new IllegalArgumentException("Card already in the deck");
            }
        }
        cardList.add(card);
        if (card.generateId().charAt(0) == 'D') {
            devCardList.add((DevelopmentCard) card);
        }
    }

    /**
     * This method removes a card from the card list and return that list without the card given as parameter.
     * @param card the card to remove.
     * @return the list without @param card.
     */
    public CardSet getCardListWithoutCard(Card card) {
        boolean isRemovedFromCardList = cardList.remove(card);
        boolean isRemovedFromDevCardList = false;
        if (isDevelopmentCard(card.getId())) {
            isRemovedFromDevCardList = devCardList.remove(card);
        }
        notify(createJSONObject(null));
        return isRemovedFromCardList ? this : null;
    }

    /**
     * Static method to determine whether a card is a leader card based on its ID.
     * @param id the card's ID.
     * @return whether the card is actually a leader card or not.
     */
    public static boolean isLeaderCard(String id){
        try {
            return id.charAt(0) == 'L';
        }catch (StringIndexOutOfBoundsException ignored){
            return false;
        }
    }

    /**
     * Static method to determine whether a card is a development card based on its ID.
     * @param id the card's ID.
     * @return whether the card is actually a development card or not.
     */
    public static boolean isDevelopmentCard(String id){
        try {
            return id.charAt(0) == 'D';
        }catch (StringIndexOutOfBoundsException ignored){
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * This method loads all cards from Cards.json file
     * @throws IllegalArgumentException if it reads a wrong piece of information
     */
    public void loadCards(){
        JSONParser jsonParser = new JSONParser();
        try {
            // Leggi il file JSON
            InputStream inputStream = getClass().getResourceAsStream("/config/cards.json");
            // Object obj = jsonParser.parse(reader);
            Object obj = jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            JSONObject readObject = (JSONObject) obj;
            JSONArray cardsList = (JSONArray) readObject.get("cards");
            cardsList.forEach(card -> parseCardObject((JSONObject) card));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        Collections.shuffle(cardList);
        notify(createJSONObject(null));
    }

    /**
     * Private method to analyse each JSONObject representing a card in Cards.json file.
     * @param card one of the cards in Cards.json file.
     */
    private void parseCardObject(JSONObject card) {
        String cardId = (String) card.get("id");
        if (isDevelopmentCard(cardId)) {
            // Caso Carta Sviluppo
            int levelInteger = Integer.parseInt((String) card.get("level"));
            Level level;
            switch (levelInteger) {
                case 1:
                    level = Level.LEVEL1;
                    break;
                case 2:
                    level = Level.LEVEL2;
                    break;
                case 3:
                    level = Level.LEVEL3;
                    break;
                default:
                    throw new IllegalArgumentException("No admissible card level");
            }
            String colorString = (String) card.get("color");
            Color color;
            switch (colorString.toUpperCase()) {
                case "YELLOW":
                    color = Color.YELLOW;
                    break;
                case "BLUE":
                    color = Color.BLUE;
                    break;
                case "GREEN":
                    color = Color.GREEN;
                    break;
                case "PURPLE":
                    color = Color.PURPLE;
                    break;
                default:
                    throw new IllegalArgumentException("No admissible card color");
            }
            int victoryPoints = Integer.parseInt((String) card.get("victoryPoints"));
            List<Marble> input = new ArrayList<>();
            List<Marble> output = new ArrayList<>();
            String ruleInput = (String) card.get("productionIn");
            fillList(input, ruleInput);
            String ruleOutput = (String) card.get("productionOut");
            fillList(output, ruleOutput);
            Rule rule = new Rule(input, output);
            Card newCard = new DevelopmentCard(level, color, rule, victoryPoints);
            List<Resource> requirements = new ArrayList<>();
            String requirementsInput = (String) card.get("requirements");
            fillResourceList(requirements, requirementsInput);
            newCard.setRequirements(requirements);
            addCard(newCard);
        } else if (isLeaderCard(cardId)) {
            // Caso Carta Leader
            int leaderVictoryPoints = Integer.parseInt((String) card.get("victoryPoints"));
            String bonusTypeString = (String) card.get("bonusType");
            BonusType bonusType = null;
            if (!bonusTypeString.equals("0")) {
                switch (bonusTypeString.toUpperCase()) {
                    case "DISCOUNT":
                        bonusType = BonusType.DISCOUNT;
                        break;
                    case "STORAGE":
                        bonusType = BonusType.STORAGE;
                        break;
                    case "BLANKMARBLES":
                        bonusType = BonusType.BLANKMARBLES;
                        break;
                    case "PRODUCTIONRULE":
                        bonusType = BonusType.PRODUCTIONRULE;
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized bonus type");
                }
            }
            int requirementsInt = Integer.parseInt((String) card.get("requirementsInt"));
            List<Color> colorList = new ArrayList<>();
            String colorInput = (String) card.get("requirementsColor");
            if (!colorInput.equals("0")) {
                fillColorList(colorList, colorInput);
            }
            List<Marble> bonusInList = new ArrayList<>();
            String bonusIn = (String) card.get("bonusIn");
            fillList(bonusInList, bonusIn);
            String bonusOut = (String) card.get("bonusOut");
            Rule bonusRule;
            if (!bonusOut.equals("0")) {
                List<Marble> bonusOutList = new ArrayList<>();
                fillList(bonusOutList, bonusOut);
                bonusRule = new Rule(bonusInList, bonusOutList);
            } else {
                bonusRule = new Rule(bonusInList.get(0));
            }
            LeaderCard newLeadCard = new LeaderCard(bonusRule, bonusType, colorList, requirementsInt, leaderVictoryPoints);
            String leaderRequirementsString = (String) card.get("requirements");
            if (!leaderRequirementsString.equals("0")) {
                List<Resource> leaderRequirements = new ArrayList<>();
                fillResourceList(leaderRequirements, leaderRequirementsString);
                newLeadCard.setRequirements(leaderRequirements);
            } else {
                newLeadCard.setRequirements(null);
            }
            addCard(newLeadCard);
        } else {
            throw new IllegalArgumentException("Unrecognized card type");
        }
    }

    /**
     * Private method to fill the input/output of production rules of cards.
     * This also includes bonus production rules.
     * @param marbleInput the list of marbles to fill.
     * @param stringInput the "productionIn"/"productionOut" field in Cards.json file.
     */
    private void fillList(List<Marble> marbleInput, String stringInput) {
        String[] marblesIn = stringInput.split("-");
        for (String marbleIn : marblesIn) {
            int times = Integer.parseInt(String.valueOf(marbleIn.charAt(0)));
            for (int i = 0; i < times; i++) {
                switch (marbleIn.substring(1).toUpperCase()) {
                    case "STONE":
                        marbleInput.add(new Marble(Resource.STONE));
                        break;
                    case "SERVANT":
                        marbleInput.add(new Marble(Resource.SERVANT));
                        break;
                    case "SHIELD":
                        marbleInput.add(new Marble(Resource.SHIELD));
                        break;
                    case "COIN":
                        marbleInput.add(new Marble(Resource.COIN));
                        break;
                    case "FAITHPOINT":
                        marbleInput.add(new Marble(SpecialType.FAITHPOINT));
                        break;
                    case "BLANK":
                        marbleInput.add(new Marble(SpecialType.BLANK));
                        break;
                    case "BLACK":
                        marbleInput.add(new Marble(SpecialType.BLACK));
                        break;
                    default: throw new IllegalArgumentException("Type not recognized");
                }
            }
        }
    }

    /**
     * Private method to fill the requirements information from Cards.json file.
     * @param resourceInput the list of sources a player is expected to possess in order to purchase a development card or activate a leader one.
     * @param stringInput the "requirements" field in Cards.json file.
     */
    private void fillResourceList(List<Resource> resourceInput, String stringInput) {
        String[] resourcesIn = stringInput.split("-");
        for (String resourceIn : resourcesIn) {
            int times = Integer.parseInt(String.valueOf(resourceIn.charAt(0)));
            for (int i = 0; i < times; i++) {
                switch (resourceIn.substring(1).toUpperCase()) {
                    case "STONE":
                        resourceInput.add(Resource.STONE);
                        break;
                    case "SERVANT":
                        resourceInput.add(Resource.SERVANT);
                        break;
                    case "SHIELD":
                        resourceInput.add(Resource.SHIELD);
                        break;
                    case "COIN":
                        resourceInput.add(Resource.COIN);
                        break;
                    default: throw new IllegalArgumentException("Type not recognized");
                }
            }
        }
    }

    /**
     * Private method to fill a list of color of development cards a player is expected to have as requirements in order to activate a specific leader card.
     * @param colorList the list of color of development cards as requirements to activate a leader card.
     * @param stringInput the "requirementsColor" field in Cards.json file (only present for leader cards!).
     */
    private void fillColorList(List<Color> colorList, String stringInput) {
        String[] colorsIn = stringInput.split("-");
        for (String colorIn : colorsIn) {
            int times = Integer.parseInt(String.valueOf(colorIn.charAt(0)));
            for (int i = 0; i < times; i++) {
                switch (colorIn.substring(1).toUpperCase()) {
                    case "YELLOW": colorList.add(Color.YELLOW); break;
                    case "BLUE": colorList.add(Color.BLUE); break;
                    case "GREEN": colorList.add(Color.GREEN); break;
                    case "PURPLE": colorList.add(Color.PURPLE); break;
                    default: throw new IllegalArgumentException("unrecognized color");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * This method creates a JSONObject containing the list of Development Cards still in the CardSet, hence not possessed by any player
     * @return the list of remaining Development Cards in the CardSet
     */
    public JSONObject createJSONObject(Card takenCard) {
        try {
            JSONObject result = new JSONObject();
            result.put("operation", "purchase Development Cards");
            JSONArray remainingDevCards = new JSONArray();
            for (DevelopmentCard dc : devCardList) {
                remainingDevCards.add(dc.generateId());
            }
            if(takenCard != null) {
                result.put("chosen Development card", takenCard.getId());
                result.put("chosen slot", ((DevelopmentCard) takenCard).getSlot());
            }
            result.put("remaining Development cards", remainingDevCards);
            return result;
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

}
