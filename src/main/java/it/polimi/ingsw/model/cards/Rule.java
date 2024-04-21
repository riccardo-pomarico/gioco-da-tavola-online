package it.polimi.ingsw.model.cards;
import it.polimi.ingsw.model.IdGenerable;
import it.polimi.ingsw.model.resources.Marble;

import java.util.*;

/**
 * Class Rule
 * Rule objects represent the production power of development cards and the bonuses offered by leader cards
 * they contain a list of requrements (Marbles) to meet to apply that Rule, and a list of products (Marbles) of the Rule.
 *
 */

public class Rule implements IdGenerable {
    private final ArrayList<Marble> in;
    private final ArrayList<Marble> out;
    private Marble outOnly;

    public Rule(){
        in = new ArrayList<>();
        out = new ArrayList<>();
    }

    /**
     * Constructor of Rule class
     * @param in requirements to meet to apply the Rule
     * @param out product of the rule
     */
    public Rule(List<Marble> in, List<Marble> out) {
        this.in = new ArrayList<>(in);
        this.out = new ArrayList<>(out);
    }

    /**
     * constructor of Rule class when a discount Rule is needed
     * @param outOnly the Marble product of the rule
     */
    public Rule(Marble outOnly){
        this.in = null;
        this.out = null;
        this.outOnly = outOnly;
    }

    /**
     * This is a specific version of apply() with a list of marbles as input parameter.
     * This is used in order to handle white marbles and a player has a leader card with WHITEMARBLE as a benefit.
     * @param in the white marble purchased from the market
     * @return the list of marbles deriving from this production with a white marble.
     * @throws IllegalArgumentException in the same cases of apply() with a list of marbles as input parameter.
     */
    public ArrayList<Marble> apply(Marble in) throws IllegalArgumentException{
        if(this.in != null && this.out!= null) {
            //se la lista in non contiene biglie nere
            if(this.in.stream().noneMatch( a -> a.generateId().equals("SB"))){
                //controlla se il numero di Marble di un dato tipo della lista fornita in ingresso corrisponde al numero di Marble di quel tipo della lista in
                Marble finalIn = in;
                if (this.in.stream().filter(a -> a.generateId().equals(finalIn.generateId())).count() == 1) {
                    throw new IllegalArgumentException("Marbles do not match");
                }
            }else{
                //se la lista contine biglie nere controlla che il numero di biglie fornite corrisponda a quelle richieste
                if (1 != this.in.size()){
                    throw new IllegalArgumentException("number of marbles does not match with number required marbles");
                }
            }
            return new ArrayList<>(out);
        }
        if(this.in == null && this.out == null) {
            return new ArrayList<>(new ArrayList<>(Collections.singletonList(outOnly)));
        }else {
            throw new IllegalArgumentException("rule not recognized");
        }
    }

    /**
     * This method executes the production rule of a card and generates the output expected by this kind of production.
     * @param in list of input marbles in the production rule.
     * @return the list of output marbles in the production rule.
     * @throws IllegalArgumentException if marbles don't match, their number doesn't correspond with the number of required marbles or the applied rule isn't recognized.
     */
    public ArrayList<Marble> apply(ArrayList<Marble> in) throws IllegalArgumentException {
        if(this.in != null && this.out!= null) {
            //se la lista in non contiene biglie nere
            if(this.in.stream().noneMatch( a -> a.generateId().equals("SB"))){
                for (Marble m : in) {
                    //controlla se il numero di Marble di un dato tipo della lista fornita in ingresso corrisponde al numero di Marble di quel tipo della lista in
                    if (this.in.stream().filter(a -> a.generateId().equals(m.generateId())).count() == in.stream().filter(a -> generateId().equals(m.generateId())).count()) {
                        throw new IllegalArgumentException("Marbles do not match");
                    }
                }
            }else{
                //se la lista contine biglie nere controlla che il numero di biglie fornite corrisponda a quelle richieste
                if (in.size() != this.in.size()){
                    throw new IllegalArgumentException("number of marbles does not match with number required marbles");
                }
            }
            return new ArrayList<>(out);
        }
        //se la Rule ha soltanto un'uscita è di tipo sconto, quindi elimina dall'ingresso la Marble in out (per il bonus di tipo sconto)
        for (Marble m : in){
            if (m.generateId().equals(outOnly.generateId())){
                in.remove(m);
                break;
            }
        }
        return in;
    }

    public ArrayList<Marble> getInRule() {
        return in != null ? new ArrayList<>(in) : null;
    }

    public ArrayList<Marble> getOutRule() {
        return new ArrayList<>(Objects.requireNonNullElseGet(out, () -> new ArrayList<>(Collections.singletonList(outOnly))));
    }

    /**
     * This method generates the Rule part of the card's ID string.
     * @return the Rule substring of the card's ID.
     */
    @Override
    public String generateId() {
        StringBuilder id = new StringBuilder();
        if(in != null) {
            for (Marble m : in) {
                id.append(m.generateId());
            }
            id.append("-");
        }else {
            id.append("0-");
        }
        if(out != null) {
            for (Marble m : out) {
                id.append(m.generateId());
            }
        }else{
            id.append(outOnly.generateId());
        }
        return id.toString();
    }
}
