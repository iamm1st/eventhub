package com.eventhub.exception;

import com.eventhub.enums.RoleName;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(RoleName roleName) {
        super("Role " + roleName + " not found");
    }
}