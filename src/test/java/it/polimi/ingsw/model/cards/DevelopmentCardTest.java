package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This class tests the DevelopmentCard class
 * @author Riccardo Pomarico
 */

public class DevelopmentCardTest {

    /**
     * This method checks whether the card level is converted correctly into an integer
     */
    @Test
    public void testGetLevel() {
        int level1 = 1, level2 = 2, level3 = 3;
        int test;
        DevelopmentCard card1 = new DevelopmentCard(Level.LEVEL1, Color.BLUE, null, 2);
        test = card1.getLevel();
        assertEquals(level1, test);

        DevelopmentCard card2 = new DevelopmentCard(Level.LEVEL2,Color.BLUE, null, 1);
        test = card2.getLevel();
        assertEquals(level2, test);

        DevelopmentCard card3 = new DevelopmentCard(Level.LEVEL3,Color.BLUE, null, 2);
        test = card3.getLevel();
        assertEquals(level3, test);
    }

    /**
     * Test method for DevelopmentCard.getColor()
     */
    @Test
    public void TestGetColor() {
        Color c1, c2, c3;
        DevelopmentCard card4 = new DevelopmentCard(Level.LEVEL1, Color.BLUE, null, 1);
        c1 = card4.getColor();
        assertEquals(Color.BLUE, c1);

        DevelopmentCard card5 = new DevelopmentCard(Level.LEVEL2, Color.PURPLE, null, 5);
        c2 = card5.getColor();
        assertEquals(Color.PURPLE, c2);

        DevelopmentCard card6 = new DevelopmentCard(Level.LEVEL3, Color.YELLOW, null, 9);
        c3 = card6.getColor();
        assertEquals(Color.YELLOW, c3);
    }

    /**
     * Test method for DevelopmentCard.getProductionRule()
     */
    @Test
    public void TestGetProductionRule() {
        List<Marble> in = new ArrayList<>();
        in.add(new Marble(Resource.COIN));
        in.add(new Marble(Resource.SERVANT));

        List<Marble> out = new ArrayList<>();
        out.add(new Marble(Resource.SHIELD));
        out.add(new Marble(Resource.SHIELD));
        out.add(new Marble(Resource.STONE));
        out.add(new Marble(Resource.STONE));
        out.add(new Marble(SpecialType.FAITHPOINT));

        Rule productionRule = new Rule(in, out);

        DevelopmentCard card7 = new DevelopmentCard(Level.LEVEL3, Color.GREEN, productionRule, 10);

        List<Marble> testProductionIn;
        List<Marble> testProductionOut;
        testProductionIn = card7.getProductionRule().getInRule();
        assertEquals(in, testProductionIn);
        testProductionOut = card7.getProductionRule().getOutRule();
        assertEquals(out, testProductionOut);
    }
}
