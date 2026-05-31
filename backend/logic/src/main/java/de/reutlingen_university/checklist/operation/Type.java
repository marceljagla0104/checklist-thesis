package de.reutlingen_university.checklist.operation;

import org.w3c.dom.Node;

public enum Type {
    TASK,
    XOR,
    PARALLEL,
    DATA,
    TEXT,
    INPUT,
    END_EVENT,
    START_EVENT,
    BOUNDARY_EVENT,
    SUBPROCESS;

    public static Type fromNode(Node node) {
        String nodeName = node.getNodeName();
        return switch (nodeName) {
            case "bpmn:task" -> Type.TASK;
            case "bpmn:exclusiveGateway" -> Type.XOR;
            case "bpmn:parallelGateway" -> Type.PARALLEL;
            case "bpmn:dataObjectReference" -> Type.DATA;
            case "bpmn:textAnnotation" -> Type.TEXT;
            case "bpmn:dataInputAssociation" -> Type.INPUT;
            case "bpmn:startEvent" -> Type.START_EVENT;
            case "bpmn:endEvent" -> Type.END_EVENT;
            case "bpmn:boundaryEvent" -> Type.BOUNDARY_EVENT;
            case "bpmn:subProcess" -> Type.SUBPROCESS;
            default -> null;
        };
    }
}
