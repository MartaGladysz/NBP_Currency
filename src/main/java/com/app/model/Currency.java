package com.app.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Currency {
    public static Map<String, LinkedList<BigDecimal>> allCurrencies(String url) {
        Map<String, LinkedList<BigDecimal>> map = new LinkedHashMap<>();

        try {
            JSONParser parser = new JSONParser();
            URL nbpURL = new URL(url);
            URLConnection nbpConnection = nbpURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(nbpConnection.getInputStream()));

            String line;
            StringBuilder sb = new StringBuilder();
            while((line = in.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jObArr = (JSONArray)parser.parse(sb.toString());

            for (int i = 0; i < jObArr.size(); i++) {
                JSONObject jOb = (JSONObject) jObArr.get(i);
                for (Object o : (JSONArray) jOb.get("rates")) {
                    JSONObject oo = (JSONObject) o;
                    map.put((oo.get("currency") + " " + oo.get("code")), new LinkedList<>());
                }
            }

            for (int i = 0; i < jObArr.size(); i++) {
                JSONObject jOb = (JSONObject) jObArr.get(i);
                for (Object o : (JSONArray) jOb.get("rates")) {
                    JSONObject oo = (JSONObject) o;
                    map.get(oo.get("currency") + " " + oo.get("code")).add(BigDecimal.valueOf(Double.valueOf(oo.get("mid").toString())));
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return map;
    }
}