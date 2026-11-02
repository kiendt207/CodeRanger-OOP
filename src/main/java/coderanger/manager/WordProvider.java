package coderanger.manager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordProvider {
    private Map<String, List<String>> words;
    private Random random ;

    public WordProvider() {
        words = new HashMap<>();
        
        // Tải các file yêu cầu theo đặc tả
        loadWordsFromFile("normal0");
        loadWordsFromFile("normal1");
        loadWordsFromFile("normal2");
        loadWordsFromFile("normal3");
        loadWordsFromFile("bonus");
        loadWordsFromFile("boss1");
        loadWordsFromFile("boss2");
        loadWordsFromFile("boss3");
    }

    private void loadWordsFromFile(String key) {
        List<String> wordList = new ArrayList<>();
        // Đọc file từ classpath (thư mục src/main/resources)
        String path = "words/" + key + ".txt";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("ERROR: 1 " + path);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) {
                    wordList.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR: 2 " + path, e);
        }

        // Fail fast nếu file rỗng
        if (wordList.isEmpty()) {
            throw new RuntimeException("ERROR: 3 " + path );
        }

        words.put(key, wordList);
    }

    private String getNextWord(String key) {
        List<String> list = words.get(key);
        int i = random.nextInt(list.size());
        return list.get(i);
    }

    public String nextNormal(int level) {
        // Trả về từ theo cấp độ (normal0 -> normal3)
        return getNextWord("normal" + Math.min(level, 3));
    }

    public String nextBonus() {
        return getNextWord("bonus");
    }

    public List<String> phaseWords(int phase) {
        // Trả về danh sách 3 từ cho Boss theo phase (1, 2, hoặc 3)
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(getNextWord("boss" + phase));
        }
        return result;
    }
}