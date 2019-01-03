package project.pinboard.Pinboard.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoteText {

    private int left;
    private int top;
    private int width;
    private int height;
    private String text;
}
