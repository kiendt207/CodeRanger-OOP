package coderanger.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HistoryStore {
    private final Path historyFile;

    public HistoryStore() {
        this(Path.of(System.getProperty("user.home"), ".coderanger", "history.tsv"));
    }

    public HistoryStore(Path historyFile) {
        this.historyFile = Objects.requireNonNull(historyFile, "historyFile");
    }

    public void save(GameRecord record) throws IOException {
        Objects.requireNonNull(record, "record");
        Path parent = historyFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String line = String.join("\t",
                clean(record.timestamp()),
                clean(record.name()),
                clean(record.outcome()),
                Long.toString(record.score()),
                Double.toString(record.seconds()),
                Integer.toString(record.correctChars()),
                Integer.toString(record.wrongChars()),
                Double.toString(record.accuracy()),
                Integer.toString(record.monstersKilled()),
                Integer.toString(record.quickKills()),
                Integer.toString(record.bonusKills()),
                Integer.toString(record.bossWords()),
                Double.toString(record.wpm()),
                Integer.toString(record.wave()),
                Integer.toString(record.hp()),
                Integer.toString(record.shield()));

        Files.writeString(historyFile, line + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<GameRecord> readRecent() {
        List<GameRecord> records = readAll().records();
        records.sort(Comparator.comparing(GameRecord::timestamp).reversed());
        return List.copyOf(records);
    }

    public List<GameRecord> top10() {
        return readAll().records().stream()
                .sorted(Comparator.comparingLong(GameRecord::score).reversed()
                        .thenComparing(GameRecord::timestamp, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }

    public int countCorruptLines() {
        return readAll().corruptLines();
    }

    private ReadResult readAll() {
        if (!Files.exists(historyFile)) {
            return new ReadResult(new ArrayList<>(), 0);
        }

        List<GameRecord> records = new ArrayList<>();
        int corruptLines = 0;
        try {
            for (String line : Files.readAllLines(historyFile, StandardCharsets.UTF_8)) {
                try {
                    records.add(parse(line));
                } catch (IllegalArgumentException | DateTimeException e) {
                    corruptLines++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read game history: " + historyFile, e);
        }
        return new ReadResult(records, corruptLines);
    }

    private static GameRecord parse(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length != 16) {
            throw new IllegalArgumentException("Expected 16 history fields");
        }
        Instant.parse(fields[0]);
        return new GameRecord(fields[0], fields[1], fields[2],
                Long.parseLong(fields[3]), Double.parseDouble(fields[4]),
                Integer.parseInt(fields[5]), Integer.parseInt(fields[6]),
                Double.parseDouble(fields[7]), Integer.parseInt(fields[8]),
                Integer.parseInt(fields[9]), Integer.parseInt(fields[10]),
                Integer.parseInt(fields[11]), Double.parseDouble(fields[12]),
                Integer.parseInt(fields[13]), Integer.parseInt(fields[14]),
                Integer.parseInt(fields[15]));
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    private record ReadResult(List<GameRecord> records, int corruptLines) {
    }
}