(async function() {
    // 1️⃣ Charger config.js et guard.js dynamiquement
    await loadScript('../js/config.js');
    await loadScript('../js/guard.js');

    // 2️⃣ Injecter le menu (nav.html) si présent
    const navPlaceholder = document.getElementById('nav-placeholder');
    if (navPlaceholder) {
        try {
            navPlaceholder.innerHTML = await fetch('../includes/nav.html').then(r => r.text());
        } catch (e) {
            console.error('Impossible de charger le menu :', e);
        }
    }
})();

function loadScript(src) {
    return new Promise((resolve, reject) => {
        const s = document.createElement('script');
        s.src = src;
        s.onload = resolve;
        s.onerror = reject;
        document.head.appendChild(s);
    });
}
