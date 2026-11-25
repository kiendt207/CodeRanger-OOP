package coderanger.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class WordProvider {
    private final Map<String, List<String>> words = new HashMap<>();
    private final Random random;
    private final Long resetSeed;

    public WordProvider() {
        this(2026L);
    }

    public WordProvider(long seed) {
        random = new Random(seed);
        resetSeed = seed;
        loadAllWords();
    }

    public WordProvider(Random random) {
        this.random = Objects.requireNonNull(random, "random");
        resetSeed = null;
        loadAllWords();
    }

    private void loadAllWords() {
        for (String key : List.of(
                "normal0", "normal1", "normal2", "normal3",
                "bonus", "boss1", "boss2", "boss3")) {
            words.put(key, loadWords(key));
        }
    }

    private List<String> loadWords(String key) {
        String path = "/words/" + key + ".txt";
        InputStream resource = WordProvider.class.getResourceAsStream(path);
        if (resource == null) {
            throw new IllegalStateException("Missing word resource: " + path);
        }

        Set<String> uniqueWords = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase(Locale.ROOT);
                if (word.isEmpty()) {
                    continue;
                }
                if (!isAsciiLowercaseWord(word)) {
                    throw new IllegalStateException("Invalid non-ASCII word '" + word + "' in " + path);
                }
                uniqueWords.add(word);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read word resource: " + path, e);
        }

        if (uniqueWords.isEmpty()) {
            throw new IllegalStateException("Word resource is empty: " + path);
        }
        return List.copyOf(uniqueWords);
    }

    private static boolean isAsciiLowercaseWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c < 'a' || c > 'z') {
                return false;
            }
        }
        return true;
    }

    private String getNextWord(String key) {
        List<String> list = words.get(key);
        return list.get(random.nextInt(list.size()));
    }

    public String nextNormal(int level) {
        int boundedLevel = Math.max(0, Math.min(3, level));
        return getNextWord("normal" + boundedLevel);
    }

    public String nextBonus() {
        return getNextWord("bonus");
    }

    public List<String> phaseWords(int phase) {
        if (phase < 1 || phase > 3) {
            throw new IllegalArgumentException("Boss phase must be between 1 and 3");
        }
        int minLength = phase == 1 ? 5 : phase == 2 ? 7 : 9;
        int maxLength = phase == 1 ? 6 : phase == 2 ? 8 : 11;
        List<String> candidates = words.get("boss" + phase).stream()
                .filter(word -> word.length() >= minLength && word.length() <= maxLength)
                .toList();
        if (candidates.size() < 3) {
            throw new IllegalStateException("Need at least 3 distinct words of required length for boss phase " + phase);
        }

        List<String> pool = new ArrayList<>(candidates);
        List<String> result = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            int idx = random.nextInt(pool.size());
            result.add(pool.remove(idx));
        }
        return List.copyOf(result);
    }

    public List<String> getWordPool(String key) {
        List<String> list = words.get(key);
        return list == null ? List.of() : list;
    }

    public void reset() {
        if (resetSeed != null) {
            random.setSeed(resetSeed);
        }
    }
}
