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

    // Apply the saved theme before the page is rendered.
    applyTheme(localStorage.getItem(storageKey) || 'light');

    document.addEventListener('DOMContentLoaded', () => {
        // The sidebar switch is only available after the body has been parsed.
        applyTheme(root.dataset.bsTheme);
    });

    document.addEventListener('change', (event) => {
        if (event.target.id !== 'sidebarThemeToggle') return;

        const theme = event.target.checked ? 'dark' : 'light';

        localStorage.setItem(storageKey, theme);
        applyTheme(theme);
    });
})();
