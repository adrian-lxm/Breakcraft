package de.breakcraft.proxy;

import com.google.inject.Inject;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.breakcraft.proxy.commands.Ban;
import de.breakcraft.proxy.commands.Lobby;
import de.breakcraft.proxy.commands.Unban;
import de.breakcraft.proxy.db.DatabaseManager;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
        id = "breakcraft-proxy",
        name = "Breakcraft-Proxy",
        version = "1.1",
        authors = {"adrian-lxm"}
)
public class ProxyPlugin {
    private static ProxyPlugin instance;
    private final static ChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.create("breakcraft", "proxy");

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
                Files.createDirectories(dataDirectory);
                var defaultNode = configLoader.createNode();
                var mysqlNode = defaultNode.node("mysql");
                mysqlNode.node("host").set("");
                mysqlNode.node("port").set(3306);
                mysqlNode.node("database").set("");
                mysqlNode.node("user").set("");
                mysqlNode.node("password").set("");
                saveConfig(defaultNode);
            } catch (IOException e) {
                logger.severe("IOException was thrown! Message of error: " + e.getMessage());
                return;
            }
            
            logger.warning( "Default config file created. Plugin will not activate any functions until restart!");
            return;
        }
        var config = getConfig().node("mysql");

        server.getChannelRegistrar().register(IDENTIFIER);

        var cm = server.getCommandManager();
        cm.register(
                cm.metaBuilder("ban").plugin(this).build(),
                new Ban()
        );
        cm.register(
                cm.metaBuilder("lobby").plugin(this).build(),
                new Lobby()
        );
        cm.register(
                cm.metaBuilder("unban").plugin(this).build(),
                new Unban()
        );

        EventManager em = server.getEventManager();
        em.register(this, new Listeners(server, IDENTIFIER));
        em.register(this, new PluginMessagesListener(IDENTIFIER));


        MysqlDataSource source = new MysqlDataSource();
        String host = config.node("host").getString();
        int port = config.node("port").getInt();
        String database = config.node("database").getString();
        source.setUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
        source.setUser(config.node("user").getString());
        source.setPassword(config.node("password").getString());
        DatabaseManager.initialize(source);

        logger.info("Plugin initialized !");
        
    }

    public static ConfigurationNode getConfig() {
        try {
            return instance.configLoader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveConfig(ConfigurationNode config) {
        try {
            configLoader.save(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProxyPlugin get() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

}
