(() => {
    const storageKey = 'pid-hc-theme';
    const root = document.documentElement;

    const applyTheme = (theme) => {
        root.dataset.bsTheme = theme;

        const toggle = document.getElementById('sidebarThemeToggle');

        if (toggle) {
            toggle.checked = theme === 'dark';
        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        applyTheme(localStorage.getItem(storageKey) || 'light');
    });

    document.addEventListener('change', (event) => {
        if (event.target.id !== 'sidebarThemeToggle') return;

        const theme = event.target.checked ? 'dark' : 'light';

        localStorage.setItem(storageKey, theme);
        applyTheme(theme);
    });
})();