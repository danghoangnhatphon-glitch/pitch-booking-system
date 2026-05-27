// ================================================================
//  main.js — Utilities dùng chung toàn bộ project
// ================================================================

function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type === 'error' ? 'error' : ''}`;
  toast.textContent = (type === 'error' ? '❌ ' : '✅ ') + message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}

function formatTien(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
}

function formatNgay(dateStr) {
  if (!dateStr) return '—';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
}

/**
 * Header cho fetch() API calls
 *
 * Project dùng FORM LOGIN (session) → không có JWT token
 * → không gửi Authorization header
 * → Spring Security dùng session cookie tự động
 *
 * Chỉ gửi JWT nếu thực sự có token (dùng API login riêng)
 */
function authHeader() {
  const token = localStorage.getItem('token');
  // Chỉ gửi nếu token thực sự tồn tại và đúng định dạng JWT
  if (token && token !== 'null' && token.split('.').length === 3) {
    return { 'Authorization': 'Bearer ' + token };
  }
  // Session login → không cần header, browser tự gửi cookie
  return {};
}

function luuToken(token) {
  localStorage.setItem('token', token);
}

function layToken() {
  return localStorage.getItem('token');
}
