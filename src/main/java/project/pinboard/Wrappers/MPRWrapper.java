package project.pinboard.Wrappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import project.pinboard.Services.FileOperations;
import javafx.util.Pair;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MPRWrapper {

    private final MultipartHttpServletRequest request;
    private final FileOperations fileOperations;

    public MPRWrapper(MultipartHttpServletRequest request, FileOperations fileOperations) {
        this.request = request;
        this.fileOperations = fileOperations;
    }

    public <T> T getData(String key, Class<T> classType) throws IOException  {
        return new ObjectMapper().readValue(request.getParameter(key), classType);
    }

    public String getString(String key) {
        return request.getParameter(key);
    }

    public Boolean getBoolean(String key) {
        return Boolean.valueOf(request.getParameter(key));
    }

    public Integer getInteger(String key) {
        return new Integer(request.getParameter(key));
    }

    public JSONObject getJSONObject(String key) {
        try {
            return new JSONObject(request.getParameter(key));
        }
        catch(Exception e) {
            return null;
        }
    }

    /*Pairin ilk elemani MultipartHttpServletRequest'teki ad, ikincisi kaydederken
    kullanilmak istenen ad*/
    public void saveFiles(String folderName, List<Pair<String, String>> fileNames) throws Exception {

        for (Pair<String, String> current : fileNames) {
            String sourceName = current.getKey();
            String targetName = current.getValue();
            byte[] data = Objects.requireNonNull(request.getFile(sourceName)).getBytes();
            fileOperations.saveBinaryFile(data, targetName, folderName);
        }
    }
}
