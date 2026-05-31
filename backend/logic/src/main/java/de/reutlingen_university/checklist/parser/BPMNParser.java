package de.reutlingen_university.checklist.parser;

import de.reutlingen_university.checklist.operation.*;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BPMNParser {

    public static BPMNParserResult parseBPMN(File bpmn) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;

        try {
            doc = dbf.newDocumentBuilder().parse(bpmn);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }

        doc.getDocumentElement().normalize();
        Node process = doc.getElementsByTagName("bpmn:process").item(0); // the whole process as node
        List<Node> associations = getElementsByTagName(doc, "bpmn:association"); // get all associations
        List<Node> textAnnotations = getElementsByTagName(doc, "bpmn:textAnnotation"); // get all text annotations

        Node startEvent = getChildElementsByTagName(process, "bpmn:startEvent").get(0); // get start event

        return parseAndBuildResult(process, startEvent, associations, textAnnotations); // walk through process nodes and build parser result
    }



    private static BPMNParserResult parseAndBuildResult(
            Node process, Node startEvent,
            List<Node> associations,
            List<Node> textAnnotations
    ) {
        List<Subprocess> subprocesses = new ArrayList<>();
        List<Node> nodes = getChildElementsByTagName(process, "bpmn:subProcess");// get all subprocess nodes

        List<Element> allElements = new ArrayList<>();
        for (Node subprocess : nodes) { // for each subprocess get elements and build Subprocess object
            String name = getName(subprocess); // extract subprocess name
            List<Element> elements = getElements(subprocess, associations, textAnnotations); // get all elements for the subprocess recursively
            allElements.addAll(elements);
            //todo use flow to find out order
            List<String> elementIds = elements.stream().map(Element::getId).toList(); // todo put in db
            subprocesses.add(Subprocess.create(name, elementIds));
        }

        Operation operation = Operation.create(getName(startEvent), subprocesses);
        return BPMNParserResult.of(operation, subprocesses, allElements);
    }

    // builds some objects to start recursion
    private static List<Element> getElements(Node subprocess, List<Node> associations, List<Node> textAnnotations) {
        Node startEvent = getChildElementsByTagName(subprocess, "bpmn:startEvent").get(0); // get start event of process
        List<Node> boundaryEvents = getChildElementsByTagName(subprocess, "bpmn:boundaryEvent"); // get all boundary events
        List<Node> dataObjectReferences = getChildElementsByTagName(subprocess, "bpmn:dataObjectReference"); // get all data object references
        String firstElementId = new ObjectId().toHexString(); // set id for first element to start recursion with

        // call recursive function to get all elements for subprocess
        List<Element> graph = buildElementGraph(
                firstElementId,
                startEvent,
                subprocess,
                associations,
                textAnnotations,
                boundaryEvents,
                dataObjectReferences,
                new ArrayList<>(),
                new ArrayList<>(),
                new LinkedList<>(),
                false
        );

        // recursive nature of buildElementGraph adds elements in reverse order
        // for better readability the list order is reversed again to get the correct order
        // but for the logic it doesn't matter
        Collections.reverse(graph);
        return graph;
    }


    // recursive function to get all elements for a subprocess
    // uses depth first search
    private static List<Element> buildElementGraph(
            String idForNewElement,
            Node rootNode,
            Node subprocess,
            List<Node> associations,
            List<Node> textAnnotations,
            List<Node> boundaryEvents,
            List<Node> dataObjectReferences,
            List<Element> elements,
            List<String> visitedIds, // tracks what elements are already in the graph
            List<String> pathIds, // tracks what branch (path) of the graph we are currently in
            boolean shouldPop // signals if a path is finished and the id should be removed from the pathIds
    ) {
        String rootElementId = getId(rootNode);
        List<String> currentPathIds = new ArrayList<>(pathIds); // copy pathIds to not mutate the original

        boolean elementAlreadyInGraph = visitedIds.contains(rootElementId);
        if (elementAlreadyInGraph) { // if element already in graph, return elements without adding anything
            return elements;
        }

        visitedIds.add(rootElementId); // add element to visitedIds to not add it again

        Type type = Type.fromNode(rootNode);

        if (type == Type.END_EVENT) { // end is reached. add element and return
            elements.add(buildEndEvent(idForNewElement, rootNode, type));
            return elements;
        }

        List<String> outgoingIds = getOutgoingIds(rootNode); // get id of outgoing flow elements
        List<String> boundaryEventIds = getBoundaryEventIds(getId(rootNode), boundaryEvents);

        List<Child> children = new ArrayList<>();
        boolean addPaths = false;

        if (type == Type.XOR || type == Type.PARALLEL) { // if XOR or PARALLEL, check if a path id should be removed
            if (outgoingIds.size() == 1) { // if it is an ending XOR or PARALLEL
                if (shouldPop) { // and the path id should be removed
                    currentPathIds.remove(currentPathIds.size() - 1); // remove last path id from path id stack
                }
            } else {
                addPaths = true; // if there are multiple outgoing flows, add those paths to the pathIds
            }
        }

        boolean noEventPaths = hasNoEventPaths(boundaryEventIds, boundaryEvents);
        boolean isNotEndingXOR = type != Type.XOR || outgoingIds.size() <= 1;
        boolean isNotEndingParallel = type != Type.PARALLEL || outgoingIds.size() <= 1;


        // if a boundary event exists for this element, the next element to be looked at will be a closing gateway
        // if you remove the path from the id's then, there will be an error because the path is not added to the id's before
        // this is because there is a direkt flow going to the gateway
        // to prevent this shouldPop is set to false
        // the pathId is only added for the elements following the boundary event
        // if the algorithm reaches the closing gateway via the event path, the path is not removed because the gateway already has been reached
        // for an example look at "Haut scharf durchtrennen" in the Cochlea bpmn. the pathIds list for "Haut scharf durchtrennen" will be []
        // if you would pop then at the following gateway, you would pop from the empty list
        // this would not be a problem there, but if the tasks would be in a path after another gateway, the gateway path would be removed instead of
        // the event path id
        // todo make this whole thing less complicated if possible
        shouldPop = noEventPaths && isNotEndingXOR && isNotEndingParallel;

        for (String outgoingId : outgoingIds) { // go deeper into the graph for all direct child elements
            addChildToListAndGoDeeper(
                    new ObjectId().toHexString(),
                    subprocess,
                    associations,
                    textAnnotations,
                    boundaryEvents,
                    dataObjectReferences,
                    elements,
                    visitedIds,
                    currentPathIds,
                    outgoingId,
                    children,
                    false,
                    addPaths,
                    shouldPop
            );
        }

        List<String> internalBoundaryEventIds = new ArrayList<>();
        for (String boundaryEventId : boundaryEventIds) { // go deeper into the graph for all boundary events
            String internalEventId = new ObjectId().toHexString();
            internalBoundaryEventIds.add(internalEventId);
            addChildToListAndGoDeeper(
                    internalEventId,
                    subprocess,
                    associations,
                    textAnnotations,
                    boundaryEvents,
                    dataObjectReferences,
                    elements,
                    visitedIds,
                    currentPathIds,
                    boundaryEventId,
                    children,
                    true,
                    true,
                    shouldPop
            );
        }

        // special rule: if a subprocess is within a subprocess, the subprocess should be added as a heading...
        if (type == Type.SUBPROCESS) {
            // get all subprocess elements recursively
            List<Element> subprocessElements = getElements(rootNode, associations, textAnnotations);

            // add the path id for the current path to the elements
            subprocessElements = subprocessElements.stream().map(e -> {
                        List<String> p = new ArrayList<>(currentPathIds);
                        p.addAll(e.getPathIds());
                        return e.withPathIds(p);
                    })
                    .filter(e -> e.getType() != Type.START_EVENT && e.getType() != Type.END_EVENT).toList(); // remove the start and end event of the subprocess

            // let the last element of the subprocess point to the first element after the subprocess
            if (!subprocessElements.isEmpty()) {
                Element lastElement = subprocessElements.get(subprocessElements.size() - 1);
                subprocessElements = subprocessElements.stream()
                        .map(e -> {
                            if (e.equals(lastElement)) {
                                return e.withChildren(children);
                            } else {
                                return e;
                            }
                        })
                        .toList();
            }

            // build the subprocess as heading
            Element subElement = Element.create(
                    idForNewElement,
                    List.of(),
                    Type.TASK,
                    InputType.HEADING,
                    getName(rootNode),
                    currentPathIds,
                    List.of(),
                    List.of(new Child(subprocessElements.get(0).getId(), "")), // point to the first element of the filtered process elements
                    null,
                    null
            );

            // reverse the subprocess elements to get the correct order
            subprocessElements = subprocessElements.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                Collections.reverse(list);
                                return list;
                            }
                    ));
            // add all subprocess elements to the elements list
            elements.addAll(subprocessElements);
            elements.add(subElement); // add the subprocess heading
            return elements;
        }

        String textAnnotation = findTextAnnotation(associations, textAnnotations, rootElementId);

        String name;
        String metaIdentifier = null;
        List<Role> roles = null;
        if (type == Type.XOR || type == Type.BOUNDARY_EVENT) {
            name = textAnnotation; // text annotation is the name for XOR and boundary events
        } else {
            // else name is the name of the element
            // and metaIdentifier and roles are extracted from the text annotation
            name = getName(rootNode);
            metaIdentifier = getMetaIdentifier(textAnnotation);
            roles = getRoles(textAnnotation);
        }

        InputType inputType = type == Type.TASK ? getInputType(name) : InputType.NONE;

        String circulatingTriggerId = null;
        if (roles != null && !roles.contains(Role.CIRCULATING)) {
            circulatingTriggerId = getElementIdToTriggerFromChildren(elements, children); // see if a circulating path is following and add id to trigger it later
        }

        // build an element from all the gathered information and add it to the elements list
        elements.add(Element.create(
                idForNewElement,
                roles,
                type,
                inputType,
                name,
                currentPathIds,
                internalBoundaryEventIds,
                children,
                metaIdentifier,
                circulatingTriggerId
        ));
        return elements;
    }

    private static Element buildEndEvent(String idForNewElement, Node rootNode, Type type) {
        String name = getName(rootNode);
        InputType inputType = getInputType(name);

        Element e = Element.create(
                idForNewElement,
                List.of(),
                type,
                inputType,
                name,
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );
        return e;
    }

    private static String getElementIdToTriggerFromChildren(List<Element> elements, List<Child> children) {
        for (Child child : children) {
            Element childElement = elements.stream()
                    .filter(e -> e.getId().equals(child.getId()))
                    .findFirst()
                    .orElse(null);
            if (childElement != null && childElement.getType() == Type.PARALLEL) {
                for (Child grandChild : childElement.getChildren()) {
                    Element grandChildElement = elements.stream()
                            .filter(e -> e.getId().equals(grandChild.getId()))
                            .findFirst()
                            .orElse(null);
                    if (grandChildElement != null && grandChildElement.getRoles() != null &&
                        grandChildElement.getRoles().contains(Role.CIRCULATING)) {
                        return grandChild.getId();
                    }
                }
            }
        }
        return null;
    }

    private static List<Role> getRoles(String textAnnotation) {
        Pattern pattern = Pattern.compile("roles:(.*)");
        Matcher matcher = pattern.matcher(textAnnotation);

        if (matcher.find()) {
            return Arrays.stream(matcher.group(1).split(","))
                    .filter(role -> !role.isEmpty())
                    .map(Role::fromString)
                    .toList();
        }

        return null;
    }

    private static String getMetaIdentifier(String textAnnotation) {
        Pattern pattern = Pattern.compile("meta:(.*)");
        Matcher matcher = pattern.matcher(textAnnotation);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static boolean hasNoEventPaths(List<String> boundaryEventIds, List<Node> boundaryEvents) {
        return boundaryEvents.stream()
                .filter(event -> boundaryEventIds.contains(getId(event)))
                .allMatch(event -> getChildElementsByTagName(event, "bpmn:outgoing").isEmpty());
    }

    private static InputType getInputType(String name) {
        if (name == null || name.isEmpty()) {
            return InputType.NONE;
        }

        if (name.contains("eingeben")) {
            return InputType.TEXT;
        }

        if (name.contains("Datei hochladen")) {
            return InputType.UPLOAD;
        }

        if (name.contains("Foto aufnehmen")) {
            return InputType.PICTURE;
        }

        if (name.contains("heading:")) {
            return InputType.HEADING;
        }

        return InputType.CHECKBOX;
    }

    private static List<String> getBoundaryEventIds(String elementId, List<Node> boundaryEvents) {
        return boundaryEvents.stream()
                .filter(event -> event.getAttributes()
                        .getNamedItem("attachedToRef")
                        .getNodeValue()
                        .equals(elementId))
                .map(event -> event.getAttributes().getNamedItem("id").getNodeValue())
                .toList();
    }

    // this function finds the next element to look at and skips the flow that connects to it
    private static void addChildToListAndGoDeeper(
            String elementId,
            Node subprocess,
            List<Node> associations,
            List<Node> textAnnotations,
            List<Node> boundaryEvents,
            List<Node> dataObjectReferences,
            List<Element> elements,
            List<String> visitedIds,
            List<String> pathIds,
            String outgoingId,
            List<Child> children,
            boolean nextIsEvent,
            boolean addPaths,
            boolean shouldPop
    ) {
        ArrayList<String> relevantIds = new ArrayList<>(pathIds);
        String nextElementId;
        String pathDescription;

        if (nextIsEvent) {
            nextElementId = outgoingId;
            pathDescription = "";

        } else {
            Node flowNode = getChildElementById(subprocess, outgoingId);
            pathDescription = getName(flowNode);
            nextElementId = flowNode.getAttributes().getNamedItem("targetRef").getNodeValue();
        }

        if (addPaths) {
            relevantIds.add(elementId);
        }

        children.add(new Child(elementId, pathDescription));
        Node nextElement = getChildElementById(subprocess, nextElementId);
        buildElementGraph(
                elementId,
                nextElement,
                subprocess,
                associations,
                textAnnotations,
                boundaryEvents,
                dataObjectReferences,
                elements,
                visitedIds,
                relevantIds,
                shouldPop
        );
    }

    private static List<String> getOutgoingIds(Node rootNode) {
        return getChildElementsByTagName(rootNode, "bpmn:outgoing").stream()
                .map(Node::getTextContent)
                .toList();
    }

    private static String findTextAnnotation(List<Node> associations, List<Node> textAnnotations, String elementId) {
        return associations.stream()
                .filter(association -> association.getAttributes()
                        .getNamedItem("sourceRef")
                        .getNodeValue()
                        .equals(elementId))
                .map(association -> association.getAttributes().getNamedItem("targetRef").getNodeValue())
                .findFirst().flatMap(id -> textAnnotations.stream()
                        .filter(annotation -> annotation.getAttributes()
                                .getNamedItem("id")
                                .getNodeValue()
                                .equals(id))
                        .findFirst()
                )
                .flatMap(textAssociation -> getChildElementByTagName(textAssociation, "bpmn:text")
                        .map(Node::getTextContent))
                .orElse("");
    }

    private static Optional<Node> getChildElementByTagName(Node textAssociation, String tagName) {
        return getChildElementsByTagName(textAssociation, tagName).stream().findFirst();
    }

    private static List<Node> getChildElementsByTagName(Node parentNode, String tagName) {
        List<Node> childElements = new ArrayList<>();

        if (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList childNodes = parentNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(tagName)) {
                    childElements.add(childNode);
                }
            }
        }

        return childElements;
    }

    private static Node getChildElementById(Node parentNode, String id) {
        if (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList childNodes = parentNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                NamedNodeMap attributes = childNode.getAttributes();
                if (childNode.getNodeType() == Node.ELEMENT_NODE && attributes != null &&
                    attributes.getNamedItem("id") != null && attributes.getNamedItem(
                        "id").getNodeValue().equals(id)) {
                    return childNode;
                }
            }
        }

        return null;
    }


    private static String getName(Node node) {
        Node name = node.getAttributes().getNamedItem("name");
        return name != null ? name.getNodeValue() : "";
    }

    private static String getId(Node doc) {
        return doc.getAttributes().getNamedItem("id").getNodeValue();
    }

    private static List<Node> getElementsByTagName(Document doc, String s) {
        List<Node> nodes = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName(s);
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

}
