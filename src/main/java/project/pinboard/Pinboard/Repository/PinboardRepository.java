package project.pinboard.Pinboard.Repository;

import project.pinboard.Pinboard.Models.Pinboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PinboardRepository extends MongoRepository<Pinboard,String> {

    Optional<Pinboard> findById(String id);
    void removeById(String id);

    @Query("{'usernames' : ?0 }")
    List<Pinboard> findAllPinboardOfUser(String username);
}
