package it.polimi.ingsw.model.resources;

import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.model.resources.SpecialType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class tests the Marble class
 * @author Riccardo Pomarico
 */

public class MarbleTest {

    /**
     * This method checks whether the marbles are associated correctly with the resources, faith point and blank
     */
    @Test
    public void testGetType() {
        String color1 = "BLUE", color2 = "WHITE", color3 = "RED", color4 = "PURPLE";
        String test;

        Marble m1 = new Marble(Resource.SHIELD);
        test = m1.getColor();
        assertEquals(color1, test);

        Marble m2 = new Marble(SpecialType.BLANK);
        test = m2.getColor();
        assertEquals(color2, test);

        Marble m3 = new Marble(SpecialType.FAITHPOINT);
        test = m3.getColor();
        assertEquals(color3, test);

        Marble m4 = new Marble(Resource.SERVANT);
        test = m4.getColor();
        assertEquals(color4, test);
    }
}
