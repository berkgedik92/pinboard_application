package project.pinboard.Services;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FileOperations {

    public void DeleteFile(String folderName, String fileName) {
        File file = new File(folderName + File.separator + fileName);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public String fileNameWithPrefix(String folderName, String prefix) {
        File dir = new File(folderName);

        FilenameFilter filter = (dir1, name) -> name.indexOf(prefix) == 0;

        File[] fList = dir.listFiles(filter);

        return Objects.requireNonNull(fList).length > 0 ? fList[0].getName(): null;
    }

    //Returns a categoriesList containing all file names in a folder
    public List<String> listFiles(String folderName) {
        File dir = new File(folderName);
        File[] fList = dir.listFiles();
        List<String> result = new ArrayList<>();

        for (File aFList : Objects.requireNonNull(fList)) result.add(aFList.getName());

        return result;
    }

    //Returns the amount of files in a folder
    public int fileAmount(String folderName) {
        File dir = new File(folderName);
        File[] fList = dir.listFiles();
        return Objects.requireNonNull(fList).length;
    }

    public void saveTextFile(String data, String fileName, String folderName)
            throws FileNotFoundException, UnsupportedEncodingException {
        String fullFilePath = folderName + File.separator + fileName;
        PrintWriter writer = new PrintWriter(fullFilePath, "UTF-8");
        writer.print(data);
        writer.close();
    }

    public void saveBinaryFile(byte[] data, String fileName, String folderName)
            throws IOException {

        String fullFilePath = folderName + File.separator + fileName;
        File file = new File(fullFilePath);
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        stream.write(data);
        stream.close();
    }

    public String readTextFile(String fileName, String folderName)
            throws IOException {

        String fullFilePath = folderName + File.separator + fileName;
        StringBuilder builder = new StringBuilder();
        String line;

        FileInputStream fileInputStream = new FileInputStream(fullFilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        while ((line = bufferedReader.readLine()) != null)
            builder.append(line).append("\n");

        fileInputStream.close();
        inputStreamReader.close();
        bufferedReader.close();

        return builder.toString();
    }
}
