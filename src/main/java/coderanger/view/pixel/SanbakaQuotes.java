package coderanger.view.pixel;

import coderanger.manager.GameRecord;
import coderanger.util.GameConstants;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * HUST IT3100 (OOP) quotes, academic standing (GPA rank), and "One More Run!" hooks.
 */
public final class SanbakaQuotes {

    public record GradeEvaluation(
            String letterGrade,
            String rankTitle,
            String subtitle,
            Color badgeColor,
            String nextRankHint) {
    }

    private static final List<List<String>> LOSS_OOP_QUOTES = List.of(
            // Kì 1 — Class & Object
            List.of(
                    "NullPointerException ngay Kì 1! Quên new Object() trước khi vào phòng thi rồi...",
                    "Chưa phân biệt được Class với Object mà đã đòi qua Kì 1 sao?!"
            ),
            // Kì 2 — Encapsulation
            List.of(
                    "Để hết thuộc tính là public! Vi phạm Encapsulation nên bị trừ sạch điểm rồi...",
                    "Quên đóng gói private field — bug chui thẳng vào model phá tan nát!"
            ),
            // Kì 3 — Inheritance
            List.of(
                    "Gõ extends nhầm lớp cha! Kế thừa nguyên cả đống bug từ kì trước...",
                    "Đòi đa kế thừa class trong Java?! Compiler báo đỏ lòm màn hình rồi!"
            ),
            // Kì 4 — Polymorphism
            List.of(
                    "Quên @Override nên Polymorphism không chạy! Đòn đánh của bé trượt sạch...",
                    "Nhầm Overloading với Overriding — hẹn gặp lại ở lớp học lại IT3100 kỳ hè!"
            ),
            // Kì 5 — Abstraction & Interface
            List.of(
                    "Khởi tạo trực tiếp new AbstractClass()?! Bị JVM đánh rớt ngay tại trận...",
                    "Quên implements đủ method trong Interface — đỏ rực nguyên cả project!"
            ),
            // Kì 6 — Composition & SOLID
            List.of(
                    "Tight Coupling quá nặng! Sửa một dòng ở View mà nổ tung luôn 5 class...",
                    "Lạm dụng Inheritance thay vì Composition — cây kế thừa đổ sập rồi!"
            ),
            // Kì 7 — Design Patterns & Unit Test
            List.of(
                    "Code chạy mượt trên máy em, nhưng JUnit 5 đỏ rực trên máy giảng viên!",
                    "StackOverflowError sát vạch đích! Chỉ còn 1 kì nữa là lên bảo vệ Đồ Án rồi!"
            ),
            // Kì 8 — Boss (Bảo vệ Đồ án OOP)
            List.of(
                    "Gục ngay hội đồng BẢO VỆ ĐỒ ÁN OOP! Thầy hỏi 1 câu Class Diagram là đứng hình...",
                    "UI lỡ mutate trực tiếp Model ngay lúc demo! Thiếu đúng chút xíu nữa là Qua Môn!"
            )
    );

    private static final List<String> WIN_QUOTES = List.of(
            "QUA MÔN IT3100 XUẤT SẮC! Full điểm 4 tính chất OOP, học bổng loại A thẳng tiến!",
            "BẢO VỆ ĐỒ ÁN THÀNH CÔNG! Clean Code, 0 Bug, 100% Polymorphism — ra cổng Parabol ăn mừng!"
    );

    private SanbakaQuotes() {}

    public static String pickQuote(boolean win, int wave) {
        if (win) {
            return WIN_QUOTES.get(ThreadLocalRandom.current().nextInt(WIN_QUOTES.size()));
        }
        int idx = Math.max(0, Math.min(LOSS_OOP_QUOTES.size() - 1, wave - 1));
        List<String> pool = LOSS_OOP_QUOTES.get(idx);
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    public static GradeEvaluation evaluateGrade(GameRecord record) {
        boolean win = "WIN".equalsIgnoreCase(record.outcome());
        long s = record.score();

        if (win && s >= 4500) {
            return new GradeEvaluation("A+", "XUẤT SẮC (HB LOẠI A)",
                    "Thủ khoa IT3100 — Đồ án OOP điểm 10 tuyệt đối!",
                    new Color(22, 163, 74),
                    "Đỉnh cao Bách Khoa! Thử phá kỷ lục WPM & Combo cao hơn nữa!");
        }
        if (win || s >= 3400) {
            long need = Math.max(100, 4500 - s);
            return new GradeEvaluation("A", "GIỎI • SV 5 TỐT",
                    "Nắm chắc Encapsulation, Inheritance, Polymorphism!",
                    new Color(37, 99, 235),
                    win ? ("Cần +" + need + " ĐRL để đạt danh hiệu A+ Thủ Khoa!")
                        : "Đã đủ điểm A — chỉ cần hạ nốt Boss Kì 8 để Tốt Nghiệp!");
        }
        if (s >= 2200) {
            return new GradeEvaluation("B+", "KHÁ GIỎI",
                    "Code chạy ổn, làm cốc trà đá Bách Khoa rồi cày lên A!",
                    new Color(13, 148, 136),
                    "Chỉ cần +" + (3400 - s) + " ĐRL nữa là chạm mốc điểm A!");
        }
        if (s >= 1300) {
            return new GradeEvaluation("C+", "TRUNG BÌNH KHÁ",
                    "Qua môn sát nút — cần giữ Combo x2.0 để bứt điểm!",
                    new Color(217, 119, 6),
                    "Chỉ thiếu +" + (2200 - s) + " ĐRL để lên hạng KHÁ GIỎI (B+)!");
        }
        if (s >= 600) {
            return new GradeEvaluation("D", "CẢNH BÁO HỌC TẬP MỨC 1",
                    "Làm cốc trà đá cổng Parabol lấy lại bình tĩnh nào!",
                    new Color(234, 88, 12),
                    "Chỉ thiếu +" + (1300 - s) + " ĐRL để thoát mức Cảnh Báo!");
        }
        return new GradeEvaluation("F", "CẢNH BÁO HỌC TẬP MỨC 2",
                "Nguy cơ học lại kỳ hè — mau đăng ký gỡ điểm ngay!",
                PixelTheme.HUST_CRIMSON,
                "Chỉ cần +" + Math.max(50, 600 - s) + " ĐRL để qua điểm liệt F!");
    }

    /**
     * Generates a personalized "One More Run!" hook showing how close the player was.
     */
    public static String buildNearMissHook(
            GameRecord record,
            long previousBestScore,
            int lastBossHp) {
        boolean win = "WIN".equalsIgnoreCase(record.outcome());
        if (win) {
            return String.format(Locale.ROOT,
                    "♦ Kỷ lục tốc độ gõ %.1f WPM | Max Combo %d — Thử thách mốc điểm cao hơn!",
                    record.wpm(), record.peakStreak());
        }

        if (record.wave() >= GameConstants.BOSS_WAVE && lastBossHp > 0) {
            return "► SUÝT QUA MÔN! Boss Đồ Án chỉ còn " + lastBossHp
                    + "/9 HP — phục thù ngay kẻo nguội tay!";
        }

        if (previousBestScore > record.score() && (previousBestScore - record.score()) <= 500) {
            long diff = previousBestScore - record.score();
            return "► SUÝT PHÁ KỶ LỤC! Chỉ thiếu đúng +" + diff + " ĐRL nữa là lập đỉnh mới!";
        }

        double waveProgress = record.seconds() % GameConstants.WAVE_DURATION;
        double remainingToNextWave = GameConstants.WAVE_DURATION - waveProgress;
        if (record.wave() < GameConstants.BOSS_WAVE && remainingToNextWave <= 11.0) {
            return String.format(Locale.ROOT,
                    "► CHỈ CÒN %.1fs NỮA LÀ QUA KÌ %d! Nhấn [ENTER] gỡ lại liền tay!",
                    remainingToNextWave, record.wave() + 1);
        }

        return "♦ MẸO CAO THỦ: Gõ xong từ ≤ 1.5s kích hoạt SHOCKWAVE dọn quái & giữ Combo Overdrive!";
    }
}
