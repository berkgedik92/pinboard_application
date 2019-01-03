package project.pinboard.Pinboard.Models.User;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserRole {

    private Set<String> rights;

    public UserRole() {
        this.rights = new HashSet<>();
    }

    public void addRight(String URL) {
        rights.add(URL);
    }

    public boolean hasAccessRight(String URL) {
        URL = URL.substring(1);
        String[] urlParts = URL.split("/");
        StringBuilder builder = new StringBuilder();

        for (String urlPart : urlParts) {
            builder.append(urlPart);
            String current = builder.toString();
            if (rights.contains(current))
                return true;
            builder.append("/");
        }
        return false;
    }
}
