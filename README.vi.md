# Lak Launcher (Tiếng Việt)

**Launcher đa ngôn ngữ cho máy chiếu JMGO N1 bản nội địa Trung Quốc (Bonfire OS)** — cài qua ADB, không cần root, có bộ kit double-click cho người dùng không biết lập trình.

> 🤖 Project được **vibe code bằng Meta Muse Spark** cùng chủ máy, kiểm chứng trên máy thật.

Bản quốc tế / hướng dẫn đầy đủ: xem [README.md](README.md) (tiếng Anh).

Tóm tắt:

- Giao diện mặc định **tiếng Anh** thay cho Bonfire OS chỉ có tiếng Trung; thêm ngôn ngữ bằng file `.txt` — dịch sẵn có `launcher/lang/vi.txt` (Tiếng Việt).
- Thêm ngôn ngữ không cần cài lại app: đẩy file `.txt` vào `/sdcard/Lak-Launcher/lang/` bằng ADB là launcher tự nhận (xem `docs/adding-a-language.md`).
- Thẻ tắt vào thẳng các mục của **SmartTube** (Lịch sử xem, Thư viện, Kênh đăng ký...) vì app YouTube chính hãng bị chặn trên máy này; nút Netflix được thay bằng VTVgo TV.
- Đặt làm Home mặc định bằng cách tắt gói launcher gốc ở mức user (`pm disable-user`) — có kit khôi phục đi kèm, không flash, không root.
- Muốn hỗ trợ thêm dòng máy khác: chạy `kit/0-PROBE.bat` rồi mở issue kèm file kết quả (xem `docs/new-device-support.md`).
