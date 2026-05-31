package de.reutlingen_university.checklist.operation;

import lombok.Value;

@Value
public class Child {
    String id;
    String pathDescription; // for XOR. Contains the option text to show for this child in the UI
}
