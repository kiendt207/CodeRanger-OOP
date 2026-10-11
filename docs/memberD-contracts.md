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
