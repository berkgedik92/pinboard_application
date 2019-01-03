package project.pinboard.Controllers;

import project.pinboard.Pinboard.Models.User.AdminUser;
import project.pinboard.Pinboard.Repository.AdminUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("tokenval")
public class ValidatorController {

    @Autowired
    private AdminUserRepo adminUserRepo;

    @ModelAttribute("userdata")
    public AdminUser getUserdata(HttpServletRequest request) {
        return (AdminUser) request.getAttribute("userdata");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> validate(@ModelAttribute("userdata") AdminUser userdata) {
        return new ResponseEntity<>(userdata, HttpStatus.OK);
    }
}
