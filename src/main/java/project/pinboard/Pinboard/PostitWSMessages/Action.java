package project.pinboard.Pinboard.PostitWSMessages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Action {

    private String noteID;
    private Object obj;
    private ActionType actionType;
    private String changerUser;
}
