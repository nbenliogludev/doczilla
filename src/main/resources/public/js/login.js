import { redirectIfAuthed, setToken } from './auth.js';
import { mountHeader } from './header.js';

await mountHeader();
redirectIfAuthed();

const form = document.getElementById('login-form');
const statusEl = document.getElementById('auth-status');
const show = (msg, cls='') => { statusEl.textContent = msg; statusEl.className = 'status ' + cls; };

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    show('Вход...');
    try {
        const r = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type':'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!r.ok) throw new Error('Неверные учетные данные');
        const json = await r.json();
        setToken(json.token);
        show('Успешный вход', 'ok');

        const next = new URLSearchParams(location.search).get('next') || '/pages/upload.html';
        location.href = next;
    } catch (err) {
        setToken(null);
        show(err.message || 'Ошибка входа', 'err');
    }
});
