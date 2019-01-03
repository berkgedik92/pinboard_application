package project.pinboard.Pinboard.PostitWSMessages;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.pinboard.Pinboard.Models.NoteFile;
import project.pinboard.Pinboard.Models.NotePicture;
import project.pinboard.Pinboard.Models.NoteText;
import project.pinboard.Pinboard.Models.Postit;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostitEdit {

    private List<NoteText> texts;
    private List<NotePicture> pictures;
    private List<NoteFile> files;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yy   HH:mm", timezone="Europe/Istanbul")
    private Date date;

    public PostitEdit(Postit postit) {
        texts = postit.getTexts();
        pictures = postit.getPictures();
        files = postit.getFiles();
        date = postit.getDate();
    }
}
