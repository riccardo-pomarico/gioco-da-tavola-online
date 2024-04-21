package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RuleTest {

    @Test
    public void apply() {
        ArrayList<Marble> standardIn = new ArrayList<>(Arrays.asList(new Marble(Resource.COIN), new Marble(Resource.SERVANT)));
        ArrayList<Marble> standardOut =  new ArrayList<>(Arrays.asList(new Marble(Resource.COIN)));
        //Rule standard
        Rule standardRule = new Rule(standardIn,standardOut);
        //Rule con biglie nere in ingresso
        ArrayList<Marble> blackIn = new ArrayList<>(Arrays.asList(new Marble(SpecialType.BLACK), new Marble(SpecialType.BLACK)));
        ArrayList<Marble> blackOut = new ArrayList<>(Arrays.asList(new Marble(Resource.COIN)));
        Rule blackRule = new Rule(blackIn,blackOut);
        //Rule con solo uscita
        Marble outOnlyIn = new Marble(Resource.SERVANT);
        Rule outOnly = new Rule(outOnlyIn);

        //input standard
        //le Marble nella lista in sono corrette
        ArrayList<Marble> in = new ArrayList<>(standardIn);
        ArrayList<Marble> out = standardRule.apply(in);
        assertEquals("RY", out.get(0).generateId());
        //le Marble nella lista non sono corrette
        standardIn.remove(0);
        standardIn.add(new Marble(Resource.STONE));
        try {
            out = standardRule.apply(standardIn);
        }catch (IllegalArgumentException e) {
            assertEquals("Marbles do not match",e.getMessage());
        }

        //test su blackRule
        //input standtardIn
        out = blackRule.apply(standardIn);
        assertEquals("RY", out.get(0).generateId());
        //input con una Marble al posto di due
        standardIn.remove(0);
        try{
            out = blackRule.apply(standardIn);
        } catch (IllegalArgumentException e){
            assertEquals("number of marbles does not match with number required marbles",e.getMessage());
        }

        //test su outOnly
        //input con una Marble da "scontare"
        out = outOnly.apply(new ArrayList<>(Arrays.asList(new Marble(Resource.COIN), new Marble(Resource.SERVANT))));
        assertEquals(1, out.size());
        assertEquals("RY",out.get(0).generateId());
        //input con due Marble da "scontare"
        out = outOnly.apply(new ArrayList<>(Arrays.asList(new Marble(Resource.COIN), new Marble(Resource.SERVANT), new Marble(Resource.SERVANT))));
        assertEquals(2, out.size());
        assertEquals("RP",out.get(1).generateId());

    }
    @Test
    public void getInRule() {

    }

    @Test
    public void getOutRule() {
    }

    @Test
    public void generateId() {
    }
}