package project.pinboard.Pinboard.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotePicture {

    private String url;
    private int left;
    private int top;
    private int width;
    private int height;
}
