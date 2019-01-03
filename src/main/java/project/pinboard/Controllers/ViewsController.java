package project.pinboard.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class ViewsController {

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLogin(@ModelAttribute("hostAddr") String hostAddr) {
        return ControllerUtils.getPage("pinboard/login.jsp", hostAddr, hostAddr + "/pinboard");
    }

    @RequestMapping(value = "pinboard", method = RequestMethod.GET)
    public ModelAndView getPinboardPage(@ModelAttribute("hostAddr") String hostAddr) {
        return ControllerUtils.getPage("pinboard/pinindex.jsp", hostAddr , "");
    }

    @RequestMapping(value = "pinboard2", method = RequestMethod.GET)
    public ModelAndView getPinboardPage(@ModelAttribute("hostAddr") String hostAddr, @RequestParam String id) {
        ModelAndView view = ControllerUtils.getPage("pinboard/index.jsp", hostAddr , "");
        view.addObject("pinboardID", id);
        return view;
    }
}