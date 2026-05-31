package de.reutlingen_university.checklist.operation;

public enum Role {
    SURGEON,
    SCRUB,
    STUDENT,
    CIRCULATING;

    public static Role fromString(String role) {
        role = role.toUpperCase();
        role = role.trim();

        return switch (role) {
            case "SURGEON" -> SURGEON;
            case "SCRUB" -> SCRUB;
            case "STUDENT" -> STUDENT;
            case "CIRCULATING" -> CIRCULATING;
            default -> throw new IllegalStateException("Unexpected role: " + role);
        };
    }
}
