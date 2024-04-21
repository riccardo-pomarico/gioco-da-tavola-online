package it.polimi.ingsw.model.player;

import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * This class tests all methods of the PopesFavorTrack class
 */
public class PopesFavorTrackTest {

    /**
     * This method checks whether all initial settings are correctly inserted.
     */
    @Test
    public void testConstructor() {
        // Testing the constructor without parameter
        PopesFavorTrack pft = new PopesFavorTrack();
        int incr = 1, points = 1, pos = 3;
        for (int i = 0; i < pft.getGoldenBoxPoints().size(); i++) {
            assertEquals((int) pft.getGoldenBoxPoints().get(pos), points);
            if (i % 2 == 1) {
                incr++;
            }
            pos = pos + 3;
            points = points + incr;
        }

        // Testing the constructor with one parameter
        ArrayList<Integer> values = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            values.add(i+10);
        }
        pos = 3;
        PopesFavorTrack pft2 = new PopesFavorTrack(values);
        for (int i = 0; i < pft2.getGoldenBoxPoints().size(); i++) {
            assertEquals((int) pft2.getGoldenBoxPoints().get(pos), (int) values.get(i));
            pos += 3;
        }
    }

    /**
     * Verifies the method obtainPositionPoints()
     */
    @Test
    public void testObtainPositionPoints() {
        PopesFavorTrack pft = new PopesFavorTrack();
        int positionPoints = 0, increment = 1;
        for (int pos = 0; pos <= 24; pos++) {
            if (pos % 3 == 0 && pos != 0) {
                positionPoints = positionPoints + increment;
                if (pos % 6 == 0) {
                    increment++;
                }
            }
            assertEquals(positionPoints, pft.obtainPositionPoints(0, pos));
            assertEquals(0, pft.obtainPositionPoints(pos, pos));
        }
    }

    /**
     * This method tests the expected behaviour in the assignment of faith track points in a path
     * where the player steps on boxes 6, 16 and 24.
     */
    @Test
    public void testPath6_16_24() {
        // Simulate a path
        PopesFavorTrack pft = new PopesFavorTrack();
        int pos1 = 0, pos2 = 6, points, popesCardPoints = 0;
        pft.setTrackPosition(pos2);
        // Someone steps on the first popesBox
        popesCardPoints += pft.obtainPopesCardPoints(8);
        points = (popesCardPoints + pft.obtainPositionPoints(pos1, pos2));
        assertEquals(4, points);
        pos1 = pos2;
        pos2 = 16;
        pft.setTrackPosition(pos2);
        // The player steps on the second popesBox
        popesCardPoints += pft.obtainPopesCardPoints(16);
        points = (popesCardPoints + pft.obtainPositionPoints(pos1, pos2));
        assertEquals(14, points);
        pos1 = pos2;
        pos2 = 24;
        pft.setTrackPosition(pos2);
        // The player steps on the final popesBox
        popesCardPoints += pft.obtainPopesCardPoints(24);
        points = (popesCardPoints + pft.obtainPositionPoints(pos1, pos2));
        assertEquals(29, points);
    }

    @Test
    public void testObtainPopesCardPoints() {
        PopesFavorTrack pft = new PopesFavorTrack();
        int points, popesBox = 8;
        for (int pos = 1; pos <= 24; pos++) {
            pft.setTrackPosition(pos);
            points = pft.obtainPopesCardPoints(popesBox);
            if ((pos >= 5 && pos <= 8) || (pos >= 12 && pos <= 16) || pos >= 19) {
                assertTrue(pft.getVaticanZone()[(popesBox/8)-1]);
                assertEquals((popesBox/8)+1, points);
            } else {
                if (pos > 8 && pos < 12)
                    assertEquals(3, points);
                if (pos > 16)
                    assertEquals(4, points);
            }
            if (pos % 8 == 0) {
                popesBox += 8;
            }
        }
        try {
            pft.obtainPositionPoints(9, 8);
        } catch (IllegalStateException e) {
            assertEquals("Illegal call of this function: the final position has to succeed the initial one.", e.getMessage());
        }
    }

    /**
     * Verifies if the vector vaticanZone[] is properly set according to the reached position.
     */
    @Test
    public void testHasReachedPopesZone1() {
        PopesFavorTrack pft = new PopesFavorTrack();
        for (int pos = 0; pos <= 24; pos++) {
            pft.hasReachedPopesZone(pos);
            if (pos >= 5 && pos <= 8) {
                assertTrue(pft.getVaticanZone()[0]);
                assertFalse(pft.getVaticanZone()[1]);
                assertFalse(pft.getVaticanZone()[2]);
            } else if (pos >= 12 && pos <= 16) {
                assertFalse(pft.getVaticanZone()[0]);
                assertTrue(pft.getVaticanZone()[1]);
                assertFalse(pft.getVaticanZone()[2]);
            } else if (pos >= 19) {
                assertFalse(pft.getVaticanZone()[0]);
                assertFalse(pft.getVaticanZone()[1]);
                assertTrue(pft.getVaticanZone()[2]);
            } else {
                for (int i = 0; i < 3; i++) {
                    assertFalse(pft.getVaticanZone()[i]);
                }
            }
        }
    }

    @Test
    public void testHasReachedPopesZone2() {
        PopesFavorTrack pft = new PopesFavorTrack();
        ArrayList<Integer> chosenVaticanZones = new ArrayList<>();
        // Test: exception of more than 3 left ends
        try {
            chosenVaticanZones.add(0, 6);
            chosenVaticanZones.add(1, 16);
            chosenVaticanZones.add(2, 18);
            chosenVaticanZones.add(3, 24);
            pft.hasReachedPopesZone(0, chosenVaticanZones);
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal call!", e.getMessage());
        }
        chosenVaticanZones.remove(chosenVaticanZones.get(3));
        pft.hasReachedPopesZone(17, chosenVaticanZones);
        assertFalse(pft.getVaticanZone()[2]);
        // Test: the second left end is after the second Pope's box
        try {
            chosenVaticanZones.set(1, 17);
            pft.hasReachedPopesZone(17, chosenVaticanZones);
        } catch (IllegalArgumentException ex) {
            assertEquals("Illegal call!", ex.getMessage());
        }
        chosenVaticanZones.set(1, 15);
        pft.hasReachedPopesZone(15, chosenVaticanZones);
        assertTrue(pft.getVaticanZone()[1]);
        assertFalse(pft.getVaticanZone()[0]);
        assertFalse(pft.getVaticanZone()[2]);
        try {
            pft.hasReachedPopesZone(-1, chosenVaticanZones);
        } catch (IllegalArgumentException e) {
            assertEquals("You're out of the track's boundaries!", e.getMessage());
        }
        for (int pos = 0; pos <= 24; pos++) {
            pft.hasReachedPopesZone(pos, chosenVaticanZones);
            if (pos >= chosenVaticanZones.get(0) && pos <= 8) {
                assertTrue(pft.getVaticanZone()[0]);
                assertFalse(pft.getVaticanZone()[1]);
                assertFalse(pft.getVaticanZone()[2]);
            } else if (pos >= chosenVaticanZones.get(1) && pos <= 16){
                assertFalse(pft.getVaticanZone()[0]);
                assertTrue(pft.getVaticanZone()[1]);
                assertFalse(pft.getVaticanZone()[2]);
            } else if (pos >= chosenVaticanZones.get(2)) {
                assertFalse(pft.getVaticanZone()[0]);
                assertFalse(pft.getVaticanZone()[1]);
                assertTrue(pft.getVaticanZone()[2]);
            } else {
                assertFalse(pft.getVaticanZone()[0]);
                assertFalse(pft.getVaticanZone()[1]);
                assertFalse(pft.getVaticanZone()[2]);
            }
        }
    }
}