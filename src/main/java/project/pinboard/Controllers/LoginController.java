package project.pinboard.Controllers;

import project.pinboard.Pinboard.Models.User.AdminUser;
import project.pinboard.Pinboard.Models.User.UserLoginRequest;
import project.pinboard.Pinboard.Repository.AdminUserRepo;
import project.pinboard.Services.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

@Controller
@RequestMapping("login")
public class LoginController {

    @Autowired private TokenManager tokenManager;
    @Autowired private AdminUserRepo adminUserRepo;

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody UserLoginRequest login) {
        try {
            if (login.getUsername() == null || login.getPassword() == null)
                return new ResponseEntity<>("Empty username or password", HttpStatus.UNAUTHORIZED);

            AdminUser user = adminUserRepo.findUser(login.getUsername(), login.getPassword());

            if (user == null)
                return new ResponseEntity<>("Wrong username or password", HttpStatus.UNAUTHORIZED);

            String token = tokenManager.produce(login.getUsername());

            HashMap<String, Object> response = new HashMap<>();
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Server exception", HttpStatus.BAD_REQUEST);
        }
    }
}