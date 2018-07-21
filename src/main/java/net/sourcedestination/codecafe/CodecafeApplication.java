package net.sourcedestination.codecafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource("classpath:exercises.xml")
@SpringBootApplication
public class CodecafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodecafeApplication.class, args);
    }
}
