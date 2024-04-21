package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.exceptions.ActionException;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.containers.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.resources.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Action used by the player to purchase items form the market, or to buy a development card
 */
public class PurchaseAction extends Action {
    private String target;
    private  Market market;
    private  CardSet cardSet;
    private boolean purchasetype;
    private int targetDeposit = 0;
    private int slot = -1;
    private  Rule[] developmentCardDiscount = new Rule[2];
    private  Marble whiteMarble;
    private  DevelopmentCard card = null;
    private final int MAX_NUM_BONUSES = 2;
    private Actions actionType;

    /**
     * Constructor for the action for market purchases
     * @param targetPlayer the player towards whom the purchase action is directed
     * @param target String containing the row or column of the market from which to withdraw the items
     *               (see Market.java documentation for the correct format to use)
     * @param market The game Market
     */
    //costruttore nel caso la action da esegure riguardi il mercato

    public PurchaseAction(Player targetPlayer, String target, Market market ){
        super(targetPlayer);
        this.target = target;
        this.market = market;
        purchasetype = true;
        whiteMarble = new Marble(SpecialType.BLANK);
        actionType = Actions.MARKETPURCHASE;
    }

    /**
     * @param targetPlayer the player towards whom the purchase action is directed
     * @param card the developmentCard to purchase
     * @param targetDeposit the deposit from which to withdraw the resources
     * @param cardSet the game CardSet, containing all the cards
     *
     */
    //costruttore nel caso la action da eseguire riguardi le carte sviluppo
    public PurchaseAction(Player targetPlayer, DevelopmentCard card, int targetDeposit, CardSet cardSet, int slot){
        super(targetPlayer);
        this.card = card;
        this.card.setSlot(slot);
        this.cardSet = cardSet;
        this.slot = slot;
        this.targetDeposit = targetDeposit;
        purchasetype = false;
        actionType = Actions.DEVCARDPURCHASE;
    }


    @Override
    public Actions getActionType() {
        return actionType;
    }

    @Override
    public void execute() throws ActionException{
        //TODO: controllare che le carte possono essere comprate in base alle carte presenti
        getPlayerBonuses();
        //controlla il tipo di purchase da eseguire, o dal mercato, o dalle carte sviluppo
        if(purchasetype){
            List<Marble> marbles;
            try{
                //prova a prelevare dal mercato, usando come input la stringa target, che contiene la riga o colonna da cui prelevare
                marbles = market.getItems(target);
            }catch (IllegalArgumentException e){
                throw new ActionException(e.getMessage());
            }
            //una volta ottenute le biglie, controlla il loro contenuto e svolge le relative funzioni
            String marbleId;
            for (Marble marble : marbles){
                if(!Marble.isType(SpecialType.FAITHPOINT,marble)) {
                    if (Marble.isType(SpecialType.BLANK, marble)) {
                        marble = whiteMarble;
                    }
                    try {
                        targetPlayer.getPersonalDashboard().addToTray(marble);
                        targetPlayer.getPersonalDashboard().notifyTray(marble, targetPlayer.getUsername());
                    } catch (IllegalArgumentException ignored) {
                    }
                }else{
                    targetPlayer.increaseTrackPosition(1);
                }
            }
        }else { //se il tipo di acquisto è di development card

            //cerca la carta nel cardset tramite l'id e la preleva
            List<Resource> requirements;
            try{
                card = (DevelopmentCard) cardSet.findCard(card.generateId());
            }catch(IllegalArgumentException e) {
                e.printStackTrace();
                throw new ActionException(e.getMessage());
            }
            //controlla il livello (il giocatore deve possedere almeno una carta "scoperta" di livello inferiore, oppure uno slot vuoto se la carta è di livello 1
            List<DevelopmentCard> devCardSlot = targetPlayer.getPersonalDashboard().getDevCardsDeck().stream().filter( card1 -> card1.getSlot() == slot).collect(Collectors.toList());

            if(card.getLevel() == 1 && devCardSlot.size() != 0){
                throw new ActionException("this slot is not empty! ");
            }
            if(card.getLevel() != 1 && devCardSlot.size() == 0){
                throw new ActionException("you must put in slot "+(slot+1)+" a card of level 1 first! ");
            }
            int maxLevel;
            Optional<Integer> maxLevelOptional = devCardSlot.stream().map(DevelopmentCard::getLevel).max(Integer::compare);
            maxLevel = maxLevelOptional.orElse(0);
            if(maxLevel != card.getLevel()-1){
                throw new ActionException("you need a card of level "+ (card.getLevel()-1) + "in this slot!");
            }
            //superati i controlli imposta lo slot
            card.setSlot(slot);

            //controlla se è possibile applicare uno sconto e nel caso lo applica
            requirements = card.getRequirements();
            for(int i = 0; i < MAX_NUM_BONUSES; i++){
                if (developmentCardDiscount[i] != null){
                    requirements = applyDiscount(developmentCardDiscount[i],requirements);
                }
            }
            //prova a prelevare le risorse dal giocatore
            if(targetDeposit == 3){
                //modalità automatica. preleva quello che riesce dal magazzino, se ce ne sono altre da prelevare le prende dal forziere
                //mappa che associa ad ogni risorsa quante ne servono, quante ce ne sono nel magazzino e quante ce ne sono nel forziere
                Map<Resource,int[]> requirementsMap = new HashMap<>();
                Arrays.asList(Resource.values()).forEach( resource ->  requirementsMap.put(resource,targetPlayer.getPersonalDashboard().countResources(resource)));

                //divisione della lista di risorse richieste in risorse da prelevare dal magazzino e risorse da prelevare dal forziere
                List<Resource> withdrawFromWarehouse = new ArrayList<>(),withdrawFromStrongbox = new ArrayList<>();
                for(Resource resource : requirements){
                    if(requirementsMap.get(resource)[1]>0){
                        withdrawFromWarehouse.add(resource);
                        requirementsMap.get(resource)[1]--;
                    }else{
                        if(requirementsMap.get(resource)[0]>0){
                            withdrawFromStrongbox.add(resource);
                            requirementsMap.get(resource)[0]--;
                        }else {
                            throw new ActionException("you don't have enough resources!");
                        }
                    }
                }


                try{
                    targetPlayer.getPersonalDashboard().withdrawResources(2,withdrawFromWarehouse);
                    targetPlayer.getPersonalDashboard().withdrawResources(1,withdrawFromStrongbox);
                }catch (WarehouseException e){
                    throw new ActionException("you don't have enough resources!");
                }

            }else {
                try {
                    targetPlayer.getPersonalDashboard().withdrawResources(targetDeposit, requirements);
                } catch (PersonalDashboardException e) {
                    throw new ActionException(e.getMessage());
                }
            }
            try {
                targetPlayer.getPersonalDashboard().addCard(card);
                cardSet.takeCard(card.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean multipleExecutions() {
        return false;
    }

    private void getPlayerBonuses(){
        int numDiscounts = 0;
        //restituisce lista delle carte leader che sono state attivate
        List<LeaderCard> leaderCardList = targetPlayer
                .getPersonalDashboard()
                .getLeadCardsDeck()
                .stream()
                .filter(Card::hasBeenActivated )
                .collect(Collectors.toList());
        for(LeaderCard card : leaderCardList){
            if (card.getBonusType() == BonusType.BLANKMARBLES){
                try {
                    whiteMarble = card.getBonusRule().apply(whiteMarble).get(0);
                }catch (NullPointerException e){
                    throw new ActionException("error");
                }
            }
            else if (card.getBonusType() == BonusType.DISCOUNT){
                try {
                    developmentCardDiscount[numDiscounts] = card.getBonusRule();
                    numDiscounts++;
                }catch (IndexOutOfBoundsException e){
                    throw new ActionException("there cannot be more than two leader card activated");
                }
            }
        }

    }
    private List<Resource> applyDiscount(Rule rule, List<Resource> requirements){
        try {
            return Marble.toResources(rule.apply(Marble.toMarbles(requirements)));
        }catch (Exception e){
            throw new ActionException("error while applying the bonus");
        }
    }

}

