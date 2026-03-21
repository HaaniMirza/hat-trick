package com.haanibiriyani.hattrick.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HatManClientHandler {

    private static final Set<UUID> transformedPlayers = new HashSet<>();

    public static void setTransformed(UUID uuid, boolean transformed) {
        if (transformed) {
            transformedPlayers.add(uuid);
        } else {
            transformedPlayers.remove(uuid);
        }
    }

    public static boolean isTransformed(UUID uuid) {
        return transformedPlayers.contains(uuid);
    }
}