(() => {
  const DISMISS_KEY = 'sanea.pwa.install.dismissed';

  const registerServiceWorker = () => {
    if (!('serviceWorker' in navigator)) return;
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js', { scope: '/' }).catch(() => {});
    });
  };

  const initInstallPrompt = () => {
    let deferredPrompt = null;

    window.addEventListener('beforeinstallprompt', (event) => {
      event.preventDefault();
      if (localStorage.getItem(DISMISS_KEY) === '1') return;
      if (window.matchMedia('(display-mode: standalone)').matches) return;
      if (navigator.standalone) return;
      deferredPrompt = event;
      showBanner(() => deferredPrompt);
    });

    window.addEventListener('appinstalled', () => {
      localStorage.setItem(DISMISS_KEY, '1');
      removeBanner();
      deferredPrompt = null;
    });
  };

  const showBanner = (getPrompt) => {
    if (document.getElementById('pwa-install-banner')) return;

    const banner = document.createElement('div');
    banner.id = 'pwa-install-banner';
    banner.className = 'pwa-install';
    banner.setAttribute('role', 'dialog');
    banner.setAttribute('aria-label', 'Instalar aplicativo');
    banner.innerHTML = `
      <div class="pwa-install__body">
        <strong>Instalar o Sanea</strong>
        <span>Acesse mais rápido, como um app no celular ou computador.</span>
      </div>
      <div class="pwa-install__actions">
        <button type="button" class="btn btn-secondary btn-sm" data-pwa-dismiss>Agora não</button>
        <button type="button" class="btn btn-primary btn-sm" data-pwa-install>Instalar</button>
      </div>
    `;
    document.body.appendChild(banner);
    requestAnimationFrame(() => banner.classList.add('is-visible'));

    banner.querySelector('[data-pwa-dismiss]')?.addEventListener('click', () => {
      localStorage.setItem(DISMISS_KEY, '1');
      removeBanner();
    });

    banner.querySelector('[data-pwa-install]')?.addEventListener('click', async () => {
      const promptEvent = getPrompt();
      if (!promptEvent) {
        removeBanner();
        return;
      }
      promptEvent.prompt();
      try {
        await promptEvent.userChoice;
      } catch (_) {
      }
      removeBanner();
    });
  };

  const removeBanner = () => {
    const banner = document.getElementById('pwa-install-banner');
    if (!banner) return;
    banner.classList.remove('is-visible');
    window.setTimeout(() => banner.remove(), 220);
  };

  registerServiceWorker();
  initInstallPrompt();
})();
