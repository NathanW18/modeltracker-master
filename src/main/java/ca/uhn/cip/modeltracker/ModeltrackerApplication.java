package ca.uhn.cip.modeltracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ModeltrackerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ModeltrackerApplication.class, args);
	}

    // Used when running as WAR
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ModeltrackerApplication.class);
    }

}
