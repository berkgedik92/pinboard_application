package project.pinboard.Pinboard.RestController;

import project.pinboard.Pinboard.Models.User.AdminUser;
import project.pinboard.Pinboard.Repository.AdminUserRepo;
import project.pinboard.Pinboard.Models.Pinboard;
import project.pinboard.Pinboard.Models.Postit;
import project.pinboard.Pinboard.Repository.PinboardRepository;
import project.pinboard.Pinboard.Repository.PostitRepository;
import project.pinboard.Pinboard.PostitWSMessages.Action;
import project.pinboard.Pinboard.PostitWSMessages.ActionType;
import project.pinboard.Pinboard.PostitWSMessages.PostitEdit;
import project.pinboard.Pinboard.PostitWSMessages.PostitPlace;
import project.pinboard.Pinboard.Websocket.PinboardWSService;
import project.pinboard.Services.DateManager;
import project.pinboard.Services.FileOperations;
import project.pinboard.Wrappers.MPRWrapper;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("api/pinboard")
public class PinboardRESTController {

    @Autowired private PostitRepository postitRepository;
    @Autowired private AdminUserRepo adminUserRepo;
    @Autowired private DateManager dateManager;
    @Autowired private PinboardWSService pinboardWS;
    @Autowired private FileOperations fileOperations;
    @Autowired private PinboardRepository pinboardRepository;

    @Value("${pinboardFolder}")
    private String pinboardFolder;

    // Thanks to that function, other functions can access to userdata by "@ModelAttribute("userdata")"
    @ModelAttribute("userdata")
    public AdminUser getUserdata(HttpServletRequest request) {
        return (AdminUser) request.getAttribute("userdata");
    }

    @RequestMapping(value = "/userdata", method = RequestMethod.POST)
    public ResponseEntity<Object> getUserData(@RequestBody String username) {
        AdminUser user =  adminUserRepo.findUser(username);

        if (user == null)
            return new ResponseEntity<>("User cannot be found!", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @RequestMapping(value = "/usernames", method = RequestMethod.GET)
    public List<AdminUser> getUserNames() {
        return adminUserRepo.findAll();
    }

    @RequestMapping(value = "/getpinboards", method = RequestMethod.GET)
    public List<Pinboard> getPinboards(@ModelAttribute("userdata") AdminUser userdata) {
        return pinboardRepository.findAllPinboardOfUser(userdata.getUsername());
    }

    @RequestMapping(value = "/createpinboard", method = RequestMethod.GET)
    public Pinboard createPinboard(@ModelAttribute("userdata") AdminUser userdata) {
        Pinboard pinboard = new Pinboard("New Board", userdata.getUsername());
        pinboard = pinboardRepository.save(pinboard);
        return pinboard;
    }

    @RequestMapping(value = "/changepinboardname", method = RequestMethod.POST)
    public ResponseEntity<Object> changePinboardName(@ModelAttribute("userdata") AdminUser userdata, @RequestBody String message) {

        JSONObject json;
        String id, name;

        try {
            json = new JSONObject(message);
            id = json.getString("id");
            name = json.getString("name");
            if (id == null || name == null)
                throw new Exception();
        }catch (Exception e) {
            return new ResponseEntity<>("Cannot parse the message", HttpStatus.BAD_REQUEST);
        }

        Pinboard pinboard = pinboardRepository.findById(id).orElse(null);
        if (pinboard == null)
            return new ResponseEntity<>("There is no such pinboard", HttpStatus.BAD_REQUEST);

        if (!pinboard.getAdminuser().equals(userdata.getUsername()))
            return new ResponseEntity<>("You do not have the right to change the title of this pinboard", HttpStatus.BAD_REQUEST);

        pinboard.setName(name);
        pinboardRepository.save(pinboard);
        return new ResponseEntity<>(pinboard, HttpStatus.OK);
    }

    @RequestMapping(value = "/changeusers", method = RequestMethod.POST)
    public ResponseEntity<Object> changeUsers(@ModelAttribute("userdata") AdminUser userdata, @RequestBody String message) {
        JSONObject json;
        String id;
        JSONArray userArr;

        try {
            json = new JSONObject(message);
            id = json.getString("id");
            String users = json.getString("users");
            if (id == null || users == null)
                throw new Exception();
            userArr = new JSONArray(users);
        }catch (Exception e) {
            return new ResponseEntity<>("Cannot parse the message", HttpStatus.BAD_REQUEST);
        }

        Pinboard pinboard = pinboardRepository.findById(id).orElse(null);
        if (pinboard == null)
            return new ResponseEntity<>("There is no such pinboard", HttpStatus.BAD_REQUEST);

        if (!pinboard.getAdminuser().equals(userdata.getUsername()))
            return new ResponseEntity<>("You do not have the right to change on this pinboard", HttpStatus.BAD_REQUEST);

        List<String> realUserList = new ArrayList<>();
        boolean isAdminChanged = true;

        try {
            String admin = pinboard.getAdminuser();
            for (int i=0; i<userArr.length(); i++) {
                String currentUser = userArr.getString(i);
                if (currentUser.equals(admin))
                    isAdminChanged = false;
                realUserList.add(currentUser);
            }
        }catch (Exception e) {
            return new ResponseEntity<>("Cannot parse users", HttpStatus.BAD_REQUEST);
        }

        pinboard.setUsernames(realUserList);
        if (isAdminChanged) {
            if (realUserList.size() == 0) {
                deletePinboard(userdata, id);
            }
            else {
                pinboard.setAdminuser(realUserList.get(0));
                pinboardRepository.save(pinboard);
            }
        }
        else {
            pinboardRepository.save(pinboard);
        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/deletepinboard", method = RequestMethod.POST)
    public ResponseEntity<Object> deletePinboard(@ModelAttribute("userdata") AdminUser userdata, @RequestBody String id) {

        Pinboard pinboard = pinboardRepository.findById(id).orElse(null);
        if (pinboard == null)
            return new ResponseEntity<>("There is no such pinboard", HttpStatus.BAD_REQUEST);

        if (!pinboard.getAdminuser().equals(userdata.getUsername()))
            return new ResponseEntity<>("You do not have the right to remove this pinboard", HttpStatus.BAD_REQUEST);

        List<Postit> postits = postitRepository.findByPinboardID(id);
        for (Postit postit : postits)
            postitRepository.removeById(postit.getId());

        pinboardRepository.removeById(id);

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/load", method = RequestMethod.POST)
    public ResponseEntity<Object> load(@ModelAttribute("userdata") AdminUser userdata, @RequestBody String pinboardID) {

        //Check if this user has the right to access this pinboard
        Pinboard pinboard = pinboardRepository.findById(pinboardID).orElse(null);

        if (pinboard == null)
            return new ResponseEntity<>("Authorisation error", HttpStatus.UNAUTHORIZED);

        if (!pinboard.getUsernames().contains(userdata.getUsername()))
            return new ResponseEntity<>("Authorisation error", HttpStatus.UNAUTHORIZED);

        Map<String, Object> data = new HashMap<>();

        List<Postit> postits = postitRepository.findByPinboardID(pinboardID);

        for (Postit postit : postits)
            data.put(postit.getId(), postit);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    private ResponseEntity<Object> create(MPRWrapper wrapper,
                                     @ModelAttribute("userdata") AdminUser userdata, String pinboardID) {
        Postit postit;
        Date date = new Date();

        Integer pictureAmount = wrapper.getInteger("pictureAmount");
        Integer fileAmount = wrapper.getInteger("fileAmount");

        try {
            postit = wrapper.getData("json", Postit.class);
        }catch (IOException e) {
            return new ResponseEntity<>("Cannot parse the data", HttpStatus.BAD_REQUEST);
        }

        postit.setOwnerName(userdata.getUsername());
        postit.setDate(date);
        postit.setPinboardID(pinboardID);
        postit = postitRepository.save(postit);

        List<Pair<String, String>> files = new ArrayList<>();

        int oldPicAmount = 0;
        int oldFileAmount = 0;

        for (int i = 0; i < pictureAmount; i++) {
            String index = Integer.toString(i + oldPicAmount);
            String sourceName = "pic_" + index;
            String targetName = postit.getId() + "_pic_" + index;
            files.add(new Pair<>(sourceName, targetName));
        }

        for (int i = 0; i < fileAmount; i++) {
            String index = Integer.toString(i + oldFileAmount);
            String sourceName = "file_" + index;
            String targetName = postit.getId() + "_file_" + index;
            files.add(new Pair<>(sourceName, targetName));
        }

        try {
            wrapper.saveFiles(pinboardFolder, files);
        }
        catch (Exception exception) {
            postitRepository.removeById(postit.getId());
            return new ResponseEntity<>("Could not save the file " + exception.toString(), HttpStatus.BAD_REQUEST);
        }

        String userName = userdata.getUsername();

        Action action = new Action(postit.getId(), postit, ActionType.CREATE, userdata.getUsername());
        pinboardWS.SendData(action, userName, pinboardID);

        Map<String, Object> response = new HashMap<>();
        response.put("id", postit.getId());
        response.put("date", dateManager.fromDateToString(date));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<Object> edit(MPRWrapper wrapper,
                                   @ModelAttribute("userdata") AdminUser userdata) {

        Postit edit, postit;
        Date date = new Date();

        String id = wrapper.getString("id");
        Integer pictureAmount = wrapper.getInteger("pictureAmount");
        Integer fileAmount = wrapper.getInteger("fileAmount");

        try {
            postit = postitRepository.findById(id).orElse(null);

            if (postit == null)
                return new ResponseEntity<>("Cannot find postit with this id", HttpStatus.BAD_REQUEST);

            edit = wrapper.getData("json", Postit.class);
        }catch (IOException e) {
            return new ResponseEntity<>("Cannot parse the data", HttpStatus.BAD_REQUEST);
        }

        List<Pair<String, String>> files = new ArrayList<>();
        int oldPicAmount = postit.getPictures().size();
        int oldFileAmount = postit.getFiles().size();

        for (int i = 0; i < pictureAmount; i++) {
            String index = Integer.toString(i + oldPicAmount);
            String sourceName = "pic_" + index;
            String targetName = postit.getId() + "_pic_" + index;
            files.add(new Pair<>(sourceName, targetName));
        }

        for (int i = 0; i < fileAmount; i++) {
            String index = Integer.toString(i + oldFileAmount);
            String sourceName = "file_" + index;
            String targetName = postit.getId() + "_file_" + index;
            files.add(new Pair<>(sourceName, targetName));
        }

        postit.setPictures(edit.getPictures());
        postit.setTexts(edit.getTexts());
        postit.setFiles(edit.getFiles());
        postit.setDate(date);
        postit = postitRepository.save(postit);

        try {
            wrapper.saveFiles(pinboardFolder, files);
        }
        catch (Exception exception) {
            postitRepository.removeById(postit.getId());
            return new ResponseEntity<>("Could not save the file " + exception.toString(), HttpStatus.BAD_REQUEST);
        }

        String userName = userdata.getUsername();
        Action action = new Action(postit.getId(), new PostitEdit(postit), ActionType.EDIT, userdata.getUsername());

        pinboardWS.SendData(action, userName, postit.getPinboardID());

        Map<String, Object> response = new HashMap<>();
        response.put("id", postit.getId());
        response.put("date", dateManager.fromDateToString(date));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<Object> save(MultipartHttpServletRequest request,
                                  @ModelAttribute("userdata") AdminUser userdata) {

        MPRWrapper wrapper = new MPRWrapper(request, fileOperations);
        Boolean isEditing = wrapper.getBoolean("editmode");
        String pinboardID = wrapper.getString("pinboardID");

        //Check if user has the right to modify this pinboard
        Pinboard p = pinboardRepository.findById(pinboardID).orElse(null);
        if (!Objects.requireNonNull(p).getUsernames().contains(userdata.getUsername()))
            return new ResponseEntity<>("Authrorisation error", HttpStatus.UNAUTHORIZED);

        if (isEditing)
            return edit(wrapper, userdata);
        else
            return create(wrapper, userdata, pinboardID);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseEntity<Object> delete(@RequestBody String id,
                                    @ModelAttribute("userdata") AdminUser userdata) {

        Postit postit = postitRepository.findById(id).orElse(null);
        if (postit == null)
            return new ResponseEntity<>("Cannot find the postit in the database", HttpStatus.BAD_REQUEST);

        //Check if user has the right to modify this pinboard
        Pinboard p = pinboardRepository.findById(postit.getPinboardID()).orElse(null);
        if (!Objects.requireNonNull(p).getUsernames().contains(userdata.getUsername()))
            return new ResponseEntity<>("Cannot find the pinboard", HttpStatus.BAD_REQUEST);

        if (!userdata.getUsername().equals(postit.getOwnerName()))
            return new ResponseEntity<>("Cannot delete postit of other user", HttpStatus.UNAUTHORIZED);

        postitRepository.removeById(id);

        String userName = userdata.getUsername();
        Action action = new Action(postit.getId(), null, ActionType.DELETE, userdata.getUsername());

        pinboardWS.SendData(action, userName, postit.getPinboardID());

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/place", method = RequestMethod.POST)
    public ResponseEntity<Object> placer(@RequestBody String data,
                                    @ModelAttribute("userdata") AdminUser userdata) {

        String id;
        int left, top;

        try {
            JSONObject message = new JSONObject(data);
            id = message.getString("id");
            left = message.getInt("left");
            top = message.getInt("top");
        }catch (JSONException e) {
            return new ResponseEntity<>("Cannot parse the data", HttpStatus.BAD_REQUEST);
        }

        Postit postit = postitRepository.findById(id).orElse(null);
        if (postit == null)
            return new ResponseEntity<>("Cannot find the postit in the database", HttpStatus.BAD_REQUEST);

        //Check if user has the right to modify this pinboard
        Pinboard p = pinboardRepository.findById(postit.getPinboardID()).orElse(null);
        if (!Objects.requireNonNull(p).getUsernames().contains(userdata.getUsername()))
            return new ResponseEntity<>("Authorisation error", HttpStatus.UNAUTHORIZED);

        postit.setLeft(left);
        postit.setTop(top);
        postitRepository.save(postit);

        String userName = userdata.getUsername();
        Action action = new Action(postit.getId(), new PostitPlace(postit), ActionType.PLACE, userdata.getUsername());

        pinboardWS.SendData(action, userName, postit.getPinboardID());

        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
