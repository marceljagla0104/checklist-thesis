package de.reutlingen_university.checklist.operation.mongodb;

import de.reutlingen_university.checklist.operation.ImageRepo;
import de.reutlingen_university.checklist.operation.meta.Image;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ImageRepository implements ImageRepo {

    private final ImageMongoRepository repo;

    @Override
    public Flux<Image> saveAll(List<Image> images) {
        return repo.saveAll(images);
    }

    @Override
    public Flux<Image> findAllById(Set<String> imageIds) {
        return repo.findAllById(imageIds);
    }
}
