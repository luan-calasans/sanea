(() => {
  const initSidebar = () => {
    const toggle = document.getElementById('sidebar-toggle');
    const backdrop = document.getElementById('sidebar-backdrop');

    const setOpen = (open) => {
      document.body.classList.toggle('sidebar-open', open);
      if (toggle) {
        toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
      }
      if (backdrop) backdrop.hidden = !open;
    };

    toggle?.addEventListener('click', () => setOpen(!document.body.classList.contains('sidebar-open')));
    backdrop?.addEventListener('click', () => setOpen(false));
    window.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') setOpen(false);
    });

    const path = window.location.pathname.replace(/\/$/, '') || '/';
    const links = Array.from(document.querySelectorAll('.sidebar__link[href]'));
    let best = null;
    let bestLen = -1;
    links.forEach((link) => {
      try {
        const hrefPath = new URL(link.href, window.location.origin).pathname.replace(/\/$/, '') || '/';
        const exact = hrefPath === path;
        const nested = hrefPath !== '/' && path.startsWith(hrefPath + '/');
        if ((exact || nested) && hrefPath.length > bestLen) {
          best = link;
          bestLen = hrefPath.length;
        }
      } catch (_) {
      }
    });
    if (best) {
      best.classList.add('is-active');
      best.setAttribute('aria-current', 'page');
    }
  };

  const initNavProgress = () => {
    const bar = document.getElementById('nav-progress');
    if (!bar) return;

    let timer;
    const start = () => {
      bar.classList.add('is-active');
      bar.style.width = '18%';
      clearInterval(timer);
      timer = setInterval(() => {
        const current = parseFloat(bar.style.width) || 18;
        if (current < 84) bar.style.width = `${current + Math.random() * 10}%`;
      }, 180);
      document.body.classList.add('is-navigating');
    };

    const finish = () => {
      clearInterval(timer);
      bar.style.width = '100%';
      document.body.classList.remove('is-navigating');
      window.setTimeout(() => {
        bar.classList.remove('is-active');
        bar.style.width = '0%';
      }, 180);
    };

    document.addEventListener('click', (event) => {
      const link = event.target.closest('a[href]');
      if (!link) return;
      if (event.defaultPrevented || event.button !== 0) return;
      if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      if (link.target === '_blank' || link.hasAttribute('download')) return;
      const href = link.getAttribute('href');
      if (!href || href.startsWith('#') || href.startsWith('mailto:') || href.startsWith('javascript:')) return;
      try {
        const url = new URL(link.href, window.location.href);
        if (url.origin !== window.location.origin) return;
        if (url.pathname === window.location.pathname && url.search === window.location.search) return;
      } catch (_) {
        return;
      }
      start();
    });

    document.addEventListener('submit', (event) => {
      const form = event.target;
      if (!(form instanceof HTMLFormElement)) return;
      if (form.method && form.method.toLowerCase() === 'get' && !form.action) return;
      start();
    });

    window.addEventListener('pageshow', finish);
  };

  const initPrefetch = () => {
    const prefetched = new Set();
    const prefetch = (href) => {
      if (!href || prefetched.has(href)) return;
      prefetched.add(href);
      const link = document.createElement('link');
      link.rel = 'prefetch';
      link.href = href;
      document.head.appendChild(link);
    };

    document.querySelectorAll('.sidebar-nav a[href]').forEach((anchor) => {
      anchor.addEventListener('pointerenter', () => prefetch(anchor.href), { once: true });
      anchor.addEventListener('focus', () => prefetch(anchor.href), { once: true });
    });
  };

  const initTipoOutro = () => {
    const select = document.getElementById('tipoChamadoId');
    const campo = document.getElementById('campo-tipo-outro');
    const input = document.getElementById('tipoOutro');
    if (!select || !campo || !input) return;

    const sync = () => {
      const option = select.options[select.selectedIndex];
      const isOutros = option && (option.dataset.codigo || '').toUpperCase() === 'OUTROS';
      campo.hidden = !isOutros;
      input.required = isOutros;
      if (!isOutros) input.value = '';
    };

    select.addEventListener('change', sync);
    sync();
  };

  const initCep = () => {
    const cepInput = document.getElementById('cep');
    if (!cepInput) return;

    const status = document.getElementById('cep-status');
    const verified = document.getElementById('enderecoVerificado');
    const fields = ['logradouro', 'bairro', 'cidade', 'uf'].map((id) => document.getElementById(id));

    const setManual = (enabled, message) => {
      fields.forEach((el) => {
        if (!el) return;
        if (enabled) el.readOnly = false;
      });
      if (status) status.textContent = message;
    };

    let timer;
    cepInput.addEventListener('input', () => {
      clearTimeout(timer);
      const digits = cepInput.value.replace(/\D/g, '');
      if (digits.length === 8) {
        cepInput.value = digits.replace(/(\d{5})(\d{3})/, '$1-$2');
      }
      timer = setTimeout(() => consultarCep(digits), 400);
    });

    async function consultarCep(digits) {
      if (digits.length !== 8) return;
      if (status) status.textContent = 'Consultando CEP...';
      try {
        const response = await fetch(`/api/cep/${digits}`, {
          headers: { Accept: 'application/json' }
        });
        if (!response.ok) throw new Error('Falha na consulta');
        const data = await response.json();
        if (data.status === 'ENCONTRADO' && data.endereco) {
          const e = data.endereco;
          const logradouro = document.getElementById('logradouro');
          const bairro = document.getElementById('bairro');
          const cidade = document.getElementById('cidade');
          const uf = document.getElementById('uf');
          if (logradouro) logradouro.value = e.logradouro || '';
          if (bairro) bairro.value = e.bairro || '';
          if (cidade) cidade.value = e.cidade || '';
          if (uf) uf.value = e.uf || '';
          if (verified) verified.value = 'true';
          setManual(false, 'Endereço encontrado');
          fields.forEach((el) => {
            if (el && !el.value) el.readOnly = false;
          });
        } else {
          if (verified) verified.value = 'false';
          setManual(true, 'CEP não encontrado ou serviço indisponível. Preencha o endereço manualmente.');
        }
      } catch (err) {
        if (verified) verified.value = 'false';
        setManual(true, 'Consulta de CEP indisponível. Preencha o endereço manualmente.');
      }
    }
  };

  const initMessages = () => {
    const thread = document.getElementById('mensagem-thread');
    if (!thread) return;

    const chamadoId = thread.dataset.chamadoId;
    let since = thread.dataset.latest || null;
    const poll = async () => {
      try {
        const url = since
          ? `/chamados/${chamadoId}/mensagens.json?desde=${encodeURIComponent(since)}`
          : `/chamados/${chamadoId}/mensagens.json`;
        const res = await fetch(url, { headers: { Accept: 'application/json' } });
        if (!res.ok) return;
        const items = await res.json();
        if (!Array.isArray(items) || items.length === 0) return;
        items.forEach((m) => {
          const article = document.createElement('article');
          article.className = 'message' + (m.visibilidade === 'INTERNA' ? ' interna' : '');
          article.innerHTML = `<div class="message-meta"><span>${escapeHtml(m.perfilAutor)}</span><time>${escapeHtml(m.criadoEm)}</time></div><div>${escapeHtml(m.corpo)}</div>`;
          thread.appendChild(article);
          since = m.criadoEm;
        });
        thread.scrollTop = thread.scrollHeight;
      } catch (_) {
      }
    };
    setInterval(poll, 12000);
  };

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  const boot = () => {
    initSidebar();
    initNavProgress();
    initPrefetch();
    initTipoOutro();
    initCep();
    initMessages();
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
