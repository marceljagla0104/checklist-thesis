package de.reutlingen_university.checklist.operation;

import de.reutlingen_university.checklist.ImageDTO;
import de.reutlingen_university.checklist.operation.meta.Image;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OperationTransformer {
    public static OperationDTO toDTO(OperationView operationView) {
        return new OperationDTO(
                operationView.getId(),
                operationView.getName(),
                operationView.getSubprocesses()
                        .stream()
                        .map(OperationTransformer::toDTO)
                        .collect(Collectors.toList()),
                operationView.getCreatedAt()
        );
    }

    private static SubprocessDTO toDTO(SubprocessView subprocess) {
        return new SubprocessDTO(
                subprocess.getId(),
                subprocess.getName(),
                subprocess.getElements()
                        .stream()
                        .map(e -> toDTO(e, subprocess.getElements()))
                        .collect(Collectors.toList()),
                subprocess.getCreatedAt()
        );
    }

    private static ElementDTO toDTO(ElementView elementView, List<ElementView> elements) {
        List<Role> roles = Optional.ofNullable(elementView.getRoles()).orElse(List.of());
        List<Image> images = Optional.ofNullable(elementView.getSurgeonImages()).orElse(List.of());
        List<Image> studentImages = Optional.ofNullable(elementView.getStudentImages()).orElse(List.of());

        int currentIndex = elements.indexOf(elementView);
        List<String> allChildIds = elements.subList(currentIndex + 1, elements.size())
                .stream()
                .map(ElementView::getId)
                .collect(Collectors.toList());
        
        return new ElementDTO(
                elementView.getId(),
                roles.stream().map(Role::name).collect(Collectors.toList()),
                elementView.getType().name(),
                elementView.getInputType().name(),
                elementView.getName(),
                elementView.getPathIds(),
                elementView.getEventIds(),
                elementView.getChildren()
                        .stream()
                        .map(OperationTransformer::toDTO)
                        .collect(Collectors.toList()),
                elementView.getInstruments(),
                elementView.getStudentInfo(),
                studentImages.stream().map(OperationTransformer::toDTO).collect(Collectors.toList()),
                elementView.getSurgeonInfo(),
                images.stream().map(OperationTransformer::toDTO).collect(Collectors.toList()),
                elementView.getCirculatingTriggerId(),
                allChildIds,
                elementView.getCreatedAt()
        );
    }

    private static ChildDTO toDTO(Child child) {
        return new ChildDTO(
                child.getId(),
                child.getPathDescription()
        );
    }

    private static ImageDTO toDTO(Image image) {
        return new ImageDTO(
                image.getUrl(),
                image.getCaption()
        );
    }
}
