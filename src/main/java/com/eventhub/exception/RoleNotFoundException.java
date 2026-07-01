package com.eventhub.exception;

import com.eventhub.enums.RoleName;

public class RoleNotFoundException extends ResourceNotFoundException {

    public RoleNotFoundException(RoleName roleName) {
        super("Role " + roleName + " not found");
    }
}