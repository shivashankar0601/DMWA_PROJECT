package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication myApplication = new SpringApplication(ProjectApplication.class);
		Properties prop = new Properties();
		prop.setProperty("spring.main.banner-mode","off");
		prop.setProperty("logging.pattern.console","");
		myApplication.setDefaultProperties(prop);
		myApplication.run(args);


//		SpringApplication.run(ProjectApplication.class, args); // by default we will get all the logs from the spring boot, so i am disabling them
		Main.main(args); // uncommenting this line will run the application with both server and the db.
	}

}
