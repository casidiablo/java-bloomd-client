package bloomd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class DockerHelper {
    public static int randomPort() {
        return 9000 + new Random().nextInt(5000);
    }

    public static String startBloomdInDocker(int port) {
        return runCommand("docker run -d -p " + port + ":8673 saidimu/bloomd:v0.7.4");
    }

    public static String stopBloomdInDocker(String containerId) {
        return runCommand("docker stop " + containerId);
    }

    private static String runCommand(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            return stdInput.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
