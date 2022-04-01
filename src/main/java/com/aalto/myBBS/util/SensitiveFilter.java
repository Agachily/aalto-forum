package com.aalto.myBBS.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // The string to replace the sensitive words
    private static final String REPLACEMENT = "***";

    // Initialize the root node
    private final TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword =  reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("Failed to load the file sensitive-words.txt" + e.getMessage());

        }
    }

    /**
     * Add a word to the trie
     * @param keyword
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;

        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            // Judge whether the character has already been add to the trie
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            tempNode = subNode;

            // Set the flag to the last character
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * Return the text that has been filtered
     * @param text
     * @return
     */
    public String filter(String text) {
        if (text == null) {
            return null;
        }

        TrieNode tempNode = rootNode; // Pointer 1
        int begin = 0; // Pointer 2
        int position = 0; // Pointer 3

        StringBuilder result = new StringBuilder();

        while (begin < text.length()) {
            char c = text.charAt(position);

            // Skip the special symbol
            if (isSymbol(c)) {
                // If the Pointer 1 is at root, this symbol will be put into result and move the pointer 2
                if (tempNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                // Move pointer 3
                position++;
                continue;
            }

            // Check the sub node
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                result.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // In this case a sensitive word is detected. In this case, replace that
                result.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else {
                if (position < text.length() - 1) {
                    position++;
                } else {
                    position = begin;
                }
            }
        }

        // Add the rest characters to the result
        result.append(text.substring(begin));

        return result.toString();
    }

    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c);
    }

    private class TrieNode {
        // The flag to indicate the end of word
        private boolean isKeywordEnd = false;

        private Map<Character, TrieNode> subnodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // Add the sub node
        public void addSubNode(Character c, TrieNode node) {
            subnodes.put(c, node);
        }

        // Get the sub node
        public TrieNode getSubNode(Character c) {
            return subnodes.get(c);
        }
    }


}
