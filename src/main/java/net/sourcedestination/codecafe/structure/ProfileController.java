package net.sourcedestination.codecafe.structure;

import net.sourcedestination.codecafe.structure.exercises.ExerciseDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired
    private ApplicationContext appContext;

    @GetMapping("/")
    public String viewExercise(Map<String, Object> model,
                               Principal principal) throws IOException {
        if(principal != null) {
            model.put("username", principal.getName());
        }

        Map<String, ExerciseDefinition> exercises = appContext.getBeansOfType(ExerciseDefinition.class);
        model.put("exercises", // find id's of all exercises
                exercises.values().stream()
                    .map(ExerciseDefinition::getId)
                    .collect(Collectors.toList())
        );

        return "splash.html";
    }


}
