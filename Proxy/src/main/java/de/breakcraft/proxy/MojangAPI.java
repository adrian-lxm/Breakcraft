package de.breakcraft.proxy;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public class MojangAPI {

    private static final Gson gson = new Gson();

    public static Optional<UUID> getUUIDByUsername(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            MojangResponse response = gson.fromJson(reader, MojangResponse.class);
            reader.close();
            if(response.id == null) return Optional.empty();
            return Optional.of(fromTrimmed(response.id));
        } catch (IOException e) {
            ProxyPlugin.get().getLogger()
                    .severe("Fehler beim Abrufen der UUID für Nutzer " + username + ": " + e.getMessage());
        }
        return Optional.empty();
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
        public String path;
        public String errorMessage;
    }

}
