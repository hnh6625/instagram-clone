# Sổ nợ kỹ thuật / lưu ý — Instagram Clone

Danh sách các quyết định "tạm chấp nhận để đi nhanh", cần quay lại xử lý khi có thời gian (ưu tiên trước khi apply/phỏng vấn nếu còn dư sức, không bắt buộc).

## 1. UserMapper bị lặp code
`AuthService` và `UserService` đang tự build `UserResponse` riêng, trùng logic.
→ Nên tách 1 mapper dùng chung (class riêng hoặc MapStruct).

## 2. RabbitMQConfig backend không tự khai báo Queue/Exchange/Binding
Backend comment hết phần khai báo, chỉ dựa vào Worker khai báo trước.
→ Rủi ro: nếu backend chạy độc lập trước khi Worker từng chạy lần nào, `rabbitTemplate.convertAndSend` gửi vào exchange chưa tồn tại, message có thể mất lặng lẽ.
→ Nên: cả 2 bên cùng khai báo (idempotent, không sao nếu trùng).

## 3. `getFeed()` lọc status bằng Java, không lọc ở tầng DB
Vì `status` giờ là `Post.getOverallStatus()` — được tính động từ danh sách `PostMedia`, không phải cột trong bảng `post` — nên không thể query thẳng `WHERE status = 'READY'` bằng JPQL đơn giản.
Hiện tại: lấy **toàn bộ** post rồi lọc bằng vòng lặp Java.
→ Không tối ưu khi dữ liệu lớn (kéo hết dữ liệu lên rồi mới lọc, tốn băng thông/RAM).
→ Hướng cải thiện sau: viết Native Query/JPQL tính điều kiện ngay trong SQL, hoặc thêm cột "status tổng" denormalized trên `Post`, được cập nhật mỗi khi 1 `PostMedia` con đổi trạng thái (đánh đổi lại: quay về rủi ro dữ liệu lệch nhau đã bàn khi chọn Hướng A — cần cân nhắc kỹ nếu đổi).

## 4. Feed chưa có Pagination
`getFeed()` hiện trả về toàn bộ post cùng lúc, không giới hạn số lượng/không hỗ trợ load thêm.
→ Nên thêm khi sửa Feed API ở Phase 7 (lúc join với bảng follows).

## 5. `ddl-auto: update` thay vì Flyway/Liquibase
Đang để Hibernate tự sinh/sửa schema. Cách này **không tự xóa cột thừa** khi entity bỏ field (ví dụ cột `media_url` cũ trên bảng `post` có thể vẫn còn tồn tại dù entity đã bỏ).
→ Cần tự kiểm tra lại schema thật trong Postgres sau khi đổi entity, dọn cột thừa nếu có.
→ Nên chuyển sang Flyway ở Phase 9 để quản lý migration rõ ràng, có lịch sử.

## 6. `CreatePostRequest` DTO không được dùng
Controller hiện nhận thẳng qua `@RequestParam` rời rạc, không bind vào DTO này.
→ Cần quyết định: xóa hẳn cho gọn, hoặc đổi Controller sang dùng DTO này cho đúng chuẩn (dùng `@ModelAttribute` với multipart).

## 7. N+1 query tiềm ẩn (Hướng A cho status)
Khi cần `overallStatus`, phải load hết `post.getMedia()` — nếu load nhiều post cùng lúc (feed) mà mỗi post lại lazy-load `media` riêng, dễ dính N+1 query.
→ Cân nhắc `JOIN FETCH` hoặc `@EntityGraph` khi query list Post nếu thấy chậm thực tế.