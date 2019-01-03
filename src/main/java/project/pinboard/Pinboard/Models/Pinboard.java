package project.pinboard.Pinboard.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pinboard {

    @Id
    private String id;
    private String name;
    private String adminuser;
    private List<String> usernames;

    public Pinboard(String name, String adminuser) {
        this.name = name;
        this.adminuser = adminuser;
        this.usernames = new ArrayList<>();
        this.usernames.add(adminuser);
    }

    public Pinboard(String name, String adminuser, List<String> usernames) {
        this.name = name;
        this.adminuser = adminuser;
        this.usernames = usernames;
    }
}
