package project.pinboard;

import project.pinboard.Pinboard.Models.User.AdminUser;
import project.pinboard.Pinboard.Repository.AdminUserRepo;
import project.pinboard.Pinboard.Models.User.UserRole;
import project.pinboard.Pinboard.Repository.PinboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@Configuration
@EnableScheduling
public class ServerApplication extends WebMvcConfigurerAdapter implements CommandLineRunner {

    @Value("${pinboardFolder}")
    private String pinboardFolder;

	@Autowired private AdminUserRepo adminUserRepo;
    @Autowired private PinboardRepository pinboardRepository;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pinboardpict/**").addResourceLocations("file:///" + pinboardFolder + "//");
    }

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Override
	public void run(String... args) {

        UserRole fullAdmin = new UserRole();
        fullAdmin.addRight("api");
        fullAdmin.addRight("tokenval");

        if (adminUserRepo.findUser("user1") == null)
            adminUserRepo.save(new AdminUser("user1", "user1", "User1", "pinboard/user1.gif", fullAdmin));

        if (adminUserRepo.findUser("user2") == null)
            adminUserRepo.save(new AdminUser("user2", "user2", "User2", "pinboard/user2.gif", fullAdmin));
    }
}
