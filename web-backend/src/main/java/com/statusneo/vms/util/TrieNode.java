package com.statusneo.vms.util;
import java.util.*;

public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean isEndOfWord;
    // store original names that end at this node (preserve casing)
    private final List<String> originals = new ArrayList<>();

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public List<String> getOriginals() {
        return originals;
    }

    public void addOriginal(String name) {
        if (name != null && !name.isBlank() && !originals.contains(name)) {
            originals.add(name);
        }
    }
}