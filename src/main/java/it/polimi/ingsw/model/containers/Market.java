package it.polimi.ingsw.model.containers;
import it.polimi.ingsw.model.resources.Marble;
import it.polimi.ingsw.model.resources.SpecialType;
import it.polimi.ingsw.model.resources.Resource;
import it.polimi.ingsw.observer.ModelObservable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.*;

/**
 * Market class
 */
public class Market extends ModelObservable implements Serializable {
    private static final int ROWS = 3, COLUMNS = 4;
    private Marble marketMatrix[][] = new Marble[ROWS][COLUMNS];
    private Marble excessItem;
    private boolean initialised = false;

    /**
     * initialises Market by shuffling the marbles
     */
    public void init() throws UnsupportedOperationException {
        Marble tmp;
        int rndIndex, i, j = 0;

        if (initialised) {
            throw new UnsupportedOperationException("Market has already been initialised!");
        }
        //creating marbles in the correct amount for each type, the total amount must be ROWS+COLUMNS+1
        Marble marbles[] = new Marble[] {
                new Marble(Resource.COIN),
                new Marble(Resource.COIN),
                new Marble(Resource.SHIELD),
                new Marble(Resource.SHIELD),
                new Marble(Resource.STONE),
                new Marble(Resource.STONE),
                new Marble(Resource.SERVANT),
                new Marble(Resource.SERVANT),
                new Marble(SpecialType.BLANK),
                new Marble(SpecialType.BLANK),
                new Marble(SpecialType.BLANK),
                new Marble(SpecialType.BLANK),
                new Marble(SpecialType.FAITHPOINT)
        };

        // shuffling the marbles array
        for (i = marbles.length-1; i > 0; i--) {
            rndIndex = new Random().nextInt(i);
            tmp = marbles[rndIndex];
            marbles[rndIndex] = marbles[i];
            marbles[i] = tmp;
        }

        //filling the marketMatrix and excessItem attributes with the shuffled marbles

        int temp = 0;

        for (i = 0; i < ROWS; i++) {
            for (j = 0; j < COLUMNS; j++) {
                marketMatrix[i][j] = marbles[temp];
                temp++;
            }
        }

        excessItem = marbles[ROWS*COLUMNS];
        initialised = true;
        notify(createJSONObject(false));
    }

    /**
     * @param vector the column or row of the market matrix from which to withdraw the items, format must be: "R/C-X" Where X is the target row or column
     * @throws IllegalArgumentException if the vector parameter is in an incorrect format
     * @return List of marbles of the selected row or column
     */
    public ArrayList<Marble> getItems(String vector) throws IllegalArgumentException{
        String[] vectorComponents = vector.split("-");
        ArrayList<Marble> marblesToReturn = new ArrayList<>();
        String rowOrColumn;
        int position;
        try {
             rowOrColumn = vectorComponents[0].toUpperCase();
             position = Integer.parseInt(vectorComponents[1]);
        }catch (IndexOutOfBoundsException | NumberFormatException e){
            throw new IllegalArgumentException("vector is not in the correct format");
        }
        if(position<0 || position>5){
            throw new IllegalArgumentException("vector is not in the correct format");
        }


        if (!initialised) {
            init();
        }
         //check if the format of vector is correct
        if ((!rowOrColumn.equals("R") && !rowOrColumn.equals("C"))  || (rowOrColumn.equals("R") && position > ROWS-1) || (rowOrColumn.equals("C") && position > COLUMNS-1) || position < 0) {
            throw new IllegalArgumentException("vector is not in the correct format");
        }

        //fills marblesToReturn with the selected row or column
        int i;
        if (rowOrColumn.equals("C")) {
            for (i = 0; i < ROWS ; i++ ) {
                marblesToReturn.add(marketMatrix[i][position]);
            }
        } else {
            for (i = 0; i < COLUMNS; i++) {
                marblesToReturn.add(marketMatrix[position][i]);
            }
        }
        //updates marble positions
        updateMatrix(rowOrColumn,position);
        return marblesToReturn;
    }

    /**
     * getter for the matrix element of the market
     * @return market matrix
     */
    public Marble[][] getMarketMatrix() {
        Marble returnMatrix[][] = new Marble[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLUMNS ; j++){
                returnMatrix[i][j] = marketMatrix[i][j];
            }
        }
        return returnMatrix;
    }
    /**
     * getter for the excess marble element of the market
     * @return excess marble
     */
    public Marble getExcessItem() {
        return excessItem;
    }

    /**
     * This method updates the market matrix by inserting the excess marble in the chosen line.
     * @param rowOrColumn the chosen row/column (depending on Cli input).
     * @param position the position of the chosen row/column ({0, 1, 2} for rows, {0, 1, 2, 3} for columns).
     */
    private void updateMatrix(String rowOrColumn, int position){
        Marble newExcessItem;
        int i;
        //shifts each marble of a given row or column and handles the excessItem
        if (rowOrColumn.equals("C")){
            newExcessItem = marketMatrix[0][position];
            for (i = 0; i < ROWS-1; i++){
                marketMatrix[i][position] = marketMatrix[i+1][position];
            }
            marketMatrix[ROWS-1][position] = excessItem;
            excessItem = newExcessItem;
        } else if (rowOrColumn.equals("R")){
            newExcessItem = marketMatrix[position][0];
            for (i = 1; i < COLUMNS; i++){
                marketMatrix[position][i-1] = marketMatrix[position][i];
            }
            marketMatrix[position][COLUMNS-1] = excessItem;
            excessItem = newExcessItem;
        }
        notify(createJSONObject(true));
    }

    /**
     * This is a static method that checks whether the input from Cli of the chosen line of marbles is actually correct.
     * @param vector the Cli input.
     * @return the potential correctness of @param vector.
     */
    public static boolean checkInputFormat(String vector){
        String[] vectorComponents = vector.split("-");
        ArrayList<Marble> marblesToReturn = new ArrayList<>();
        String rowOrColumn;
        int position;
        try {
            rowOrColumn = vectorComponents[0].toUpperCase();
            position = Integer.parseInt(vectorComponents[1]);
        }catch (IndexOutOfBoundsException | NumberFormatException e){
            return false;
        }
        return (rowOrColumn.equals("R") || rowOrColumn.equals("C")) && (!rowOrColumn.equals("R") || position <= ROWS - 1) && (!rowOrColumn.equals("C") || position <= COLUMNS - 1) && position >= 0;
    }


    @SuppressWarnings("unchecked")
    /**
     * This method creates a JSONObject through which the model indicates the change of status of the market.
     * @return a JSONObject indicating the executed operation and the new market status
     */
    public JSONObject createJSONObject(boolean isInitialized) {
        JSONObject result = new JSONObject();
        result.put("operation", "update market");
        JSONArray arrayMarket = new JSONArray();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                arrayMarket.add(marketMatrix[i][j].getType().substring(0,4));
            }
        }
        result.put("market status", arrayMarket);
        result.put("new excess item", excessItem.getType().substring(0,4));
        result.put("is initialized", isInitialized);
        return result;
    }
}
