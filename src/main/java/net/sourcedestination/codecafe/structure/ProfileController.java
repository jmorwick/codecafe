package net.sourcedestination.codecafe.structure;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class ProfileController {

    @GetMapping("/")
    public String viewExercise(Map<String, Object> model,
                               Principal principal) throws IOException {
        if(principal != null) {
            model.put("username", principal.getName());
        }

        return "splash.html";
    }


}
