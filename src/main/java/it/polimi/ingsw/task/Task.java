package it.polimi.ingsw.task;

import java.util.function.Consumer;

/**
 * Class generalizing all tasks: a Task is a consumer of any class.
 * @param <T> the class involved in the Consumer interface.
 */
public class Task<T> {
    private Consumer<T> operation;
    public Task(Consumer<T> operation){
        this.operation = operation;
    }
    public void execute(T target) {
        operation.accept(target);
    }
}
