package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.actions.ActionParser;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class InputHandler {

    public static  <T,R> T getInput(Predicate<T> condition, Consumer<R> errorTask, R target, Supplier<T> nextType){
        T input;
        while (true){
            try {
                input = nextType.get();
            }catch (Exception ignored){
                errorTask.accept(target);
                continue;
            }
            if(!condition.test(input))
                errorTask.accept(target);
            else
                break;
        }
        return input;
    }
    public static <T> int getInt(Predicate<Integer> condition, Consumer<T> errorTask, T target){
        return getInput(condition,errorTask,target, () -> new Scanner(System.in).nextInt());
    }
    public static <T> String getString(Predicate<String> condition, Consumer<T> errorTask, T target){
        return getInput(condition,errorTask,target, () -> new Scanner(System.in).nextLine());
    }
    public static boolean checkIPAddress(String serverAddress){
        String[] split = serverAddress.split("\\.");
        if(split.length != 4)
            return false;
        int oct;
        for (String s : split){
            try {
                oct = Integer.parseInt(s);
            }catch (NumberFormatException ignored){
                return false;
            }
            if(oct < 0 || oct > 255)
                return false;
        }
        return true;
    }
    public static boolean checkUsername(String username){
        if(username.length()<2 || username.length()>10){
            return false;
        }
        int asciiChar;
        for(int i = 0; i<username.length(); i++){
            asciiChar = username.charAt(i);
            if(asciiChar < 48 || (asciiChar > 57 && asciiChar < 65) || (asciiChar > 90 && asciiChar < 97) || asciiChar > 122 ){
                return false;
            }
        }
        return true;
    }
    public static boolean checkPort(int port){
        return port > 1024 && port < 65535;
    }

    public static boolean checkNumPlayers(int numPlayers){
        return numPlayers > 0 && numPlayers < 5;
    }

    public static String getServerAddressError(){
        return "please enter a valid server address ([0-255].[0-255].[0-255].[0-255])";
    }
    public static String getPortError(){
        return "please select a valid port [1024-65535]";
    }
    public static String getUsernameError(){
        return "please enter a valid username (must be between 1 and 8 characters long, only letters and numbers)";
    }
    public static String getNumPlayersError(){
        return  "the number of players must be between 1 and 4";
    }


}
