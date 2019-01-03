package project.pinboard.Pinboard.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Postit {

    @Id private String id;

    private String pinboardID;
    private String ownerName;

    private int left;
    private int top;

    private List<NoteText> texts;
    private List<NotePicture> pictures;
    private List<NoteFile> files;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yy   HH:mm", timezone="Europe/Istanbul")
    private Date date;
}
