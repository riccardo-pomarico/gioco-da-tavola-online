package it.polimi.ingsw.model.exceptions;

import it.polimi.ingsw.model.resources.Resource;

public class ImpossibleStorageException extends RuntimeException {
    Resource resourceToDiscard;
    public ImpossibleStorageException(Resource r, String message) {
        super(message);
        resourceToDiscard = r;
    }

    public Resource getResourceToDiscard() {
        return resourceToDiscard;
    }
}
