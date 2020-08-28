package com.codecool.krk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class GuestBook implements HttpHandler {

    private static final List<UserPost> posts = new ArrayList<>();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String method = httpExchange.getRequestMethod();

        if(method.equals("POST")){
            submitPost(httpExchange);
        }
        showGuestBook(httpExchange);
    }

    private void submitPost(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String formData = br.readLine();
        Map<String,String> inputs = parseFormData(formData);
        UserPost userPost = createUserPost(inputs);
        posts.add(userPost);
    }

    private UserPost createUserPost(Map<String, String> inputs) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String strDate = formatter.format(date);
        return new UserPost(inputs.get("user-name"), inputs.get("user-message"), strDate);
    }

    private void showGuestBook(HttpExchange httpExchange) throws IOException {
        String response = "";
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/guestBookTemplate.twig");
        JtwigModel model = JtwigModel.newModel();
        model.with("posts", posts);
        response = template.render(model);

        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Form data is sent as a urlencoded string. Thus we have to parse this string to get data that we want.
     * See: https://en.wikipedia.org/wiki/POST_(HTTP)
     */
    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            // We have to decode the value because it's urlencoded. see: https://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }
        return map;
    }

}
