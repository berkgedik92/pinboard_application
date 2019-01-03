package project.pinboard.Pinboard.PostitWSMessages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.pinboard.Pinboard.Models.Postit;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostitPlace {

    private int left;
    private int top;

    public PostitPlace(Postit postit) {
        left = postit.getLeft();
        top = postit.getTop();
    }
}
