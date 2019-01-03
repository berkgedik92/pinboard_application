package project.pinboard.Pinboard.Repository;

import project.pinboard.Pinboard.Models.Postit;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PostitRepository extends MongoRepository<Postit,String> {
    List<Postit> findAll();
    List<Postit> findByPinboardID(String pinboardID);
    void removeById(String id);
    Optional<Postit> findById(String id);
}
