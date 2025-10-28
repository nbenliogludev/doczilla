import { isAuthed, setToken } from './auth.js';

export async function mountHeader() {
    const host = document.getElementById('app-header');
    if (!host) return;

    try {
        const res = await fetch('/components/header.html', { cache: 'no-store' });
        host.innerHTML = await res.text();
    } catch {
        host.innerHTML = '<div class="header"><strong>Doczilla</strong></div>';
    }

    const btnLogout = document.getElementById('nav-logout');
    const linkLogin = document.getElementById('nav-login');
    const linkReg   = document.getElementById('nav-register');
    const linkUp    = document.getElementById('nav-upload');

    if (isAuthed()) {
        btnLogout?.classList.remove('hidden');
        linkLogin?.classList.add('hidden');
        linkReg?.classList.add('hidden');
        linkUp?.classList.remove('hidden');
    } else {
        btnLogout?.classList.add('hidden');
        linkLogin?.classList.remove('hidden');
        linkReg?.classList.remove('hidden');
        linkUp?.classList.add('hidden');
    }

    btnLogout?.addEventListener('click', () => {
        setToken(null);
        window.location.href = '/pages/login.html';
    });
}
