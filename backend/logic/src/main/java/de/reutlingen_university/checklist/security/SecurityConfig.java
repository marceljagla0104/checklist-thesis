package de.reutlingen_university.checklist.security;

import java.util.Map;
import lombok.Data;


@Data
public class SecurityConfig {
    private Map<String, IntentPermission> intentPermissions;

    private Map<String, Object> activities;
    
}
