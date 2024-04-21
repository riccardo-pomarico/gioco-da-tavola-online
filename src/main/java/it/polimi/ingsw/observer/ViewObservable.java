package it.polimi.ingsw.observer;

import it.polimi.ingsw.task.Task;

import java.util.ArrayList;
import java.util.List;

public abstract class ViewObservable {
    protected final List<ViewObserver> viewObservers = new ArrayList<>();

    public void addViewObserver(ViewObserver obs) {
        viewObservers.add(obs);
    }

    /**
     * Notify method of this Observer pattern between view and client controller.
     * @param operation the operation that client controller has to execute.
     */
    protected void notify(Task<ViewObserver> operation) {
        for (ViewObserver viewObserver : viewObservers) {
            viewObserver.update(operation);
        }
    }
}
