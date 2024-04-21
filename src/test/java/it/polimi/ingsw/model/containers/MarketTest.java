package it.polimi.ingsw.model.containers;
import it.polimi.ingsw.model.containers.Market;
import it.polimi.ingsw.model.resources.Marble;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test class for Market.java
 * @author Federico Garzia
 */
public class MarketTest {
    /**
     * test method for Market.getItems()
     */
    @Test
    public void testGetItems() {
        Market m = new Market();
        m.init();
        ArrayList<Marble> items;
        Marble column[] = new Marble[3];
        Marble matrix[][];

        for (int j = 0; j < 4; j++){
            matrix = m.getMarketMatrix();
            items = m.getItems("C-"+j);
            Iterator<Marble> iter = items.iterator();
            for (int i = 0; i < 3; i++){
                column[i] = matrix[i][j];
            }
            assertArrayEquals(column,items.toArray());
        }
    }

    /**
     * test method for Market.Init()
     */
    @Test
    public void testInit() {
        Market m = new Market();
        m.init();
        Marble[][] matrixOld, matrixNew;
        Marble excessItemOld, excessItemNew;
        int equals = 0;
        int numEquals = 0;
        //checks if in 1000 attempts to init the matrix the initialised market is different from the previous one
        for ( int i = 0; i < 100000 ; i++){
                matrixOld = m.getMarketMatrix();
                excessItemOld = m.getExcessItem();
                m = new Market();
                m.init();
                matrixNew = m.getMarketMatrix();
                excessItemNew = m.getExcessItem();
                for (int j = 0; j < 3; j++){
                    for (int k = 0; k < 4; k++){
                        if ((matrixOld[j][k].getType()).equals(matrixNew[j][k].getType())){
                            equals ++;
                        }
                    }
                }
                if (excessItemNew.getType().equals(excessItemOld.getType())){
                    equals++;
                }
                if(equals == 13){
                    numEquals++;
                }
                //assertNotEquals(13,equals);
                equals = 0;
            }
        //ricava la percentuale di volte in cui la configurazione del mercato precedente è uguale a quella attuale
        numEquals /= 1000;
        //questa percentuale deve essere inferiore allo 0.05%
        assertTrue(numEquals < 0.05);
    }
}
