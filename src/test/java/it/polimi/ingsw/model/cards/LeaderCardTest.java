package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.resources.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This class tests the LeaderCard class
 */

public class LeaderCardTest {

    /**
     * Test method for LeaderCard.getBonusType() and LeaderCard.getBonusRule()
     */
    @Test
    public void testGetBonus() {
        BonusType bonus1 = BonusType.DISCOUNT;

        List<Marble> m1 = new ArrayList<>();
        m1.add(new Marble(Resource.STONE));

        List<Marble> n = new ArrayList<>();

        Rule b1 = new Rule(m1, n);

        List<Color> reqC1 = new ArrayList<>();
        reqC1.add(Color.BLUE);
        reqC1.add(Color.GREEN);

        LeaderCard card1 = new LeaderCard(b1, bonus1, reqC1, 0, 2);
        BonusType testBT1;
        Rule testBR1;
        testBT1 = card1.getBonusType();
        assertEquals(bonus1, testBT1);
        testBR1 = card1.getBonusRule();
        assertEquals(b1, testBR1);

        BonusType bonus2 = BonusType.BLANKMARBLES;

        List<Marble> m2 = new ArrayList<>();
        m2.add(new Marble(Resource.COIN));

        Rule b2 = new Rule(m2, n);

        List<Color> reqC2 = new ArrayList<>();
        reqC2.add(Color.PURPLE);
        reqC2.add(Color.PURPLE);
        reqC2.add(Color.GREEN);

        LeaderCard card2 = new LeaderCard(b2, bonus2, reqC2, 0, 5);
        BonusType testBT2;
        Rule testBR2;
        testBT2 = card2.getBonusType();
        assertEquals(bonus2, testBT2);
        testBR2 = card2.getBonusRule();
        assertEquals(b2, testBR2);
    }

    /**
     * Test method for LeaderCard.getRequirementsColor() and LeaderCard.getRequirementsInt()
     */
    @Test
    public void testGetRequirements() {
        BonusType bonus3 = BonusType.BLANKMARBLES;

        List<Marble> m3 = new ArrayList<>();
        m3.add(new Marble(Resource.SERVANT));

        List<Marble> n = new ArrayList<>();

        Rule b3 = new Rule(m3, n);

        List<Color> reqC3 = new ArrayList<>();
        reqC3.add(Color.BLUE);
        reqC3.add(Color.YELLOW);
        reqC3.add(Color.YELLOW);

        LeaderCard card3 = new LeaderCard(b3, bonus3, reqC3, 0, 5);
        List<Color> testReqC3;
        int testReqI3;
        testReqC3 = card3.getRequirementsColor();
        assertEquals(reqC3, testReqC3);
        testReqI3 = card3.getRequirementsInt();
        assertEquals(0, testReqI3);

        BonusType bonus4 = BonusType.PRODUCTIONRULE;

        List<Marble> m4In = new ArrayList<>();
        m4In.add(new Marble(Resource.SERVANT));

        List<Marble> m4Out = new ArrayList<>();
        m4Out.add(new Marble(SpecialType.BLACK));
        m4Out.add(new Marble(SpecialType.FAITHPOINT));

        Rule b4 = new Rule(m4In, m4Out);

        List<Color> reqC4 = new ArrayList<>();
        reqC4.add(Color.BLUE);

        int reqI4;
        reqI4 = 2;

        LeaderCard card4 = new LeaderCard(b4, bonus4, reqC4, reqI4, 4);
        List<Color> testReqC4;
        int testReqI4;
        testReqC4 = card4.getRequirementsColor();
        assertEquals(reqC4, testReqC4);
        testReqI4 = card4.getRequirementsInt();
        assertEquals(2, testReqI4);
    }
}
