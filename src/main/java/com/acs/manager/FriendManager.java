package com.acs.manager;

import java.util.HashSet;
import java.util.Set;

public class FriendManager {
    public static final FriendManager INSTANCE = new FriendManager();
    private final Set<String> friends = new HashSet<>();

    public void addFriend(String name) {
        friends.add(name);
    }

    public void removeFriend(String name) {
        friends.remove(name);
    }

    public boolean isFriend(String name) {
        return friends.contains(name);
    }

    public Set<String> getFriends() {
        return friends;
    }
}
