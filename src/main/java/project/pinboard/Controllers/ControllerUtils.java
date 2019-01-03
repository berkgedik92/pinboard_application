package project.pinboard.Controllers;

import org.springframework.web.servlet.ModelAndView;

public class ControllerUtils {

    public static ModelAndView getPage(String url, String hostAddr, String redirectFrom) {
        ModelAndView view = new ModelAndView(url);
        view.addObject("hostAddr", "../" + hostAddr);
        view.addObject("redirectURL", "../login");
        view.addObject("tokenGiver", "../login/token");
        view.addObject("tokenValidator", "../tokenval");
        view.addObject("redirectFrom", redirectFrom);
        return view;
    }
}
