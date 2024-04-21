package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.exceptions.PersonalDashboardException;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.resources.*;

import java.util.*;

/**
 * Strategy pattern: this is the action carried out when playing a Leader card or activating a production.
 */

public class ActivationAction extends Action{
    private List<DevelopmentCard> dCard;
    private LeaderCard leadCard;
    private boolean production = false;
    private boolean requirements = true;
    private boolean levelOk = false;
    private boolean basicProduction = false;
    private boolean bonusLeader = false;
    // private int level;
    private List<Resource> resIn;
    private List<Resource> resOut;
    private List<Resource> input = new ArrayList<>();
    private Resource out;
    private int discountCoin = 0;
    private int discountShield = 0;
    private int discountStone = 0;
    private int discountServant = 0;
    private int purpleCard = 0;
    private int yellowCard = 0;
    private int greenCard = 0;
    private int blueCard = 0;
    private int source;
    private boolean multipleExecutions;
    private Actions actionType;

    /**
     * Constructor when a player wants to activate a production
     * @param targetPlayer the player who is playing his turn
     * @param devCard the development card used to activate the production
     */
    public ActivationAction(Player targetPlayer, List<DevelopmentCard> devCard, int source) throws IllegalArgumentException{
        super(targetPlayer);
        this.source = source;
        // level = devCard.getLevel();
        dCard = devCard;
        int activeCards = 0;
        int slot1 = 0;
        int slot2 = 0;
        int slot3 = 0;
        int slot = 0;
        BonusType bonus;
        production = true;
        this.multipleExecutions = false;
        actionType = Actions.ACTIVATEPRODUCTION;

        for (LeaderCard leaderCard : targetPlayer.getPersonalDashboard().getLeadCardsDeck()) {
            if(leaderCard.hasBeenActivated()) {
                bonus = leaderCard.getBonusType();

                //Gestione caso in cui ci sono carte leader già attive con il bonus di tipo sconto su un certo tipo di risorse
                //(Assumo gli sconti vengano segnati nell'ingresso della Rule class)
                if (bonus == BonusType.DISCOUNT) {

                    for(Marble m : leaderCard.getBonusRule().getInRule()){
                        switch (m.getColor()) {
                            case "BLUE":
                                discountShield++; break;
                            case "PURPLE":
                                discountServant++; break;
                            case "YELLOW":
                                discountCoin++; break;
                            case "GREY":
                                discountStone++; break;
                            default:
                                throw new IllegalArgumentException("It does not match any type.");
                        }
                    }

                }
            }
        }

        for (DevelopmentCard card : devCard) {
            if(card.hasBeenActivated()) {
                activeCards++;
                slot = card.getSlot();
                if (slot == 1) {
                    slot1 = 1;
                }
                if (slot == 2) {
                    slot2 = 1;
                }
                if (slot == 3) {
                    slot3 = 1;
                }
            }
        }

        /* Due scenari possibili:
            1. La carta sviluppo è di livello 1 e quindi controllo che ci siano al massimo altre due carte sviluppo attive,
            solo se viene soddisfatta questa richiesta posso procedere con l'attivazione della carta perchè c'è almeno
            uno slot ancora libero;
            2. La carta sviluppo è di livello N e quindi controllo che ci sia almeno una carta attiva di livello N-1.
         */
        for (DevelopmentCard dc : dCard){
            if (dc.getLevel() == 1) {

                if (activeCards < 3) {
                    levelOk = true;

                    // Assumo che le carte di livello 1 vengano posizionate seguendo l'ordine automatico di slot1, slot2 e slot3

                    if (slot1 == 0) {

                        dc.setSlot(1);

                    } else if (slot2 == 0) {

                        dc.setSlot(2);

                    } else if (slot3 == 0) {

                        dc.setSlot(3);

                    }

                }

            } else {

                // Mi fermo quando trovo una carta di livello X - 1 rispetto a quella che voglio attivare, settandola come carta usata
                // ai fini del conteggio dei punti vittoria finali, e posizionando la nuova carta sviluppo su quello slot

                for (DevelopmentCard card : targetPlayer.getPersonalDashboard().getDevCardsDeck()) {
                    if (card.hasBeenActivated()) {
                        if (dc.getLevel() == card.getLevel() + 1) {
                            levelOk = true;
                            slot = card.getSlot();
                            card.used();
                            dc.setSlot(slot);
                            dc.activate();
                            // targetPlayer.getPersonalDashboard().setCardActivated(card);
                            break;
                        }
                    }
                }

            }
        }
    }

    /**
     * Constructor when a player wants to activate a basic production power
     * @param targetPlayer the player who is playing his turn
     * @param in1 resource needed to start the basic production power
     * @param in2 resource needed to start the basic production power
     * @param out resource earned by the player
     */
    public ActivationAction(Player targetPlayer, Resource in1, Resource in2, Resource out) {
        super(targetPlayer);
        this.out = out;
        basicProduction = true;
        input.add(in1);
        input.add(in2);
        this.multipleExecutions = false;
        if (in2 == null) {
            actionType = Actions.ACTIVATELEADERPRODUCTION;
        } else {
            actionType = Actions.ACTIVATEBASIC;
        }
    }

    /**
     * Constructor when a player wants to play a leader card
     * @param targetPlayer the player who is playing his turn
     * @param leadCard the leader card used to activate the production
     */
    public ActivationAction(Player targetPlayer, LeaderCard leadCard) {
        super(targetPlayer);
        bonusLeader = true;
        this.leadCard = leadCard;
        this.multipleExecutions = true;
        actionType = Actions.ACTIVATELEADER;
    }

    @Override
    public Actions getActionType() {
        return actionType;
    }

    /**
     * Action execution method. It handles three possible cases:
     * 1) Development card production;
     * 2) Basic production;
     * 3) Leader card activation.
     * @throws IllegalArgumentException if something goes wrong with the activation: the reason is specified in the message attached to the thrown exception.
     * @throws ActionException if something goes wrong that the action parser should be aware of.
     */
    @Override
    public void execute() throws IllegalArgumentException {

        //Caso attivazione produzione
        if (production) {

            if (!levelOk) {
                //Non è possibile attivare la produzione
                throw new ActionException("It is not possible to activate the production");

            } else {

                //Viene attivata la produzione
                /* si può usare il metodo statico in Marble*/
                //Conversione dell'ingresso della ProductionRule delle biglie in risorse
                resIn = new ArrayList<>();
                for (DevelopmentCard dc : dCard){
                    for (Marble m : dc.getProductionRule().getInRule()) {
                        switch (m.getColor()) {
                            case "PURPLE":
                                if (discountServant == 0) {
                                    resIn.add(Resource.SERVANT);
                                } else {
                                    discountServant--;
                                }
                                break;
                            case "YELLOW":
                                if (discountCoin == 0) {
                                    resIn.add(Resource.COIN);
                                } else {
                                    discountCoin--;
                                }
                                break;
                            case "GREY":
                                if (discountStone == 0) {
                                    resIn.add(Resource.STONE);
                                } else {
                                    discountStone--;
                                }
                                break;
                            case "BLUE":
                                if (discountShield == 0) {
                                    resIn.add(Resource.SHIELD);
                                } else {
                                    discountShield--;
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("It does not match any type.");
                        }
                    }
                }
                try {
                    targetPlayer.getPersonalDashboard().withdrawResources(3, resIn);
                } catch (PersonalDashboardException ex) {
                    throw new ActionException(ex.getMessage());
                }
                for (DevelopmentCard dc : dCard) {
                    resOut = new ArrayList<>();
                    /* si può usare il metodo statico in Marble*/
                    //Conversione dell'uscita della ProductionRule delle biglie in risorse
                    for (Marble m : dc.getProductionRule().getOutRule()) {
                        switch (m.getColor()) {
                            case "BLUE":
                                resOut.add(Resource.SHIELD);
                                break;
                            case "PURPLE":
                                resOut.add(Resource.SERVANT);
                                break;
                            case "YELLOW":
                                resOut.add(Resource.COIN);
                                break;
                            case "GREY":
                                resOut.add(Resource.STONE);
                                break;
                            case "RED":
                                targetPlayer.increaseTrackPosition(1);
                                break;
                            case "BLACK":
                                targetPlayer.getPersonalDashboard().addToTray(new Marble(SpecialType.BLACK));
                                targetPlayer.getPersonalDashboard().notifyTray(new Marble(SpecialType.BLACK), targetPlayer.getUsername());
                                break;
                            default:
                                throw new IllegalArgumentException("It does not match any type.");
                        }
                    }

                    for (Resource r : resOut) { targetPlayer.getPersonalDashboard().depositResource(1, 0, r); }

                    targetPlayer.getPersonalDashboard().notify(targetPlayer.getPersonalDashboard().setCardActivated(dc));
                }

            }
            targetPlayer.getPersonalDashboard().notify(targetPlayer.getPersonalDashboard().notifyEndUpdate());
        }

        if (basicProduction) {
            if(input.get(1) == null){
                input.remove(1);
                targetPlayer.getPersonalDashboard().withdrawResources(2, input);
                targetPlayer.getPersonalDashboard().depositResource(1, 0, out);
                targetPlayer.increaseTrackPosition(1);
                targetPlayer.getPersonalDashboard().notify(targetPlayer.getPersonalDashboard().notifyEndUpdate());

            }
            try {
                targetPlayer.getPersonalDashboard().withdrawResources(2, input);
                targetPlayer.getPersonalDashboard().depositResource(1, 0, out);
                targetPlayer.getPersonalDashboard().notify(targetPlayer.getPersonalDashboard().notifyEndUpdate());
            } catch (PersonalDashboardException e) {
                throw new ActionException(e.getMessage());
            }
        }

        if (bonusLeader) {

            checkRequirements();

            //Attivo il bonus della carta fino alla fine della partita
            try {
                targetPlayer.activateLeader(leadCard.generateId());
            }catch (IllegalArgumentException e){
                throw new ActionException("card not in the player's deck");
            }
            if (leadCard.getBonusType() == BonusType.STORAGE) {
                // Prendo la terza sottostringa dell'id, che in questo caso rappresenta la risorsa a cui destinare lo storage extra del magazzino
                String[] id = leadCard.getId().split("-");
                String resourceString = id[2];
                Resource extraStorageResource = fromStringToResource(resourceString);
                targetPlayer.getPersonalDashboard().getWarehouse().createExtraStorage(extraStorageResource);
            }
        }

    }
    @Override
    public boolean multipleExecutions(){
        return multipleExecutions;
    }

    /**
     * This method checks whether all requirements are met to activate a leader card.
     */
    private void checkRequirements(){
        if (leadCard.getRequirements() != null) {
            if ((targetPlayer.getPersonalDashboard().countResources(leadCard.getRequirements().get(0))[0] + targetPlayer.getPersonalDashboard().countResources(leadCard.getRequirements().get(0))[1] < 5))
                throw new ActionException("Cannot activate leader: you only have "+targetPlayer.getPersonalDashboard().countResources(leadCard.getRequirements().get(0))[0] + " instead of 5");
        }
        //Controllo che vengano soddisfatti i requisiti nel caso in cui siano richieste carte di livello maggiore o uguale a 2
        if (leadCard.getRequirementsInt() > 1) {
            for (DevelopmentCard dc : targetPlayer.getPersonalDashboard().getDevCardsDeck())
                for (Color color : leadCard.getRequirementsColor()) {
                    Color c = dc.getColor();
                    if (c == color && dc.getLevel() >= 2) {
                        return;
                    }
                }
            throw new ActionException("Cannot activate leader: you don't have enough cards with the required level");
        }else{
            if(!leadCard.getRequirementsColor().isEmpty()) {
                //mappa che associa ad ogni colore un intero che rappresenta i requisiti per quel colore
                Map<Color, Integer> colorRequirements = new HashMap<>();

                //inizializzazione mappa
                Arrays.asList(Color.values()).forEach(color -> colorRequirements.put(color, 0));

                //per ogni colore, aumenta di 1 il value corrispondente a quel colore
                leadCard.getRequirementsColor().forEach(color -> colorRequirements.put(color, colorRequirements.get(color) + 1));

                //recupera il deck delle carte sviluppo e controlla se i requisiti di colore vengono soddisfatti
                targetPlayer.getPersonalDashboard().getDevCardsDeck().forEach(card -> colorRequirements.put(card.getColor(), colorRequirements.get(card.getColor()) - 1));

                Arrays.asList(Color.values()).forEach(color -> {
                    if (colorRequirements.get(color) > 0)
                        throw new ActionException("Cannot activate leader: you don't have enough " + color.toString().toLowerCase() + " development cards");
                });
            }
        }
    }

    /**
     * Static method to convert a string (such as RG, RY, etc.) into a resource.
     * @param s the string associated to the resource.
     * @return the resource deriving from that string.
     */
    public static Resource fromStringToResource(String s) {
        switch (s) {
            case "RB": return Resource.SHIELD;
            case "RY": return Resource.COIN;
            case "RG": return Resource.STONE;
            case "RP": return Resource.SERVANT;
            default: throw new IllegalArgumentException("unrecognized resource string");
        }
    }

}