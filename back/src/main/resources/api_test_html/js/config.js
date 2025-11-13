const host = location.hostname === 'localhost' && location.port === '63342'
    ? 'localhost:8080'
    : location.host;
window.API_BASE_URL = `${location.protocol}//${host}/api`;

function getJwtToken() {
    return localStorage.getItem('jwtToken');
}

function authHeaders() {
    const token = getJwtToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    return headers;
}


;(function() {
    const _fetch = window.fetch;
    window.fetch = (input, init = {}) => {
        const url = typeof input === 'string' ? input : input.url;
        if (url.startsWith(window.API_BASE_URL)) {
            const isForm = init.body instanceof FormData;
            const token = localStorage.getItem('jwtToken');
            init.headers = {
                ...(init.headers || {}),
                // seulement JSON
                ...(!isForm ? { 'Content-Type': 'application/json' } : {}),
                // bearer si présent
                ...(token ? { 'Authorization': 'Bearer ' + token } : {})
            };
        }
        return _fetch(input, init);
    };
})();

async function apiFetch(path, options = {}) {
    const url = `${window.API_BASE_URL}${path}`;
    const headers = {
        ...authHeaders(),
        ...(options.headers || {})
    };
    // Si on envoie du JSON, on précise le Content-Type ici
    if (options.body && typeof options.body === 'string') {
        headers['Content-Type'] = 'application/json';
    }
    const response = await fetch(url, { ...options, headers });
    if (!response.ok) {
        const err = new Error(`HTTP ${response.status}`);
        err.response = response;
        throw err;
    }
    return response.json();
}

// Exportez-le globalement
window.apiFetch = apiFetch;