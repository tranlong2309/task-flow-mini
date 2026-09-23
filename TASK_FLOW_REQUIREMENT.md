# Task Flow - Product Requirement Document (PRD)

## 1. Tóm tắt bối cảnh

- Đối tượng sử dụng: team nhỏ 8-12 người trong agency marketing/digital, gồm Designer, Copywriter, Developer, QA, Account Manager và Leader.
- Mô hình làm việc: nhiều task chạy song song qua nhiều dự án, có deadline rõ ràng, cần theo dõi tiến độ theo trạng thái từng task.
- Nghiệp vụ chính: giao việc, cập nhật trạng thái, theo dõi deadline, báo cáo tiến độ và xử lý task bị chậm / bị treo.
- Mục tiêu của sản phẩm: giúp team thấy rõ "ai đang làm gì, task nào bị kẹt, deadline nào đang nguy hiểm" mà không cần báo cáo thủ công trên chat/email.

---

## 2. Problem Statement

### 2.1 Persona 1: Member

- Là Member, tôi gặp khó khăn khi task được giao không rõ trạng thái và không có thông tin đủ về mục tiêu/độ ưu tiên, dẫn đến mất thời gian xác nhận, sai lệch scope và phải hỏi lại nhiều lần.
- Là Member, tôi gặp khó khăn khi nhiều task cùng lúc được giao mà không có lịch làm việc rõ ràng, dẫn đến việc bị trễ deadline do không biết task nào cần ưu tiên trước.
- Là Member, tôi gặp khó khăn khi task bị "đóng băng" hoặc thiếu phản hồi từ Lead/Manager, dẫn đến thời gian chờ kéo dài và dự án bị chậm không rõ nguyên nhân.
- Là Member, tôi gặp khó khăn khi phải cập nhật tiến độ thủ công qua chat, email hoặc spreadsheet, dẫn đến công việc lặp lại, sai lệch dữ liệu và mất nhiều thời gian mỗi ngày.
- Là Member, tôi gặp khó khăn khi không nhìn thấy task của mình trong một board thống nhất, dẫn đến bỏ sót công việc, quên follow-up và đánh giá sai mức độ tải công việc.

### 2.2 Persona 2: Manager / Lead

- Là Manager, tôi gặp khó khăn khi không biết ai đang quá tải, ai đang rảnh và task nào đang bị chậm, dẫn đến việc phân bổ lại công việc trễ và làm giảm hiệu suất team.
- Là Manager, tôi gặp khó khăn khi task không có owner rõ ràng hoặc trạng thái không được cập nhật thường xuyên, dẫn đến việc lead phải chase thủ công và mất thời gian kiểm tra từng task.
- Là Manager, tôi gặp khó khăn khi không có visibility về deadline và task nguy cơ trễ, dẫn đến việc phát hiện quá muộn và phải xử lý khẩn cấp.
- Là Manager, tôi gặp khó khăn khi tiến độ báo cáo phải tổng hợp từ nhiều nguồn (chat, email, spreadsheet), dẫn đến tốn 1-2 giờ mỗi tuần cho việc thu thập dữ liệu thay vì tập trung vào quyết định.
- Là Manager, tôi gặp khó khăn khi task bị stuck không có ghi chú rõ ràng về nguyên nhân, dẫn đến khó khăn trong review sprint / dự án và thiếu căn cứ để hỗ trợ team.

---

## 3. Goals & Success Metrics

### 3.1 Mục tiêu sản phẩm

- Giảm thời gian quản lý task và theo dõi tiến độ bằng cách tập trung tất cả task vào một board có trạng thái rõ ràng.
- Tăng khả năng visibility cho cả Member và Manager về status, assignee, deadline và blocker.
- Giảm tình trạng task bị thiếu owner, bị treo hoặc trễ do thiếu thông tin.
- Tạo cơ sở cho báo cáo tiến độ nhanh, chính xác và ít thao tác thủ công.

### 3.2 Chỉ số đo lường thành công

- Giảm 40% thời gian cần để tạo báo cáo tiến độ hàng tuần.
- Tăng 60% tỷ lệ task có owner, deadline và trạng thái hợp lệ ngay khi được giao.
- Giảm 30% số task quá hạn so với cách làm hiện tại.
- Giảm 50% số lần Manager phải nhắn tin / chase thông tin thủ công về tiến độ task.
- Tăng 70% mức độ rõ ràng về blocker hoặc nguyên nhân chậm trễ trong task.
- Tối thiểu 80% task được cập nhật trạng thái trong vòng 24 giờ khi có thay đổi quan trọng.

---

## 4. User Stories

### Epic 1: Quản lý board và task

#### Story 1
- As a Manager, I want to create a board, add task columns and define workflow, so that the team has one source of truth for work status.
- Acceptance criteria:
  - Manager có thể tạo board mới với tên và mô tả.
  - Có thể tùy chỉnh các trạng thái mặc định như Todo, In Progress, Review, Done.
  - Manager có thể thêm/đổi tên/ẩn các cột theo nhu cầu team.
  - Board hiển thị rõ task theo từng cột và không bị trùng dữ liệu.

#### Story 2
- As a Member, I want to create a task, assign an owner and set a deadline, so that work is clear and accountable.
- Acceptance criteria:
  - Member có thể tạo task với tiêu đề, mô tả, assignee, deadline và priority.
  - Nếu thiếu owner hoặc deadline, hệ thống hiển thị cảnh báo rõ ràng.
  - Task được hiện thị đúng trên board của assignee và board tổng quan.
  - Chỉ người có quyền mới được sửa task của team.

#### Story 3
- As a Manager, I want to move tasks across columns and update status, so that I can track work progress without manual reporting.
- Acceptance criteria:
  - Task có thể kéo thả giữa các cột hoặc cập nhật trạng thái bằng hành động rõ ràng.
  - Mỗi thay đổi trạng thái được lưu lịch sử và hiển thị thời gian cập nhật.
  - Manager có thể xem tổng quan số lượng task theo trạng thái.

### Epic 2: Giao việc và theo dõi tiến độ

#### Story 4
- As a Manager, I want to assign tasks to specific people with priorities and due dates, so that responsibilities are clear and deadlines are manageable.
- Acceptance criteria:
  - Manager có thể giao task cho một người hoặc nhiều người theo quyền hạn.
  - Mỗi task có thể gắn mức độ ưu tiên thấp/ trung/ cao.
  - Due date hiển thị trên board và trong danh sách task của assignee.
  - Nếu due date bị thay đổi, lịch sử cập nhật được lưu và hiển thị.

#### Story 5
- As a Member, I want to update task status and add notes when work changes, so that my team knows the current state and blocker.
- Acceptance criteria:
  - Member có thể chỉnh sửa trạng thái task và ghi chú ngắn khi có diễn biến mới.
  - Có thể điền "blocker" hoặc "đang chờ" nếu task có dependency.
  - Ghi chú được hiển thị trong detail task và lịch sử thay đổi.
  - Hệ thống không yêu cầu member nhập quá nhiều thông tin để cập nhật nhanh.

#### Story 6
- As a Manager, I want to see workload by assignee and overdue tasks, so that I can rebalance work before delivery slips.
- Acceptance criteria:
  - Board hiển thị số lượng task theo assignee và trạng thái tương ứng.
  - Task quá hạn được đánh dấu rõ ràng và lọc theo ngày.
  - Manager có thể xem tổng số task đang active, completed, blocked của từng member.
  - Không cần export dữ liệu ra spreadsheet để đánh giá tải công việc.

### Epic 3: Thông báo & nhắc việc

#### Story 7
- As a Manager, I want to receive reminders for tasks nearing deadline or overdue, so that I can intervene early.
- Acceptance criteria:
  - Hệ thống gửi cảnh báo trước deadline theo mốc thời gian tùy chọn (ví dụ 1 ngày, 3 ngày, 1 tuần).
  - Task quá hạn được hiển thị trên dashboard và qua email/notification trong app.
  - Người nhận cảnh báo có thể là assignee, manager hoặc cả hai.
  - Cảnh báo không gây spam khi task đã được cập nhật và nợ thời gian không còn.

#### Story 8
- As a Member, I want to receive task reminders and updates from my team, so that I do not miss work or dependency.
- Acceptance criteria:
  - Member nhận thông báo khi task mới được giao, deadline thay đổi hoặc có comment mới.
  - Thông báo được hiển thị trong giao diện web/app và email nếu được bật.
  - Member có thể đánh dấu đã đọc hoặc đóng thông báo.
  - Không gửi duplicate notification cho cùng một sự kiện.

### Epic 4: Báo cáo tiến độ

#### Story 9
- As a Manager, I want to view a summary of board progress by team and by project, so that I can report status quickly to stakeholders.
- Acceptance criteria:
  - Dashboard hiển thị tổng số task, hoàn thành, đang tiến hành, chậm và blockers.
  - Manager có thể lọc theo dự án, team hoặc assignee.
  - Báo cáo có thể xem trong app và export ra CSV/PDF ở mức MVP tối thiểu.
  - Các số liệu hiển thị dựa trên dữ liệu thời gian thực hoặc gần thời gian thực.

#### Story 10
- As a Lead, I want to track blocked tasks and reasons for delay, so that I can support the team and make decisions on priority.
- Acceptance criteria:
  - Task có thể gắn trạng thái "Blocked" và mô tả lý do chặn.
  - Manager có thể lọc task bị block và xem người chịu trách nhiệm, nguyên nhân, thời gian phát sinh.
  - Nếu không có blocker, hệ thống yêu cầu người dùng nhập lý do hoặc xác nhận "không có".
  - Task bị block phải được nhắc nhở trong dashboard và báo cáo weekly.

---

## 5. Core Features (MVP) vs Nice-to-have

### 5.1 Core Features (MVP)

Ưu tiên theo mức độ giải quyết pain point chính:

- Quản lý board Kanban cơ bản
  - Tạo board, cột trạng thái, kéo thả task.
  - Mục tiêu: giải quyết pain point về visibility và thiếu một nguồn dữ liệu thống nhất.

- Tạo task và giao việc
  - Tạo task, gắn owner, deadline, priority, mô tả task.
  - Mục tiêu: giải quyết pain point về task thiếu owner, deadline không rõ và việc phân công không kiểm soát.

- Cập nhật trạng thái và ghi chú
  - Đổi trạng thái task, thêm note, đánh dấu blocker.
  - Mục tiêu: giải quyết pain point về task bị stuck và thiếu thông tin nguyên nhân.

- Dashboard theo dõi tiến độ
  - Tổng quan board, task quá hạn, workload theo assignee.
  - Mục tiêu: giải quyết pain point về báo cáo thủ công và không biết ai đang quá tải.

- Thông báo cơ bản
  - Reminder trước deadline và khi task quá hạn.
  - Mục tiêu: giải quyết pain point về việc phát hiện trễ quá muộn.

### 5.2 Nice-to-have (không bắt buộc ở MVP)

- Tạo nhiều board theo project hoặc team.
- Phân quyền chi tiết theo role và permission granularity cao hơn.
- Comment thread trên task và @mention.
- Export báo cáo PDF/Excel theo ngày hoặc tuần.
- Automations đơn giản như "khi task chuyển sang Done, auto gửi thông báo".
- Tính năng time tracking hoặc estimate effort.
- AI gợi ý ưu tiên task hoặc tóm tắt status.
- Integrations với Slack/Email/Google Calendar.

---

## 6. Edge cases / Rủi ro

- Task không ai nhận: cần cảnh báo rõ trên board và chặn task không thể hoàn thành nếu chưa có owner.
- Board quá nhiều task: cần lọc, sắp xếp, search và hiển thị workload để người dùng không mất thời gian tìm task.
- Người dùng rời nhóm hoặc thay đổi role: task cũ cần được chuyển giao hoặc gán lại owner; lịch sử task không bị mất.
- Deadline thay đổi liên tục: cần lưu lịch sử thay đổi và cập nhật cảnh báo tương ứng.
- Task bị block nhưng không có lý do: cần bắt buộc nhập blocker note hoặc yêu cầu xác nhận.
- Nhiều task cùng lúc cùng assignee: cần hiển thị workload để tránh overloading.
- User có quyền chỉnh sửa nhưng không đúng context: cần role-based permission và kiểm tra quyền rõ ràng.
- Dữ liệu không đồng nhất giữa các board hoặc member: cần quy tắc naming và mô tả task chuẩn để board dễ đọc.
- Notification spam: cần lọc duplicate event và giới hạn số lượng thông báo gửi đi.
- Phần mềm sử dụng bởi team có nhịp làm việc khác nhau: dashboard và reminder phải ưu tiên sạch, ngắn, không làm người dùng bị chán/ảnh hưởng.

---

## 7. Out of Scope

Những tính năng bên dưới KHÔNG nằm trong scope của bản đầu để tránh scope creep:

- Không xây dựng hệ thống chat nội bộ chuyên biệt.
- Không tích hợp full ERP/CRM hay quản lý tài chính dự án.
- Không tính năng theo dõi thời gian làm việc chi tiết đến từng phút.
- Không có dashboard business intelligence phức tạp với nhiều biểu đồ phân tích nâng cao.
- Không có AI sinh báo cáo tự động ở phiên bản đầu.
- Không hỗ trợ roadmap dài hạn hoặc quản lý portfolio nhiều năm.
- Không có tính năng payroll, payroll approval hoặc quỹ thưởng dựa trên hiệu suất.
- Không xây dựng mobile app native ở MVP; ưu tiên web app responsive trước.
- Không phát triển workflow phức tạp nhiều điều kiện ràng buộc theo quy tắc nghiệp vụ.

---

## 8. Yêu cầu chất lượng & điều kiện chấp nhận

- Giao diện dễ học trong vòng 30 phút cho user mới.
- Tối thiểu 90% task có thể được tạo và quản lý bằng thao tác trên board trong 3 bước.
- Hệ thống phải đủ nhanh để cập nhật trạng thái task và hiển thị dashboard trong vòng 2-3 giây ở điều kiện sử dụng bình thường.
- Dữ liệu task phải được lưu an toàn và không mất khi đổi trạng thái hoặc reload trang.
- Chỉ người được phân quyền mới được sửa task, assignment hoặc board settings.

---

## 9. Kết luận

Task Flow không được xây dựng như một công cụ “quản lý công việc” thuần túy; nó cần giải quyết một vấn đề thực tế: team nhỏ dễ mất visibility về tình trạng work, task bị chậm vì thiếu owner, và manager phải báo cáo thủ công mỗi tuần. Nền tảng cần tập trung vào việc làm rõ ownership, trạng thái và deadline, rồi mới mở rộng ra báo cáo và thông báo. Nếu giải quyết được ba lớp vấn đề này, sản phẩm sẽ tạo giá trị ngay trong quá trình vận hành team hàng ngày.
