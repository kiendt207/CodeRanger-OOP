package coderanger.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HistoryStore {
    private static final int MAX_RECORDS = 20;
    private static final String HEADER =
            "timestamp\tname\toutcome\tscore\tseconds\tcorrect\twrong\taccuracy";

    private final Path historyFile;

    public HistoryStore() {
        this(Path.of(System.getProperty("user.home"), ".coderanger", "history.tsv"));
    }

    public HistoryStore(Path historyFile) {
        this.historyFile = Objects.requireNonNull(historyFile, "historyFile");
    }

    public void save(GameRecord record) throws IOException {
        Objects.requireNonNull(record, "record");
        Path absoluteFile = historyFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Files.createDirectories(parent);

        List<GameRecord> records = readValidRecords();
        records.add(record);
        records.sort(Comparator.comparing(r -> Instant.parse(r.timestamp())));
        if (records.size() > MAX_RECORDS) {
            records = new ArrayList<>(records.subList(records.size() - MAX_RECORDS, records.size()));
        }

        StringBuilder contents = new StringBuilder(HEADER).append(System.lineSeparator());
        for (GameRecord savedRecord : records) {
            contents.append(serialize(savedRecord)).append(System.lineSeparator());
        }

        Path temporaryFile = Files.createTempFile(parent, ".history-", ".tmp");
        try {
            Files.writeString(temporaryFile, contents, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, absoluteFile,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
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
                if (line.equals(HEADER)) {
                    continue;
                }
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

    private List<GameRecord> readValidRecords() throws IOException {
        if (!Files.exists(historyFile)) {
            return new ArrayList<>();
        }

        List<GameRecord> records = new ArrayList<>();
        for (String line : Files.readAllLines(historyFile, StandardCharsets.UTF_8)) {
            if (line.equals(HEADER)) {
                continue;
            }
            try {
                records.add(parse(line));
            } catch (IllegalArgumentException | DateTimeException ignored) {
                // Corrupt rows are excluded when the capped history file is rewritten.
            }
        }
        return records;
    }

    private static GameRecord parse(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length == 8) {
            Instant.parse(fields[0]);
            return new GameRecord(fields[0], fields[1], fields[2],
                    Long.parseLong(fields[3]), Double.parseDouble(fields[4]),
                    Integer.parseInt(fields[5]), Integer.parseInt(fields[6]),
                    Double.parseDouble(fields[7]), 0, 0, 0, 0, 0.0, 0, 0, 0);
        }
        if (fields.length != 16) {
            throw new IllegalArgumentException("Expected 8 history fields");
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

    private static String serialize(GameRecord record) {
        return String.join("\t",
                clean(record.timestamp()),
                clean(record.name()),
                clean(record.outcome()),
                Long.toString(record.score()),
                Double.toString(record.seconds()),
                Integer.toString(record.correctChars()),
                Integer.toString(record.wrongChars()),
                Double.toString(record.accuracy()));
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    private record ReadResult(List<GameRecord> records, int corruptLines) {
    }
}