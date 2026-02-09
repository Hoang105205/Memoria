## 1. Cấu trúc Cây thư mục (Tree View

```
com.team8.memoria
 ├── MainActivity.java           // Activity chính (Cái vỏ chứa toàn bộ App)
 ├── MainViewModel.java          // Logic dùng chung (Shared State)
 ├── MemoriaApplication.java     // (Optional) Khởi tạo Global (Firebase, Room...)
 │
 ├── data                        // LAYER 1: DỮ LIỆU & LOGIC NỀN
 │    ├── api                    // Chứa Interface gọi API (Retrofit)
 │    ├── database               // Chứa Room Database & DAO (Local DB)
 │    ├── model                  // Chứa các Object dữ liệu (Entity: User, Card, Deck...)
 │    └── repository             // Trung gian quyết định lấy dữ liệu từ API hay DB
 │
 ├── ui                          // LAYER 2: GIAO DIỆN NGƯỜI DÙNG
 │    ├── auth                   // Màn hình Đăng nhập / Đăng ký
 │    ├── home                   // Màn hình Chính (Dashboard, Streak)
 │    ├── search                 // Màn hình Tra từ điển (API Search)
 │    ├── library                // Màn hình Quản lý bộ thẻ (Local DB)
 │    ├── study                  // Màn hình Học & Kiểm tra (Game Logic)
 │    └── profile                // Màn hình Cá nhân & Cài đặt
 │
 └── utils                       // LAYER 3: TIỆN ÍCH HỖ TRỢ
      ├── Constants.java         // Chứa hằng số (API Key, Base URL...)
      └── Helper.java            // Các hàm nhỏ lẻ (Format ngày tháng, Ẩn bàn phím...)
```

## 2. Giải thích chi tiết
### 🏠 Root (Gốc)

- MainActivity.java: Chỉ chứa BottomNavigation và FragmentContainer. Không viết logic nghiệp vụ vào đây.

- MainViewModel.java: Dùng để chia sẻ dữ liệu giữa các màn hình (Ví dụ: Chuyển bộ thẻ từ Library sang Study).

### 🗄️ Package data (Back-end của App)
- model: Nơi chứa các class Java đại diện cho bảng dữ liệu (VD: User.java, Card.java). Bắt buộc dùng Lombok (@Data).

- database: Nơi làm việc với Room DB (Tạo bảng, viết câu lệnh SQL trong DAO).

- api: Nơi khai báo các đường dẫn gọi về Server (VD: DictionaryService.java).

- repository: Class chịu trách nhiệm gọi API hoặc lấy từ DB rồi trả về cho ViewModel.

### 📱 Package ui (Front-end của App)
- Quy tắc: Mỗi tính năng là 1 package con. Bên trong chứa Fragment và ViewModel riêng của tính năng đó.

### 🛠️ Package utils
- Nơi chứa các đoạn code dùng đi dùng lại nhiều nơi (VD: Hàm kiểm tra có mạng hay không, hàm format tiền tệ...).