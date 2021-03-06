package app;

import app.biz.HandleBiz;
import app.dao.Handle;
import app.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.stream.Stream;

@SpringBootApplication
public class AppStarter extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(AppStarter.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }
}
