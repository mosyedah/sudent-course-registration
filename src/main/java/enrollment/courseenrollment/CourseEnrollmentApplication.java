package enrollment.courseenrollment;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import enrollment.courseenrollment.repository.dynamodb.TableInitialiser;
import enrollment.courseenrollment.repository.dynamodb.TablePopulator;

@SpringBootApplication
public class CourseEnrollmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseEnrollmentApplication.class, args);
    }
    
//    @Bean // no longer needed , will just show login page as cicd demo
//    CommandLineRunner init() {
//    	return args -> {
//    		TableInitialiser.Initialiser();
//    		TablePopulator.populateAll();
//    	};
//    }
}
