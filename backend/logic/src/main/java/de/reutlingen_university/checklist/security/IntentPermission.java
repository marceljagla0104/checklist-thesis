package de.reutlingen_university.checklist.security;

import java.util.List;
import lombok.Data;


@Data
public class IntentPermission {
    private List<String> allowedRoles;
    private String description;
}
