package de.breakcraft.proxy;

import com.google.inject.Inject;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.breakcraft.proxy.Commands.Ban;
import de.breakcraft.proxy.Commands.Lobby;
import de.breakcraft.proxy.Commands.Shutdown;
import de.breakcraft.proxy.Commands.Unban;
import de.breakcraft.proxy.db.DatabaseManager;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
        id = "breakcraft-proxy",
        name = "Breakcraft-Proxy",
        version = "1.0-revived",
        authors = {"DeinName"}
)
public class ProxyPlugin {
    private static ProxyPlugin instance;
    public static final String PREFIX = "[Breakcraft-Proxy]";

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private YamlConfigurationLoader configLoader;

    @Inject
    public ProxyPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        instance = this;

        Path configFile = dataDirectory.resolve("config.yml");
        configLoader = YamlConfigurationLoader.builder()
                .path(configFile)
                .build();

        if(Files.notExists(configFile)) {
            try {
                ConfigurationNode config = getConfig();
                config.node("mysql", "db-name").set("");
                config.node("mysql", "db-user").set("");
                config.node("mysql", "db-password").set("");
                saveConfig(config);
            } catch (IOException e) {
                logger.severe(PREFIX + " IOException was thrown! Message of error: " + e.getMessage());
                return;
            }
            
            logger.warning( PREFIX + " Default config file created. Plugin will not activate any functions until restart!");
            return;
        }

        PluginManager pm = server.getPluginManager();
        pm.registerListener(this, new Listeners());
        pm.registerCommand(this, new Lobby());
        pm.registerCommand(this, new Ban());
        pm.registerCommand(this, new Unban());
        pm.registerCommand(this, new Shutdown());

        ConfigurationNode config = getConfig().node("mysql");
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl("jdbc:mysql://localhost:3306/" + config.node("db-name").getString());
        mysqlDataSource.setUser(config.node("db-user").getString());
        mysqlDataSource.setPassword(config.node("db-password").getString());
        DatabaseManager.initialize(mysqlDataSource);


        
    }

    public static ConfigurationNode getConfig() {
        try {
            return instance.configLoader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveConfig(ConfigurationNode config) {
        try {
            instance.configLoader.save(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
