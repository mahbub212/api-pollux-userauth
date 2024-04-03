package id.co.msiglife.pollux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
public class ApiPolluxUserauthApplication implements CommandLineRunner {

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) {
		SpringApplication.run(ApiPolluxUserauthApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("===================================");
		System.out.println("Spring Boot application is running! Please be aware your active profile");
		System.out.println("Active profile: " + activeProfile);
		System.out.println("DataSource URL: " + dataSource.getConnection().getMetaData().getURL());
		System.out.println("===================================");
	}
}
