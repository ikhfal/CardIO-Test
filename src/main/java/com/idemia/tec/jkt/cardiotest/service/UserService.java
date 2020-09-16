package com.idemia.tec.jkt.cardiotest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idemia.tec.jkt.cardiotest.model.CardioUser;

import java.io.File;
import java.io.IOException;

public class UserService {

    private static File userSettings;

    public static CardioUser initUser() {
        userSettings = new File("user.json");
        if (userSettings.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            try { return mapper.readValue(userSettings, CardioUser.class); }
            catch (IOException e) { e.printStackTrace(); }
        }
        else return new CardioUser("", "", "", false);
        return null;
    }

    public static void saveUser(CardioUser user) {
        ObjectMapper mapper = new ObjectMapper();
        try { mapper.writerWithDefaultPrettyPrinter().writeValue(userSettings, user); }
        catch (IOException e) { e.printStackTrace(); }
    }

}
