const TOKEN_KEY = 'doczilla.token';

export function setToken(t) {
    if (t) localStorage.setItem(TOKEN_KEY, t);
    else localStorage.removeItem(TOKEN_KEY);
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY) || '';
}

export function isAuthed() {
    return !!getToken();
}

// Guards
export function requireAuthOrRedirect() {
    if (!isAuthed()) {
        window.location.href = '/pages/login.html?next=' + encodeURIComponent(location.pathname);
    }
}

export function redirectIfAuthed() {
    if (isAuthed()) {
        window.location.href = '/pages/upload.html';
    }
}
