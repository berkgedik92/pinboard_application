package project.pinboard.Pinboard.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import project.pinboard.Pinboard.Models.User.AdminUser;
import java.util.List;
import java.util.Optional;

public interface AdminUserRepo extends MongoRepository<AdminUser,String>  {

    List<AdminUser> findAll();
    void deleteAll();

    Optional<AdminUser> findById(String id);

    @Query("{'username' : ?0}")
    AdminUser findUser(String username);

    @Query("{'username' : ?0, 'password' : ?1}")
    AdminUser findUser(String username, String password);
}
