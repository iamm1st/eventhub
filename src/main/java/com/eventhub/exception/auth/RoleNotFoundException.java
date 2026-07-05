package com.eventhub.exception.auth;

import com.eventhub.enums.RoleName;
import com.eventhub.exception.ResourceNotFoundException;

public class RoleNotFoundException extends ResourceNotFoundException {

    public RoleNotFoundException(RoleName roleName) {
        super("Role " + roleName + " not found");
    }
}