package it.polimi.ingsw.view.gui.scene;

import it.polimi.ingsw.observer.ViewObservable;
import it.polimi.ingsw.view.gui.SceneController;
import javafx.application.Platform;
import javafx.scene.Node;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Abstract class used to define all the scene controllers.
 */
public abstract class GenericScene extends ViewObservable {
    private String title;
    private String sceneId;
    private SceneController sceneController;

    public GenericScene(String sceneId){
        this.sceneId = sceneId;
    }
    public String getSceneId(){
        return this.sceneId;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setSceneController(SceneController sceneController){
        this.sceneController = sceneController;
    }

    protected void changeScene(String sceneId) {
        Platform.runLater(() -> sceneController.changeScene(sceneId));
    }
    protected void loadScene(String sceneId) {
        Platform.runLater(() -> sceneController.loadScene(sceneId));
    }
    protected GenericScene getScene(String sceneId) throws IllegalArgumentException{
        return sceneController.getScene(sceneId);
    }
}
