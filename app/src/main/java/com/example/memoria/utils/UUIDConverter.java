package com.example.memoria.utils;

import androidx.room.TypeConverter;
import java.util.UUID;

public class UUIDConverter {
    @TypeConverter
    public static String fromUUID(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    @TypeConverter
    public static UUID toUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }
}
