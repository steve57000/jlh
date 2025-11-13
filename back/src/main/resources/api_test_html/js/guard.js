// pages publiques (pas de redirection si on est déjà sur l’une d’elles)
const PUBLIC_PAGES = ['auth.html', 'login.html'];

document.addEventListener('DOMContentLoaded', () => {
    const page = location.pathname.split('/').pop();
    if (PUBLIC_PAGES.includes(page)) {
        // si déjà connecté, on peut renvoyer vers index
        if (getJwtToken() && page === 'auth.html') {
            window.location.href = 'index.html';
        }
        return;
    }

    // partout ailleurs, on exige un token
    if (!getJwtToken()) {
        window.location.href = 'auth.html';
    }
});