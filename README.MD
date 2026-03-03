Báo cáo đăng nhập lấy token cho các request API
![alt text](image.png)
Thread Group 1: Kịch bản cơ bản
![alt text](image-1.png)
![alt text](image-2.png)
Thread Group 2: Kịch bản tải nặng
-Trường hợp 1: lấy id của 1 sản phẩm
![alt text](image-3.png)
![alt text](image-4.png)
-Trường hợp 2: lấy danh sách tất cả sản phẩm
![alt text](image-5.png)
![alt text](image-6.png)
Thread Group 3: Kịch bản tùy chỉnh
![alt text](image-7.png)
![alt text](image-8.png)
Báo cáo tổng chạy cả 4 Thread Group
![alt text](image-9.png)
Đánh giá qua: Phần lớn đã chạy thành công tất cả các Thread Group nhưng vẫn ở cột ERROR% vẫn có 30% và 1.33% là do Backend khởi động chậm nên các request API không thể thực hiện được