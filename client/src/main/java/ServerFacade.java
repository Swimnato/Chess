import chess.datastructures.RegisterInfo;
import chess.datastructures.UsernameAuthTokenPair;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class ServerFacade {
    String ip;
    int port;
    String linkAndPort;
    int authToken;

    public ServerFacade(int port, String ip) {
        this.ip = ip;
        this.port = port;
        linkAndPort = "http://" + ip + ':' + port;
    }

    public ServerFacade() {
        ip = "127.0.0.1";
        port = 8080;
        linkAndPort = "http://" + ip + ':' + port;
    }

    private String makeRequest(String path, String type, String body) throws IOException, URISyntaxException, ErrorResponseException {
        URL url = (new URI(linkAndPort + path)).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(type);
        conn.setDoOutput(true);
        conn.addRequestProperty("Accept", "application/json");
        if (body != null) {
            try (OutputStream reqBody = conn.getOutputStream()) {
                reqBody.write(body.getBytes());
            }
        }
        conn.connect();
        var inputStream = conn.getInputStream();
        StringBuilder output = new StringBuilder();
        while (inputStream.available() > 0)
            output.append((char) inputStream.read());
        if (conn.getResponseCode() != 200) {
            throw new ErrorResponseException(output.toString());
        }
        return output.toString();
    }

    public String register(String username, String password, String email) throws InvalidSyntaxException, ErrorResponseException {
        RegisterInfo info = new RegisterInfo(username, password, email);
        String output;
        try {
            output = makeRequest("/user", "POST", new Gson().toJson(info));
            UsernameAuthTokenPair usernameAuthTokenPair = new Gson().fromJson(output, UsernameAuthTokenPair.class);
            authToken = usernameAuthTokenPair.getAuthToken();
        } catch (IOException | URISyntaxException e) {
            throw new InvalidSyntaxException(e.getMessage());
        }
        return "Registered Successfully!";
    }


    public boolean testServer() throws IOException, URISyntaxException, ErrorResponseException {
        return !makeRequest("", "GET", null).isEmpty();
    }
}
