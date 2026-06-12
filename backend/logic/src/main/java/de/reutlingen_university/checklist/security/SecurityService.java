package de.reutlingen_university.checklist.security;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;


@Service
public class SecurityService {

    private Map<?, ?> securityConfig;

    @PostConstruct
    public void init() {
        Yaml yaml = new Yaml();
    
        try (InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("META.yaml")) {
            
            if (inputStream == null) {
                System.err.println("[SECURITYSERVICE] FEHLER: META.yaml nicht im src/main/resources-Ordner gefunden!");
                return;
            }
        
            this.securityConfig = (Map<?, ?>) yaml.load(inputStream);
            System.out.println("==============================================================");
            System.out.println("[SECURITYSERVICE] ERFOLG! META.yaml portabel ueber Classpath geladen.");
            System.out.println("==============================================================");
        
        } catch (Exception e) {
            System.err.println("[SECURITYSERVICE] Fehler beim Lesen der META.yaml: " + e.getMessage());
        }
    }

    public boolean isActionAllowed(String intent, String userRole) {
        if (securityConfig == null) {
            System.out.println("[SECURITYSERVICE IAA] Keine Konfiguration geladen! Zugriff verweigert.");
            return false;
        }

        Object intentPermissionsObj = securityConfig.get("intentPermissions");
        if (intentPermissionsObj instanceof Map) {
            Map<?, ?> intentsMap = (Map<?, ?>) intentPermissionsObj;
            Object intentConfigObj = intentsMap.get(intent);
            
            if (intentConfigObj instanceof Map) {
                Map<?, ?> intentConfig = (Map<?, ?>) intentConfigObj;
                Object allowedRolesObj = intentConfig.get("allowedRoles");
                
                if (allowedRolesObj instanceof List) {
                    List<?> allowedRoles = (List<?>) allowedRolesObj;

                    for (Object role : allowedRoles) {
                        if (role instanceof String && ((String) role).equalsIgnoreCase(userRole)) {
                            System.out.println("[SECURITYSERVICE IAA] Zugriff ERLAUBT fuer Intent: " + intent + " und Rolle: " + userRole);
                            return true;
                        }
                    }
                }
            }
        }

        System.out.println("[SECURITYSERVICE IAA] Zugriff VERWEIGERT fuer Intent: " + intent + " und Rolle: " + userRole);
        return false;
    }

    public Map<?, ?> getActivityInfo(String activityId) {
        if (securityConfig == null) return null;
        
        Object activitiesObj = securityConfig.get("activities");
        if (activitiesObj instanceof Map) {
            Map<?, ?> activitiesMap = (Map<?, ?>) activitiesObj;
            Object activityInfo = activitiesMap.get(activityId);
            if (activityInfo instanceof Map) {
                return (Map<?, ?>) activityInfo;
            }
        }
        return null;
    }
}
