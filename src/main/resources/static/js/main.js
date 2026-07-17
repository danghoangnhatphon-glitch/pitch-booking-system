
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

function authHeader() {
  const token = localStorage.getItem('token');
  if (token && token !== 'null' && token.split('.').length === 3) {
    return { 'Authorization': 'Bearer ' + token };
  }
  return {};
}

function luuToken(token) {
  localStorage.setItem('token', token);
}

function layToken() {
  return localStorage.getItem('token');
}

/* ── Mobile nav (hamburger) ─────────────────────────────── */
function toggleMobileNav() {
  const links = document.getElementById('navbarLinks');
  const hamburger = document.getElementById('navHamburger');
  if (!links) return;
  const isOpen = links.classList.toggle('open');
  if (hamburger) hamburger.classList.toggle('open', isOpen);
}

// Đóng menu mobile khi bấm 1 link, hoặc khi resize lên desktop
document.addEventListener('click', function(e) {
  const links = document.getElementById('navbarLinks');
  if (!links || !links.classList.contains('open')) return;
  if (e.target.closest('#navbarLinks a') || e.target.closest('#navbarLinks button[type="submit"]')) {
    links.classList.remove('open');
    const hamburger = document.getElementById('navHamburger');
    if (hamburger) hamburger.classList.remove('open');
  }
});
window.addEventListener('resize', function() {
  if (window.innerWidth > 768) {
    const links = document.getElementById('navbarLinks');
    if (links) links.classList.remove('open');
    const hamburger = document.getElementById('navHamburger');
    if (hamburger) hamburger.classList.remove('open');
  }
});

/* ── Dark / Light theme toggle ──────────────────────────── */
function applyThemeIcons() {
  const isLight = document.documentElement.getAttribute('data-theme') === 'light';
  const iconDark = document.getElementById('themeIconDark');
  const iconLight = document.getElementById('themeIconLight');
  if (iconDark) iconDark.style.display = isLight ? 'none' : 'inline';
  if (iconLight) iconLight.style.display = isLight ? 'inline' : 'none';
}

function toggleTheme() {
  const html = document.documentElement;
  const isLight = html.getAttribute('data-theme') === 'light';
  if (isLight) {
    html.removeAttribute('data-theme');
    localStorage.setItem('theme', 'dark');
  } else {
    html.setAttribute('data-theme', 'light');
    localStorage.setItem('theme', 'light');
  }
  applyThemeIcons();
}

document.addEventListener('DOMContentLoaded', applyThemeIcons);
