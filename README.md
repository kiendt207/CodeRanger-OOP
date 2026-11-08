# CodeRanger

Game phòng thủ gõ phím 2D một người chơi, viết bằng **Java 25**, **Swing + Graphics2D**, **Maven**.
Chạy độc lập, không cần mạng, không cần database.

> Một lập trình viên bước vào thế giới số để chống virus và cứu hệ thống máy chủ.
> Phi thuyền là hình tượng của người chơi trong không gian số.

## Giới thiệu

Người chơi ở đáy màn hình (ship tại tâm `(270, 820)`, không di chuyển). Quái là các từ tiến từ trên
xuống. Gõ đúng từ để tiêu diệt quái trước khi chúng vượt **danger line** `y = 760`.

- Sống sót đến phút thứ 4, đánh bại **Boss** → hoạt cảnh phi thuyền đi vào lỗ hổng không gian → **You win**.
- HP về 0 ở bất kỳ thời điểm nào → **Game over**.
- Cả hai kết thúc đều hiện thống kê gõ phím và lưu vào lịch sử.

Bàn chơi dọc **540 × 900** logical pixels, gốc `(0, 0)` ở góc trên trái, x tăng sang phải, y tăng xuống.
Không dùng lane.

## Yêu cầu

- JDK 25 trở lên
- Maven 3.x

## Build và chạy

```bash
mvn clean test      # chạy toàn bộ unit/integration test (JUnit 5)
mvn exec:java       # chạy game (main class: coderanger.Main)
```

## Cách chơi

| Thao tác | Tác dụng |
|---|---|
| Gõ `a`–`z` | Khóa mục tiêu theo chữ đầu, rồi gõ tiếp các chữ còn lại của từ |
| `Esc` | Tạm dừng; nhấn `Esc` lần nữa để tiếp tục (thời gian không chạy khi pause) |
| Menu → ô tên | 1–16 ký tự chữ, số hoặc khoảng trắng; để trống sẽ thành `Player` |

Quy tắc chính:

- Chỉ nhận `a`–`z` (tự chuyển lowercase); ký tự khác và phím giữ lặp bị bỏ qua.
- Gõ sai **không** đổi mục tiêu và không phạt thời gian, nhưng phá combo.
- Quái chạm danger line: tiêu 1 shield nếu còn, nếu không mất 1 HP; quái bị hủy ngay.
- **HP** 5 (tối đa 5), **shield** bắt đầu 1 (tối đa 3).
- **Bonus monster** (nhanh hơn, 3–4 ký tự): diệt được thưởng 1 shield; nếu đầy shield thì hồi 1 HP.
- **Quick kill**: hoàn thành Normal/Bonus trong ≤ 1.5 s kể từ lúc spawn → +50 điểm và bắn
  **shockwave** (cooldown 5 s) diệt thêm một quái nguy hiểm nhất (+25 điểm).
- **Boss** xuất hiện tại 240 s: 9 HP, 3 phase × 3 từ, mỗi từ có countdown (14/16/18 s);
  hết giờ thì boss gây 1 damage; phase 2–3 có minions.

### Độ khó

`level = min(4, floor(giây sống sót / 60))`; wave thường `floor(giây / 40) + 1` (wave 1–6), wave 7 là boss.

| Thời gian | Level | Spawn Normal | Tốc độ (px/s) | Độ dài từ | Tối đa quái |
|---|---|---|---|---|---|
| 0–59 s | 0 | 3.0 s | 55 | 3–5 | 5 |
| 60–119 s | 1 | 2.7 s | 60 | 4–6 | 6 |
| 120–179 s | 2 | 2.4 s | 65 | 5–7 | 7 |
| 180–239 s | 3 | 2.1 s | 70 | 6–8 | 8 |
| ≥ 240 s | 4 | tắt | minions 68 | boss | 3 minions |

### Điểm và thống kê

- Mỗi kill trực tiếp: `baseScore × multiplier`, `multiplier = min(2.0, 1.0 + 0.1 × floor(streak / 5))`.
- Base score: Normal 100, minion 80, Bonus 150; Boss +1000 một lần, mỗi boss word trước đó +100.
- Thống kê cuối trận: score, wave, HP/shield cuối, thời gian sống, số quái hạ, quick kills, bonus kills,
  boss words, ký tự đúng/sai, accuracy, WPM.

## Lịch sử và Leaderboard

- Lưu tối đa **20** bản ghi mới nhất tại `${user.home}/.coderanger/history.tsv` (UTF-8).
- Header: `timestamp  name  outcome  score  seconds  correct  wrong  accuracy` (phân tách bằng tab);
  các file 16 cột cũ vẫn được đọc để tương thích.
- Ghi bằng file tạm cùng thư mục rồi `Files.move` (ATOMIC_MOVE nếu filesystem hỗ trợ).
- File thiếu → danh sách rỗng; dòng hỏng bị bỏ qua và Leaderboard hiện cảnh báo nhỏ.
- Màn Leaderboard hiện **top 10 score** và **10 trận gần nhất**.
- Không commit file lịch sử cá nhân; không lưu trong `src/main/resources`.

## Kiến trúc

Một `javax.swing.Timer(16, …)` trên EDT điều khiển vòng lặp (xấp xỉ 60 FPS), `dt = min(0.05, elapsed)`.
Không dùng thread riêng. `paintComponent` chỉ vẽ snapshot, không thay đổi trạng thái.
Luồng màn hình: `MENU → PLAYING ↔ PAUSED → (VICTORY_ANIMATION → WIN | LOSS) → RESULTS → MENU / RESTART`.

```
pom.xml
README.md
docs/memberD-contracts.md
src/main/java/coderanger/
├── Main.java
├── model/      GameObject, Monster, NormalMonster, BonusMonster, BossMonster, Player, LaserEffect
├── manager/    GameManager, TargetManager, WaveManager, Difficulty, MonsterFactory, WordProvider,
│               BuffManager, GameStats, HistoryStore, GameRecord
├── view/       GameFrame, GamePanel, MenuScreen, PlayingScreen, ResultsScreen, LeaderboardScreen
├── input/      InputHandler
├── resource/   ResourceManager
│   └── word/   normal0–3.txt, bonus.txt, boss1–3.txt
└── util/       GameConstants
src/test/java/coderanger/{model,manager}/
```

Maven đóng gói các file từ `src/main/java/coderanger/resource/word/` vào classpath `/words/`.

Nguyên tắc OOP áp dụng: encapsulation (mọi field `private`), abstraction (`GameObject`, `Monster`),
inheritance (`Monster` → Normal/Bonus/Boss), polymorphism (`update`/`render`/`onDeath`),
composition (`GameManager` sở hữu Player, TargetManager, WaveManager, BuffManager, GameStats).
UI chỉ quan sát model và gửi input; logic game nằm ở `manager`.

## Quy trình làm việc với Git

- Nhánh `main` chỉ nhận bản phát hành; `dev` là nhánh tích hợp. Không push trực tiếp vào `main`/`dev`.
- Mỗi task một nhánh: `feature/memberX-short-topic` hoặc `fix/memberX-short-topic`, mở PR vào `dev`.
- Commit: `feat|fix|test|docs|refactor(scope): imperative description`.
- PR: một review từ người khác, `mvn clean test` xanh, squash merge.
- Release: PR `dev` → `main`, sau đó tag `v1.0.0`.

```bash
git switch dev && git pull --ff-only origin dev
git switch -c feature/memberA-short-topic
# code, rồi:
mvn clean test
git add src pom.xml
git commit -m "feat(view): add something"
git push -u origin feature/memberA-short-topic
```

## Phân công

| Thành viên | Phụ trách |
|---|---|
| A | `Main`, `GameFrame`, `GamePanel`, `PlayingScreen`, `InputHandler`, `GameConstants`, HUD, hoạt cảnh thắng |
| B | `GameObject`, `Monster` và các lớp con, `Player`, `LaserEffect`, test model |
| C | `TargetManager`, `WordProvider`, `MonsterFactory`, file từ vựng, test target/factory |
| D | `GameManager`, `WaveManager`, `Difficulty`, `BuffManager`, `GameStats`, integration test |
| E | `MenuScreen`, `ResultsScreen`, `LeaderboardScreen`, `HistoryStore`, `GameRecord`, `ResourceManager`, README |

## Tài liệu

Ghi chú hợp đồng và tích hợp của Member D nằm trong
[`docs/memberD-contracts.md`](docs/memberD-contracts.md). Mọi thay đổi hằng số core phải có PR cập nhật
tài liệu và test.
