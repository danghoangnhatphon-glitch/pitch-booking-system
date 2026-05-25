import 'package:flutter/material.dart';

void main() => runApp(const VatLieuApp());

class VatLieuApp extends StatelessWidget {
  const VatLieuApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.orange),
      // Bắt đầu từ màn hình Đăng nhập
      home: const LoginScreen(), 
    );
  }
}

// ---------------- 1. MÀN HÌNH ĐĂNG NHẬP ----------------
class LoginScreen extends StatelessWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.construction, size: 100, color: Colors.orange),
            const Text('QUẢN LÝ VẬT LIỆU', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
            const SizedBox(height: 30),
            const TextField(decoration: InputDecoration(labelText: 'Tài khoản', border: OutlineInputBorder())),
            const SizedBox(height: 15),
            const TextField(obscureText: true, decoration: InputDecoration(labelText: 'Mật khẩu', border: OutlineInputBorder())),
            const SizedBox(height: 20),
            // Nút đăng nhập sử dụng ElevatedButton
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const MainTabScreen())),
                child: const Text('ĐĂNG NHẬP'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------------- 2. MÀN HÌNH CHÍNH (TABS) ----------------
class MainTabScreen extends StatefulWidget {
  const MainTabScreen({super.key});
  @override
  State<MainTabScreen> createState() => _MainTabScreenState();
}

class _MainTabScreenState extends State<MainTabScreen> {
  // Danh sách dữ liệu có thể thay đổi (State) để thực hiện Thêm/Xóa
  List<String> danhSachVatLieu = ['Xi măng Portland', 'Sắt phi 12', 'Gạch ống 4 lỗ'];

  void _themVatLieu() {
    setState(() {
      danhSachVatLieu.add('Vật liệu mới ${danhSachVatLieu.length + 1}');
    });
  }

  void _xoaVatLieu(int index) {
    setState(() {
      danhSachVatLieu.removeAt(index);
    });
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Quản lý Vật liệu'),
          bottom: const TabBar(tabs: [Tab(text: 'Danh sách'), Tab(text: 'Thống kê')]),
        ),
        // 3.2.3.5. Drawer tích hợp Đăng xuất và Trang cá nhân
        drawer: Drawer(
          child: ListView(
            padding: EdgeInsets.zero,
            children: [
              const UserAccountsDrawerHeader(
                decoration: BoxDecoration(color: Colors.orange),
                accountName: Text("Admin App"),
                accountEmail: Text("admin@vatlieu.com"),
                currentAccountPicture: CircleAvatar(backgroundColor: Colors.white, child: Icon(Icons.person, color: Colors.orange)),
              ),
              ListTile(
                leading: const Icon(Icons.person_outline),
                title: const Text('Trang cá nhân'),
                onTap: () {
                  Navigator.pop(context); // Đóng drawer
                  Navigator.push(context, MaterialPageRoute(builder: (context) => const ProfileScreen()));
                },
              ),
              ListTile(
                leading: const Icon(Icons.logout, color: Colors.red),
                title: const Text('Đăng xuất'),
                onTap: () {
                  Navigator.pop(context);
                  Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const LoginScreen()));
                },
              ),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            // TAB 1: DANH SÁCH & XÓA HÀNG
            ListView.builder(
              itemCount: danhSachVatLieu.length,
              itemBuilder: (context, index) {
                return Card(
                  child: ListTile(
                    leading: const Icon(Icons.inventory),
                    title: Text(danhSachVatLieu[index]),
                    trailing: IconButton(
                      icon: const Icon(Icons.delete, color: Colors.red),
                      onPressed: () => _xoaVatLieu(index), // Xóa hàng
                    ),
                  ),
                );
              },
            ),
            const Center(child: Text('Trang thống kê')),
          ],
        ),
        // NÚT THÊM HÀNG
        floatingActionButton: FloatingActionButton(
          onPressed: _themVatLieu,
          child: const Icon(Icons.add),
        ),
      ),
    );
  }
}

// ---------------- 3. TRANG CÁ NHÂN ----------------
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Thông tin cá nhân')),
      body: Center(
        child: Column(
          children: [
            const SizedBox(height: 30),
            const CircleAvatar(radius: 60, child: Icon(Icons.person, size: 60)),
            const SizedBox(height: 20),
            const Text('Họ tên: Nguyễn Văn A', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            const Text('Chức vụ: Quản lý kho'),
            const SizedBox(height: 30),
            OutlinedButton(onPressed: () => Navigator.pop(context), child: const Text('Quay lại'))
          ],
        ),
      ),
    );
  }
}