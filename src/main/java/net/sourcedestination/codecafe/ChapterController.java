package net.sourcedestination.codecafe;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
public class ChapterController {
    @GetMapping("/chapters/{chapterId}")
    public String viewExercise(Map<String, Object> model,
                               @PathVariable("chapterId") String chapterId,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        var username = request.getUserPrincipal().getName();

        return "chapters/"+chapterId+".html";
    }
}
