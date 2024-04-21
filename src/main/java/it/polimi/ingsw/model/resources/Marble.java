package it.polimi.ingsw.model.resources;

import it.polimi.ingsw.model.IdGenerable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Marble
 */

public class Marble implements IdGenerable {
    private Resource typeResource = null;
    private SpecialType typeSpecialType = null;
    private String color;

    public Marble(String id){
        try {
            if (id.charAt(0) == 'R') {
                resourceMarble(resourceFromID(id));
            } else if (id.charAt(0) == 'S') {
                switch (id.charAt(1)) {
                    case 'W':
                        specialTypeMarble(SpecialType.BLANK);
                        break;
                    case 'R':
                        specialTypeMarble(SpecialType.FAITHPOINT);
                        break;
                    case 'B':
                        specialTypeMarble(SpecialType.BLACK);
                        break;
                    default:
                        throw new IllegalArgumentException("cannot create a marble from [" + id + "]");
                }
            } else {
                throw new IllegalArgumentException("cannot create a marble from [" + id + "]");
            }
        }catch (StringIndexOutOfBoundsException e){
            throw new IllegalArgumentException("marble is not in the correct format");
        }
    }
    /** Marble Constructor for Resource
     * @param resource type of the marble (resource)
     */
    public Marble(Resource resource) {
        resourceMarble(resource);
    }

    /**
     * Marble Constructor for faithpoint and blank types
     * @param typeSpecialType of the marble (string)
     */
    public Marble(SpecialType typeSpecialType) {
        specialTypeMarble(typeSpecialType);
    }


    private void resourceMarble(Resource resource){
        this.typeResource = resource;
        switch (resource) {
            case SHIELD:
                color = "BLUE"; break;
            case SERVANT:
                color = "PURPLE"; break;
            case COIN:
                color = "YELLOW"; break;
            case STONE:
                color = "GREY"; break;
            default:
                throw new IllegalArgumentException("It does not match any type.");
        }
    }
    private void specialTypeMarble(SpecialType specialType){
        this.typeSpecialType = specialType;
        switch (typeSpecialType) {
            case FAITHPOINT:
                color = "RED";
                break;
            case BLANK:
                color = "WHITE";
                break;
            case BLACK:
                color = "BLACK";
                break;
            default:
                throw new IllegalArgumentException("It does not match any type.");

        }
    }
    //TODO: Gestisci la biglia nera in una nuova action


    /**
     * @return String which contains the color of the marble
     */
    public String getColor() {
        return color;
    }

    /**
     * @return String which contains the type of the marble
     */
    public String getType() {
        if (typeResource != null) {
            return typeResource.toString();
        } else if (typeSpecialType != null) {
            return typeSpecialType.toString();
        } else {
            return null;
        }
    }

    /**
     * @return id of the marble: XY where X is R if the marble contains a resource or S if it's a special
     * marble, Y is the color of the marble
     */
    @Override
    public String generateId() {
        String type;
        if (typeResource != null) {
            type = "R";
        } else {
            type = "S";
        }
        return  ""+type+getColor().charAt(0);
    }

    /**
     * Converts a list of Resources in an ArrayList of Marbles
     * @param in list of Resources to turn in list of Marbles
     * @return list of Marbles
     */
    public static ArrayList<Marble> toMarbles(List<Resource> in){
        ArrayList<Marble> out = new ArrayList<>();
        for (Resource r : in){
            out.add(new Marble(r));
        }
        return out;
    }

    /**
     * Converts a list of Marbles in an ArrayList of Resources
     * @param in list of marbles to turn in list of Resources
     * @return list of Resources
     */
    public static ArrayList<Resource> toResources(List<Marble> in){
        ArrayList<Resource> out = new ArrayList<>();
        for (Marble m : in){
            if (m.generateId().charAt(0) == 'S'){
                throw new IllegalArgumentException("cannot convert the list in a resource list");
            }
            out.add(resourceFromID(m.generateId()));
        }
        return out;
    }


    public static Resource resourceFromID(String id){
        if(id.charAt(0)!='R'){
            throw new IllegalArgumentException("cannot create a Resource from ["+id+"]");
        }
        switch (id.charAt(1)){
            case 'Y':
                return Resource.COIN;
            case 'P':
                return Resource.SERVANT;
            case 'B':
                return Resource.SHIELD;
            case 'G':
                return Resource.STONE;
            default:
                throw new IllegalArgumentException("cannot create a Resource from ["+id+"]");
        }
    }

    public static boolean isType(Resource resource, Marble marble){
        return marble.getType().equals(resource.toString());
    }
    public static boolean isType(SpecialType specialType, Marble marble){
        return marble.getType().equals(specialType.toString());
    }

}
