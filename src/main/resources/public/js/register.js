import { redirectIfAuthed, setToken } from './auth.js';
import { mountHeader } from './header.js';

await mountHeader();
redirectIfAuthed();

const form = document.getElementById('register-form');
const statusEl = document.getElementById('register-status');
const show = (msg, cls='') => { statusEl.textContent = msg; statusEl.className = 'status ' + cls; };

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value;

    show('Регистрация...');
    try {
        const r = await fetch('/api/register', {
            method: 'POST',
            headers: { 'Content-Type':'application/json' },
            body: JSON.stringify({ username, password })
        });
        const json = await r.json().catch(() => ({}));
        if (!r.ok) throw new Error(json.error || 'Ошибка регистрации');

        setToken(json.token);
        show('Аккаунт создан. Перенаправление...', 'ok');
        setTimeout(() => location.href = '/pages/upload.html', 350);
    } catch (err) {
        show(err.message || 'Ошибка регистрации', 'err');
    }
});
