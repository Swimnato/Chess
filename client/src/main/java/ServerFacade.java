import java.io.IOException;
import java.net.*;

public class ServerFacade {
    String ip;
    int port;
    String linkAndPort;

    public ServerFacade(int port, String ip) {
        this.ip = ip;
        this.port = port;
        linkAndPort = ip + ':' + port;
    }

    public ServerFacade() {
        ip = "127.0.0.1";
        port = 8080;
        linkAndPort = "http://" + ip + ':' + port;
    }

    private String makeRequest(String path, String type) throws IOException, URISyntaxException {
        URL url = (new URI(linkAndPort + path)).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(type);
        conn.addRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.connect();
        var inputStream = conn.getInputStream();
        StringBuilder output = new StringBuilder();
        while (inputStream.available() > 0)
            output.append((char) inputStream.read());
        return output.toString();
    }


    public boolean testServer() throws IOException, URISyntaxException {
        return !makeRequest("", "GET").isEmpty();
    }
}
