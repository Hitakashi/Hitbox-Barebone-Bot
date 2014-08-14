package com.hitakashi;
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author Alex
 *
 * This shouldn't be used right out of the box. It's just to show you HOW to connect to websockets. 
 *
 */
public class Main extends WebSocketClient {

    public static WebSocketClient client;
    public String authToken;

    public Main(URI serverURI) throws URISyntaxException {
        super(serverURI);
    }

    public static void main(String[] args) throws Exception {
        // Get the auth code.
        String fullAuth = readUrl("http://54.235.51.79/socket.io/1/");
        String auth = fullAuth.substring(0, fullAuth.indexOf(":"));
        Main.client = new Main(new URI("ws://54.235.51.79/socket.io/1/websocket/" + auth));

        client.connectBlocking();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        try {
            setHitboxAuth();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        String joinChannel = "5:::{\"name\":\"message\",\"args\":[{\"method\":\"joinChannel\",\"params\":{\"channel\":\"hitakashi\",\"name\":\"Hitakashi\",\"token\":\"" + authToken + "\",\"isAdmin\":true}}]}";
        client.send(joinChannel);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Message: " + message);
        if (message.equals("2::")) {
            client.send("2::");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Close Connection. Code: " + code + " Reason: " + reason + " Remote: " + remote);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println(ex);
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();

            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    //    }
    private void setHitboxAuth() throws Exception {

        String url = "http://api.hitbox.tv/auth/token/";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String urlParameters = "login=USERNAME&pass=PASSWORD";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        JsonParser parser = new JsonParser();
        Object objjson = parser.parse(response.toString());
        JsonObject mainBot = (JsonObject) objjson;
        authToken = mainBot.get("authToken").getAsString();
        System.out.println(authToken);

    }

}
