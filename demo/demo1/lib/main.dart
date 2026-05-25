import 'package:flutter/material.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Thuyết trình Flutter Tuần 5',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const HomeScreen(),
    );
  }
}

// =============================================================
// 3.2.5. ỨNG DỤNG WIDGET XÂY DỰNG GIAO DIỆN (MÀN HÌNH CHÍNH)
// =============================================================
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold( // Nhóm C: Scaffold đóng vai trò "khung xương" 
      appBar: AppBar(title: const Text('Màn hình chính')),
      
      // 3.2.3.5. DRAWER (THANH ĐIỀU HƯỚNG BÊN) 
      drawer: Drawer(
        child: ListView( // Dùng ListView để nội dung không bị tràn và hỗ trợ cuộn 
          padding: EdgeInsets.zero,
          children: <Widget>[
            // DrawerHeader: Hiển thị thông tin tài khoản 
            const DrawerHeader(
              decoration: BoxDecoration(color: Colors.blue),
              child: Text(
                'Tài khoản của tôi',
                style: TextStyle(color: Colors.white, fontSize: 24),
              ),
            ),
            // ListTile: Widget tiêu chuẩn cho mục menu 
            ListTile(
              leading: const Icon(Icons.home),
              title: const Text('Trang chủ'),
              onTap: () {
                // Quy trình điều hướng chuẩn: Bước 1 - Đóng Drawer 
                Navigator.pop(context); 
              },
            ),
            ListTile(
              leading: const Icon(Icons.person),
              title: const Text('Trang cá nhân'),
              onTap: () {
                // Bước 1: Đóng Drawer 
                Navigator.pop(context); 
                // Bước 2: Chuyển màn hình mới 
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const ProfileScreen()),
                );
              },
            ),
          ],
        ),
      ),

      // 3.2.3.6. MỘT SỐ CÁC WIDGETS KHÁC & LAYOUT 
      body: SingleChildScrollView( // Tránh lỗi Overflow (tràn màn hình) 
        child: Column( // Sắp xếp theo chiều dọc 
          children: [
            // A. Nhóm hiển thị thông tin 
            const Padding(
              padding: EdgeInsets.all(20.0),
              child: Text(
                'A. Nhóm hiển thị thông tin',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
            ),
            const Text('Đây là widget Text với TextStyle tùy chỉnh'),
            const Icon(Icons.star, color: Colors.orange, size: 50), // Widget Icon 
            Image.network( // Widget Image lấy từ internet 
              'https://tinyurl.com/flutter-icon-bin', 
              height: 100,
            ),

            const Divider(height: 50),

            // B. Nhóm tương tác (Buttons) 
            const Text(
              'B. Nhóm tương tác (Buttons)',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            Row( // Sắp xếp theo chiều ngang 
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton( // Nút có đổ bóng cho hành động chính 
                  onPressed: () {}, 
                  child: const Text('Elevated'),
                ),
                OutlinedButton( // Nút có viền cho hành động phụ 
                  onPressed: () {}, 
                  child: const Text('Outlined'),
                ),
                IconButton( // Chỉ bao gồm biểu tượng 
                  onPressed: () {}, 
                  icon: const Icon(Icons.thumb_up),
                ),
              ],
            ),

            const Divider(height: 50),

            // C. Nhóm bố cục và chứa đựng 
            const Text(
              'C. Nhóm bố cục (Container)',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            Container( // Widget đa năng để bao bọc và vẽ viền 
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.blue[50],
                borderRadius: BorderRadius.circular(15),
                border: Border.all(color: Colors.blue),
              ),
              child: const Text('Dữ liệu nằm trong một Container'),
            ),
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }
}

// =============================================================
// MÀN HÌNH PHỤ ĐỂ MINH HỌA ĐIỀU HƯỚNG
// =============================================================
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Trang cá nhân'), 
        backgroundColor: Colors.green,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.person_pin, size: 100, color: Colors.green),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Quay lại Màn hình chính'),
            ),
          ],
        ),
      ),
    );
  }
}