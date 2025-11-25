import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class GoogleJavaFormatter {
    private static final Logger logger = Logger.getLogger(GoogleJavaFormatter.class.getName());
    private static final String JAR_NAME = "google-java-format.jar";

    public static void main(String[] args) throws Exception {

        String version = args[0];
        logger.info("running google java format >>> ");
        logger.info("version >>> " + version);
        boolean useAosp = Boolean.parseBoolean(args[1]);
        String[] folders = args[2].split("\\s+");

        // 1. Download jar dynamically
        downloadFormatterJar(version);

        // 2. Format all Java files
        formatFiles(folders, useAosp);

        //commit these changes


    }

    private static void formatFiles(String[] folders, boolean useAosp) throws Exception {
        for (String folder : folders) {
            Path root = Paths.get(folder);
            if (!Files.exists(root)) {
                logger.info("Skipping missing path: " + folder);
                continue;
            }

            logger.info("Formatting Java files in folder: " + folder);
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(f -> f.toString().endsWith(".java"))
                        .forEach(f -> formatFile(f, useAosp));
            }
        }
    }

    private static void downloadFormatterJar(String version)
            throws IOException, InterruptedException {
        Path jarPath = Paths.get(JAR_NAME);

        // Only download if not already present
        if (Files.exists(jarPath)) {
            logger.info("google-java-format already downloaded");
            return;
        }

        String downloadUrl =
                String.format(
                        "https://github.com/google/google-java-format/releases/download/v%s/google-java-format-%s-all-deps.jar",
                        version, version);

        logger.info("Downloading google-java-format version " + version);
        /*try (InputStream in = new URL(downloadUrl).openStream()) {
            Files.copy(in, jarPath);
        }*/

        downloadFile(downloadUrl);
    }

    public static void downloadFile(String urlString)
            throws IOException {
        URI uri = URI.create(urlString);
        HttpURLConnection connection = getHttpURLConnection(uri);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, Paths.get(JAR_NAME), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection getHttpURLConnection (URI uri)
            throws IOException {
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set connection and read timeouts to prevent infinite blocking
        connection.setConnectTimeout(5000); // 5 seconds
        connection.setReadTimeout(10000);   // 10 seconds

        // Ensure the connection is successful (HTTP 200 OK)
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned non-OK response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
        return connection;
    }

    private static void formatFile(Path file, boolean aosp) {
        try {
            List<String> cmd = List.of("java", "-jar", JAR_NAME, aosp ? "--aosp" : "", "--replace", file.toString());
            executeCommand(cmd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.inheritIO().start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("command execution failed with exit code " + exitCode);
        } else {
            logger.info("command executed successfully. " + command);
        }
    }

}
