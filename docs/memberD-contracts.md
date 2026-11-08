# CodeRanger - Triển khai tuần 1 & 2

---

## 1. Phân chia trách nhiệm & Ownership
- **Member A**: `Main`, `GameFrame`, `GamePanel`, `PlayingScreen`, `InputHandler`, `GameConstants`, Loop 60 FPS, HUD.
- **Member B**: `GameObject`, `Monster`, `NormalMonster`, `BonusMonster`, `BossMonster`, `Player`, `LaserEffect`.
- **Member C**: `TargetManager`, `WordProvider`, `MonsterFactory`, tài nguyên từ vựng `words/*.txt`.
- **Member D (Core Orchestrator)**: `GameManager`, `WaveManager`, `Difficulty`, `BuffManager`, `GameStats`, Integration tests.
- **Member E**: `MenuScreen`, `ResultsScreen`, `LeaderboardScreen`, `HistoryStore`, `GameRecord`, `ResourceManager`.

---

## 2. Checklist

### 📌 Gửi Member A (UI / Loop)
1. **Truy cập dữ liệu**: `GamePanel` chỉ đọc trạng thái thông qua các getter: `getMonsters()`, `getPlayer()`, `getEffects()`, `getStats()`. **Không được phép sửa đổi trực tiếp dữ liệu bên trong Model/Manager.**
2. **Xử lý phím**:
   - `InputHandler` bắt sự kiện `KEY_TYPED` cho các ký tự `a-z`, chuyển thành lowercase rồi gọi duy nhất hàm: `gameManager.handleCharacter(c)`.
   - Phím `Esc` bắt ở `KEY_PRESSED` để gọi: `gameManager.togglePause()`.

### 📌 Gửi Member B (Entity / Model)
1. **`Player`**:
   - Constructor: `Player(double x, double y)`.
   - Hàm máu/khiên:
     - `takeDamage(int damage)`: Tự trừ khiên trước; khi hết khiên mới trừ HP; clamp tối thiểu là 0.
     - `addShield()`: Trả về `boolean` (`true` nếu cộng thành công, `false` nếu đã đủ 3 tầng).
     - `heal(int amount)`: Hồi máu tối đa 5 HP.
     - `isDead()`: Trả về `true` khi `hp == 0`.
2. **`Monster`**:
   - `checkNextChar(char c)`: Chỉ kiểm tra ký tự và dịch con trỏ từ, **không được tự set `alive = false`**.
   - Cung cấp getter `getY()` và `getSpawnSequence()` để `GameManager` xử lý va chạm vạch nguy hiểm `y = 760`.

### 📌 Gửi Member C (Target / Factory)
1. **`MonsterFactory`**:
   - Constructor: `MonsterFactory(WordProvider provider, Random random)` (dùng `new Random(2026)` để test tái lập).
   - Cung cấp các hàm tạo: `createNormal(int level, double now, boolean isMinion)`, `createBonus(...)`, `createBoss(...)`.
2. **`TargetManager`**:
   - Cung cấp hàm `findAndLockTarget(char c, List<Monster> active)`: Ưu tiên quái có thời gian tới vạch nguy hiểm nhỏ nhất `(760 - y) / vy`.
   - Cung cấp hàm `processInput(char c)` trả về `boolean`.

### 📌 Gửi Member E (History / Storage)
1. **`GameRecord`**: Constructor canonical nhận đúng thứ tự 16 tham số như đặc tả mục 17:
   `(timestamp, name, outcome, score, seconds, correctChars, wrongChars, accuracy, kills, quick, bonus, bossWords, wpm, wave, hp, shield)`.
2. **`HistoryStore`**: Cung cấp hàm `save(GameRecord record)` ném `IOException` nếu ghi thất bại.

---

## Cập nhật tích hợp tuần 4–7

- `TargetManager` khóa mục tiêu theo thời gian dự kiến tới vạch nguy hiểm; các mục tiêu thường/minion được ưu tiên hơn Boss khi bắt đầu khóa. Một mục tiêu Boss đã khóa không bị minion mới cướp khóa.
- `GameManager` điều phối quick kill, shockwave cooldown 5 giây, buff Bonus, chuyển wave 7 ở giây 240, penalty theo timer của Boss, victory animation 2,5 giây và chụp/lưu kết quả đúng một lần.
- `MonsterFactory` sử dụng `Random(2026)` theo mặc định và có thể reset để replay tái lập; factory lấy từ qua `WordProvider`. Maven đóng gói các file trong `src/main/java/coderanger/resource/word` dưới classpath `words/`.
- `HistoryStore` ghi tối đa 20 bản ghi mới nhất theo schema 8 cột trong README; ghi file qua file tạm cùng thư mục và atomic move khi filesystem hỗ trợ. Các file 16 cột cũ vẫn được đọc để tương thích.
- `GameManager.getMonsters()` và `getEffects()` trả về snapshot chỉ đọc; thay đổi entity phải đi qua manager.
- `GameFrame` nối Menu, Playing, Results và Leaderboard qua `CardLayout`; cửa sổ mất focus tự pause game. `Main` khởi tạo các dependency và hiện menu.
- `mvn clean test` dùng Maven Surefire hỗ trợ JUnit 5. Bộ test headless hiện bao gồm tích hợp combat/Boss, lifecycle, persistence, input và mô phỏng soak 15 phút.

## Việc còn phụ thuộc nhóm / phát hành

- Rà soát từ điển Boss đã sửa file phase 3 về độ dài 9–11 ký tự; `WordProvider` lọc phase 2 về độ dài 7–8 theo hợp đồng.
- Chạy smoke test trên hai máy thật, triage PR liên nhóm, merge `dev` vào `main`, ký duyệt và tạo tag `v1.0.0` là bước release cần thực hiện cộng tác.