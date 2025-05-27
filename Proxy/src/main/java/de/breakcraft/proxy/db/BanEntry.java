package de.breakcraft.proxy.db;

import java.util.Objects;
import java.util.UUID;

public class BanEntry {
    private final int id;
    private final UUID uuid;
    private final long timestamp;
    private final long duration;
    private final String reason;

    BanEntry(int id, UUID uuid, long timestamp, long duration, String reason) {
        this.id = id;
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.duration = duration;
        this.reason = reason;
    }

    public boolean isActive() {
        return duration == -1 || System.currentTimeMillis() < (timestamp + duration);
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

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, timestamp, duration, reason);
    }

}
