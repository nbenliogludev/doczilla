// ====== простая локальная "сессия" токена ======
const TOKEN_KEY = 'doczilla.token';

function setToken(t) {
    if (t) localStorage.setItem(TOKEN_KEY, t);
    else localStorage.removeItem(TOKEN_KEY);
    updateAuthUI();
}

function getToken() {
    return localStorage.getItem(TOKEN_KEY) || '';
}

// ====== элементы ======
const registerForm   = document.getElementById('register-form');
const registerStatus = document.getElementById('register-status');

const loginForm   = document.getElementById('login-form');
const logoutBtn   = document.getElementById('logout-btn');
const authStatus  = document.getElementById('auth-status');

const uploadCard  = document.getElementById('upload-card');
const uploadForm  = document.getElementById('upload-form');
const fileInput   = document.getElementById('file-input');
const progressWrap= document.getElementById('progress-wrap');
const progressBar = document.getElementById('progress-bar');
const uploadStatus= document.getElementById('upload-status');
const resultBox   = document.getElementById('result');
const downloadLink= document.getElementById('download-link');
const copyBtn     = document.getElementById('copy-btn');

// ====== UI helpers ======
function updateAuthUI() {
    const has = !!getToken();
    authStatus.textContent = has ? 'Вы вошли' : 'Не авторизованы';
    authStatus.className = `status ${has ? 'ok' : 'err'}`;
    logoutBtn.classList.toggle('hidden', !has);
    // загрузка доступна только после авторизации
    uploadCard.style.display = has ? '' : 'none';
}

function showStatus(el, msg, type) {
    el.textContent = msg || '';
    el.className = `status ${type || ''}`;
}

function showUploadStatus(msg, ok = false) {
    showStatus(uploadStatus, msg, ok ? 'ok' : 'err');
}

function resetProgress() {
    progressWrap.classList.add('hidden');
    progressBar.style.width = '0%';
}

function showResult(url) {
    resultBox.classList.remove('hidden');
    downloadLink.href = url;
    downloadLink.textContent = location.origin + url;
}

// ====== Регистрация ======
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('reg-username').value.trim();
        const password = document.getElementById('reg-password').value;

        showStatus(registerStatus, 'Регистрация...', '');

        try {
            const r = await fetch('/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const json = await r.json().catch(() => ({}));
            if (!r.ok) {
                throw new Error(json.error || 'Ошибка регистрации');
            }

            // автологин
            setToken(json.token);
            showStatus(registerStatus, 'Аккаунт создан. Вы вошли.', 'ok');
            registerForm.reset();
        } catch (err) {
            showStatus(registerStatus, err.message || 'Ошибка регистрации', 'err');
        }
    });
}

// ====== Вход ======
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        showStatus(authStatus, 'Вход...', '');

        try {
            const r = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!r.ok) {
                // сервер может вернуть text/plain → просто бросаем по статусу
                throw new Error('Неверные учетные данные');
            }
            const json = await r.json();
            setToken(json.token);
            showStatus(authStatus, 'Успешный вход', 'ok');
            loginForm.reset();
        } catch (err) {
            setToken(null);
            showStatus(authStatus, err.message || 'Ошибка входа', 'err');
        }
    });
}

logoutBtn.addEventListener('click', () => {
    setToken(null);
    showStatus(authStatus, 'Вы вышли', '');
    resultBox.classList.add('hidden');
    resetProgress();
});

// ====== Загрузка файла с прогрессом ======
if (uploadForm) {
    uploadForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const token = getToken();
        if (!token) {
            showUploadStatus('Сначала войдите', false);
            return;
        }
        if (!fileInput.files || !fileInput.files[0]) {
            showUploadStatus('Выберите файл', false);
            return;
        }

        resultBox.classList.add('hidden');
        showStatus(uploadStatus, 'Загрузка...', '');
        progressWrap.classList.remove('hidden');
        progressBar.style.width = '0%';

        const form = new FormData();
        form.append('file', fileInput.files[0]);

        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/api/upload', true);
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);

        xhr.upload.onprogress = (evt) => {
            if (evt.lengthComputable) {
                const pct = Math.round((evt.loaded / evt.total) * 100);
                progressBar.style.width = pct + '%';
            }
        };

        xhr.onload = () => {
            if (xhr.status >= 200 && xhr.status < 300) {
                try {
                    const json = JSON.parse(xhr.responseText);
                    showUploadStatus('Файл загружен', true);
                    showResult(json.downloadUrl);
                    resetProgress();
                    uploadForm.reset();
                } catch {
                    showUploadStatus('Ошибка разбора ответа', false);
                }
            } else if (xhr.status === 401) {
                showUploadStatus('Авторизация просрочена. Войдите снова.', false);
            } else {
                showUploadStatus('Ошибка загрузки: ' + xhr.status, false);
            }
        };

        xhr.onerror = () => {
            showUploadStatus('Сетевая ошибка', false);
        };

        xhr.send(form);
    });
}

// ====== Копирование ссылки ======
if (copyBtn) {
    copyBtn.addEventListener('click', async () => {
        const url = downloadLink.href;
        try {
            await navigator.clipboard.writeText(url);
            showStatus(uploadStatus, 'Ссылка скопирована', 'ok');
        } catch {
            showStatus(uploadStatus, 'Не удалось скопировать', 'err');
        }
    });
}

// init
updateAuthUI();
resetProgress();
