package com.statusneo.vms.config;

import com.statusneo.vms.model.AzureADUser;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserTrie {
    private TrieNode root;

    public UserTrie() {
        this.root = new TrieNode();
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Set<AzureADUser> users = new HashSet<>();
        boolean isEndOfWord;
    }

    public void addUser(AzureADUser user) {
        // Index by name
        if (user.displayName() != null) {
            addWordForUser(user.displayName().toLowerCase(), user);
        }
        // Index by email
        if (user.mail() != null) {
            addWordForUser(user.mail().toLowerCase(), user);
        }
        // Index by UPN
        if (user.userPrincipalName() != null) {
            addWordForUser(user.userPrincipalName().toLowerCase(), user);
        }
    }

    private void addWordForUser(String word, AzureADUser user) {
        TrieNode current = root;
        // Add each word token (split by space for names)
        for (String token : word.split("\\\\s+")) {
            addTokenForUser(token, user, current);
        }
    }

    private void addTokenForUser(String token, AzureADUser user, TrieNode startNode) {
        TrieNode current = startNode;
        for (char ch : token.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
            current.users.add(user);
        }
        current.isEndOfWord = true;
    }

    public Set<AzureADUser> search(String prefix, int limit) {
        prefix = prefix.toLowerCase();
        TrieNode current = root;

        // Navigate to the last node of the prefix
        for (char ch : prefix.toCharArray()) {
            TrieNode node = current.children.get(ch);
            if (node == null) {
                return Collections.emptySet();
            }
            current = node;
        }

        // Return users associated with this prefix, limited to specified size
        return new HashSet<>(new ArrayList<>(current.users).subList(0,
                Math.min(current.users.size(), limit)));
    }

    public void removeUser(AzureADUser user) {
        if (user.displayName() != null) {
            removeWordForUser(user.displayName().toLowerCase(), user);
        }
        if (user.mail() != null) {
            removeWordForUser(user.mail().toLowerCase(), user);
        }
        if (user.userPrincipalName() != null) {
            removeWordForUser(user.userPrincipalName().toLowerCase(), user);
        }
    }

    private void removeWordForUser(String word, AzureADUser user) {
        for (String token : word.split("\\\\s+")) {
            removeTokenForUser(token, user, root);
        }
    }

    private void removeTokenForUser(String token, AzureADUser user, TrieNode startNode) {
        TrieNode current = startNode;
        for (char ch : token.toCharArray()) {
            TrieNode node = current.children.get(ch);
            if (node == null) return;
            node.users.remove(user);
            current = node;
        }
    }
}
