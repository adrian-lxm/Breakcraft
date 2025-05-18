package de.breakcraft.proxy.db;

import java.util.UUID;

public class BanEntry {
    private int id;
    private UUID uuid;
    private long timestamp;
    private long duration;
    private String reason;

    BanEntry(int id, UUID uuid, long timestamp, long duration, String reason) {
        this.id = id;
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.duration = duration;
        this.reason = reason;
    }

    public boolean isActive() {
        return duration != -1 && System.currentTimeMillis() >= (timestamp + duration);
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public String getReason() {
        return reason;
    }
}
