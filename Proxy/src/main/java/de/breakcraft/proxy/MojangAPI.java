package de.breakcraft.proxy;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class MojangAPI {

    private static Gson gson = new Gson();

    public static UUID getUUIDByUsername(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            MojangResponse response = gson.fromJson(reader, MojangResponse.class);
            reader.close();
            if(response.id == null) return null;
            return fromTrimmed(response.id);
        } catch (IOException e) {
            System.err.println("Fehler beim Abrufen der UUID für Nutzer " + username + ": " + e.getMessage());
        }
        return null;
    }

    private static UUID fromTrimmed(String trimmedUUID) {
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e){
            throw new IllegalArgumentException();
        }

        return UUID.fromString(builder.toString());
    }

    static class MojangResponse {
        public String name;
        public String id;
    }

}
