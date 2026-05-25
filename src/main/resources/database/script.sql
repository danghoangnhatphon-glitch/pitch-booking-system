
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'sanbong_db')
    CREATE DATABASE sanbong_db;
GO

USE sanbong_db;
GO

-- ============================================================
-- BẢNG 1: nguoi_dung
-- Lưu thông tin tất cả người dùng: khách hàng, chủ sân, admin
-- ============================================================
IF OBJECT_ID('nguoi_dung', 'U') IS NOT NULL DROP TABLE nguoi_dung;

CREATE TABLE nguoi_dung (
                            id              BIGINT          IDENTITY(1,1)   PRIMARY KEY,
                            ho_ten          NVARCHAR(100)   NOT NULL,
                            email           NVARCHAR(100)   NOT NULL,
                            mat_khau        NVARCHAR(255)   NOT NULL,           -- BCrypt hash, KHÔNG lưu plaintext
                            so_dien_thoai   NVARCHAR(15),
                            vai_tro         VARCHAR(20)     NOT NULL DEFAULT 'KHACH_HANG'
                                CONSTRAINT chk_vaitro CHECK (vai_tro IN ('KHACH_HANG', 'CHU_SAN', 'ADMIN')),
                            anh_dai_dien    NVARCHAR(500),                      -- URL ảnh
                            is_active       BIT             NOT NULL DEFAULT 1, -- 0 = bị khóa tài khoản
                            created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

                            CONSTRAINT uq_email UNIQUE (email)
);
GO

-- ============================================================
-- BẢNG 2: san_bong
-- Thông tin từng sân bóng
-- ============================================================
IF OBJECT_ID('san_bong', 'U') IS NOT NULL DROP TABLE san_bong;

CREATE TABLE san_bong (
                          id              BIGINT          IDENTITY(1,1)   PRIMARY KEY,
                          ten_san         NVARCHAR(100)   NOT NULL,
                          loai_san        VARCHAR(10)     NOT NULL
                              CONSTRAINT chk_loaisan CHECK (loai_san IN ('SAN_5', 'SAN_7', 'SAN_11')),
                          vi_tri          NVARCHAR(300),                      -- Địa chỉ đầy đủ
                          quan_huyen      NVARCHAR(100),                      -- Dùng để filter theo khu vực
                          mo_ta           NVARCHAR(MAX),
                          anh_san         NVARCHAR(500),                      -- URL ảnh sân
                          trang_thai      VARCHAR(20)     NOT NULL DEFAULT 'HOAT_DONG'
                              CONSTRAINT chk_trangthai_san CHECK (trang_thai IN ('HOAT_DONG', 'DONG_CUA', 'BAO_TRI')),
                          chu_san_id      BIGINT          NOT NULL,            -- FK tới nguoi_dung (vai_tro = CHU_SAN)
                          created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

                          CONSTRAINT fk_san_chusan FOREIGN KEY (chu_san_id) REFERENCES nguoi_dung(id)
);
GO

-- ============================================================
-- BẢNG 3: khung_gio
-- Template các khung giờ, dùng lại cho mọi ngày
-- VD: 17:30-19:00, 19:00-20:30...
-- ============================================================
IF OBJECT_ID('khung_gio', 'U') IS NOT NULL DROP TABLE khung_gio;

CREATE TABLE khung_gio (
                           id                  BIGINT      IDENTITY(1,1)   PRIMARY KEY,
                           gio_bat_dau         TIME        NOT NULL,            -- VD: 17:30:00
                           gio_ket_thuc        TIME        NOT NULL,            -- VD: 19:00:00
                           la_gio_cao_diem     BIT         NOT NULL DEFAULT 0,  -- 1 = giờ cao điểm (đắt hơn)

                           CONSTRAINT chk_gio CHECK (gio_bat_dau < gio_ket_thuc)
);
GO

-- ============================================================
-- BẢNG 4: gia_san
-- Giá mỗi sân theo từng khung giờ
-- Tách riêng để chủ sân có thể tùy chỉnh linh hoạt
-- ============================================================
IF OBJECT_ID('gia_san', 'U') IS NOT NULL DROP TABLE gia_san;

CREATE TABLE gia_san (
                         id              BIGINT          IDENTITY(1,1)   PRIMARY KEY,
                         san_id          BIGINT          NOT NULL,
                         khung_gio_id    BIGINT          NOT NULL,
                         don_gia         DECIMAL(12,0)   NOT NULL,            -- Đơn vị: VNĐ

                         CONSTRAINT fk_giasan_san        FOREIGN KEY (san_id)        REFERENCES san_bong(id),
                         CONSTRAINT fk_giasan_khunggio   FOREIGN KEY (khung_gio_id)  REFERENCES khung_gio(id),

    -- Mỗi sân + khung giờ chỉ có 1 mức giá
                         CONSTRAINT uq_san_khunggio UNIQUE (san_id, khung_gio_id)
);
GO

-- ============================================================
-- BẢNG 5: phieu_dat_san
-- Header của đơn đặt sân (1 phiếu có thể đặt nhiều ca)
-- ============================================================
IF OBJECT_ID('phieu_dat_san', 'U') IS NOT NULL DROP TABLE phieu_dat_san;

CREATE TABLE phieu_dat_san (
                               id                      BIGINT          IDENTITY(1,1)   PRIMARY KEY,
                               nguoi_dat_id            BIGINT          NOT NULL,
                               ngay_tao                DATE            NOT NULL DEFAULT CAST(GETDATE() AS DATE),
                               tong_tien               DECIMAL(12,0)   NOT NULL,

                               trang_thai              VARCHAR(20)     NOT NULL DEFAULT 'CHO_DUYET'
                                   CONSTRAINT chk_trangthai_phieu
                                       CHECK (trang_thai IN ('CHO_DUYET', 'DA_DUYET', 'DA_HUY')),

                               trang_thai_thanh_toan   VARCHAR(20)     NOT NULL DEFAULT 'CHUA_THANH_TOAN'
                                   CONSTRAINT chk_trangthai_tt
                                       CHECK (trang_thai_thanh_toan IN ('CHUA_THANH_TOAN', 'DA_THANH_TOAN', 'HOAN_TIEN')),

                               phuong_thuc_thanh_toan  VARCHAR(30)     -- 'TIEN_MAT', 'CHUYEN_KHOAN', 'MOMO', 'VNPAY'
                                   CONSTRAINT chk_phuongthuc
                                       CHECK (phuong_thuc_thanh_toan IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'MOMO', 'VNPAY')),

                               ghi_chu                 NVARCHAR(500),
                               created_at              DATETIME2       NOT NULL DEFAULT GETDATE(),

                               CONSTRAINT fk_phieu_nguoidat FOREIGN KEY (nguoi_dat_id) REFERENCES nguoi_dung(id)
);
GO

-- ============================================================
-- BẢNG 6: chi_tiet_dat_san  ← TRÁI TIM của hệ thống
-- Mỗi row = 1 ca đặt sân cụ thể (sân X, giờ Y, ngày Z)
-- UNIQUE KEY ở đây là vũ khí chống trùng lịch
-- ============================================================
IF OBJECT_ID('chi_tiet_dat_san', 'U') IS NOT NULL DROP TABLE chi_tiet_dat_san;

CREATE TABLE chi_tiet_dat_san (
                                  id              BIGINT          IDENTITY(1,1)   PRIMARY KEY,
                                  phieu_dat_id    BIGINT          NOT NULL,
                                  san_id          BIGINT          NOT NULL,
                                  khung_gio_id    BIGINT          NOT NULL,
                                  ngay_su_dung    DATE            NOT NULL,
                                  don_gia         DECIMAL(12,0)   NOT NULL,   -- Lưu giá tại thời điểm đặt (giá có thể thay đổi sau)

                                  CONSTRAINT fk_chitiet_phieu    FOREIGN KEY (phieu_dat_id)  REFERENCES phieu_dat_san(id),
                                  CONSTRAINT fk_chitiet_san      FOREIGN KEY (san_id)        REFERENCES san_bong(id),
                                  CONSTRAINT fk_chitiet_khunggio FOREIGN KEY (khung_gio_id)  REFERENCES khung_gio(id),

    -- ✅ CHÌA KHÓA CHỐNG TRÙNG LỊCH
    -- DB sẽ từ chối bất kỳ ai cố ghi trùng sân + giờ + ngày
                                  CONSTRAINT uq_san_khunggio_ngay UNIQUE (san_id, khung_gio_id, ngay_su_dung)
);
GO

-- ============================================================
-- BẢNG 7: danh_gia  (optional, nâng tầm project)
-- Khách hàng đánh giá sau khi đã đá
-- ============================================================
IF OBJECT_ID('danh_gia', 'U') IS NOT NULL DROP TABLE danh_gia;

CREATE TABLE danh_gia (
                          id              BIGINT          IDENTITY(1,1)   PRIMARY KEY,
                          phieu_dat_id    BIGINT          NOT NULL,
                          nguoi_danh_gia  BIGINT          NOT NULL,
                          san_id          BIGINT          NOT NULL,
                          so_sao          TINYINT         NOT NULL
                              CONSTRAINT chk_sosao CHECK (so_sao BETWEEN 1 AND 5),
                          noi_dung        NVARCHAR(1000),
                          created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

                          CONSTRAINT fk_danhgia_phieu     FOREIGN KEY (phieu_dat_id)      REFERENCES phieu_dat_san(id),
                          CONSTRAINT fk_danhgia_nguoidung FOREIGN KEY (nguoi_danh_gia)    REFERENCES nguoi_dung(id),
                          CONSTRAINT fk_danhgia_san       FOREIGN KEY (san_id)            REFERENCES san_bong(id),

    -- Mỗi phiếu chỉ được đánh giá 1 lần
                          CONSTRAINT uq_danhgia_phieu UNIQUE (phieu_dat_id)
);
GO

-- ============================================================
-- INDEX — Tăng tốc các truy vấn phổ biến
-- ============================================================

-- Tìm sân theo khu vực (người dùng filter)
CREATE INDEX idx_san_quanhuyen    ON san_bong (quan_huyen);
CREATE INDEX idx_san_loaisan      ON san_bong (loai_san);
CREATE INDEX idx_san_trangthai    ON san_bong (trang_thai);

-- Tìm lịch đặt theo ngày (query phổ biến nhất)
CREATE INDEX idx_chitiet_ngay     ON chi_tiet_dat_san (ngay_su_dung);
CREATE INDEX idx_chitiet_san_ngay ON chi_tiet_dat_san (san_id, ngay_su_dung);

-- Tìm lịch sử đặt của 1 người dùng
CREATE INDEX idx_phieu_nguoidat   ON phieu_dat_san (nguoi_dat_id);
CREATE INDEX idx_phieu_ngaytao    ON phieu_dat_san (ngay_tao);
GO

-- ============================================================
-- DỮ LIỆU MẪU (seed data)
-- ============================================================

-- Tài khoản mẫu (password đều là "123456" đã BCrypt)
INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, vai_tro) VALUES
(N'Admin Hệ Thống',  'admin@sanbong.vn',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkCPZEWD64m', '0900000001', 'ADMIN'),
(N'Nguyễn Văn Chủ',  'chusan@sanbong.vn',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkCPZEWD64m', '0900000002', 'CHU_SAN'),
(N'Trần Văn Khách',  'khach1@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkCPZEWD64m', '0912345678', 'KHACH_HANG'),
(N'Lê Thị Bình',     'khach2@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LkCPZEWD64m', '0987654321', 'KHACH_HANG');
GO

-- Sân bóng mẫu (chu_san_id = 2 là Nguyễn Văn Chủ)
INSERT INTO san_bong (ten_san, loai_san, vi_tri, quan_huyen, mo_ta, chu_san_id) VALUES
(N'Sân 5 Số 1',  'SAN_5',  N'123 Nguyễn Trãi, P.2',       N'Quận 5',       N'Sân cỏ nhân tạo, đèn LED cao cấp',  2),
(N'Sân 5 Số 2',  'SAN_5',  N'456 Lê Văn Sỹ, P.14',        N'Quận 3',       N'Sân mới, mái che toàn bộ',           2),
(N'Sân 7 Số 1',  'SAN_7',  N'789 Hoàng Văn Thụ, P.4',     N'Quận Phú Nhuận', N'Sân 7 tiêu chuẩn, có phòng thay đồ', 2),
(N'Sân 11 Lớn',  'SAN_11', N'12 Đường D1, KĐT Vạn Phúc',  N'Thủ Đức',     N'Sân chuẩn thi đấu, khán đài 200 chỗ', 2);
GO

-- Khung giờ mẫu
INSERT INTO khung_gio (gio_bat_dau, gio_ket_thuc, la_gio_cao_diem) VALUES
('06:00', '07:30', 0),  -- id=1, sáng sớm, giờ thấp điểm
('07:30', '09:00', 0),  -- id=2, sáng, giờ thấp điểm
('09:00', '10:30', 0),  -- id=3, sáng muộn, thấp điểm
('15:00', '16:30', 0),  -- id=4, chiều sớm, thấp điểm
('16:30', '18:00', 1),  -- id=5, chiều tối, CAO ĐIỂM
('18:00', '19:30', 1),  -- id=6, tối, CAO ĐIỂM
('19:30', '21:00', 1),  -- id=7, tối muộn, CAO ĐIỂM
('21:00', '22:30', 0);  -- id=8, khuya, thấp điểm
GO

-- Giá sân (sân 5 - id=1)
INSERT INTO gia_san (san_id, khung_gio_id, don_gia) VALUES
(1, 1, 150000), (1, 2, 150000), (1, 3, 150000), (1, 4, 180000),
(1, 5, 250000), (1, 6, 280000), (1, 7, 280000), (1, 8, 200000);

-- Giá sân (sân 5 - id=2)
INSERT INTO gia_san (san_id, khung_gio_id, don_gia) VALUES
                                                        (2, 1, 140000), (2, 2, 140000), (2, 3, 140000), (2, 4, 170000),
                                                        (2, 5, 230000), (2, 6, 260000), (2, 7, 260000), (2, 8, 190000);

-- Giá sân (sân 7 - id=3)
INSERT INTO gia_san (san_id, khung_gio_id, don_gia) VALUES
                                                        (3, 1, 250000), (3, 2, 250000), (3, 3, 250000), (3, 4, 280000),
                                                        (3, 5, 400000), (3, 6, 450000), (3, 7, 450000), (3, 8, 350000);

-- Giá sân (sân 11 - id=4)
INSERT INTO gia_san (san_id, khung_gio_id, don_gia) VALUES
                                                        (4, 5, 700000), (4, 6, 800000), (4, 7, 800000), (4, 8, 600000);
GO

-- Phiếu đặt mẫu (để test giao diện lịch sử)
INSERT INTO phieu_dat_san (nguoi_dat_id, ngay_tao, tong_tien, trang_thai, trang_thai_thanh_toan, phuong_thuc_thanh_toan)
VALUES (3, CAST(GETDATE() AS DATE), 280000, 'DA_DUYET', 'DA_THANH_TOAN', 'CHUYEN_KHOAN');

INSERT INTO chi_tiet_dat_san (phieu_dat_id, san_id, khung_gio_id, ngay_su_dung, don_gia)
VALUES (1, 1, 6, CAST(GETDATE() AS DATE), 280000);
GO

-- ============================================================
-- STORED PROCEDURE — Kiểm tra sân còn trống
-- Dùng trong backend trước khi cho phép đặt
-- ============================================================
CREATE OR ALTER PROCEDURE sp_KiemTraSanTrong
    @san_id         BIGINT,
    @khung_gio_id   BIGINT,
    @ngay_su_dung   DATE
    AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM chi_tiet_dat_san
        WHERE san_id        = @san_id
          AND khung_gio_id  = @khung_gio_id
          AND ngay_su_dung  = @ngay_su_dung
    )
SELECT 0 AS con_trong, N'Sân đã được đặt' AS thong_bao;
ELSE
SELECT 1 AS con_trong, N'Sân còn trống'   AS thong_bao;
END;
GO

-- ============================================================
-- VIEW — Lịch đặt sân theo ngày (dùng cho dashboard chủ sân)
-- ============================================================
CREATE OR ALTER VIEW v_lich_dat_san AS
SELECT
    ct.ngay_su_dung,
    sb.ten_san,
    sb.loai_san,
    kg.gio_bat_dau,
    kg.gio_ket_thuc,
    kg.la_gio_cao_diem,
    ct.don_gia,
    nd.ho_ten       AS ten_nguoi_dat,
    nd.so_dien_thoai,
    p.trang_thai    AS trang_thai_phieu,
    p.trang_thai_thanh_toan
FROM chi_tiet_dat_san ct
         JOIN san_bong       sb  ON sb.id  = ct.san_id
         JOIN khung_gio      kg  ON kg.id  = ct.khung_gio_id
         JOIN phieu_dat_san  p   ON p.id   = ct.phieu_dat_id
         JOIN nguoi_dung     nd  ON nd.id  = p.nguoi_dat_id;
GO

-- ============================================================
-- QUERY MẪU — hay dùng trong code Java
-- ============================================================

-- 1. Xem tất cả khung giờ của 1 sân trong 1 ngày + trạng thái trống/bận
/*
SELECT
    kg.id           AS khung_gio_id,
    kg.gio_bat_dau,
    kg.gio_ket_thuc,
    kg.la_gio_cao_diem,
    gs.don_gia,
    CASE WHEN ct.id IS NULL THEN 1 ELSE 0 END AS con_trong
FROM khung_gio kg
JOIN gia_san gs ON gs.khung_gio_id = kg.id AND gs.san_id = 1
LEFT JOIN chi_tiet_dat_san ct
    ON ct.khung_gio_id = kg.id
    AND ct.san_id = 1
    AND ct.ngay_su_dung = '2025-06-01'
ORDER BY kg.gio_bat_dau;
*/

-- 2. Thống kê doanh thu theo tháng (dashboard chủ sân)
/*
SELECT
    YEAR(p.ngay_tao)    AS nam,
    MONTH(p.ngay_tao)   AS thang,
    COUNT(*)            AS so_phieu,
    SUM(p.tong_tien)    AS doanh_thu
FROM phieu_dat_san p
WHERE p.trang_thai = 'DA_DUYET'
  AND p.trang_thai_thanh_toan = 'DA_THANH_TOAN'
GROUP BY YEAR(p.ngay_tao), MONTH(p.ngay_tao)
ORDER BY nam DESC, thang DESC;
*/
GO

