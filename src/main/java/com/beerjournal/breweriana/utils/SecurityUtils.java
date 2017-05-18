package com.beerjournal.breweriana.utils;

import com.beerjournal.breweriana.user.persistence.User;
import com.beerjournal.infrastructure.security.BjPrincipal;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public final class SecurityUtils {

    public boolean checkIfAuthorized(String userId) {
        return checkAuthorization(Converters.toObjectId(userId));
    }

    public boolean checkIfAuthorized(ObjectId userId) {
        return checkAuthorization(userId);
    }

    public ObjectId getCurrentlyLoggedInUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("User not found!");
        }
        BjPrincipal bjPrincipal = (BjPrincipal) authentication.getPrincipal();
        return bjPrincipal.getDbUser().getId();
    }

    private boolean checkAuthorization(ObjectId userId) {
        ObjectId currentUserId = getCurrentlyLoggedInUserId();
        return currentUserId.equals(userId);
    }

}
