# Sanbaka CodeRanger • Bách Khoa OOP Survival

Game phòng thủ gõ phím 2D một người chơi mang phong cách **Pixel Art × Sanbaka (百科ちゃん)** dành cho sinh viên Bách Khoa, viết bằng **Java 25**, **Swing + Graphics2D + javax.sound.sampled**, **Maven**. Chạy độc lập 100%, không cần mạng, không cần database hay thư viện runtime bên ngoài.

> Hóa thân cùng **Sanbaka (百科ちゃん)** vượt qua **8 Học Kì Bách Khoa** — tung chiêu code tiêu diệt Bug, Deadline, Giáo trình và bảo vệ thành công **Đồ Án OOP (IT3100)** tại Cổng Parabol!

---

## Điểm nổi bật của bản nâng cấp Sanbaka Pixel Edition

- **Đồ họa Pixel Art Bách Khoa đồng bộ**:
  - Nhân vật trung tâm **Sanbaka (百科ちゃん)** với hoạt ảnh chiến đấu (`idle`, `cast strike`, `hurt`), hào quang **Overdrive / Fever**, và khung chân dung biểu cảm thời gian thực trên HUD & màn Kết Quả (`x _ x` chảy dãi `"ぽかーん…"` khi toang, đội mũ tốt nghiệp `"クリア！"` khi qua môn, uống trà đá khi Pause).
  - Bối cảnh đêm Bách Khoa có bóng **Cổng Parabol & Thư viện Tạ Quang Bửu**, bảng màu Đỏ Đô HUST halftone + thẻ giấy thi màu kem viền vàng-đen.
- **Lộ trình 8 Học Kì & Độ khó Động (`Streak Escalation / Overdrive`)**:
  - **Kì 1 → Kì 7** (`35 giây / kì`, tổng `245s`) tăng dần tốc độ, mật độ quái và độ dài từ vựng tiếng Anh chủ đề **IT, Bách Khoa & OOP**.
  - **Kì 8 (`≥ 245s`)**: Hội đồng **Boss Đồ Án OOP** 3 Phase × 3 từ (countdown `11s / 13s / 15s`, triệu hồi tối đa 4 Minion Điểm Danh).
  - **Cơ chế Áp lực Combo (`Overdrive I–III` & `FEVER MAX!`)**: Chuỗi gõ đúng (`streak`) càng cao (`10 / 20 / 30 / 45+`), quái xuất hiện nhanh hơn tới **42%**, giới hạn quái tăng thêm **+1..+4** và chạy nhanh hơn tới **+18%** — đổi lại nhân hệ số điểm lên tới **×2.0** và dồn chuỗi Quick Kill / Shockwave!
- **Cơ chế "One More Run!" cuốn hút**:
  - Màn kết quả hiển thị **Thanh tiến độ 8 Học Kì (`K1 → B8`)**, xếp loại GPA (`F` → `A+`), dấu đóng **`KỶ LỤC MỚI!`**, câu thoại cà khịa **môn OOP (IT3100)** riêng cho từng kì, gợi nhắc *"Suýt nữa thì..."*, và phím tắt **`[ENTER]` / `[SPACE]`** để vào lại ván mới tức thì.
- **Âm thanh ra chiêu 8-bit Chiptune (`SoundEngine`)**:
  - Tự tổng hợp sóng âm 8-bit thuần Java cho từng hành động của Sanbaka (tung đòn tăng dần cao độ theo combo, nổ kết liễu, tuyệt chiêu Shockwave, uống Trà Đá hồi giáp, vỡ khiên, còi Boss, nhạc thắng/thua), có nút Bật/Tắt tiếng ngay trên Menu và HUD.

---

## Yêu cầu & Cách chạy

- **JDK 25** trở lên
- **Maven 3.x**

```bash
mvn clean test      # chạy toàn bộ unit & integration tests (JUnit 5)
mvn exec:java       # khởi chạy game (main class: coderanger.Main)
```

---

## Điều khiển & Luật chơi chính

| Thao tác | Tác dụng |
| :--- | :--- |
| `a`–`z` | Khóa mục tiêu nguy hiểm nhất khớp chữ đầu và bắn tia năng lượng tiêu diệt từ |
| `Esc` | Tạm nghỉ uống trà đá (Pause) / Tiếp tục; ở màn Kết Quả / Bảng Vàng quay về Menu |
| `Enter` / `Space` | Vào thi từ Menu hoặc **Đăng ký học lại ngay lập tức** tại màn Kết Quả |
| Click biểu tượng `🔊` | Bật / Tắt âm thanh 8-bit trên Menu hoặc góc phải trên HUD |

- **HP**: `5` (tối đa 5) • **Giáp (Shield)**: bắt đầu `1` (tối đa 3).
- **Trà Đá Bách Khoa (Bonus Monster)**: Diệt được tăng `+1` Giáp; nếu đầy 3 Giáp sẽ hồi `+1` HP.
- **Quick Kill & Shockwave**: Hoàn thành từ thường/bonus trong `≤ 1.5s` kể từ lúc xuất hiện được `+50` điểm và phóng vòng sóng **Shockwave** (`0.35s`, hồi chiêu `5s`) tiêu diệt thêm 1 quái nguy hiểm nhất (`+25` điểm).
- **Lịch sử cục bộ**: Tự động lưu tối đa `20` trận gần nhất tại `${user.home}/.coderanger/history.tsv` (UTF-8, atomic move).
