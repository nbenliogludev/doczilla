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

const showUploadStatus = (msg, ok=false) => {
    uploadStatus.textContent = msg;
    uploadStatus.className = 'status ' + (ok ? 'ok' : 'err');
};

const resetProgress = () => {
    progressWrap.classList.add('hidden');
    progressBar.style.width = '0%';
};

const showResult = (url) => {
    resultBox.classList.remove('hidden');
    downloadLink.href = url;
    downloadLink.textContent = location.origin + url;
};

uploadForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const token = getToken();
    if (!fileInput.files || !fileInput.files[0]) { showUploadStatus('Выберите файл'); return; }

    resultBox.classList.add('hidden');
    uploadStatus.textContent = 'Загрузка...';
    uploadStatus.className = 'status';
    progressWrap.classList.remove('hidden');
    progressBar.style.width = '0%';

    const form = new FormData();
    form.append('file', fileInput.files[0]);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', '/api/upload', true);
    xhr.setRequestHeader('Authorization', 'Bearer ' + token);

    xhr.upload.onprogress = (evt) => {
        if (evt.lengthComputable) {
            progressBar.style.width = Math.round((evt.loaded / evt.total) * 100) + '%';
        }
    };

    xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
            try {
                const json = JSON.parse(xhr.responseText);
                showUploadStatus('Файл загружен', true);
                showResult(json.downloadUrl);
                uploadForm.reset();
                resetProgress();
            } catch {
                showUploadStatus('Ошибка разбора ответа');
            }
        } else if (xhr.status === 401) {
            window.location.href = '/pages/login.html?next=' + encodeURIComponent('/pages/upload.html');
        } else {
            showUploadStatus('Ошибка загрузки: ' + xhr.status);
        }
    };

    xhr.onerror = () => showUploadStatus('Сетевая ошибка');

    xhr.send(form);
});

copyBtn.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(downloadLink.href);
        uploadStatus.textContent = 'Ссылка скопирована';
        uploadStatus.className = 'status ok';
    } catch {
        uploadStatus.textContent = 'Не удалось скопировать';
        uploadStatus.className = 'status err';
    }
});

resetProgress();
