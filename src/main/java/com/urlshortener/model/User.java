package com.urlshortener.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private final UUID id;
    private final List<String> createdLinkIds;

    public User() {
        this.id = UUID.randomUUID();
        this.createdLinkIds = new ArrayList<>();
    }

    public User(UUID existingId) {
        this.id = existingId;
        this.createdLinkIds = new ArrayList<>();
    }

    public UUID getId() { return id; }
    public List<String> getCreatedLinkIds() { return new ArrayList<>(createdLinkIds); }

    public void addCreatedLink(String linkId) {
        createdLinkIds.add(linkId);
    }

    public boolean ownsLink(String linkId) {
        return createdLinkIds.contains(linkId);
    }

    public void removeLink(String linkId) {
        createdLinkIds.remove(linkId);
    }
}