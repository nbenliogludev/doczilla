import { requireAuthOrRedirect, getToken } from './auth.js';
import { mountHeader } from './header.js';

await mountHeader();
requireAuthOrRedirect();

const uploadForm   = document.getElementById('upload-form');
const fileInput    = document.getElementById('file-input');
const progressWrap = document.getElementById('progress-wrap');
const progressBar  = document.getElementById('progress-bar');
const uploadStatus = document.getElementById('upload-status');
const resultBox    = document.getElementById('result');
const downloadLink = document.getElementById('download-link');
const copyBtn      = document.getElementById('copy-btn');

const historyList  = document.getElementById('history-list');
const historyEmpty = document.getElementById('history-empty');
const clearBtn     = document.getElementById('clear-history');

const LS_KEY = 'doczilla.links'; // [{url, name, size, ts}]

// ---------- helpers ----------
const resetProgress = () => {
    progressWrap.classList.add('hidden');
    progressBar.style.width = '0%';
};

const showUploadStatus = (msg, ok = false) => {
    uploadStatus.textContent = msg;
    uploadStatus.className = 'status ' + (ok ? 'ok' : 'err');
};

const showResult = (url) => {
    const absolute = new URL(url, location.origin).toString();
    downloadLink.href = absolute;
    downloadLink.textContent = absolute;
    resultBox.classList.remove('hidden');
    copyBtn.disabled = false;
};

const hideResult = () => {
    resultBox.classList.add('hidden');
    downloadLink.href = '#';
    downloadLink.textContent = '';
    copyBtn.disabled = true;
};

const humanSize = (bytes) => {
    if (bytes == null) return '';
    const units = ['Б','КБ','МБ','ГБ','ТБ'];
    let i = 0, n = Number(bytes);
    while (n >= 1024 && i < units.length - 1) { n /= 1024; i++; }
    return `${n.toFixed(n >= 10 ? 0 : 1)} ${units[i]}`;
};

// ---------- history (localStorage) ----------
const loadHistory = () => {
    try { return JSON.parse(localStorage.getItem(LS_KEY) || '[]'); }
    catch { return []; }
};

const saveHistory = (items) => {
    localStorage.setItem(LS_KEY, JSON.stringify(items));
};

const addToHistory = ({ url, name, size }) => {
    const items = loadHistory();
    const entry = {
        url: new URL(url, location.origin).toString(),
        name: name || '(без имени)',
        size: size ?? null,
        ts: Date.now()
    };
    items.unshift(entry);           // newest first
    saveHistory(items.slice(0, 100)); // cap to 100
    renderHistory();
};

const renderHistory = () => {
    const items = loadHistory();
    historyList.innerHTML = '';
    if (items.length === 0) {
        historyEmpty.style.display = 'block';
        return;
    }
    historyEmpty.style.display = 'none';
    for (const item of items) {
        const li = document.createElement('li');
        li.className = 'list-item';

        const a = document.createElement('a');
        a.href = item.url;
        a.target = '_blank';
        a.rel = 'noopener';
        a.textContent = item.url;

        const meta = document.createElement('div');
        meta.className = 'muted';
        const date = new Date(item.ts).toLocaleString();
        meta.textContent = `${item.name} • ${humanSize(item.size)} • ${date}`;

        const copy = document.createElement('button');
        copy.className = 'link-btn';
        copy.type = 'button';
        copy.textContent = 'Копировать';
        copy.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(item.url);
                showUploadStatus('Ссылка скопирована', true);
            } catch {
                showUploadStatus('Не удалось скопировать');
            }
        });

        const row = document.createElement('div');
        row.className = 'row';
        row.appendChild(a);
        row.appendChild(copy);

        li.appendChild(row);
        li.appendChild(meta);
        historyList.appendChild(li);
    }
};

clearBtn.addEventListener('click', () => {
    localStorage.removeItem(LS_KEY);
    renderHistory();
});

// initial UI state
hideResult();
resetProgress();
copyBtn.disabled = true;
renderHistory();

// clear stale link on file selection
fileInput.addEventListener('change', () => {
    hideResult();
    showUploadStatus('', false);
    resetProgress();
});

// ---------- upload flow ----------
uploadForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const token = getToken();
    const file = fileInput.files?.[0];

    hideResult();
    resetProgress();
    progressWrap.classList.remove('hidden');
    progressBar.style.width = '0%';

    if (!file) {
        showUploadStatus('Выберите файл');
        return;
    }

    const form = new FormData();
    form.append('file', file);

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
                addToHistory({ url: json.downloadUrl, name: file.name, size: file.size });
                uploadForm.reset();
                resetProgress();
            } catch {
                hideResult();
                showUploadStatus('Ошибка разбора ответа');
            }
        } else if (xhr.status === 401) {
            window.location.href = '/pages/login.html?next=' + encodeURIComponent('/pages/upload.html');
        } else {
            hideResult();
            showUploadStatus('Ошибка загрузки: ' + xhr.status);
        }
    };

    xhr.onerror = () => {
        hideResult();
        showUploadStatus('Сетевая ошибка');
    };

    xhr.send(form);
});

// copy latest link
copyBtn.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(downloadLink.href);
        showUploadStatus('Ссылка скопирована', true);
    } catch {
        showUploadStatus('Не удалось скопировать');
    }
});
