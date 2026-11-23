package pathfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the PathFinder web server. Only the {@code pathfinder.api} package
 * uses Spring; the grid, search, and generation packages stay plain Java.
 */
@SpringBootApplication
public class PathFinderApplication {
    /** Creates the application; Spring calls this, not user code. */
    public PathFinderApplication() {
    }

    /**
     * Starts the embedded web server.
     *
     * @param args command-line arguments, passed on to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(PathFinderApplication.class, args);
    }
}