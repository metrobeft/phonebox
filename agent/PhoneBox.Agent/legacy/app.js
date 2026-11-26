/* PhoneBox Legacy Web App — Pure ES5 for Android 4.2.2 (HP 7.1 1201, WebKit 534.30)
   No let/const, no arrow functions, no fetch, no Promise, no template literals, no ES6 methods. */
(function () {
  'use strict';

  var DEVICE_ID = 'hp71tablet';
  var DEVICE_MODEL = 'HP 7.1 1201 (Web)';
  var APP_TAG = 'legacy-web';

  var NAV_SVGS = {
    home: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 10.5L12 3l9 7.5"></path><path d="M5 10v10h14V10"></path></svg>',
    system: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="12" rx="2"></rect><path d="M8 20h8M12 16v4"></path></svg>',
    deck: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"></rect><rect x="14" y="3" width="7" height="7" rx="1"></rect><rect x="3" y="14" width="7" height="7" rx="1"></rect><rect x="14" y="14" width="7" height="7" rx="1"></rect></svg>',
    procs: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="4" y1="6" x2="20" y2="6"></line><line x1="4" y1="12" x2="20" y2="12"></line><line x1="4" y1="18" x2="20" y2="18"></line></svg>',
    ai: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15 9 22 12 15 15 12 22 9 15 2 12 9 9 12 2"></polygon></svg>',
    discord: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>',
    world: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"></circle><polyline points="12 7 12 12 15 15"></polyline></svg>',
    custom: '<svg class="nav-ico" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="3"></rect><line x1="9" y1="3" x2="9" y2="21"></line></svg>'
  };

  var DEFAULT_ACTIVE_PAGES = [
    { id: 'home', title: 'Inicio', icon: 'home', viewIdx: 0, domId: 'page-home', idleSec: null, pageCfg: null },
    { id: 'system', title: 'Sistema', icon: 'system', viewIdx: 1, domId: 'page-system', idleSec: null, pageCfg: null },
    { id: 'deck', title: 'Atalhos', icon: 'deck', viewIdx: 2, domId: 'page-deck', idleSec: null, pageCfg: null },
    { id: 'procs', title: 'Processos', icon: 'procs', viewIdx: 3, domId: 'page-procs', idleSec: null, pageCfg: null },
    { id: 'ai', title: 'IA', icon: 'ai', viewIdx: 4, domId: 'page-ai', idleSec: null, pageCfg: null },
    { id: 'discord', title: 'Discord', icon: 'discord', viewIdx: 5, domId: 'page-discord', idleSec: null, pageCfg: null },
    { id: 'world', title: 'Clima/Hora', icon: 'world', viewIdx: 6, domId: 'page-night', idleSec: null, pageCfg: null }
  ];

  var SAKURA_THEME = { bg: '#090516', bar: 'rgba(20, 12, 43, 0.94)', card: 'rgba(24, 16, 52, 0.80)', cardAlt: 'rgba(33, 22, 69, 0.84)', focal: 'rgba(40, 25, 84, 0.86)', inset: 'rgba(17, 10, 38, 0.86)', stripe: 'rgba(46, 29, 92, 0.70)', border: '#6b4c9a', line: '#442f6e', text: '#fff5fd', muted: '#c0a2e8', accent: '#ff7ed4', isAnime: true, world: 'sakura', art: '/legacy/theme_anime.png', starGold: '#ffd56b', gridCol: 'rgba(199, 146, 255, 0.25)', ramCol: '#c792ff', gpuCol: '#5bf5b8', tempCol: '#ffd56b' };
  var THEMES = {
    'default': { bg: '#0b1427', bar: '#101e36', card: '#152641', cardAlt: '#12223d', focal: '#203c63', inset: '#1d3455', stripe: '#29476f', border: '#354f76', line: '#2b4267', text: '#f2f7ff', muted: '#b2c5e1' },
    'sakura': SAKURA_THEME,
    'anime': SAKURA_THEME,
    'lofi': { bg: '#130a1e', bar: 'rgba(29, 16, 44, 0.94)', card: 'rgba(33, 19, 48, 0.82)', cardAlt: 'rgba(42, 24, 61, 0.84)', focal: 'rgba(53, 30, 74, 0.86)', inset: 'rgba(21, 11, 34, 0.86)', stripe: 'rgba(58, 34, 79, 0.70)', border: '#835584', line: '#4f3058', text: '#fff4ec', muted: '#c9a6c4', accent: '#ffb38a', isAnime: true, world: 'lofi', art: '/legacy/theme_lofi.png', starGold: '#ffe066', gridCol: 'rgba(255, 179, 138, 0.24)', ramCol: '#d4a5ff', gpuCol: '#9fe3c9', tempCol: '#ffe066' },
    'starfall': { bg: '#020512', bar: 'rgba(7, 18, 43, 0.94)', card: 'rgba(10, 24, 51, 0.80)', cardAlt: 'rgba(16, 34, 71, 0.84)', focal: 'rgba(19, 43, 87, 0.86)', inset: 'rgba(6, 16, 36, 0.86)', stripe: 'rgba(22, 50, 97, 0.70)', border: '#2e6aa6', line: '#1d436e', text: '#f0fbff', muted: '#88b4dc', accent: '#7fe7ff', isAnime: true, world: 'starfall', art: '/legacy/theme_starfall.png', starGold: '#6dffb0', gridCol: 'rgba(127, 231, 255, 0.24)', ramCol: '#b57cff', gpuCol: '#6dffb0', tempCol: '#ffe066' },
    'miku': { bg: '#031015', bar: 'rgba(6, 28, 36, 0.94)', card: 'rgba(7, 32, 41, 0.80)', cardAlt: 'rgba(10, 43, 54, 0.84)', focal: 'rgba(13, 54, 68, 0.86)', inset: 'rgba(4, 20, 26, 0.86)', stripe: 'rgba(15, 61, 76, 0.70)', border: '#218688', line: '#134f57', text: '#eefffe', muted: '#79c4c7', accent: '#39c5bb', isAnime: true, world: 'miku', art: '/legacy/theme_miku.png', starGold: '#ff2e88', gridCol: 'rgba(57, 197, 187, 0.25)', ramCol: '#ff5ca8', gpuCol: '#5bf5b8', tempCol: '#ffd866' },
    'hack': { bg: '#000000', bar: 'rgba(2, 10, 5, 0.94)', card: 'rgba(3, 16, 8, 0.80)', cardAlt: 'rgba(5, 22, 12, 0.84)', focal: 'rgba(6, 31, 17, 0.86)', inset: 'rgba(1, 10, 5, 0.88)', stripe: 'rgba(9, 36, 20, 0.72)', border: '#15693b', line: '#0e3e22', text: '#e8fff0', muted: '#3fb874', accent: '#00ff66', isHack: true },
    'amoled': { bg: '#000000', bar: '#050608', card: '#0a0c10', cardAlt: '#07090d', border: '#1a202c', line: '#141923', text: '#f1f5f9', muted: '#8592a6' },
    'midnight': { bg: '#060d1f', bar: '#0a1530', card: '#0f1d3d', cardAlt: '#0c1733', border: '#1e3361', line: '#192b52', text: '#eaf2ff', muted: '#8da7cc' },
    'warm': { bg: '#120e0b', bar: '#1a1410', card: '#221a15', cardAlt: '#1c1611', border: '#3a2d23', line: '#30251d', text: '#f5ece4', muted: '#a89687' },
    'graphite': { bg: '#111317', bar: '#171a20', card: '#1d2128', cardAlt: '#181c22', border: '#2e3440', line: '#262b35', text: '#eceff4', muted: '#9aa5b4' }
  };

  var DAYS_PT = ['domingo', 'segunda-feira', 'terca-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sabado'];
  var MONTHS_PT = ['janeiro', 'fevereiro', 'marco', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];
  var WMO_PT = {
    0: 'Ceu limpo', 1: 'Poucas nuvens', 2: 'Parcialmente nublado', 3: 'Nublado',
    45: 'Neblina', 48: 'Nevoeiro', 51: 'Garoa leve', 53: 'Garoa', 55: 'Garoa forte',
    61: 'Chuva fraca', 63: 'Chuva', 65: 'Chuva forte', 80: 'Pancadas leves',
    81: 'Pancadas de chuva', 82: 'Chuva intensa', 95: 'Tempestade', 96: 'Tempestade', 99: 'Tempestade forte'
  };

  var state = {
    page: 0,
    prevPage: 0,
    activePages: DEFAULT_ACTIVE_PAGES.slice(0),
    navSignature: '',
    isPreview: false,
    previewConfig: false,
    accentColor: '#8bd0ff',
    themeColors: THEMES['default'],
    vpH: 460,
    orientMode: 'auto', // auto | landscape | portrait
    dimLevel: 0,
    sortProcs: 'ram',
    procsLimit: 15,
    lastProcs: null,
    lastProcsSig: '',
    lastDisksSig: '',
    lastHwSig: '',
    dcSubTab: 'chans',
    dcGuild: 'all',
    dcOnlyOccupied: false,
    dcOnlySpeaking: false,
    dcSearch: '',
    dcExpanded: {},
    dcLastData: null,
    dcSigTop: '',
    dcSigMembers: '',
    dcSigChans: '',
    dcSigNotifs: '',
    cfgVersion: '',
    config: null,
    lastState: null,
    lastQuotas: null,
    lastWork: null,
    lastPush: null,
    weather: null,
    evSeq: 0,
    manualOrient: false,
    manualDim: false,
    autoNightActive: false,
    idleDimmed: false,
    lastTouchAt: new Date().getTime(),
    artKey: '',
    confirmKill: '',
    confirmKillAt: 0,
    activeApproval: null,
    failCount: 0,
    histCpu: [],
    histRam: [],
    histGpu: [],
    histTemp: [],
    customCache: {},
    pomo: { running: false, secLeft: 1500, totalSec: 1500 },
    inFlight: {},
    activeXhr: {}
  };

  var MAX_HIST = 60;
  var toastTimer = null;
  var aiTimer = null;

  function $(id) { return document.getElementById(id); }

  function storeGet(key, fallback) {
    try {
      var v = window.localStorage.getItem('pb_hp71_' + key);
      return v !== null ? v : fallback;
    } catch (e) { return fallback; }
  }

  function storeSet(key, val) {
    try { window.localStorage.setItem('pb_hp71_' + key, String(val)); } catch (e) { }
  }

  function pad2(n) { return (n < 10 ? '0' : '') + n; }

  function round1(n) {
    if (typeof n !== 'number' || isNaN(n)) return '--';
    return String(Math.round(n * 10) / 10);
  }

  function fmtBytesPerSec(bps) {
    if (typeof bps !== 'number' || isNaN(bps) || bps < 0) return '0 B/s';
    if (bps >= 1048576) return round1(bps / 1048576) + ' MB/s';
    if (bps >= 1024) return Math.round(bps / 1024) + ' KB/s';
    return Math.round(bps) + ' B/s';
  }

  function fmtUptime(sec) {
    if (typeof sec !== 'number' || isNaN(sec) || sec < 0) return '--';
    var d = Math.floor(sec / 86400);
    var h = Math.floor((sec % 86400) / 3600);
    var m = Math.floor((sec % 3600) / 60);
    if (d > 0) return d + 'd ' + h + 'h ' + m + 'm';
    if (h > 0) return h + 'h ' + m + 'm';
    return m + 'm';
  }

  function escHtml(s) {
    if (s === null || s === undefined) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function showToast(msg, level, durationMs) {
    var el = $('toast');
    if (!el) return;
    el.textContent = msg;
    var lvCls = '';
    if (level === 'ok') lvCls = ' toast-ok';
    else if (level === 'warn') lvCls = ' toast-warn';
    else if (level === 'error') lvCls = ' toast-error';
    el.className = 'toast' + lvCls;
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { el.className = 'toast hidden'; }, durationMs || 2600);
  }

  function addClass(el, cls) {
    if (!el) return;
    if ((' ' + el.className + ' ').indexOf(' ' + cls + ' ') === -1) {
      el.className = (el.className ? el.className + ' ' : '') + cls;
    }
  }

  function removeClass(el, cls) {
    if (!el) return;
    el.className = (' ' + el.className + ' ').replace(' ' + cls + ' ', ' ').replace(/^\s+|\s+$/g, '');
  }

  // ==================== XHR WRAPPER (PURE ES5 + WATCHDOG) ====================
  function apiRequest(method, url, key, onOk, onErr) {
    var now = new Date().getTime();
    if (key && state.inFlight[key]) {
      if ((now - state.inFlight[key]) < 4000) return;
      if (state.activeXhr[key]) {
        try { state.activeXhr[key].abort(); } catch (e) { }
        state.activeXhr[key] = null;
      }
      state.inFlight[key] = 0;
    }

    var xhr = new XMLHttpRequest();
    if (key) {
      state.inFlight[key] = now;
      state.activeXhr[key] = xhr;
    }

    var sep = url.indexOf('?') === -1 ? '?' : '&';
    var finalUrl = url + sep + '_t=' + now;
    xhr.open(method, finalUrl, true);
    try { xhr.timeout = 3800; } catch (e) { }
    try {
      if (!state.isPreview) {
        xhr.setRequestHeader('X-PhoneBox-Device', DEVICE_ID);
        xhr.setRequestHeader('X-PhoneBox-Model', DEVICE_MODEL);
        xhr.setRequestHeader('X-PhoneBox-App', APP_TAG);
      }
    } catch (e) { }

    var finished = false;
    var watchdog = null;

    function done(ok, data, errMsg) {
      if (finished) return;
      finished = true;
      if (watchdog) {
        clearTimeout(watchdog);
        watchdog = null;
      }
      if (key) {
        state.inFlight[key] = 0;
        state.activeXhr[key] = null;
      }
      if (ok) {
        if (onOk) {
          try { onOk(data); } catch (e) { }
        }
      } else {
        if (onErr) {
          try { onErr(data, errMsg || ''); } catch (e) { }
        }
      }
    }

    watchdog = setTimeout(function () {
      try { xhr.abort(); } catch (e) { }
      done(false, 'timeout', '');
    }, 3800);

    xhr.onreadystatechange = function () {
      if (xhr.readyState !== 4) return;
      if (xhr.status >= 200 && xhr.status < 300) {
        var text = xhr.responseText || '';
        if (!text) { done(true, null); return; }
        var parsed = null;
        var parseOk = false;
        try {
          parsed = JSON.parse(text);
          parseOk = true;
        } catch (e) { }
        if (parseOk) done(true, parsed);
        else done(false, 'json', '');
      } else {
        var errMsg = '';
        try {
          if (xhr.responseText && xhr.responseText.charAt(0) === '{') {
            var ej = JSON.parse(xhr.responseText);
            errMsg = ej.error || ej.detail || '';
          }
        } catch (e) { }
        done(false, xhr.status, errMsg);
      }
    };
    xhr.ontimeout = function () { done(false, 'timeout', ''); };
    xhr.onerror = function () { done(false, 'network', ''); };

    try { xhr.send(null); }
    catch (e) { done(false, 'send', ''); }
  }

  // ==================== ORIENTATION & RESPONSIVE LAYOUT (ANDROID 4.2.2 48px NAV BAR AWARE) ====================
  function updateDiscordScrollBounds() {
    var vpH = state.vpH || 460;
    var isPort = document.body.className.indexOf('orient-portrait') !== -1;
    var topCard = null;
    var pg = $('page-discord');
    if (pg) {
      var cards = pg.getElementsByTagName('div');
      for (var i = 0; i < cards.length; i++) {
        if ((' ' + cards[i].className + ' ').indexOf(' dc-top-card ') !== -1) {
          topCard = cards[i];
          break;
        }
      }
    }
    var topH = (topCard && topCard.offsetHeight > 40) ? topCard.offsetHeight : (isPort ? 168 : 122);
    var avail = vpH - topH - 22;
    var memMax, chanMax, notifMax;
    if (isPort) {
      memMax = Math.max(150, Math.min(240, Math.round(vpH * 0.27)));
      chanMax = Math.max(200, Math.min(390, Math.round(vpH * 0.42)));
      notifMax = Math.max(200, Math.min(380, Math.round(vpH * 0.40)));
    } else {
      memMax = Math.max(120, avail - 44);
      chanMax = Math.max(120, avail - 118);
      notifMax = Math.max(120, avail - 56);
    }
    if ($('dc-members-scroll')) $('dc-members-scroll').style.maxHeight = memMax + 'px';
    if ($('dc-channels-scroll')) $('dc-channels-scroll').style.maxHeight = chanMax + 'px';
    if ($('dc-notifs-scroll')) $('dc-notifs-scroll').style.maxHeight = notifMax + 'px';
  }

  function updateLayout() {
    var w = window.innerWidth || document.documentElement.clientWidth || 1024;
    var h = window.innerHeight || document.documentElement.clientHeight || 600;
    var isLand;
    if (state.orientMode === 'landscape') isLand = true;
    else if (state.orientMode === 'portrait') isLand = false;
    else isLand = (w >= h);

    var navTop = !!(state.config && state.config.display && state.config.display.controlsTop);
    var isCompact = isLand && h <= 565;
    var isUltra = isLand && h <= 500;
    var bodyCls = isLand ? 'orient-landscape' : 'orient-portrait';
    if (isCompact) bodyCls += ' compact-h';
    if (isUltra) bodyCls += ' ultra-compact-h';
    if (navTop) bodyCls += ' nav-top';
    document.body.className = bodyCls;

    var topH = isLand ? (isUltra ? 32 : (isCompact ? 34 : 38)) : 40;
    var navH = isLand ? (isUltra ? 44 : (isCompact ? 48 : 54)) : 54;
    var vp = $('viewport');
    if (vp) {
      var vpH = h - topH - navH;
      if (vpH < 180) vpH = 180;
      state.vpH = vpH;
      vp.style.top = (navTop ? (topH + navH) : topH) + 'px';
      vp.style.bottom = (navTop ? 0 : navH) + 'px';
      vp.style.height = vpH + 'px';
    }

    var obtn = $('btn-orient');
    if (obtn) {
      obtn.textContent = state.orientMode === 'auto' ? 'AUTO' : (state.orientMode === 'landscape' ? 'HORIZ' : 'VERT');
      if (state.orientMode !== 'auto') addClass(obtn, 'active');
      else removeClass(obtn, 'active');
    }

    updateDiscordScrollBounds();
    redrawCharts();
    try {
      if (isHackThemeActive()) {
        drawMatrixRainFrame(false);
        drawGlitchOverlayFrame(false);
        drawHackTopTerminal(false);
      } else if (isAnimeThemeActive()) {
        drawAnimeSkyFrame(false);
        drawAnimeSparkleFrame(false);
        drawAnimeTopRibbon(false);
      }
    } catch (e) { }
  }

  // ==================== DYNAMIC PAGES, NAVIGATION & SWIPE ====================
  function activePageObj() {
    if (!state.activePages || !state.activePages.length) return DEFAULT_ACTIVE_PAGES[0];
    return state.activePages[state.page] || state.activePages[0];
  }

  function activeViewIdx() {
    var p = activePageObj();
    return p ? p.viewIdx : 0;
  }

  function findTabByViewIdx(vIdx) {
    for (var i = 0; i < state.activePages.length; i++) {
      if (state.activePages[i].viewIdx === vIdx) return i;
    }
    return -1;
  }

  function findTabByPageId(pageId) {
    var key = String(pageId || '').toLowerCase();
    if (!key) return -1;
    var i;
    for (i = 0; i < state.activePages.length; i++) {
      if (String(state.activePages[i].id || '').toLowerCase() === key) return i;
    }
    var num = parseInt(key, 10);
    if (!isNaN(num) && num >= 1 && num <= state.activePages.length) return num - 1;
    var alias = {
      'home': 0,
      'system': 1, 'dashboard': 1, 'status': 1,
      'deck': 2, 'shortcuts': 2, 'audio-deck': 2, 'system-deck': 2,
      'procs': 3, 'processes': 3,
      'ai': 4, 'codexbar': 4, 'work': 4, 'devops': 4,
      'discord': 5,
      'world': 6, 'clock': 6, 'ambient': 6, 'music': 6, 'nightstand': 6, 'daily': 6, 'weekend': 6, 'calm': 6, 'planner': 6
    };
    if (typeof alias[key] === 'number') return findTabByViewIdx(alias[key]);
    return -1;
  }

  function resolveBuiltInView(p) {
    var id = String((p && p.id) || '').toLowerCase();
    if (id === 'home') return { viewIdx: 0, domId: 'page-home', ico: 'home' };
    if (id === 'system' || id === 'dashboard' || id === 'status') return { viewIdx: 1, domId: 'page-system', ico: 'system' };
    if (id === 'deck' || id === 'shortcuts' || id === 'audio-deck' || id === 'system-deck') return { viewIdx: 2, domId: 'page-deck', ico: 'deck' };
    if (id === 'procs' || id === 'processes') return { viewIdx: 3, domId: 'page-procs', ico: 'procs' };
    if (id === 'ai' || id === 'codexbar' || id === 'work' || id === 'devops') return { viewIdx: 4, domId: 'page-ai', ico: 'ai' };
    if (id === 'discord') return { viewIdx: 5, domId: 'page-discord', ico: 'discord' };
    if (id === 'world' || id === 'clock' || id === 'ambient' || id === 'nightstand') return { viewIdx: 6, domId: 'page-night', ico: 'world' };

    var ws = (p && p.widgets) ? p.widgets : [];
    var hasShortcuts = false, hasProcs = false, hasAi = false, hasDiscord = false, hasWeather = false, hasMedia = false, hasChart = false;
    for (var i = 0; i < ws.length; i++) {
      var wt = ws[i] && ws[i].type;
      if (wt === 'shortcuts' || wt === 'audio-mixer') hasShortcuts = true;
      else if (wt === 'processes' || wt === 'topprocs') hasProcs = true;
      else if (wt === 'quotas' || wt === 'codexbar' || wt === 'sessions') hasAi = true;
      else if (wt === 'discord') hasDiscord = true;
      else if (wt === 'weather' || wt === 'forecast' || wt === 'worldclock') hasWeather = true;
      else if (wt === 'media') hasMedia = true;
      else if (wt === 'chart' || wt === 'disks') hasChart = true;
    }
    if (hasDiscord && ws.length <= 2) return { viewIdx: 5, domId: 'page-discord', ico: 'discord' };
    if (hasProcs && ws.length <= 2) return { viewIdx: 3, domId: 'page-procs', ico: 'procs' };
    if (hasShortcuts && !hasMedia && ws.length <= 3) return { viewIdx: 2, domId: 'page-deck', ico: 'deck' };
    if (hasAi && !hasMedia && !hasChart) return { viewIdx: 4, domId: 'page-ai', ico: 'ai' };
    if (hasWeather && !hasMedia && !hasChart) return { viewIdx: 6, domId: 'page-night', ico: 'world' };
    return null;
  }

  function syncPagesFromConfig(cfg) {
    if (!cfg || !cfg.pages || !cfg.pages.length) return;
    var curObj = activePageObj();
    var curId = curObj ? curObj.id : '';
    var usedDom = {};
    var nextPages = [];
    var vp = $('viewport');
    var i;

    for (i = 0; i < cfg.pages.length; i++) {
      var p = cfg.pages[i];
      if (!p || p.enabled === false) continue;
      var resolved = resolveBuiltInView(p);
      var pid = p.id || ('p' + i);
      if (resolved && !usedDom[resolved.domId]) {
        usedDom[resolved.domId] = true;
        nextPages.push({
          id: pid,
          title: p.title || pid,
          icon: resolved.ico,
          rawIcon: p.icon || '',
          viewIdx: resolved.viewIdx,
          domId: resolved.domId,
          idleSec: typeof p.idleSec === 'number' ? p.idleSec : null,
          pageCfg: p
        });
      } else {
        var customDomId = 'page-custom-' + String(pid).replace(/[^a-zA-Z0-9_-]/g, '_') + '-' + i;
        usedDom[customDomId] = true;
        if (vp && !$(customDomId)) {
          var sec = document.createElement('section');
          sec.id = customDomId;
          sec.className = 'page page-custom';
          vp.appendChild(sec);
        }
        nextPages.push({
          id: pid,
          title: p.title || ('Pagina ' + (nextPages.length + 1)),
          icon: 'custom',
          rawIcon: p.icon || '',
          viewIdx: -1,
          domId: customDomId,
          idleSec: typeof p.idleSec === 'number' ? p.idleSec : null,
          pageCfg: p
        });
      }
    }

    if (!nextPages.length) nextPages = DEFAULT_ACTIVE_PAGES.slice(0);

    // Clean up obsolete custom sections
    if (vp) {
      var secs = vp.getElementsByTagName('section');
      for (i = secs.length - 1; i >= 0; i--) {
        if (secs[i].id && secs[i].id.indexOf('page-custom-') === 0 && !usedDom[secs[i].id]) {
          vp.removeChild(secs[i]);
        }
      }
    }

    state.activePages = nextPages;
    rebuildNavbar();

    var targetIdx = curId ? findTabByPageId(curId) : state.page;
    if (targetIdx < 0 || targetIdx >= state.activePages.length) targetIdx = 0;
    setPage(targetIdx, true);
  }

  function rebuildNavbar() {
    var nav = $('navbar');
    if (!nav) return;
    var list = state.activePages;
    var sig = '';
    var i;
    for (i = 0; i < list.length; i++) {
      sig += list[i].id + '|' + list[i].title + '|' + list[i].icon + '|' + (list[i].rawIcon || '') + ';';
    }
    if (sig === state.navSignature) return;
    state.navSignature = sig;

    var pctW = (Math.floor(10000 / Math.max(1, list.length)) / 100) + '%';
    var html = '';
    for (i = 0; i < list.length; i++) {
      var pg = list[i];
      var icoHtml = NAV_SVGS[pg.icon] || NAV_SVGS.custom;
      if (pg.viewIdx === -1 && pg.rawIcon) {
        icoHtml = '<span class="nav-ico-txt">' + escHtml(pg.rawIcon) + '</span>';
      }
      html += '<button type="button" class="nav-btn' + (i === state.page ? ' active' : '') + '" data-page="' + i + '" style="width:' + pctW + '">'
        + icoHtml + '<span>' + escHtml(pg.title) + '</span></button>';
    }
    nav.innerHTML = html;

    var btns = nav.getElementsByTagName('button');
    for (i = 0; i < btns.length; i++) {
      btns[i].onclick = function () {
        setPage(parseInt(this.getAttribute('data-page'), 10));
      };
    }
  }

  function setPage(idx, silent) {
    var len = state.activePages.length || 1;
    if (idx < 0) idx = len - 1;
    if (idx >= len) idx = 0;
    if (idx !== state.page) state.prevPage = state.page;
    var tabChanged = (idx !== state.page);
    state.page = idx;
    if (!state.isPreview) storeSet('page', idx);

    var cur = activePageObj();
    var vp = $('viewport');
    var i;
    if (vp) {
      var secs = vp.getElementsByTagName('section');
      for (i = 0; i < secs.length; i++) {
        secs[i].className = secs[i].id === cur.domId ? 'page active' : 'page';
      }
      if (!silent) vp.scrollTop = 0;
    }

    var nav = $('navbar');
    if (nav) {
      var btns = nav.getElementsByTagName('button');
      for (i = 0; i < btns.length; i++) {
        var p = parseInt(btns[i].getAttribute('data-page'), 10);
        btns[i].className = p === idx ? 'nav-btn active' : 'nav-btn';
      }
    }

    if (aiTimer) {
      clearInterval(aiTimer);
      aiTimer = null;
    }

    var vIdx = cur.viewIdx;
    if (vIdx === 1) {
      if (state.lastState && state.lastState.pc) {
        try { renderDisks(state.lastState.pc.disks || []); } catch (e) { }
        try { renderHardwareDetails(state.lastState.pc, state.lastState.pc.gpu || null, state.lastState.ping || {}); } catch (e) { }
      }
      setTimeout(redrawCharts, 30);
    }
    if (vIdx === 2) pollControls();
    if (vIdx === 3) pollProcs();
    if (vIdx === 4) {
      pollAiWork();
      aiTimer = setInterval(function () {
        if (activeViewIdx() === 4) pollAiWork();
      }, 5000);
    }
    if (vIdx === 5) {
      pollDiscord();
      setTimeout(updateDiscordScrollBounds, 30);
    }
    if (vIdx === 6) setTimeout(redrawCharts, 30);
    if (vIdx === -1) {
      renderCustomPage(cur);
      pollCustomPageWidgets(cur);
    }
    if (tabChanged && !silent) {
      try { triggerHackTabGlitch(); } catch (e) { }
      try { triggerAnimeTabFx(); } catch (e) { }
    }
  }

  function setupSwipe() {
    var vp = $('viewport');
    if (!vp) return;
    var startX = 0, startY = 0, tracking = false;

    vp.addEventListener('touchstart', function (e) {
      if (!e.touches || e.touches.length !== 1) return;
      startX = e.touches[0].clientX;
      startY = e.touches[0].clientY;
      tracking = true;
    }, false);

    vp.addEventListener('touchend', function (e) {
      if (!tracking || !e.changedTouches || !e.changedTouches.length) return;
      tracking = false;
      var dx = e.changedTouches[0].clientX - startX;
      var dy = e.changedTouches[0].clientY - startY;
      if (Math.abs(dx) > 65 && Math.abs(dx) > Math.abs(dy) * 1.6) {
        if (dx < 0) setPage(state.page + 1);
        else setPage(state.page - 1);
      }
    }, false);
  }

  // ==================== CLOCK, WORD CLOCK, WORLD CLOCKS, CALENDAR & DIMMER ====================
  var HOURS_WORDS_PT = ['meia-noite', 'uma', 'duas', 'tres', 'quatro', 'cinco', 'seis', 'sete', 'oito', 'nove', 'dez', 'onze', 'meio-dia'];
  var DAYS_SHORT_PT = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sab'];
  var TZ_OFFSETS_HOURS = {
    'America/Sao_Paulo': -3, 'America/Rio_Branco': -5, 'America/Manaus': -4, 'America/Noronha': -2,
    'America/New_York': -5, 'America/Chicago': -6, 'America/Denver': -7, 'America/Los_Angeles': -8,
    'America/Argentina/Buenos_Aires': -3, 'America/Santiago': -3, 'America/Mexico_City': -6,
    'Europe/Lisbon': 0, 'Europe/London': 0, 'UTC': 0, 'Etc/UTC': 0,
    'Europe/Paris': 1, 'Europe/Berlin': 1, 'Europe/Madrid': 1, 'Europe/Rome': 1,
    'Europe/Tallinn': 2, 'Europe/Athens': 2, 'Europe/Istanbul': 3, 'Europe/Moscow': 3,
    'Asia/Dubai': 4, 'Asia/Kolkata': 5.5, 'Asia/Bangkok': 7, 'Asia/Singapore': 8,
    'Asia/Shanghai': 8, 'Asia/Tokyo': 9, 'Asia/Seoul': 9, 'Australia/Sydney': 11
  };

  function wordClockPt(h24, m) {
    var h12 = h24 % 12;
    var period = h24 < 6 ? ' da madrugada' : (h24 < 12 ? ' da manha' : (h24 < 18 ? ' da tarde' : ' da noite'));
    var hWord = (h24 === 0) ? 'Meia-noite' : ((h24 === 12) ? 'Meio-dia' : HOURS_WORDS_PT[h12]);
    hWord = hWord.charAt(0).toUpperCase() + hWord.substring(1);
    var mRound = Math.round(m / 5) * 5;
    if (mRound === 60) mRound = 55;
    var mText = '';
    if (mRound === 0) mText = (h24 === 0 || h24 === 12) ? ' em ponto' : ' horas';
    else if (mRound === 15) mText = ' e quinze';
    else if (mRound === 30) mText = ' e meia';
    else if (mRound === 45) mText = ' e quarenta e cinco';
    else mText = ' e ' + mRound + ' min';
    if (h24 === 0 || h24 === 12) period = '';
    return hWord + mText + period;
  }

  function formatHourMin(h24, m, use12h) {
    if (!use12h) return { hm: pad2(h24) + ':' + pad2(m), suffix: '' };
    var ampm = h24 >= 12 ? 'PM' : 'AM';
    var h = h24 % 12;
    if (h === 0) h = 12;
    return { hm: pad2(h) + ':' + pad2(m), suffix: ' ' + ampm };
  }

  function lastCalendarDayKey() {
    var d = new Date();
    return d.getFullYear() + '-' + d.getMonth() + '-' + d.getDate();
  }
  var renderedCalDayKey = '';

  function updateClock() {
    var now = new Date();
    var disp = (state.config && state.config.display) ? state.config.display : {};
    var showSec = disp.showSeconds !== false;
    var use12h = disp.hour12 === true || disp.hour12 === 'true';

    var f = formatHourMin(now.getHours(), now.getMinutes(), use12h);
    var ss = pad2(now.getSeconds());
    var dateStr = DAYS_PT[now.getDay()] + ', ' + now.getDate() + ' de ' + MONTHS_PT[now.getMonth()] + ' de ' + now.getFullYear();

    if ($('home-clock')) $('home-clock').textContent = f.hm;
    if ($('home-sec')) {
      $('home-sec').textContent = (showSec ? ':' + ss : '') + f.suffix;
      $('home-sec').style.display = (showSec || f.suffix) ? 'inline-block' : 'none';
    }
    if ($('home-date')) $('home-date').textContent = dateStr + ' · Horario de Brasilia (UTC-3)';

    if ($('wx-mini-clock')) $('wx-mini-clock').textContent = f.hm + (showSec ? ':' + ss : '') + f.suffix;
    if ($('wx-t-clock')) $('wx-t-clock').textContent = f.hm;
    if ($('wx-t-sec')) {
      $('wx-t-sec').textContent = (showSec ? ':' + ss : '') + f.suffix;
      $('wx-t-sec').style.display = (showSec || f.suffix) ? 'inline-block' : 'none';
    }

    if ($('n-clock')) $('n-clock').textContent = f.hm;
    if ($('n-sec')) {
      $('n-sec').textContent = (showSec ? ss : '') + f.suffix;
      $('n-sec').style.display = (showSec || f.suffix) ? 'inline-block' : 'none';
    }
    if ($('n-date')) $('n-date').textContent = dateStr;
    if ($('n-wordclock')) $('n-wordclock').textContent = wordClockPt(now.getHours(), now.getMinutes());

    // Update live elements inside any active custom page
    if (activeViewIdx() === -1) {
      tickCustomPageClock(now, f, ss, showSec, dateStr);
    }
    if (state.pomo.running && state.pomo.secLeft > 0) {
      state.pomo.secLeft--;
      if (state.pomo.secLeft === 0) {
        state.pomo.running = false;
        showToast('Pomodoro concluido!', 'ok', 4500);
      }
      var pEl = $('cp-pomo-disp');
      if (pEl) pEl.textContent = pad2(Math.floor(state.pomo.secLeft / 60)) + ':' + pad2(state.pomo.secLeft % 60);
    }

    renderWorldClocks(now, use12h);
    if (now.getSeconds() % 10 === 0 || !renderedCalDayKey) {
      renderTimeProgress(now);
    }
    var dayKey = lastCalendarDayKey();
    if (dayKey !== renderedCalDayKey) {
      renderedCalDayKey = dayKey;
      renderTimeProgress(now);
      renderMonthCalendar(now);
    }
    checkAutoNightDim(now);
    checkIdleBehaviors(now);
  }

  function checkAutoNightDim(now) {
    if (state.isPreview || state.manualDim || !state.config || !state.config.night) return;
    var n = state.config.night;
    if (!n.enabled) {
      if (state.autoNightActive) {
        state.autoNightActive = false;
        applyDimmer(0, true);
      }
      return;
    }
    var curMin = now.getHours() * 60 + now.getMinutes();
    var sParts = String(n.start || '00:30').split(':');
    var eParts = String(n.end || '07:30').split(':');
    var sMin = (parseInt(sParts[0], 10) || 0) * 60 + (parseInt(sParts[1], 10) || 0);
    var eMin = (parseInt(eParts[0], 10) || 0) * 60 + (parseInt(eParts[1], 10) || 0);
    var inNight = sMin <= eMin ? (curMin >= sMin && curMin < eMin) : (curMin >= sMin || curMin < eMin);
    if (inNight) {
      state.autoNightActive = true;
      var targetDim = Math.max(0.15, Math.min(0.85, 1 - (typeof n.brightness === 'number' ? n.brightness : 0.15)));
      if (Math.abs(state.dimLevel - targetDim) > 0.08) applyDimmer(targetDim, true);
    } else if (state.autoNightActive) {
      state.autoNightActive = false;
      applyDimmer(0, true);
    }
  }

  function checkIdleBehaviors(now) {
    if (state.isPreview || !state.config || !state.config.display) return;
    var disp = state.config.display;
    var idleMs = now.getTime() - state.lastTouchAt;
    var cur = activePageObj();
    var retSec = (cur && typeof cur.idleSec === 'number') ? cur.idleSec : (typeof disp.idleReturnSec === 'number' ? disp.idleReturnSec : 0);
    if (retSec > 0 && idleMs >= retSec * 1000) {
      var homeIdx = disp.homePage ? findTabByPageId(disp.homePage) : 0;
      if (homeIdx < 0) homeIdx = 0;
      if (state.page !== homeIdx) {
        state.lastTouchAt = now.getTime();
        setPage(homeIdx);
      }
    }
    var dimMin = typeof disp.idleDimMin === 'number' ? disp.idleDimMin : 0;
    if (dimMin > 0 && !state.manualDim && !state.idleDimmed && idleMs >= dimMin * 60000) {
      state.idleDimmed = true;
      if (state.dimLevel < 0.65) applyDimmer(0.72, true);
    }
  }

  function getConfiguredWorldZones() {
    var defaultZones = [
      'Rio de Janeiro=America/Sao_Paulo',
      'Lisboa=Europe/Lisbon',
      'Londres=Europe/London',
      'Nova York=America/New_York',
      'Berlim=Europe/Berlin',
      'Toquio=Asia/Tokyo'
    ];
    if (!state.config || !state.config.pages) return defaultZones;
    var pages = state.config.pages;
    for (var i = 0; i < pages.length; i++) {
      var p = pages[i];
      if (!p || !p.widgets) continue;
      for (var j = 0; j < p.widgets.length; j++) {
        var w = p.widgets[j];
        if (w && w.type === 'worldclock' && w.options && w.options.zones && w.options.zones.length) {
          var z = w.options.zones.slice(0);
          var hasBr = false;
          for (var k = 0; k < z.length; k++) {
            if (String(z[k]).indexOf('Sao_Paulo') !== -1 || String(z[k]).toLowerCase().indexOf('rio') !== -1) {
              hasBr = true;
            }
          }
          if (!hasBr) z.unshift('Rio de Janeiro=America/Sao_Paulo');
          return z;
        }
      }
    }
    return defaultZones;
  }

  function renderWorldClocks(now, use12h) {
    var box = $('cw-worldclock');
    if (!box) return;
    var zones = getConfiguredWorldZones();
    var utcMs = now.getTime() + (now.getTimezoneOffset() * 60000);
    var html = '';
    for (var i = 0; i < zones.length && i < 6; i++) {
      var raw = String(zones[i] || '');
      var eq = raw.indexOf('=');
      var label = eq !== -1 ? raw.substring(0, eq) : raw;
      var tz = eq !== -1 ? raw.substring(eq + 1) : raw;
      if (label === 'Tallinn' && tz === 'Europe/Tallinn' && i === 0) {
        label = 'Rio de Janeiro';
        tz = 'America/Sao_Paulo';
      }
      var offsetH = typeof TZ_OFFSETS_HOURS[tz] === 'number' ? TZ_OFFSETS_HOURS[tz] : -3;
      var zDate = new Date(utcMs + offsetH * 3600000);
      var f = formatHourMin(zDate.getHours(), zDate.getMinutes(), use12h);
      var isLocal = (tz === 'America/Sao_Paulo');
      var sign = offsetH >= 0 ? '+' + offsetH : String(offsetH);
      html += '<div class="cw-wc-box"><div class="cw-wc-in' + (isLocal ? ' cw-wc-local' : '') + '">'
        + '<div class="cw-wc-city">' + escHtml(label) + '</div>'
        + '<div class="cw-wc-time">' + f.hm + escHtml(f.suffix) + '</div>'
        + '<div class="cw-wc-tag">UTC' + sign + (isLocal ? ' · Local' : '') + '</div>'
        + '</div></div>';
    }
    box.innerHTML = html;
  }

  function renderTimeProgress(now) {
    var box = $('cw-progress');
    if (!box) return;
    var dayPct = Math.round(((now.getHours() * 3600 + now.getMinutes() * 60 + now.getSeconds()) / 86400) * 100);
    var dow = now.getDay() === 0 ? 7 : now.getDay(); // Mon=1..Sun=7
    var weekPct = Math.round((((dow - 1) + (dayPct / 100)) / 7) * 100);
    var daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
    var monthPct = Math.round((((now.getDate() - 1) + (dayPct / 100)) / daysInMonth) * 100);
    var startYear = new Date(now.getFullYear(), 0, 1).getTime();
    var endYear = new Date(now.getFullYear() + 1, 0, 1).getTime();
    var yearPct = Math.round(((now.getTime() - startYear) / (endYear - startYear)) * 100);

    var items = [
      ['Dia de hoje', dayPct, 'bg-teal'],
      ['Semana atual', weekPct, 'bg-sky'],
      ['Mes de ' + MONTHS_PT[now.getMonth()], monthPct, 'bg-purple'],
      ['Ano de ' + now.getFullYear(), yearPct, 'bg-green']
    ];
    var html = '';
    for (var i = 0; i < items.length; i++) {
      html += '<div class="disk-item">'
        + '<div class="disk-top clearfix"><span>' + escHtml(items[i][0]) + '</span><span class="right">' + items[i][1] + '%</span></div>'
        + '<div class="bar-bg"><div class="bar-fill ' + items[i][2] + '" style="width:' + items[i][1] + '%"></div></div>'
        + '</div>';
    }
    box.innerHTML = html;
  }

  function renderMonthCalendar(now) {
    var box = $('cw-calendar');
    if (!box) return;
    var y = now.getFullYear();
    var m = now.getMonth();
    var today = now.getDate();
    if ($('cw-cal-title')) $('cw-cal-title').textContent = 'CALENDARIO · ' + MONTHS_PT[m].toUpperCase() + ' DE ' + y;

    var firstDow = new Date(y, m, 1).getDay();
    var totalDays = new Date(y, m + 1, 0).getDate();
    var html = '<table class="cw-cal-table"><thead><tr>';
    for (var h = 0; h < 7; h++) html += '<th>' + DAYS_SHORT_PT[h] + '</th>';
    html += '</tr></thead><tbody><tr>';

    var cell = 0;
    for (var b = 0; b < firstDow; b++) {
      html += '<td></td>';
      cell++;
    }
    for (var d = 1; d <= totalDays; d++) {
      if (cell > 0 && cell % 7 === 0) html += '</tr><tr>';
      html += '<td' + (d === today ? ' class="cw-cal-today"' : '') + '>' + d + '</td>';
      cell++;
    }
    while (cell % 7 !== 0) {
      html += '<td></td>';
      cell++;
    }
    html += '</tr></tbody></table>';
    box.innerHTML = html;
  }

  function applyDimmer(level, isAuto) {
    if (!isAuto) state.manualDim = true;
    var v = parseFloat(level);
    if (isNaN(v) || v < 0) v = 0;
    if (v > 0.85) v = 0.85;
    state.dimLevel = v;
    storeSet('dim', v);
    var d = $('dimmer');
    if (d) d.style.opacity = String(v);

    var opts = document.getElementsByTagName('button');
    for (var i = 0; i < opts.length; i++) {
      var attr = opts[i].getAttribute('data-dim');
      if (attr !== null) {
        var active = Math.abs(parseFloat(attr) - v) < 0.05;
        opts[i].className = active ? 'dim-opt active' : 'dim-opt';
      }
    }

    var dbtn = $('btn-dim');
    if (dbtn) {
      if (v > 0.05) addClass(dbtn, 'active');
      else removeClass(dbtn, 'active');
    }
  }

  function toggleFullscreen() {
    var doc = document;
    var el = doc.documentElement;
    var isFull = !!(doc.fullscreenElement || doc.webkitFullscreenElement);
    try {
      if (!isFull) {
        if (el.webkitRequestFullscreen) el.webkitRequestFullscreen();
        else if (el.webkitRequestFullScreen) el.webkitRequestFullScreen();
        else if (el.requestFullscreen) el.requestFullscreen();
      } else {
        if (doc.webkitExitFullscreen) doc.webkitExitFullscreen();
        else if (doc.webkitCancelFullScreen) doc.webkitCancelFullScreen();
        else if (doc.exitFullscreen) doc.exitFullscreen();
      }
    } catch (e) { }
    try { window.scrollTo(0, 1); } catch (e) { }
  }

  // ==================== CANVAS CHARTS (ES5 + ANDROID 4.2.2 SAFE) ====================
  function pushHist(arr, val) {
    arr.push(typeof val === 'number' && !isNaN(val) ? val : null);
    if (arr.length > MAX_HIST) arr.shift();
  }

  function hexToRgba(hex, alpha) {
    if (!hex || typeof hex !== 'string' || hex.charAt(0) !== '#' || hex.length !== 7) {
      return 'rgba(45, 212, 191, ' + alpha + ')';
    }
    var r = parseInt(hex.substring(1, 3), 16) || 45;
    var g = parseInt(hex.substring(3, 5), 16) || 212;
    var b = parseInt(hex.substring(5, 7), 16) || 191;
    return 'rgba(' + r + ',' + g + ',' + b + ',' + alpha + ')';
  }

  function drawSparkline(canvas, data, color) {
    if (!canvas || !canvas.getContext) return;
    var w = canvas.clientWidth || 180;
    var h = canvas.clientHeight || 44;
    if (canvas.width !== w) canvas.width = w;
    if (canvas.height !== h) canvas.height = h;
    var ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, w, h);

    var th = state.themeColors || THEMES['default'];
    ctx.strokeStyle = th.border || '#354f76';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, h - 1);
    ctx.lineTo(w, h - 1);
    ctx.stroke();

    if (data.length < 2) return;
    ctx.strokeStyle = color;
    ctx.lineWidth = 2;
    ctx.beginPath();
    var started = false;
    for (var i = 0; i < data.length; i++) {
      if (data[i] === null || typeof data[i] !== 'number' || isNaN(data[i])) {
        started = false;
        continue;
      }
      var x = (i / (MAX_HIST - 1)) * w;
      var pct = Math.max(0, Math.min(100, data[i]));
      var y = h - 3 - (pct / 100) * (h - 6);
      if (!started) { ctx.moveTo(x, y); started = true; }
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
  }

  function drawDualChart(canvas, s1, c1, s2, c2) {
    if (!canvas || !canvas.getContext) return;
    var w = canvas.clientWidth || 420;
    var h = canvas.clientHeight || 110;
    if (w < 60 || h < 40) return;
    if (canvas.width !== w) canvas.width = w;
    if (canvas.height !== h) canvas.height = h;
    var ctx = canvas.getContext('2d');
    var th = state.themeColors || THEMES['default'];
    var hack = !!(th && th.isHack);
    var anime = !!(th && th.isAnime);
    ctx.fillStyle = hack ? 'rgba(1, 10, 5, 0.90)' : (anime ? (th.inset || 'rgba(17, 10, 38, 0.86)') : (th.inset || th.cardAlt));
    ctx.fillRect(0, 0, w, h);

    // Horizontal grid lines (75%, 50%, 25%) + vertical radar grid in Hack mode
    ctx.strokeStyle = hack ? 'rgba(21, 105, 59, 0.45)' : (anime ? (th.gridCol || 'rgba(199, 146, 255, 0.25)') : (th.line || '#182233'));
    ctx.lineWidth = 1;
    ctx.font = hack ? '10px Consolas, monospace' : '10px Roboto, Arial, sans-serif';
    ctx.fillStyle = th.muted || '#b2c5e1';
    ctx.textAlign = 'left';
    var ticks = ['75', '50', '25'];
    for (var g = 1; g <= 3; g++) {
      var gy = Math.round((h / 4) * g) + 0.5;
      ctx.beginPath();
      ctx.moveTo(0, gy);
      ctx.lineTo(w, gy);
      ctx.stroke();
      ctx.fillText(ticks[g - 1], 4, gy - 2);
    }
    if (hack) {
      for (var vg = 1; vg <= 5; vg++) {
        var gx = Math.round((w / 6) * vg) + 0.5;
        ctx.beginPath();
        ctx.moveTo(gx, 0);
        ctx.lineTo(gx, h);
        ctx.stroke();
      }
    }

    function plot(arr, strokeColor, fillColor, isPrimary) {
      if (!arr || arr.length < 2) return;
      var padX = 2;
      var usableW = w - padX * 2;
      var segStart = -1;
      var lastX = 0;
      var lastY = h - 4;
      var hasPt = false;

      // Draw translucent area fill BEFORE stroke so line stays sharp
      if (fillColor) {
        ctx.fillStyle = fillColor;
        for (var fi = 0; fi <= arr.length; fi++) {
          var v = fi < arr.length ? arr[fi] : null;
          var valid = (v !== null && typeof v === 'number' && !isNaN(v));
          if (valid) {
            var fx = padX + ((MAX_HIST - arr.length + fi) / (MAX_HIST - 1)) * usableW;
            var fpct = Math.max(0, Math.min(100, v));
            var fy = h - 4 - (fpct / 100) * (h - 10);
            if (segStart < 0) {
              segStart = fx;
              ctx.beginPath();
              ctx.moveTo(fx, h);
              ctx.lineTo(fx, fy);
            } else {
              ctx.lineTo(fx, fy);
            }
            lastX = fx;
          } else if (segStart >= 0) {
            ctx.lineTo(lastX, h);
            ctx.closePath();
            ctx.fill();
            segStart = -1;
          }
        }
      }

      // Draw line segments skipping null/NaN gaps
      var drawTrace = function (col, lw) {
        ctx.strokeStyle = col;
        ctx.lineWidth = lw;
        ctx.beginPath();
        var drawing = false;
        for (var i = 0; i < arr.length; i++) {
          var val = arr[i];
          if (val === null || typeof val !== 'number' || isNaN(val)) {
            drawing = false;
            continue;
          }
          var x = padX + ((MAX_HIST - arr.length + i) / (MAX_HIST - 1)) * usableW;
          var pct = Math.max(0, Math.min(100, val));
          var y = h - 4 - (pct / 100) * (h - 10);
          if (!drawing) { ctx.moveTo(x, y); drawing = true; }
          else ctx.lineTo(x, y);
          lastX = x;
          lastY = y;
          hasPt = true;
        }
        ctx.stroke();
      };
      if ((hack || anime) && isPrimary) drawTrace(hexToRgba(strokeColor, anime ? 0.30 : 0.24), 5);
      drawTrace(strokeColor, 2);
      if ((hack || anime) && isPrimary && hasPt) {
        ctx.fillStyle = anime ? (th.starGold || '#ffd56b') : '#00f0ff';
        ctx.beginPath();
        ctx.arc(lastX, lastY, 3, 0, Math.PI * 2, false);
        ctx.fill();
      }
    }

    plot(s2, c2, null, false);
    plot(s1, c1, hexToRgba(c1, anime ? 0.20 : (hack ? 0.18 : 0.14)), true);
  }

  function redrawCharts() {
    var acc = state.accentColor || '#8bd0ff';
    var th = state.themeColors || THEMES['default'];
    var hack = !!(th && th.isHack);
    var anime = !!(th && th.isAnime);
    drawSparkline($('spark-cpu'), state.histCpu, acc);
    var vIdx = activeViewIdx();
    if (vIdx === 1) {
      drawDualChart($('chart-cpu'), state.histCpu, acc, state.histRam, hack ? '#e879f9' : (anime ? (th.ramCol || '#c792ff') : '#a78bfa'));
      drawDualChart($('chart-gpu'), state.histGpu, hack ? '#00ff66' : (anime ? (th.gpuCol || '#5bf5b8') : '#22c55e'), state.histTemp, hack ? '#ffb800' : (anime ? (th.tempCol || '#ffd56b') : '#f59e0b'));
    }
    if (vIdx === 6 && state.weather && state.weather.hourly) {
      drawHourlyWeatherCanvas(state.weather.hourly, '°' + (state.weather.unit || 'C'));
    }
  }

  // ==================== RENDER STATE ====================
  function renderState(d) {
    state.lastState = d;
    if (!d) return;
    try { updateHackTerminalTelemetry(d); } catch (e) { }
    try { updateAnimeRibbonTelemetry(d); } catch (e) { }

    var pc = d.pc || {};
    var gpu = pc.gpu || null;
    var cpuPct = typeof pc.cpu === 'number' ? Math.round(pc.cpu) : 0;
    var ramUsed = typeof pc.ramUsedGb === 'number' ? pc.ramUsedGb : 0;
    var ramTot = typeof pc.ramTotalGb === 'number' && pc.ramTotalGb > 0 ? pc.ramTotalGb : 1;
    var ramPct = Math.round((ramUsed / ramTot) * 100);
    var gpuPct = gpu && typeof gpu.util === 'number' ? Math.round(gpu.util) : 0;
    var gpuTemp = gpu && typeof gpu.temp === 'number' ? Math.round(gpu.temp) : null;

    pushHist(state.histCpu, cpuPct);
    pushHist(state.histRam, ramPct);
    pushHist(state.histGpu, gpu && typeof gpu.util === 'number' ? gpuPct : null);
    pushHist(state.histTemp, gpuTemp !== null ? gpuTemp : null);

    // Top bar
    if ($('host-name')) $('host-name').textContent = d.host || 'PhoneBox PC';
    if ($('tb-mini')) {
      $('tb-mini').textContent = 'CPU ' + cpuPct + '% · GPU ' + gpuPct + '%' + (gpuTemp !== null ? ' ' + gpuTemp + '°C' : '') + ' · RAM ' + ramPct + '%';
    }

    // Privacy badge
    var priv = d.privacy || {};
    var privParts = [];
    if (priv.mic) privParts.push('MIC' + (priv.micApps && priv.micApps.length ? ': ' + priv.micApps.join(', ') : ''));
    if (priv.camera) privParts.push('CAM' + (priv.cameraApps && priv.cameraApps.length ? ': ' + priv.cameraApps.join(', ') : ''));
    var pb = $('tb-privacy');
    if (pb) {
      if (privParts.length) {
        pb.textContent = privParts.join(' · ');
        pb.className = 'priv-badge';
      } else {
        pb.className = 'priv-badge hidden';
      }
    }
    if ($('h-priv')) {
      $('h-priv').textContent = privParts.length ? privParts.join(' · ') : 'Microfone / Camera: livres';
      $('h-priv').className = privParts.length ? 'm-sub c-red' : 'm-sub';
    }

    // Home metrics
    if ($('h-cpu')) $('h-cpu').textContent = cpuPct + '%';
    if ($('h-cpu-bar')) $('h-cpu-bar').style.width = Math.min(100, cpuPct) + '%';
    if ($('h-cpu-sub')) $('h-cpu-sub').textContent = (pc.cpuTemp ? Math.round(pc.cpuTemp) + '°C · ' : '') + (pc.cpuName || 'Processador');

    if ($('h-gpu')) $('h-gpu').textContent = gpu ? (gpuPct + '%') : '--';
    if ($('h-gpu-bar')) $('h-gpu-bar').style.width = (gpu ? Math.min(100, gpuPct) : 0) + '%';
    if ($('h-gpu-temp')) {
      $('h-gpu-temp').textContent = gpuTemp !== null ? (gpuTemp + '°C') : '--°C';
      $('h-gpu-temp').className = gpuTemp !== null && gpuTemp >= 80 ? 'c-red' : (gpuTemp !== null && gpuTemp >= 68 ? 'c-amber' : 'c-green');
    }
    if ($('h-gpu-pwr')) $('h-gpu-pwr').textContent = (gpu && typeof gpu.power === 'number') ? (Math.round(gpu.power) + ' W') : '-- W';
    if ($('h-gpu-sub')) {
      if (gpu && typeof gpu.vramUsed === 'number' && typeof gpu.vramTotal === 'number') {
        $('h-gpu-sub').textContent = 'VRAM ' + round1(gpu.vramUsed) + ' / ' + round1(gpu.vramTotal) + ' GB';
      } else {
        $('h-gpu-sub').textContent = gpu && gpu.name ? gpu.name : 'Sem GPU';
      }
    }

    if ($('h-ram')) $('h-ram').textContent = ramPct + '%';
    if ($('h-ram-bar')) $('h-ram-bar').style.width = Math.min(100, ramPct) + '%';
    if ($('h-ram-used')) $('h-ram-used').textContent = round1(ramUsed) + ' GB';
    if ($('h-ram-tot')) $('h-ram-tot').textContent = '/ ' + round1(ramTot) + ' GB';
    if ($('h-ram-sub')) $('h-ram-sub').textContent = 'Livre: ' + round1(Math.max(0, ramTot - ramUsed)) + ' GB';

    var ping = d.ping || {};
    if ($('h-ping')) $('h-ping').textContent = (typeof ping.ms === 'number') ? (Math.round(ping.ms) + ' ms') : '-- ms';
    if ($('h-down')) $('h-down').textContent = fmtBytesPerSec(pc.netDownBps);
    if ($('h-up')) $('h-up').textContent = fmtBytesPerSec(pc.netUpBps);

    // Media
    try { renderMedia(d.media); } catch (e) { }

    // Volume
    try { renderAudio(d.audio); } catch (e) { }

    // System page details
    if ($('s-cpu')) $('s-cpu').textContent = cpuPct + '%';
    if ($('s-ram')) $('s-ram').textContent = ramPct + '%';
    if ($('s-cpu-name')) {
      var cpuFoot = (pc.cpuName || '--') + ' · RAM ' + round1(ramUsed) + '/' + round1(ramTot) + ' GB';
      if (typeof pc.cpuTemp === 'number' && !isNaN(pc.cpuTemp) && pc.cpuTemp > 0) cpuFoot += ' · ' + Math.round(pc.cpuTemp) + '°C';
      $('s-cpu-name').textContent = cpuFoot;
    }
    if ($('s-uptime')) $('s-uptime').textContent = 'Uptime: ' + fmtUptime(pc.uptimeSec);
    if ($('s-gpu')) $('s-gpu').textContent = gpu ? (gpuPct + '%') : '--';
    if ($('s-gpu-temp')) $('s-gpu-temp').textContent = gpuTemp !== null ? (gpuTemp + '°C') : '--';
    if ($('s-gpu-name')) $('s-gpu-name').textContent = gpu && gpu.name ? gpu.name : '--';
    if ($('s-gpu-extra')) {
      if (gpu) {
        var memTxt = (typeof gpu.vramTotal === 'number' && gpu.vramTotal > 0)
          ? (round1(gpu.vramUsed) + '/' + round1(gpu.vramTotal) + ' GB')
          : ((typeof gpu.sharedTotal === 'number' && gpu.sharedTotal > 0) ? ('Comp. ' + round1(gpu.sharedUsed) + '/' + round1(gpu.sharedTotal) + ' GB') : '');
        var pwrTxt = typeof gpu.power === 'number' ? (Math.round(gpu.power) + 'W') : '';
        $('s-gpu-extra').textContent = (memTxt && pwrTxt) ? (memTxt + ' · ' + pwrTxt) : (memTxt || pwrTxt);
      } else {
        $('s-gpu-extra').textContent = '';
      }
    }

    if (activeViewIdx() === 1) {
      try { renderDisks(pc.disks || []); } catch (e) { }
      try { renderHardwareDetails(pc, gpu, ping); } catch (e) { }
    }

    // Night page stats
    if ($('n-stats')) {
      $('n-stats').textContent = 'CPU ' + cpuPct + '%  ·  GPU ' + gpuPct + '%' + (gpuTemp !== null ? ' (' + gpuTemp + '°C)' : '') + '  ·  RAM ' + round1(ramUsed) + '/' + round1(ramTot) + ' GB';
    }
    if ($('n-media')) {
      $('n-media').textContent = d.media && d.media.title ? (d.media.title + (d.media.artist ? ' — ' + d.media.artist : '')) : '';
    }

    // Toggle states on shortcuts
    if (d.toggles && state.config && activeViewIdx() === 2) {
      try { updateShortcutToggleClasses(d.toggles); } catch (e) { }
    }

    if (activeViewIdx() === -1) {
      try { renderCustomPage(activePageObj()); } catch (e) { }
    }

    try { redrawCharts(); } catch (e) { }

    // Reload config if changed on desktop settings (skip when overridden by live preview)
    if (!state.previewConfig && d.cfgVersion && d.cfgVersion !== state.cfgVersion) {
      pollConfig();
    }
  }

  function renderMedia(media) {
    var titleEl = $('media-title');
    var artistEl = $('media-artist');
    var appEl = $('media-app');
    var playIco = $('ico-play');
    var pauseIco = $('ico-pause');
    var artImg = $('media-art');
    var artEmpty = $('media-art-empty');

    var hasTrack = !!(media && ((media.title && media.title.length > 0) || media.playing));
    if (!hasTrack) {
      if (appEl) appEl.textContent = 'MIDIA';
      if (titleEl) titleEl.textContent = 'Nada tocando';
      if (artistEl) artistEl.textContent = 'Reproduza musica ou video no PC';
      if (playIco) removeClass(playIco, 'hidden');
      if (pauseIco) addClass(pauseIco, 'hidden');
      if (state.artKey !== '') {
        state.artKey = '';
        if (artImg) addClass(artImg, 'hidden');
        if (artEmpty) removeClass(artEmpty, 'hidden');
      }
      return;
    }

    var appLbl = media.app || 'MIDIA';
    if (appEl) appEl.textContent = appLbl + (media.playing ? ' · TOCANDO' : ' · PAUSADO');
    if (titleEl) titleEl.textContent = media.title || appLbl;
    if (artistEl) artistEl.textContent = media.artist || media.album || '';

    if (media.playing) {
      if (playIco) addClass(playIco, 'hidden');
      if (pauseIco) removeClass(pauseIco, 'hidden');
    } else {
      if (playIco) removeClass(playIco, 'hidden');
      if (pauseIco) addClass(pauseIco, 'hidden');
    }

    var artId = typeof media.artId === 'number' ? media.artId : 0;
    var hasArt = (artId > 0) || !!media.hasArt;
    var key = (media.title || '') + '|' + (media.artist || '') + '|' + artId + '|' + (hasArt ? '1' : '0');
    if (key !== state.artKey) {
      state.artKey = key;
      if (hasArt && artImg) {
        artImg.onload = function () {
          removeClass(artImg, 'hidden');
          if (artEmpty) addClass(artEmpty, 'hidden');
        };
        artImg.onerror = function () {
          addClass(artImg, 'hidden');
          if (artEmpty) removeClass(artEmpty, 'hidden');
        };
        artImg.src = '/api/art?v=' + artId + '&_t=' + (new Date().getTime());
      } else {
        if (artImg) addClass(artImg, 'hidden');
        if (artEmpty) removeClass(artEmpty, 'hidden');
      }
    }
  }

  function renderAudio(audio) {
    if (!audio) return;
    var vol = typeof audio.volume === 'number' ? Math.max(0, Math.min(100, Math.round(audio.volume))) : 0;
    var muted = !!audio.muted;

    if ($('v-fill')) $('v-fill').style.width = vol + '%';
    if ($('v-pct')) $('v-pct').textContent = muted ? '0%' : (vol + '%');

    var mb = $('v-mute');
    var icoOn = $('ico-vol-on');
    var icoMute = $('ico-vol-mute');
    if (mb) {
      if (muted) addClass(mb, 'muted-on');
      else removeClass(mb, 'muted-on');
    }
    if (icoOn && icoMute) {
      if (muted) { addClass(icoOn, 'hidden'); removeClass(icoMute, 'hidden'); }
      else { removeClass(icoOn, 'hidden'); addClass(icoMute, 'hidden'); }
    }
  }

  function renderDisks(disks) {
    var box = $('disks-list');
    if (!box) return;
    var list = disks || [];
    if ($('s-disk-count')) $('s-disk-count').textContent = list.length + (list.length === 1 ? ' unidade' : ' unidades');
    var sig = '';
    var i;
    for (i = 0; i < list.length; i++) {
      var sd = list[i];
      if (!sd) continue;
      sig += sd.name + '|' + Math.round(sd.usedGb || 0) + '|' + Math.round(sd.totalGb || 0) + ';';
    }
    if (sig === state.lastDisksSig && box.childNodes.length > 0) return;
    state.lastDisksSig = sig;
    if (!list.length) { box.innerHTML = '<span class="muted">Nenhum disco reportado.</span>'; return; }

    var html = '';
    for (i = 0; i < list.length; i++) {
      var dk = list[i];
      if (!dk) continue;
      var tot = dk.totalGb || 1;
      var used = dk.usedGb || 0;
      var pct = Math.min(100, Math.round((used / tot) * 100));
      var freeGb = Math.max(0, tot - used);
      var barCls = pct >= 90 ? 'bg-red' : (pct >= 80 ? 'bg-amber' : 'bg-sky');
      html += '<div class="disk-item">'
        + '<div class="disk-top clearfix">'
        + '<span>' + escHtml(dk.name) + ' <span class="disk-free">(' + Math.round(freeGb) + ' GB livres)</span></span>'
        + '<span class="right">' + Math.round(used) + ' / ' + Math.round(tot) + ' GB (' + pct + '%)</span>'
        + '</div>'
        + '<div class="bar-bg"><div class="bar-fill ' + barCls + '" style="width:' + pct + '%"></div></div>'
        + '</div>';
    }
    box.innerHTML = html;
  }

  function renderHardwareDetails(pc, gpu, ping) {
    var box = $('hw-details');
    if (!box) return;
    var rows = [];
    rows.push(['RAM', round1(pc.ramUsedGb || 0) + ' / ' + round1(pc.ramTotalGb || 0) + ' GB']);
    if (typeof pc.cpuTemp === 'number' && !isNaN(pc.cpuTemp) && pc.cpuTemp > 0) {
      rows.push(['Temp. CPU', Math.round(pc.cpuTemp) + ' °C']);
    }
    if (gpu) {
      rows.push(['GPU', gpu.name || '--']);
      if (typeof gpu.clock === 'number' || typeof gpu.memClock === 'number') {
        rows.push(['Clock GPU / Mem', (typeof gpu.clock === 'number' ? Math.round(gpu.clock) + ' MHz' : '--') + ' / ' + (typeof gpu.memClock === 'number' ? Math.round(gpu.memClock) + ' MHz' : '--')]);
      }
      if (typeof gpu.power === 'number') {
        rows.push(['Energia', round1(gpu.power) + ' W' + (typeof gpu.powerLimit === 'number' && gpu.powerLimit > 0 ? ' / ' + round1(gpu.powerLimit) + ' W' : '')]);
      }
      if (typeof gpu.vramTotal === 'number' && gpu.vramTotal > 0) {
        rows.push(['VRAM', round1(gpu.vramUsed) + ' / ' + round1(gpu.vramTotal) + ' GB']);
      } else if (typeof gpu.sharedTotal === 'number' && gpu.sharedTotal > 0) {
        rows.push(['Mem. Compartilhada', round1(gpu.sharedUsed) + ' / ' + round1(gpu.sharedTotal) + ' GB']);
      }
      if (typeof gpu.fan === 'number' && !isNaN(gpu.fan)) rows.push(['Ventoinha GPU', Math.round(gpu.fan) + '%']);
      if (gpu.driver) rows.push(['Driver', gpu.driver]);
    }
    rows.push(['Download / Upload', fmtBytesPerSec(pc.netDownBps) + ' / ' + fmtBytesPerSec(pc.netUpBps)]);
    var pingVal = typeof ping.ms === 'number' ? Math.round(ping.ms) + ' ms' : '--';
    if (typeof ping.avgMs === 'number') pingVal += ' (med ' + round1(ping.avgMs) + 'ms)';
    if (typeof ping.lossPct === 'number' && ping.lossPct > 0) pingVal += ' · ' + Math.round(ping.lossPct) + '% perda';
    rows.push(['Ping (' + (ping.host || '1.1.1.1') + ')', pingVal]);
    rows.push(['Uptime', fmtUptime(pc.uptimeSec)]);

    var sig = '';
    var i;
    for (i = 0; i < rows.length; i++) sig += rows[i][0] + '=' + rows[i][1] + ';';
    if (sig === state.lastHwSig && box.childNodes.length > 0) return;
    state.lastHwSig = sig;

    var html = '';
    for (i = 0; i < rows.length; i++) {
      html += '<div class="kv-row clearfix"><span class="kv-k">' + escHtml(rows[i][0]) + '</span><span class="kv-v">' + escHtml(rows[i][1]) + '</span></div>';
    }
    box.innerHTML = html;
  }

  // ==================== WEATHER, SVG ICONS, GRAPHICAL GAUGES & FORECASTS ====================
  function windDirPt(deg) {
    if (typeof deg !== 'number' || isNaN(deg)) return '';
    var dirs = ['N', 'NE', 'L', 'SE', 'S', 'SO', 'O', 'NO'];
    return dirs[Math.round(((deg % 360) < 0 ? (deg + 360) : deg) / 45) % 8];
  }

  function uvLabelPt(uv) {
    if (typeof uv !== 'number' || isNaN(uv)) return 'Baixo';
    if (uv < 3) return 'Baixo';
    if (uv < 6) return 'Moderado';
    if (uv < 8) return 'Alto';
    if (uv < 11) return 'Muito Alto';
    return 'Extremo';
  }

  function aqiLabelPt(aqi) {
    if (typeof aqi !== 'number' || isNaN(aqi) || aqi <= 0) return 'Boa';
    if (aqi <= 50) return 'Boa';
    if (aqi <= 100) return 'Moderada';
    if (aqi <= 150) return 'Sensivel';
    if (aqi <= 200) return 'Insalubre';
    return 'Critica';
  }

  function extractTimePart(isoStr) {
    if (!isoStr) return '--:--';
    var tIdx = String(isoStr).indexOf('T');
    if (tIdx === -1) return String(isoStr);
    return String(isoStr).substring(tIdx + 1, tIdx + 6);
  }

  function timeToMinutes(hhmm) {
    var p = String(hhmm || '').split(':');
    if (p.length < 2) return 0;
    return (parseInt(p[0], 10) || 0) * 60 + (parseInt(p[1], 10) || 0);
  }

  function getWeatherSvg(code, isDay, size) {
    var sz = size || 48;
    var c = typeof code === 'number' ? code : 0;
    var day = isDay !== false;
    var inner = '';

    var sunRays = '<g stroke="#f59e0b" stroke-width="3" stroke-linecap="round">'
      + '<line x1="32" y1="5" x2="32" y2="11"/><line x1="32" y1="53" x2="32" y2="59"/>'
      + '<line x1="5" y1="32" x2="11" y2="32"/><line x1="53" y1="32" x2="59" y2="32"/>'
      + '<line x1="13" y1="13" x2="17" y2="17"/><line x1="47" y1="47" x2="51" y2="51"/>'
      + '<line x1="51" y1="13" x2="47" y2="17"/><line x1="17" y1="47" x2="13" y2="51"/>'
      + '</g><circle cx="32" cy="32" r="13" fill="#fbbf24" stroke="#fde047" stroke-width="2"/>';

    var moonStars = '<path d="M38 14 A16 16 0 1 0 50 42 A13 13 0 1 1 38 14 Z" fill="#a5b4fc" stroke="#e0e7ff" stroke-width="1.5"/>'
      + '<circle cx="16" cy="18" r="2" fill="#fde047"/><circle cx="24" cy="11" r="1.4" fill="#fde047"/><circle cx="49" cy="18" r="1.6" fill="#fde047"/>';

    var cloudMain = '<path d="M20 46 H46 A10 10 0 0 0 47 26 A13 13 0 0 0 22 23 A9 9 0 0 0 20 46 Z" fill="#e2e8f0" stroke="#bbcee8" stroke-width="1.5"/>';
    var cloudDark = '<path d="M16 40 H42 A8 8 0 0 0 43 24 A10 10 0 0 0 23 22 A7 7 0 0 0 16 40 Z" fill="#a8bad5"/>'
      + '<path d="M21 45 H48 A9 9 0 0 0 49 27 A12 12 0 0 0 25 25 A8 8 0 0 0 21 45 Z" fill="#d9e6fa" stroke="#bbcee8" stroke-width="1.2"/>';

    if (c === 0 || c === 1) {
      inner = day ? sunRays : moonStars;
    } else if (c === 2) {
      if (day) {
        inner = '<g transform="translate(-7,-6) scale(0.82)">' + sunRays + '</g>' + cloudMain;
      } else {
        inner = '<g transform="translate(-6,-6) scale(0.8)">' + moonStars + '</g>' + cloudMain;
      }
    } else if (c === 3 || c === 45 || c === 48) {
      inner = cloudDark;
      if (c >= 45) {
        inner += '<g stroke="#bbcee8" stroke-width="2.5" stroke-linecap="round">'
          + '<line x1="16" y1="51" x2="48" y2="51"/><line x1="20" y1="57" x2="44" y2="57"/></g>';
      }
    } else if (c >= 51 && c <= 61 || c === 80) {
      // Drizzle / light rain
      inner = cloudDark
        + '<g stroke="#38bdf8" stroke-width="2.8" stroke-linecap="round">'
        + '<line x1="23" y1="49" x2="20" y2="58"/>'
        + '<line x1="33" y1="49" x2="30" y2="58"/>'
        + '<line x1="43" y1="49" x2="40" y2="58"/></g>';
    } else if (c >= 63 && c <= 82) {
      // Rain / heavy showers
      inner = cloudDark
        + '<g stroke="#0ea5e9" stroke-width="3" stroke-linecap="round">'
        + '<line x1="19" y1="48" x2="15" y2="60"/>'
        + '<line x1="27" y1="48" x2="23" y2="60"/>'
        + '<line x1="35" y1="48" x2="31" y2="60"/>'
        + '<line x1="43" y1="48" x2="39" y2="60"/>'
        + '<line x1="50" y1="48" x2="46" y2="60"/></g>';
    } else {
      // Thunderstorm
      inner = cloudDark
        + '<polygon points="34,42 25,54 32,54 29,63 42,49 35,49" fill="#facc15" stroke="#ca8a04" stroke-width="1"/>'
        + '<g stroke="#38bdf8" stroke-width="2.6" stroke-linecap="round">'
        + '<line x1="20" y1="48" x2="17" y2="58"/><line x1="46" y1="48" x2="43" y2="58"/></g>';
    }

    return '<svg width="' + sz + '" height="' + sz + '" viewBox="0 0 64 64" style="vertical-align:middle">' + inner + '</svg>';
  }

  function getRainStatusInfo(code, pop, precipMm) {
    var c = typeof code === 'number' ? code : 0;
    var p = typeof pop === 'number' ? pop : 0;
    var mm = typeof precipMm === 'number' ? precipMm : 0;
    var isRainingNow = (c >= 51) || (mm > 0.05);

    var dropSvg = '<svg width="13" height="13" viewBox="0 0 24 24" fill="#38bdf8" style="vertical-align:-2px;margin-right:4px"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>';
    var sunCheckSvg = '<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="2.5" style="vertical-align:-2px;margin-right:4px"><polyline points="20 6 9 17 4 12"/></svg>';

    if (isRainingNow) {
      return {
        pillHtml: '<span class="wx-pill wx-pill-rain">' + dropSvg + 'CHOVENDO AGORA' + (mm > 0 ? ' · ' + mm + ' mm' : '') + '</span>',
        bannerCls: 'wx-rain-banner is-raining',
        bannerHtml: dropSvg + 'Chuva ativa no momento na sua localidade' + (mm > 0 ? ' (' + mm + ' mm)' : '') + ' · Chance hoje: ' + p + '%'
      };
    }
    if (p >= 50) {
      return {
        pillHtml: '<span class="wx-pill wx-pill-warn">' + dropSvg + 'ALTA CHANCE DE CHUVA · ' + p + '%</span>',
        bannerCls: 'wx-rain-banner is-warn',
        bannerHtml: dropSvg + 'Alerta de chuva hoje (' + p + '% de probabilidade) — leve guarda-chuva'
      };
    }
    if (p >= 20) {
      return {
        pillHtml: '<span class="wx-pill wx-pill-low">' + dropSvg + 'CHANCE MODERADA · ' + p + '%</span>',
        bannerCls: 'wx-rain-banner',
        bannerHtml: dropSvg + 'Sem chuva agora · Possibilidade leve de chuva hoje (' + p + '%)'
      };
    }
    return {
      pillHtml: '<span class="wx-pill wx-pill-dry">' + sunCheckSvg + 'SEM CHUVA · ' + p + '%</span>',
      bannerCls: 'wx-rain-banner',
      bannerHtml: sunCheckSvg + 'Tempo firme sem chuva no momento · Probabilidade de chuva: ' + p + '%'
    };
  }

  function calcMoonPhase(now) {
    var synodic = 29.53058867;
    var knownNewMoonMs = Date.UTC(2000, 0, 6, 18, 14, 0);
    var daysSince = (now.getTime() - knownNewMoonMs) / 86400000;
    var phase = ((daysSince % synodic) + synodic) % synodic;
    var ratio = phase / synodic;
    var illum = Math.round((1 - Math.cos(2 * Math.PI * ratio)) * 50);
    var name = 'Lua Nova';
    if (ratio < 0.03 || ratio >= 0.97) name = 'Lua Nova';
    else if (ratio < 0.22) name = 'Lua Crescente';
    else if (ratio < 0.28) name = 'Quarto Crescente';
    else if (ratio < 0.47) name = 'Gibosa Crescente';
    else if (ratio < 0.53) name = 'Lua Cheia';
    else if (ratio < 0.72) name = 'Gibosa Minguante';
    else if (ratio < 0.78) name = 'Quarto Minguante';
    else name = 'Lua Minguante';
    var daysToFull = ratio <= 0.5 ? Math.round((0.5 - ratio) * synodic) : Math.round((1.5 - ratio) * synodic);
    var daysToNew = Math.round((1 - ratio) * synodic);
    return { name: name, illum: illum, daysToFull: daysToFull, daysToNew: daysToNew };
  }

  function drawHourlyWeatherCanvas(hList, unitStr) {
    var canvas = $('wx-hourly-canvas');
    if (!canvas || !canvas.getContext || !hList || hList.length < 2) return;
    var w = canvas.clientWidth || 440;
    var h = canvas.clientHeight || 82;
    if (w < 80) w = 440;
    if (canvas.width !== w) canvas.width = w;
    if (canvas.height !== h) canvas.height = h;
    var ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, w, h);

    var n = Math.min(12, hList.length);
    var minT = 999, maxT = -999;
    var i;
    for (i = 0; i < n; i++) {
      var t = typeof hList[i].temp === 'number' ? hList[i].temp : 0;
      if (t < minT) minT = t;
      if (t > maxT) maxT = t;
    }
    if (maxT - minT < 3) { minT -= 1.5; maxT += 1.5; }

    var padX = 24;
    var topY = 20;
    var botY = h - 18;
    var usableW = w - padX * 2;
    var usableH = botY - topY;

    // 1. Draw subtle rain probability bars along the bottom
    for (i = 0; i < n; i++) {
      var pop = Math.max(0, Math.min(100, hList[i].pop || 0));
      var xBar = padX + (i / (n - 1)) * usableW;
      var barH = Math.round((pop / 100) * (usableH * 0.55));
      if (barH > 1) {
        ctx.fillStyle = 'rgba(56, 189, 248, 0.25)';
        ctx.fillRect(xBar - 7, h - 4 - barH, 14, barH);
      }
    }

    // 2. Draw temperature area fill & smooth polyline
    ctx.beginPath();
    for (i = 0; i < n; i++) {
      var px = padX + (i / (n - 1)) * usableW;
      var py = botY - ((hList[i].temp - minT) / (maxT - minT)) * usableH;
      if (i === 0) ctx.moveTo(px, py);
      else ctx.lineTo(px, py);
    }
    ctx.strokeStyle = '#8bd0ff';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.lineTo(padX + usableW, h - 4);
    ctx.lineTo(padX, h - 4);
    ctx.closePath();
    ctx.fillStyle = 'rgba(45, 212, 191, 0.14)';
    ctx.fill();

    // 3. Draw data points & temperature labels every 2 hours
    ctx.font = 'bold 10px Roboto, Arial, sans-serif';
    ctx.textAlign = 'center';
    for (i = 0; i < n; i += 2) {
      var dx = padX + (i / (n - 1)) * usableW;
      var dy = botY - ((hList[i].temp - minT) / (maxT - minT)) * usableH;
      ctx.beginPath();
      ctx.arc(dx, dy, 3, 0, Math.PI * 2, false);
      ctx.fillStyle = '#38bdf8';
      ctx.fill();
      ctx.fillStyle = '#e2e8f0';
      ctx.fillText(Math.round(hList[i].temp) + '°', dx, dy - 6);
    }
  }

  function renderWeather(w) {
    state.weather = w;
    var cfgW = (state.config && state.config.weather) ? state.config.weather : {};
    var city = (w && w.city) ? w.city : (cfgW.city || 'Rio de Janeiro, Brasil');
    var lat = (w && typeof w.lat === 'number') ? w.lat : (typeof cfgW.lat === 'number' ? cfgW.lat : -22.9068);
    var lon = (w && typeof w.lon === 'number') ? w.lon : (typeof cfgW.lon === 'number' ? cfgW.lon : -43.1729);
    var coordsTxt = 'Lat ' + lat + ' · Lon ' + lon;

    if ($('home-loc')) $('home-loc').textContent = city;
    if ($('n-loc-badge')) $('n-loc-badge').textContent = city.toUpperCase();
    if ($('cw-city-title')) $('cw-city-title').textContent = city.toUpperCase();

    if (!w || !w.ok) {
      if ($('w-temp')) $('w-temp').textContent = '--°';
      if ($('w-meta')) $('w-meta').textContent = city + ' · Clima indisponivel';
      if ($('n-weather')) $('n-weather').textContent = city;
      return;
    }

    var desc = WMO_PT[w.code] || 'Ceu limpo';
    var u = '°' + (w.unit || 'C');
    var sr = extractTimePart(w.sunrise);
    var ss = extractTimePart(w.sunset);
    var feels = typeof w.feels === 'number' ? Math.round(w.feels) : Math.round(w.temp);
    var hum = typeof w.humidity === 'number' ? w.humidity : 0;
    var wind = typeof w.windKmh === 'number' ? Math.round(w.windKmh) : 0;
    var wDir = windDirPt(w.windDir);
    var pop = typeof w.rainProb === 'number' ? w.rainProb : 0;
    var isDay = w.isDay !== false;
    var rainInfo = getRainStatusInfo(w.code, pop, w.precipMm);

    // 1. Update Home Card
    if ($('home-wx-ico')) $('home-wx-ico').innerHTML = getWeatherSvg(w.code, isDay, 44);
    if ($('home-rain-badge')) $('home-rain-badge').innerHTML = rainInfo.pillHtml;
    if ($('w-temp')) $('w-temp').textContent = Math.round(w.temp) + u;
    if ($('w-meta')) $('w-meta').textContent = desc + ' · Min ' + Math.round(w.lo) + '° / Max ' + Math.round(w.hi) + '°';
    if ($('w-sub')) {
      $('w-sub').textContent = 'Sensacao ' + feels + u + ' · Umidade ' + hum + '% · Vento ' + wind + ' km/h ' + wDir + ' · Sol ' + sr + '–' + ss;
    }

    // 2. Update Clima/Hora Top Bar & Hero Card
    if ($('wx-top-rain')) $('wx-top-rain').innerHTML = rainInfo.pillHtml;
    if ($('wx-hero-svg')) $('wx-hero-svg').innerHTML = getWeatherSvg(w.code, isDay, 70);
    if ($('wx-hero-temp')) $('wx-hero-temp').textContent = Math.round(w.temp) + u;
    if ($('wx-hero-cond')) $('wx-hero-cond').textContent = desc;
    if ($('wx-hero-feels')) $('wx-hero-feels').textContent = 'Sensacao termica: ' + feels + u;
    if ($('wx-rain-banner')) {
      $('wx-rain-banner').className = rainInfo.bannerCls;
      $('wx-rain-banner').innerHTML = rainInfo.bannerHtml;
    }
    if ($('wx-tr-lo')) $('wx-tr-lo').textContent = Math.round(w.lo) + u;
    if ($('wx-tr-hi')) $('wx-tr-hi').textContent = Math.round(w.hi) + u;
    if ($('wx-tr-sun')) $('wx-tr-sun').textContent = 'Sol ' + sr + ' as ' + ss;
    if ($('wx-tr-dot')) {
      var spanToday = Math.max(1, (w.hi || 30) - (w.lo || 20));
      var curPos = Math.max(4, Math.min(96, Math.round(((w.temp - w.lo) / spanToday) * 100)));
      $('wx-tr-dot').style.left = curPos + '%';
    }

    // 3. Update 4 Graphical Gauge Cards (2x2 Grid)
    var gBox = $('wx-gauges');
    if (gBox) {
      var uvVal = typeof w.uv === 'number' ? w.uv : 0;
      var uvPct = Math.max(4, Math.min(100, Math.round((uvVal / 11) * 100)));
      var uvBarCls = uvVal >= 8 ? 'bg-red' : (uvVal >= 5 ? 'bg-amber' : 'bg-green');
      var aqiVal = (w.air && (w.air.euAqi || w.air.usAqi)) ? (w.air.euAqi || w.air.usAqi) : 35;
      var aqiPct = Math.max(6, Math.min(100, Math.round((aqiVal / 200) * 100)));
      var aqiBarCls = aqiVal > 120 ? 'bg-amber' : 'bg-teal';
      var popBarCls = pop >= 55 ? 'bg-amber' : 'bg-sky';
      var humStatus = hum >= 75 ? 'Umido' : (hum < 35 ? 'Seco' : 'Confortavel');

      var gauges = [
        {
          title: 'CHUVA & PRECIPITACAO',
          val: pop + '%',
          sub: (w.precipMm && w.precipMm > 0) ? (w.precipMm + ' mm agora') : (pop >= 40 ? 'Risco de chuva' : 'Sem chuva'),
          pct: Math.max(3, Math.min(100, pop)),
          cls: popBarCls
        },
        {
          title: 'UMIDADE DO AR',
          val: hum + '%',
          sub: humStatus + ' · Sens. ' + feels + u,
          pct: Math.max(4, Math.min(100, hum)),
          cls: 'bg-sky'
        },
        {
          title: 'INDICE UV & SOL',
          val: 'UV ' + uvVal,
          sub: uvLabelPt(uvVal) + ' (Max ' + (w.uvMax || uvVal) + ')',
          pct: uvPct,
          cls: uvBarCls
        },
        {
          title: 'VENTO & QUALIDADE DO AR',
          val: wind + ' km/h ' + wDir,
          sub: 'Ar ' + aqiLabelPt(aqiVal) + ' · ' + (w.pressureHpa ? Math.round(w.pressureHpa) + ' hPa' : ''),
          pct: aqiPct,
          cls: aqiBarCls
        }
      ];

      var gHtml = '';
      for (var gIdx = 0; gIdx < gauges.length; gIdx++) {
        var g = gauges[gIdx];
        gHtml += '<div class="wx-gauge-wrap"><div class="wx-gauge-card">'
          + '<div class="wx-g-head">' + escHtml(g.title) + '</div>'
          + '<div class="wx-g-main clearfix">'
          + '<span class="wx-g-val">' + escHtml(g.val) + '</span>'
          + '<span class="wx-g-sub">' + escHtml(g.sub) + '</span>'
          + '</div>'
          + '<div class="bar-bg"><div class="bar-fill ' + g.cls + '" style="width:' + g.pct + '%"></div></div>'
          + '</div></div>';
      }
      gBox.innerHTML = gHtml;
    }

    // 4. Update 12-Hour Graphical Canvas & 6-Slot Visual Strip
    var hList = w.hourly || [];
    drawHourlyWeatherCanvas(hList, u);
    var hBox = $('cw-hourly');
    if (hBox) {
      if (!hList.length) {
        hBox.innerHTML = '<span class="muted">Previsao horaria indisponivel.</span>';
      } else {
        var maxPop12 = 0;
        for (var mIdx = 0; mIdx < Math.min(12, hList.length); mIdx++) {
          if ((hList[mIdx].pop || 0) > maxPop12) maxPop12 = hList[mIdx].pop || 0;
        }
        if ($('wx-h12-summary')) {
          $('wx-h12-summary').textContent = maxPop12 >= 35 ? ('Pico de chuva: ' + maxPop12 + '%') : 'Sem chuva forte nas prox. 12h';
        }
        var hHtml = '';
        var step = hList.length >= 12 ? 2 : 1;
        var shownSlots = 0;
        for (var hIdx = 0; hIdx < hList.length && shownSlots < 6; hIdx += step) {
          var hItem = hList[hIdx];
          var hTime = extractTimePart(hItem.time);
          var hHourNum = parseInt(hTime.split(':')[0], 10);
          var slotIsDay = isNaN(hHourNum) ? isDay : (hHourNum >= 6 && hHourNum < 18);
          var hPop = hItem.pop || 0;
          hHtml += '<div class="wx-h-slot"><div class="wx-h-slot-in' + (shownSlots === 0 ? ' now-slot' : '') + '">'
            + '<div class="wx-hs-time">' + escHtml(shownSlots === 0 ? 'Agora' : hTime) + '</div>'
            + '<div class="wx-hs-ico">' + getWeatherSvg(hItem.code, slotIsDay, 28) + '</div>'
            + '<div class="wx-hs-temp">' + Math.round(hItem.temp) + '°</div>'
            + '<div class="wx-hs-pop">' + hPop + '%</div>'
            + '<div class="wx-hs-bar-bg"><div class="wx-hs-bar-fill" style="width:' + Math.min(100, hPop) + '%"></div></div>'
            + '</div></div>';
          shownSlots++;
        }
        hBox.innerHTML = hHtml;
      }
    }

    // 5. Update 7-Day Forecast with SVG Icons & Proportional Temperature Range Bars
    var dBox = $('cw-daily');
    if (dBox) {
      var dList = w.daily || [];
      if (!dList.length) {
        dBox.innerHTML = '<span class="muted">Previsao de 7 dias indisponivel.</span>';
      } else {
        var weekMin = 999, weekMax = -999;
        var dIdx;
        for (dIdx = 0; dIdx < dList.length; dIdx++) {
          if (dList[dIdx].lo < weekMin) weekMin = dList[dIdx].lo;
          if (dList[dIdx].hi > weekMax) weekMax = dList[dIdx].hi;
        }
        var weekSpan = Math.max(1, weekMax - weekMin);
        var dHtml = '';
        for (dIdx = 0; dIdx < dList.length; dIdx++) {
          var dItem = dList[dIdx];
          var dParts = String(dItem.date || '').split('-');
          var dName = dItem.date;
          if (dParts.length === 3) {
            var dt = new Date(parseInt(dParts[0], 10), parseInt(dParts[1], 10) - 1, parseInt(dParts[2], 10));
            var dayTitle = dIdx === 0 ? 'Hoje' : (dIdx === 1 ? 'Amanha' : DAYS_SHORT_PT[dt.getDay()]);
            dName = dayTitle + ' ' + dParts[2] + '/' + dParts[1];
          }
          var leftPct = Math.max(0, Math.min(90, Math.round(((dItem.lo - weekMin) / weekSpan) * 100)));
          var widthPct = Math.max(10, Math.min(100 - leftPct, Math.round(((dItem.hi - dItem.lo) / weekSpan) * 100)));
          var dPop = dItem.pop || 0;
          var dotHtml = '';
          if (dIdx === 0) {
            var todayDotPct = Math.max(4, Math.min(96, Math.round(((w.temp - weekMin) / weekSpan) * 100)));
            dotHtml = '<div class="wx-range-dot" style="left:' + todayDotPct + '%"></div>';
          }
          dHtml += '<div class="wx-d-row clearfix">'
            + '<div class="wx-d-day">' + escHtml(dName) + '</div>'
            + '<div class="wx-d-ico">' + getWeatherSvg(dItem.code, true, 22) + '</div>'
            + '<div class="wx-d-pop">' + (dPop > 0 ? (dPop + '%') : '<span class="muted">0%</span>') + '</div>'
            + '<div class="wx-d-lo">' + Math.round(dItem.lo) + '°</div>'
            + '<div class="wx-d-bar-wrap"><div class="wx-range-track"><div class="wx-range-fill" style="left:' + leftPct + '%;width:' + widthPct + '%"></div>' + dotHtml + '</div></div>'
            + '<div class="wx-d-hi">' + Math.round(dItem.hi) + '°</div>'
            + '</div>';
        }
        dBox.innerHTML = dHtml;
      }
    }

    // 6. Update Sub-View 1 (Solar Arc Progress Bar & Lunar Phase Illumination Bar)
    var smBox = $('cw-sunmoon');
    if (smBox) {
      var moon = calcMoonPhase(new Date());
      var nowNow = new Date();
      var curMins = nowNow.getHours() * 60 + nowNow.getMinutes();
      var srMins = timeToMinutes(sr) || 300;
      var ssMins = timeToMinutes(ss) || 1095;
      var sunPct = 0;
      var sunStatus = 'Noite';
      if (curMins >= srMins && curMins <= ssMins) {
        sunPct = Math.max(2, Math.min(98, Math.round(((curMins - srMins) / Math.max(1, ssMins - srMins)) * 100)));
        var remMins = ssMins - curMins;
        sunStatus = 'Sol acima do horizonte · Restam ' + Math.floor(remMins / 60) + 'h ' + (remMins % 60) + 'm de luz';
      } else if (curMins < srMins) {
        var toSr = srMins - curMins;
        sunStatus = 'Antes do amanhecer · Nasce em ' + Math.floor(toSr / 60) + 'h ' + (toSr % 60) + 'm';
      } else {
        sunStatus = 'Sol posto as ' + ss;
      }
      var dlHrs = w.daylightSec ? Math.floor(w.daylightSec / 3600) + 'h ' + Math.round((w.daylightSec % 3600) / 60) + 'm' : '13h 20m';

      smBox.innerHTML = '<div class="disk-item">'
        + '<div class="disk-top clearfix"><span>Ciclo Solar · Nascer ' + escHtml(sr) + ' / Por ' + escHtml(ss) + '</span><span class="right c-amber">' + escHtml(dlHrs) + '</span></div>'
        + '<div class="bar-bg"><div class="bar-fill bg-amber" style="width:' + sunPct + '%"></div></div>'
        + '<div class="item-sub">' + escHtml(sunStatus) + '</div>'
        + '</div>'
        + '<div class="disk-item" style="margin-top:10px">'
        + '<div class="disk-top clearfix"><span>' + escHtml(moon.name) + '</span><span class="right c-purple">' + moon.illum + '% iluminada</span></div>'
        + '<div class="bar-bg"><div class="bar-fill bg-purple" style="width:' + Math.max(4, moon.illum) + '%"></div></div>'
        + '<div class="item-sub">Proxima Lua Cheia em ' + moon.daysToFull + ' dias · Lua Nova em ' + moon.daysToNew + ' dias</div>'
        + '</div>';
    }

    // 7. Update Sub-View 2 (Desk / Night Stand Clock)
    if ($('n-weather-ico')) $('n-weather-ico').innerHTML = getWeatherSvg(w.code, isDay, 34);
    if ($('n-weather')) $('n-weather').textContent = Math.round(w.temp) + u + ' · ' + desc + ' (' + city + ')';
    if ($('n-weather-sub')) {
      $('n-weather-sub').textContent = 'Sensacao ' + feels + u + ' · Min ' + Math.round(w.lo) + u + ' / Max ' + Math.round(w.hi) + u + ' · Chuva ' + pop + '%';
    }
    if ($('n-loc-detail')) {
      $('n-loc-detail').textContent = city + ' · ' + coordsTxt + (w.elevation ? ' · Elev. ' + w.elevation + 'm' : '') + ' · ' + (w.timezone || 'America/Sao_Paulo');
    }
  }

  // ==================== SHORTCUTS & MIXER ====================
  function monogramFor(label) {
    var clean = String(label || '?').replace(/^\s+|\s+$/g, '');
    if (!clean) return '?';
    var parts = clean.split(/\s+/);
    if (parts.length >= 2) return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    return clean.substring(0, 2).toUpperCase();
  }

  function findWidgetOptions(cfg, typeName) {
    if (!cfg || !cfg.pages) return null;
    for (var i = 0; i < cfg.pages.length; i++) {
      var p = cfg.pages[i];
      if (!p || p.enabled === false || !p.widgets) continue;
      for (var j = 0; j < p.widgets.length; j++) {
        if (p.widgets[j] && p.widgets[j].type === typeName) {
          return p.widgets[j].options || {};
        }
      }
    }
    return null;
  }

  function hasWidgetOnBuiltInPage(cfg, viewIdx, typeName) {
    if (!cfg || !cfg.pages) return true;
    for (var i = 0; i < cfg.pages.length; i++) {
      var p = cfg.pages[i];
      if (!p || p.enabled === false) continue;
      var r = resolveBuiltInView(p);
      if (r && r.viewIdx === viewIdx) {
        var ws = p.widgets || [];
        for (var j = 0; j < ws.length; j++) {
          if (ws[j] && ws[j].type === typeName) return true;
        }
        return false;
      }
    }
    return true;
  }

  function renderShortcuts(cfg) {
    var grid = $('shortcuts-grid');
    if (!grid) return;
    var allList = (cfg && cfg.shortcuts) ? cfg.shortcuts : [];
    var scOpts = findWidgetOptions(cfg, 'shortcuts') || {};
    var cols = typeof scOpts.columns === 'number' && scOpts.columns >= 2 && scOpts.columns <= 8 ? scOpts.columns : 7;
    var list = allList;
    if (scOpts.ids && scOpts.ids.length) {
      var filtered = [];
      for (var fi = 0; fi < scOpts.ids.length; fi++) {
        for (var si = 0; si < allList.length; si++) {
          if (allList[si] && allList[si].id === scOpts.ids[fi]) {
            filtered.push(allList[si]);
            break;
          }
        }
      }
      if (filtered.length) list = filtered;
    }

    var mixCard = $('mixer-card');
    if (mixCard) {
      mixCard.style.display = hasWidgetOnBuiltInPage(cfg, 2, 'audio-mixer') ? '' : 'none';
    }

    if ($('deck-count')) $('deck-count').textContent = list.length + ' icones · ' + cols + ' col.';
    if (!list.length) {
      grid.innerHTML = '<div class="muted">Nenhum atalho configurado em http://127.0.0.1:8765</div>';
      return;
    }

    var isPort = document.body.className.indexOf('orient-portrait') !== -1;
    var effCols = isPort ? Math.min(4, cols) : cols;
    var pctW = (Math.floor(10000 / effCols) / 100) + '%';

    var html = '';
    for (var i = 0; i < list.length; i++) {
      var s = list[i];
      var color = s.color || state.accentColor || '#8bd0ff';
      var lbl = s.label || s.id;
      var iconSrc = s.iconRev
        ? ('/api/shortcuts/icon?id=' + encodeURIComponent(s.id) + '&v=' + encodeURIComponent(s.iconRev))
        : ('/legacy/icons/' + encodeURIComponent(s.id) + '.png');
      var mono = escHtml(monogramFor(lbl));
      html += '<div class="sc-wrap" style="width:' + pctW + '">'
        + '<button type="button" class="sc-btn" data-sc="' + escHtml(s.id) + '" data-sc-lbl="' + escHtml(lbl) + '" title="' + escHtml(lbl) + '" aria-label="' + escHtml(lbl) + '" style="border-bottom-color:' + escHtml(color) + '">'
        + '<div class="sc-ico-only"><img src="' + iconSrc + '" alt="' + mono + '" onerror="this.style.display=\'none\';this.parentNode.innerHTML=\'<span class=sc-ico-fallback>' + mono + '</span>\'"></div>'
        + '</button></div>';
    }
    grid.innerHTML = html;

    var btns = grid.getElementsByTagName('button');
    for (var j = 0; j < btns.length; j++) {
      btns[j].onclick = function () {
        var id = this.getAttribute('data-sc');
        var lbl = this.getAttribute('data-sc-lbl') || id;
        runShortcut(id, lbl);
      };
    }

    if (state.lastState && state.lastState.toggles) {
      updateShortcutToggleClasses(state.lastState.toggles);
    }
  }

  function updateShortcutToggleClasses(toggles) {
    var grid = $('shortcuts-grid');
    if (!grid) return;
    var btns = grid.getElementsByTagName('button');
    for (var i = 0; i < btns.length; i++) {
      var id = btns[i].getAttribute('data-sc');
      if (toggles[id]) addClass(btns[i], 'sc-on');
      else removeClass(btns[i], 'sc-on');
    }
  }

  function runShortcut(id, label) {
    if (!id) return;
    apiRequest('POST', '/api/run/' + encodeURIComponent(id), null, function () {
      showToast('Aberto: ' + (label || id));
      pollState();
    }, function () {
      showToast('Falha ao executar atalho', 'error');
    });
  }

  function renderMixer(data) {
    var box = $('mixer-list');
    if (!box) return;
    var audio = (data && data.audio) ? data.audio : null;
    var apps = audio ? (audio.sessions || audio.apps || []) : [];
    if (!apps.length) {
      box.innerHTML = '<span class="muted">Nenhum aplicativo reproduzindo audio agora.</span>';
      return;
    }
    var spkOn = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" style="vertical-align:-3px"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path></svg>';
    var spkOff = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" style="vertical-align:-3px"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><line x1="23" y1="9" x2="17" y2="15"></line><line x1="17" y1="9" x2="23" y2="15"></line></svg>';

    var html = '';
    for (var i = 0; i < apps.length; i++) {
      var a = apps[i];
      var pct = Math.max(0, Math.min(100, Math.round(typeof a.volume === 'number' ? a.volume : (typeof a.level === 'number' ? a.level : 0))));
      var aid = a.id || a.name || '';
      var appName = a.name || a.label || a.processName || aid;
      var muted = !!a.muted;
      var procKey = String(a.processName || a.name || aid).replace(/\.exe$/i, '');
      var barCls = muted ? 'bg-red' : 'bg-teal';
      var mono = escHtml(monogramFor(appName));
      html += '<div class="mixer-row clearfix" title="' + escHtml(appName) + '">'
        + '<div class="mixer-app-ico" title="' + escHtml(appName) + '"><img src="/api/procs/icon?name=' + encodeURIComponent(procKey) + '" alt="' + mono + '" onerror="this.style.display=\'none\';this.parentNode.innerHTML=\'' + mono + '\'"></div>'
        + '<div class="mixer-bar-col" data-mix-track="' + escHtml(aid) + '" title="' + escHtml(appName) + ' — toque na barra para ajustar">'
        + '<div class="pr-mini-track" style="height:12px;margin-top:5px"><div class="pr-mini-fill ' + barCls + '" style="width:' + (muted ? 0 : pct) + '%"></div></div>'
        + '</div>'
        + '<div class="mixer-pct-col">' + (muted ? '0%' : pct + '%') + (a.active ? '<span class="mixer-active-dot"></span>' : '') + '</div>'
        + '<div class="mixer-ctrls">'
        + '<button type="button" class="mix-ico-btn" data-mix-op="step" data-mix-val="-10" data-mix-id="' + escHtml(aid) + '" data-mix-cur="' + pct + '" title="-10%">−</button>'
        + '<button type="button" class="mix-ico-btn" data-mix-op="step" data-mix-val="10" data-mix-id="' + escHtml(aid) + '" data-mix-cur="' + pct + '" title="+10%">+</button>'
        + '<button type="button" class="mix-ico-btn' + (muted ? ' danger' : '') + '" data-mix-op="mute" data-mix-val="' + (muted ? 'false' : 'true') + '" data-mix-id="' + escHtml(aid) + '" title="' + (muted ? 'Ativar som' : 'Mutar') + '">' + (muted ? spkOff : spkOn) + '</button>'
        + '</div></div>';
    }
    box.innerHTML = html;

    var btns = box.getElementsByTagName('button');
    for (var j = 0; j < btns.length; j++) {
      btns[j].onclick = function () {
        var id = this.getAttribute('data-mix-id');
        var op = this.getAttribute('data-mix-op');
        if (op === 'mute') {
          var m = this.getAttribute('data-mix-val');
          apiRequest('POST', '/api/controls/command/session?id=' + encodeURIComponent(id) + '&muted=' + m, null, function () { pollControls(); });
        } else {
          var cur = parseInt(this.getAttribute('data-mix-cur'), 10) || 0;
          var delta = parseInt(this.getAttribute('data-mix-val'), 10) || 0;
          var next = Math.max(0, Math.min(100, cur + delta));
          apiRequest('POST', '/api/controls/command/session?id=' + encodeURIComponent(id) + '&value=' + next, null, function () { pollControls(); });
        }
      };
    }

    var tracks = box.getElementsByTagName('div');
    for (var k = 0; k < tracks.length; k++) {
      if (tracks[k].getAttribute('data-mix-track')) {
        tracks[k].onclick = function (e) {
          var id = this.getAttribute('data-mix-track');
          var rect = this.getBoundingClientRect();
          var x = (e.clientX || 0) - rect.left;
          var next = Math.max(0, Math.min(100, Math.round((x / (rect.width || 1)) * 100)));
          apiRequest('POST', '/api/controls/command/session?id=' + encodeURIComponent(id) + '&value=' + next, null, function () { pollControls(); });
        };
      }
    }
  }

    // ==================== PROCESSES (VISUAL REDESIGN WITH KPIS, ICONS & DUAL BARS) ====================
    function fmtMemMb(mb) {
      if (typeof mb !== 'number' || isNaN(mb)) return '0 MB';
      return mb >= 1024 ? (round1(mb / 1024) + ' GB') : (Math.round(mb) + ' MB');
    }

    function renderProcs(data) {
      state.lastProcs = data;
      var box = $('procs-list');
      var kpiBox = $('pr-kpis');
      var meta = $('procs-meta');
      if (!box) return;

      var procsPage = $('page-procs');
      if (procsPage && data && typeof data.allowKill === 'boolean') {
        if (data.allowKill === false) addClass(procsPage, 'pr-nokill');
        else removeClass(procsPage, 'pr-nokill');
      }

      var groups = (data && data.groups) ? data.groups.slice(0) : [];
      var totalProcs = (data && data.total) ? data.total : 0;
      var groupCount = (data && data.groupCount) ? data.groupCount : groups.length;
      if (meta) meta.textContent = totalProcs + ' processos · ' + groupCount + ' aplicativos';

      // Find Top CPU, Top RAM, Heavy count
      var topCpu = null, topRam = null, heavyCount = 0;
      var maxMemMb = 512;
      var idx;
      for (idx = 0; idx < groups.length; idx++) {
        var item = groups[idx];
        if (!topCpu || (item.cpu || 0) > (topCpu.cpu || 0)) topCpu = item;
        if (!topRam || (item.memMb || 0) > (topRam.memMb || 0)) topRam = item;
        if ((item.memMb || 0) > maxMemMb) maxMemMb = item.memMb;
        if ((item.memMb || 0) >= 300 || (item.cpu || 0) >= 5) heavyCount++;
      }

      // Render 4 Visual KPI Cards
      if (kpiBox) {
        var pc = (state.lastState && state.lastState.pc) ? state.lastState.pc : {};
        var sysCpu = typeof pc.cpu === 'number' ? Math.round(pc.cpu) : 0;
        var sysRamUsed = typeof pc.ramUsedGb === 'number' ? pc.ramUsedGb : 0;
        var sysRamTot = (typeof pc.ramTotalGb === 'number' && pc.ramTotalGb > 0) ? pc.ramTotalGb : 32;
        var sysRamPct = Math.min(100, Math.round((sysRamUsed / sysRamTot) * 100));

        var topCpuName = topCpu ? (topCpu.title || topCpu.name || '?') : '--';
        var topCpuKey = topCpu ? (topCpu.name || '') : '';
        var topCpuHasIco = !!(topCpu && topCpu.hasIcon !== false && topCpuKey);
        var topCpuVal = topCpu ? round1(topCpu.cpu || 0) : '0';
        var topCpuPct = Math.max(4, Math.min(100, Math.round(topCpu ? (topCpu.cpu || 0) : 0)));
        var topCpuBar = topCpuPct >= 50 ? 'bg-red' : (topCpuPct >= 25 ? 'bg-amber' : 'bg-teal');

        var topRamName = topRam ? (topRam.title || topRam.name || '?') : '--';
        var topRamKey = topRam ? (topRam.name || '') : '';
        var topRamHasIco = !!(topRam && topRam.hasIcon !== false && topRamKey);
        var topRamVal = topRam ? fmtMemMb(topRam.memMb || 0) : '0 MB';
        var topRamPct = Math.max(5, Math.min(100, Math.round(((topRam ? (topRam.memMb || 0) : 0) / (sysRamTot * 1024)) * 100)));
        var topRamBar = topRamPct >= 35 ? 'bg-red' : (topRamPct >= 18 ? 'bg-amber' : 'bg-purple');

        var heavyPct = Math.max(6, Math.min(100, Math.round((heavyCount / Math.max(1, groups.length)) * 100)));

        kpiBox.innerHTML = ''
          + '<div class="pr-kpi-col"><div class="pr-kpi-card">'
          + '<div class="pr-kpi-head">MAIOR USO DE CPU</div>'
          + '<div class="pr-kpi-body clearfix">'
          + '<div class="pr-kpi-ico">' + (topCpuHasIco ? ('<img src="/api/procs/icon?name=' + encodeURIComponent(topCpuKey) + '" alt="" onerror="this.style.display=\'none\'">') : escHtml(monogramFor(topCpuName))) + '</div>'
          + '<span class="pr-kpi-val c-teal">' + topCpuVal + '%</span>'
          + '<span class="pr-kpi-sub">' + escHtml(topCpuName) + '</span>'
          + '</div>'
          + '<div class="bar-bg"><div class="bar-fill ' + topCpuBar + '" style="width:' + topCpuPct + '%"></div></div>'
          + '</div></div>'

          + '<div class="pr-kpi-col"><div class="pr-kpi-card">'
          + '<div class="pr-kpi-head">MAIOR USO DE RAM</div>'
          + '<div class="pr-kpi-body clearfix">'
          + '<div class="pr-kpi-ico">' + (topRamHasIco ? ('<img src="/api/procs/icon?name=' + encodeURIComponent(topRamKey) + '" alt="" onerror="this.style.display=\'none\'">') : escHtml(monogramFor(topRamName))) + '</div>'
          + '<span class="pr-kpi-val c-purple">' + escHtml(topRamVal) + '</span>'
          + '<span class="pr-kpi-sub">' + escHtml(topRamName) + '</span>'
          + '</div>'
          + '<div class="bar-bg"><div class="bar-fill ' + topRamBar + '" style="width:' + topRamPct + '%"></div></div>'
          + '</div></div>'

          + '<div class="pr-kpi-col"><div class="pr-kpi-card">'
          + '<div class="pr-kpi-head">CARGA DO SISTEMA</div>'
          + '<div class="pr-kpi-body clearfix">'
          + '<span class="pr-kpi-val">' + sysRamPct + '% RAM</span>'
          + '<span class="pr-kpi-sub">CPU ' + sysCpu + '% · ' + round1(sysRamUsed) + '/' + round1(sysRamTot) + 'G</span>'
          + '</div>'
          + '<div class="bar-bg"><div class="bar-fill ' + (sysRamPct >= 85 ? 'bg-red' : 'bg-sky') + '" style="width:' + sysRamPct + '%"></div></div>'
          + '</div></div>'

          + '<div class="pr-kpi-col"><div class="pr-kpi-card">'
          + '<div class="pr-kpi-head">APPS PESADOS / TOTAL</div>'
          + '<div class="pr-kpi-body clearfix">'
          + '<span class="pr-kpi-val c-amber">' + heavyCount + ' apps</span>'
          + '<span class="pr-kpi-sub">' + totalProcs + ' instancias</span>'
          + '</div>'
          + '<div class="bar-bg"><div class="bar-fill bg-amber" style="width:' + heavyPct + '%"></div></div>'
          + '</div></div>';
      }

      groups.sort(function (a, b) {
        var diff;
        if (state.sortProcs === 'cpu') {
          diff = (b.cpu || 0) - (a.cpu || 0);
          if (Math.abs(diff) > 0.05) return diff;
          diff = (b.memMb || 0) - (a.memMb || 0);
          if (Math.abs(diff) > 0.5) return diff;
        } else {
          diff = (b.memMb || 0) - (a.memMb || 0);
          if (Math.abs(diff) > 0.5) return diff;
          diff = (b.cpu || 0) - (a.cpu || 0);
          if (Math.abs(diff) > 0.05) return diff;
        }
        return String(a.name || '').toLowerCase() < String(b.name || '').toLowerCase() ? -1 : 1;
      });

      var limit = Math.min(groups.length, state.procsLimit || 15);
      var now = new Date().getTime();
      if (state.confirmKill && (now - state.confirmKillAt) >= 5000) state.confirmKill = '';

      // Dirty-check signature so unchanged polls never rebuild DOM or flicker icons
      var sig = state.sortProcs + '|' + limit + '|' + state.confirmKill + '|';
      var i;
      for (i = 0; i < limit; i++) {
        var sg = groups[i];
        sig += (sg.name || '') + ':' + Math.round((sg.cpu || 0) * 10) + ':' + Math.round(sg.memMb || 0) + ':' + (sg.count || 1) + ':' + (sg.locked ? 1 : 0) + ';';
      }
      if (sig === state.lastProcsSig && box.childNodes.length > 0) return;
      state.lastProcsSig = sig;

      var killSvg = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" style="vertical-align:-2px"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>';

      var html = '';
      for (i = 0; i < limit; i++) {
        var g = groups[i];
        var name = g.name || '?';
        var label = g.title || name;
        var isConfirm = (state.confirmKill === name && (now - state.confirmKillAt) < 5000);
        var cpuVal = g.cpu || 0;
        var cpuBarW = Math.max(cpuVal > 0.1 ? 3 : 0, Math.min(100, Math.round(cpuVal)));
        var cpuCls = cpuVal >= 40 ? 'bg-red' : (cpuVal >= 15 ? 'bg-amber' : 'bg-teal');
        var memMb = g.memMb || 0;
        var ramBarW = Math.max(3, Math.min(100, Math.round((memMb / maxMemMb) * 100)));
        var ramCls = memMb >= 2048 ? 'bg-amber' : 'bg-purple';
        var mono = escHtml(monogramFor(label));
        var icoInner = (g.hasIcon === false)
          ? mono
          : ('<img src="/api/procs/icon?name=' + encodeURIComponent(name) + '" alt="' + mono + '" onerror="this.style.display=\'none\';this.parentNode.innerHTML=\'' + mono + '\'">');
        var actHtml = g.locked
          ? '<span class="pr-lock-pill" title="Processo protegido do sistema">Sist.</span>'
          : ('<button type="button" class="pr-kill-btn' + (isConfirm ? ' danger' : '') + '" data-kill="' + escHtml(name) + '" title="Encerrar ' + escHtml(label) + '">' + (isConfirm ? 'Fechar?' : killSvg) + '</button>');

        html += '<div class="proc-row clearfix">'
          + '<div class="pr-app-cell clearfix">'
          + '<div class="pr-app-ico">' + icoInner + '</div>'
          + '<div class="pr-app-txt"><div class="pr-app-name" title="' + escHtml(label) + '">' + escHtml(label) + (g.count > 1 ? '<span class="proc-cnt">×' + g.count + '</span>' : '') + '</div></div>'
          + '</div>'

          + '<div class="pr-bar-cell pr-bar-cpu">'
          + '<div class="pr-bar-top clearfix"><span class="c-teal">' + round1(cpuVal) + '%</span></div>'
          + '<div class="pr-mini-track"><div class="pr-mini-fill ' + cpuCls + '" style="width:' + cpuBarW + '%"></div></div>'
          + '</div>'

          + '<div class="pr-bar-cell pr-bar-ram">'
          + '<div class="pr-bar-top clearfix"><span class="c-purple">' + escHtml(fmtMemMb(memMb)) + '</span></div>'
          + '<div class="pr-mini-track"><div class="pr-mini-fill ' + ramCls + '" style="width:' + ramBarW + '%"></div></div>'
          + '</div>'

          + '<div class="proc-act">' + actHtml + '</div>'
          + '</div>';
      }
      box.innerHTML = html || '<div class="muted">Sem processos listados.</div>';

      var btns = box.getElementsByTagName('button');
      for (var j = 0; j < btns.length; j++) {
        btns[j].onclick = function () {
          var target = this.getAttribute('data-kill');
          var t = new Date().getTime();
          if (state.confirmKill === target && (t - state.confirmKillAt) < 5000) {
            state.confirmKill = '';
            state.lastProcsSig = '';
            apiRequest('POST', '/api/procs/kill?name=' + encodeURIComponent(target), null, function () {
              showToast('Encerrado: ' + target, 'ok');
              pollProcs();
            }, function (code, errMsg) {
              var msg = errMsg || (code === 403 ? 'Encerrar processos desabilitado nas configuracoes' : 'Nao foi possivel encerrar ' + target);
              showToast(msg, 'error');
              pollProcs();
            });
          } else {
            // Reset any previously armed button cleanly
            var allB = box.getElementsByTagName('button');
            for (var k = 0; k < allB.length; k++) {
              removeClass(allB[k], 'danger');
              allB[k].innerHTML = killSvg;
            }
            state.confirmKill = target;
            state.confirmKillAt = t;
            this.textContent = 'Fechar?';
            addClass(this, 'danger');
          }
        };
      }
    }

      // ==================== AI / WORK / PUSH / APPROVALS ====================
      function fmtResetTime(resetsAtMs) {
        if (typeof resetsAtMs !== 'number' || isNaN(resetsAtMs) || resetsAtMs <= 0) return '';
        var now = new Date().getTime();
        var diffSec = Math.max(0, Math.floor((resetsAtMs - now) / 1000));
        var d = new Date(resetsAtMs);
        var hm = pad2(d.getHours()) + ':' + pad2(d.getMinutes());
        if (diffSec <= 0) return 'Resetando agora';
        var days = Math.floor(diffSec / 86400);
        var hrs = Math.floor((diffSec % 86400) / 3600);
        var mins = Math.floor((diffSec % 3600) / 60);
        if (days > 0) return 'Reseta em ' + days + 'd ' + hrs + 'h (' + pad2(d.getDate()) + '/' + pad2(d.getMonth() + 1) + ' ' + hm + ')';
        if (hrs > 0) return 'Reseta em ' + hrs + 'h ' + mins + 'm (' + hm + ')';
        return 'Reseta em ' + mins + 'm (' + hm + ')';
      }

      function translateLimitLabel(raw, dupCount) {
        var s = String(raw || 'Limite');
        s = s.replace('5-hour session', 'Sessao de 5 horas')
          .replace('5-hour', 'Janela de 5 horas')
          .replace('Weekly', 'Limite Semanal');
        if (dupCount > 1) s += ' #' + dupCount;
        return s;
      }

      function renderQuotas(data) {
        state.lastQuotas = data;
        var box = $('quotas-list');
        if (!box) return;
        var stamp = $('ai-quota-stamp');
        if (stamp) {
          var now = new Date();
          stamp.textContent = '· ' + pad2(now.getHours()) + ':' + pad2(now.getMinutes()) + ':' + pad2(now.getSeconds()) + ' (5s)';
        }
        var allProvs = (data && data.providers) ? data.providers : [];
        var allowed = (state.config && state.config.codexBar && state.config.codexBar.providers && state.config.codexBar.providers.length)
          ? state.config.codexBar.providers : null;
        var provs = [];
        for (var pIdx = 0; pIdx < allProvs.length; pIdx++) {
          var cand = allProvs[pIdx];
          if (!allowed) {
            provs.push(cand);
          } else {
            for (var aIdx = 0; aIdx < allowed.length; aIdx++) {
              if (allowed[aIdx] === cand.id) { provs.push(cand); break; }
            }
          }
        }
        if (!provs.length) provs = allProvs;
        if (!provs.length) {
          box.innerHTML = '<span class="muted">Nenhuma cota de IA configurada/ativa.</span>';
          return;
        }
        var html = '';
        for (var i = 0; i < provs.length; i++) {
          var p = provs[i];
          var pName = p.name || p.id || 'IA';
          var pPlan = p.plan && p.plan !== pName ? p.plan : '';
          var limits = p.limits || [];
          html += '<div class="q-provider">'
            + '<div class="q-head clearfix"><span>' + escHtml(pName) + '</span>'
            + (pPlan ? '<span class="pill-tag pill-plan">' + escHtml(pPlan) + '</span>' : '')
            + '</div>';

          var seenLabels = {};
          for (var j = 0; j < limits.length; j++) {
            var l = limits[j];
            var rawLbl = l.label || 'Limite';
            seenLabels[rawLbl] = (seenLabels[rawLbl] || 0) + 1;
            var lbl = translateLimitLabel(rawLbl, seenLabels[rawLbl]);
            var usedPct = typeof l.percent === 'number' ? Math.max(0, Math.min(100, Math.round(l.percent * 10) / 10)) : 0;
            var remPct = Math.max(0, Math.round((100 - usedPct) * 10) / 10);
            var barCls = (l.severity === 'critical' || usedPct >= 95) ? 'bg-red' : ((l.severity === 'warning' || usedPct >= 80) ? 'bg-amber' : 'bg-teal');
            var valCls = (l.severity === 'critical' || usedPct >= 95) ? 'c-red' : ((l.severity === 'warning' || usedPct >= 80) ? 'c-amber' : 'c-teal');
            var resetTxt = l.resetDescription || fmtResetTime(l.resetsAt);

            html += '<div class="q-limit">'
              + '<div class="q-limit-top clearfix">'
              + '<span>' + escHtml(lbl) + '</span>'
              + '<span class="right ' + valCls + '">' + usedPct + '% usado</span>'
              + '</div>'
              + '<div class="bar-bg"><div class="bar-fill ' + barCls + '" style="width:' + Math.min(100, usedPct) + '%"></div></div>'
              + '<div class="q-limit-meta clearfix">'
              + '<span>Disponivel: <b>' + remPct + '%</b></span>'
              + (resetTxt ? '<span class="right">' + escHtml(resetTxt) + '</span>' : '')
              + '</div>'
              + '</div>';
          }

          if (p.extra) {
            html += '<div class="item-sub" style="margin-top:6px">' + escHtml(p.extra) + '</div>';
          }

          if (p.error) {
            var errMsg = p.error;
            if (errMsg.indexOf('No Claude Code sign-in') !== -1) {
              errMsg = 'Plano ' + (pPlan || 'Claude') + ' detectado. Para ler a cota direta do Claude Code CLI, execute "claude auth login" no terminal do PC (os limites de Claude/GPT do Antigravity ja aparecem no card Antigravity).';
            }
            html += '<div class="q-note">' + escHtml(errMsg) + '</div>';
          } else if (!limits.length) {
            html += '<div class="item-sub">Sem limites reportados no momento.</div>';
          }

          html += '</div>';
        }
        box.innerHTML = html;
      }

      function fmtTokens(n) {
        if (typeof n !== 'number' || isNaN(n) || n <= 0) return '';
        if (n >= 1000000) return round1(n / 1000000) + 'M tok';
        if (n >= 1000) return round1(n / 1000) + 'k tok';
        return n + ' tok';
      }

      function renderWork(data) {
        state.lastWork = data;
        var box = $('work-list');
        if (!box) return;
        var sessions = (data && data.sessions) ? data.sessions : [];
        var gh = (data && data.github) ? data.github : null;
        var html = '';

        if (!sessions.length) {
          html += '<div class="muted" style="margin-bottom:8px">Nenhuma sessao ativa de Claude Code, Codex ou Antigravity.</div>';
        } else {
          for (var i = 0; i < sessions.length; i++) {
            var s = sessions[i];
            var toolName = (s.tool || 'IA').toUpperCase();
            var st = s.status || 'idle';
            var stLabel = st === 'busy' ? 'EM EXECUCAO' : (st === 'waiting' ? 'AGUARDANDO' : 'CONCLUIDO');
            var stCls = st === 'busy' ? 'pill-busy' : (st === 'waiting' ? 'pill-wait' : '');
            var title = s.title || s.project || s.name || s.id || 'Sessao';
            var metaParts = [toolName];
            if (s.model) metaParts.push(s.model);
            if (s.lastTool) metaParts.push('tool: ' + s.lastTool);
            var tok = fmtTokens(s.totalTokens);
            if (tok) metaParts.push(tok);

            html += '<div class="item-row">'
              + '<div class="item-title clearfix">'
              + '<span>' + escHtml(title) + '</span>'
              + '<span class="pill-tag ' + stCls + ' right">' + stLabel + '</span>'
              + '</div>'
              + '<div class="item-sub">' + escHtml(metaParts.join(' · ')) + '</div>'
              + '</div>';
          }
        }

        if (gh && gh.user) {
          var prs = gh.myPrs || [];
          var revs = gh.reviewRequests || [];
          html += '<div style="margin-top:10px;padding-top:8px;border-top:1px solid #354f76">'
            + '<div class="card-title" style="margin-bottom:6px">GITHUB (@' + escHtml(gh.user) + ') · ' + prs.length + ' PRs · ' + revs.length + ' Reviews</div>';
          for (var k = 0; k < prs.length; k++) {
            var pr = prs[k];
            html += '<div class="item-row">'
              + '<div class="item-title">' + escHtml(pr.repo + ' #' + pr.number) + '</div>'
              + '<div class="item-sub">' + escHtml(pr.title || '') + '</div>'
              + '</div>';
          }
          html += '</div>';
        }

        box.innerHTML = html;
      }

      function formatPushValue(raw) {
        if (raw === null || raw === undefined) return { main: '--', sub: '' };
        if (typeof raw === 'object') {
          if (raw.value !== undefined) {
            var main = String(raw.value) + (raw.unit ? ' ' + raw.unit : '');
            var sub = raw.sub || raw.title || '';
            return { main: main, sub: sub };
          }
          if (raw.text !== undefined) {
            return { main: String(raw.text), sub: raw.title || raw.sub || '' };
          }
          try { return { main: JSON.stringify(raw), sub: '' }; } catch (e) { return { main: String(raw), sub: '' }; }
        }
        return { main: String(raw), sub: '' };
      }

      function renderPush(data) {
        state.lastPush = data;
        var box = $('push-list');
        if (!box) return;
        var raw = (data && data.items) ? data.items : null;
        var entries = [];
        if (raw && Object.prototype.toString.call(raw) === '[object Array]') {
          for (var a = 0; a < raw.length; a++) {
            entries.push({ key: raw[a].key || String(a), data: raw[a].value !== undefined ? raw[a].value : raw[a].data, at: raw[a].at });
          }
        } else if (raw && typeof raw === 'object') {
          for (var k in raw) {
            if (Object.prototype.hasOwnProperty.call(raw, k)) {
              var v = raw[k] || {};
              entries.push({ key: k, data: v.data !== undefined ? v.data : v.value, at: v.at });
            }
          }
        }
        if (!entries.length) {
          box.innerHTML = '<span class="muted">Nenhum valor enviado via webhook.</span>';
          return;
        }
        var html = '';
        for (var i = 0; i < entries.length; i++) {
          var it = entries[i];
          var fv = formatPushValue(it.data);
          var meta = fv.sub;
          if (it.at) meta = (meta ? meta + ' · ' : '') + fmtTimeOnly(it.at);
          html += '<div class="item-row">'
            + '<div class="clearfix"><span class="item-title">' + escHtml(it.key) + '</span>'
            + '<span class="right c-teal" style="font-weight:bold">' + escHtml(fv.main) + '</span></div>'
            + (meta ? '<div class="item-sub">' + escHtml(meta) + '</div>' : '')
            + '</div>';
        }
        box.innerHTML = html;
      }

      function renderApprovals(data) {
        var items = (data && data.items) ? data.items : [];
        var modal = $('approval-modal');
        if (!modal) return;
        if (!items.length) {
          state.activeApproval = null;
          addClass(modal, 'hidden');
          return;
        }
        var a = items[0];
        state.activeApproval = a;
        if ($('ap-source')) $('ap-source').textContent = (a.source || 'Agente IA') + ' · Permissao';
        if ($('ap-project')) $('ap-project').textContent = (a.project || '') + (a.cwd ? ' — ' + a.cwd : '');
        if ($('ap-tool')) $('ap-tool').textContent = a.tool || 'Acao';
        if ($('ap-detail')) $('ap-detail').textContent = a.detail || a.description || '';
        removeClass(modal, 'hidden');
      }

      function sendApproval(decision) {
        if (!state.activeApproval) return;
        var id = state.activeApproval.id;
        apiRequest('POST', '/api/approvals/' + encodeURIComponent(id) + '?decision=' + encodeURIComponent(decision), null, function () {
          showToast(decision === 'allow' ? 'Permitido' : 'Negado');
          state.activeApproval = null;
          addClass($('approval-modal'), 'hidden');
        });
      }

      // ==================== DISCORD (REDESIGN ESCALÁVEL PARA MUITOS SERVIDORES E USUÁRIOS) ====================
      function fmtTimeOnly(tsMs) {
        if (typeof tsMs !== 'number' || isNaN(tsMs) || tsMs <= 0) return '';
        var d = new Date(tsMs);
        return pad2(d.getHours()) + ':' + pad2(d.getMinutes()) + ':' + pad2(d.getSeconds());
      }

      function setDiscordSubTab(tab) {
        state.dcSubTab = tab === 'notifs' ? 'notifs' : 'chans';
        if ($('dc-tab-chans')) $('dc-tab-chans').className = state.dcSubTab === 'chans' ? 'dc-subtab active' : 'dc-subtab';
        if ($('dc-tab-notifs')) $('dc-tab-notifs').className = state.dcSubTab === 'notifs' ? 'dc-subtab active' : 'dc-subtab';
        if ($('dc-panel-chans')) $('dc-panel-chans').className = state.dcSubTab === 'chans' ? 'dc-subpanel active' : 'dc-subpanel';
        if ($('dc-panel-notifs')) $('dc-panel-notifs').className = state.dcSubTab === 'notifs' ? 'dc-subpanel active' : 'dc-subpanel';
        updateDiscordScrollBounds();
      }

      function renderDiscord(data, force) {
        if (!data) return;
        state.dcLastData = data;

        var badge = $('dc-badge');
        var titleEl = $('dc-call-title');
        var subEl = $('dc-call-sub');
        var banner = $('dc-call-banner');
        var membersBox = $('dc-members');
        var notifsBox = $('dc-notifs');
        var chansBox = $('dc-channels');
        var chipsBox = $('dc-guild-chips');
        var speakCountEl = $('dc-speaking-count');
        var notifCountEl = $('dc-notif-count');
        var chanCountEl = $('dc-chan-count');
        var occBtn = $('dc-only-occupied');
        var spkBtn = $('dc-only-speaking');

        var enabled = !!data.enabled;
        var s = data.snapshot || {};
        var connected = !!s.connected;
        var i;

        // Controls state
        var muteBtn = $('dc-mute');
        var deafBtn = $('dc-deafen');
        var modeBtn = $('dc-inputmode');
        if (muteBtn) {
          muteBtn.textContent = s.muted ? 'Mic: MUTADO' : 'Mic: ATIVO';
          muteBtn.className = s.muted ? 'dc-btn dc-on-warn' : 'dc-btn';
        }
        if (deafBtn) {
          deafBtn.textContent = s.deafened ? 'Audio: SURDO' : 'Audio: OK';
          deafBtn.className = s.deafened ? 'dc-btn dc-on-warn' : 'dc-btn';
        }
        if (modeBtn) {
          var isPtt = s.inputMode === 'PUSH_TO_TALK';
          modeBtn.textContent = isPtt ? 'Push-to-Talk' : 'Voz Auto';
          modeBtn.className = isPtt ? 'dc-btn dc-on-ptt' : 'dc-btn';
        }
        if (spkBtn) {
          spkBtn.className = state.dcOnlySpeaking ? 'dc-filter-pill right active' : 'dc-filter-pill right';
        }

        if (!enabled) {
          if (badge) { badge.textContent = 'DESATIVADO'; badge.className = 'pill-tag pill-wait'; }
          if (banner) banner.className = 'dc-call-banner';
          if (titleEl) titleEl.textContent = 'Integracao do Discord desativada';
          if (subEl) subEl.textContent = 'Ative em http://127.0.0.1:8765 (General > Discord) com Client ID e Secret.';
          if (membersBox) membersBox.innerHTML = '<div class="q-note">Salve o Client ID e Client Secret em http://127.0.0.1:8765 e autorize no Discord Desktop.</div>';
          if (chipsBox) chipsBox.innerHTML = '';
          if (chansBox) chansBox.innerHTML = '<span class="muted">Aguardando ativacao do Discord.</span>';
          if (notifsBox) notifsBox.innerHTML = '<span class="muted">Aguardando ativacao do Discord.</span>';
          state.dcSigMembers = ''; state.dcSigChans = ''; state.dcSigNotifs = '';
          return;
        }

        if (!connected) {
          if (badge) { badge.textContent = 'DESCONECTADO'; badge.className = 'pill-tag pill-wait'; }
          if (banner) banner.className = 'dc-call-banner';
          var errTxt = s.error || 'Discord Desktop nao esta aberto ou aguardando autorizacao.';
          if (errTxt.indexOf('Enter the application') !== -1) {
            errTxt = 'Informe o Client ID e Secret em http://127.0.0.1:8765 (General > Discord).';
          } else if (errTxt.indexOf('Waiting for you to approve') !== -1) {
            errTxt = 'Clique em "Authorize" na janela do Discord Desktop no PC.';
          } else if (errTxt.indexOf('Discord is not running') !== -1) {
            errTxt = 'Abra o Discord Desktop no PC para conectar automaticamente.';
          }
          if (titleEl) titleEl.textContent = 'Aguardando Discord Desktop';
          if (subEl) subEl.textContent = errTxt;
          if (membersBox) membersBox.innerHTML = '<div class="q-note">' + escHtml(errTxt) + '</div>';
          if (chipsBox) chipsBox.innerHTML = '';
          state.dcSigMembers = ''; state.dcSigChans = ''; state.dcSigNotifs = '';
          return;
        }

        // Connected
        if (badge) {
          badge.textContent = 'CONECTADO' + (s.user ? ' · @' + s.user : '');
          badge.className = 'pill-tag pill-busy';
        }

        var members = (s.members || []).slice(0);
        var speakingCount = 0;
        for (i = 0; i < members.length; i++) {
          if (members[i].speaking) speakingCount++;
        }

        if (s.channelId || s.channelName) {
          if (banner) banner.className = 'dc-call-banner in-call';
          var kindMap = { 'call': 'Chamada DM', 'group call': 'Grupo', 'stage': 'Palco', 'voice': 'Canal de Voz' };
          var kindLbl = kindMap[s.channelKind] || 'Em Chamada';
          if (titleEl) titleEl.textContent = (s.channelName || 'Chamada ativa') + ' (' + members.length + ' na call)';
          var pttWarn = (s.inputMode === 'PUSH_TO_TALK' && !s.pttKey) ? ' · Push-to-Talk sem tecla!' : '';
          if (subEl) subEl.textContent = kindLbl + (speakingCount > 0 ? ' · ' + speakingCount + ' falando agora' : ' · Conectado') + pttWarn;
        } else {
          if (banner) banner.className = 'dc-call-banner';
          if (titleEl) titleEl.textContent = 'Fora de chamada de voz';
          if (subEl) subEl.textContent = 'Escolha um servidor ou toque em "Entrar" em qualquer canal ao lado.';
        }

        // 1. Render Active Call Members (Speaking pinned to top + 2-col compact grid for >4 users)
        if (speakCountEl) {
          speakCountEl.textContent = speakingCount > 0 ? ('(' + speakingCount + ' falando / ' + members.length + ')') : (members.length ? ('(' + members.length + ')') : '');
        }
        members.sort(function (a, b) {
          if (!!b.speaking !== !!a.speaking) return b.speaking ? 1 : -1;
          if (!!a.deafened !== !!b.deafened) return a.deafened ? 1 : -1;
          if (!!a.muted !== !!b.muted) return a.muted ? 1 : -1;
          return String(a.name || '').toLowerCase() < String(b.name || '').toLowerCase() ? -1 : 1;
        });

        var filteredMembers = [];
        for (i = 0; i < members.length; i++) {
          if (!state.dcOnlySpeaking || members[i].speaking) filteredMembers.push(members[i]);
        }

        var memSig = (state.dcOnlySpeaking ? '1' : '0') + '|' + filteredMembers.length + '|';
        for (i = 0; i < filteredMembers.length; i++) {
          var fm = filteredMembers[i];
          memSig += (fm.id || fm.name) + ':' + (fm.speaking ? 1 : 0) + (fm.muted ? 1 : 0) + (fm.deafened ? 1 : 0) + ';';
        }
        if ((force || memSig !== state.dcSigMembers) && membersBox) {
          state.dcSigMembers = memSig;
          if (!members.length) {
            membersBox.innerHTML = '<span class="muted">Voce nao esta em nenhum canal de voz agora.</span>';
          } else if (!filteredMembers.length) {
            membersBox.innerHTML = '<span class="muted">Ninguem falando neste instante (' + members.length + ' na call).</span>';
          } else {
            var useGrid = filteredMembers.length > 4;
            var mHtml = useGrid ? '<div class="dc-members-grid clearfix">' : '';
            for (i = 0; i < filteredMembers.length; i++) {
              var m = filteredMembers[i];
              var isSpeak = !!m.speaking;
              var avInner = m.avatar
                ? '<img src="/api/discord/img?kind=user&id=' + encodeURIComponent(m.id) + '&hash=' + encodeURIComponent(m.avatar) + '" alt="">'
                : escHtml(monogramFor(m.name || '?'));
              var stTxt = isSpeak ? 'Falando' : (m.deafened ? 'Surdo' : (m.muted ? 'Mutado' : 'Ouvindo'));
              var cardHtml = '<div class="dc-member clearfix' + (isSpeak ? ' dc-speaking' : '') + '">'
                + '<div class="dc-avatar">' + avInner + '</div>'
                + '<div class="dc-m-info">'
                + '<div class="dc-m-name" title="' + escHtml(m.name || '') + '">' + escHtml(m.name || 'Usuario')
                + (isSpeak ? '<span class="dc-speak-pill">VOZ</span>' : '')
                + (m.deafened ? '<span class="dc-mute-pill">SURDO</span>' : (m.muted ? '<span class="dc-mute-pill">MUDO</span>' : ''))
                + '</div>'
                + '<div class="dc-m-state">' + escHtml(stTxt) + '</div>'
                + '</div></div>';
              mHtml += useGrid ? ('<div class="dc-m-tile">' + cardHtml + '</div>') : cardHtml;
            }
            if (useGrid) mHtml += '</div>';
            membersBox.innerHTML = mHtml;
          }
        }

        // 2. Render Notifications
        var notifs = s.notifications || [];
        if (notifCountEl) notifCountEl.textContent = notifs.length ? ('(' + notifs.length + ')') : '';
        var notifSig = notifs.length + '|' + (notifs.length ? ((notifs[notifs.length - 1].ts || '') + '|' + (notifs[notifs.length - 1].body || '')) : '');
        if ((force || notifSig !== state.dcSigNotifs) && notifsBox) {
          state.dcSigNotifs = notifSig;
          if (!notifs.length) {
            notifsBox.innerHTML = '<span class="muted">Nenhuma notificacao recente do Discord.</span>';
          } else {
            var nHtml = '';
            for (i = notifs.length - 1; i >= 0; i--) {
              var n = notifs[i];
              nHtml += '<div class="dc-notif">'
                + '<div class="dc-notif-head clearfix"><span>' + escHtml(n.title || 'Discord') + '</span>'
                + '<span class="right muted">' + escHtml(fmtTimeOnly(n.ts)) + '</span></div>'
                + '<div class="dc-notif-body">' + escHtml(n.body || '') + '</div>'
                + '</div>';
            }
            notifsBox.innerHTML = nHtml;
          }
        }

        // 3. Render Scalable Guild Chips + Collapsible Server Accordions (NO 45-channel cutoff!)
        var channels = s.channels || [];
        var guildMap = {};
        var guildOrder = [];
        var totalOccupiedChans = 0;
        var curGuildKey = '';

        for (i = 0; i < channels.length; i++) {
          var ch = channels[i];
          var gKey = ch.guildId || ch.guild || 'Servidor';
          var gName = ch.guild || 'Servidor';
          if (!guildMap[gKey]) {
            guildMap[gKey] = { key: gKey, name: gName, channels: [], peopleTotal: 0, hasCurrent: false };
            guildOrder.push(guildMap[gKey]);
          }
          var pCount = (ch.people && ch.people.length) ? ch.people.length : (ch.peopleCount || 0);
          var isCurCh = !!(s.channelId && ch.id === s.channelId);
          if (pCount > 0) totalOccupiedChans++;
          if (isCurCh) {
            guildMap[gKey].hasCurrent = true;
            curGuildKey = gKey;
          }
          guildMap[gKey].peopleTotal += pCount;
          guildMap[gKey].channels.push(ch);
        }

        // Sort guilds: current guild first, then by active people desc, then alphabetically
        guildOrder.sort(function (a, b) {
          if (a.hasCurrent !== b.hasCurrent) return a.hasCurrent ? -1 : 1;
          if (b.peopleTotal !== a.peopleTotal) return b.peopleTotal - a.peopleTotal;
          return a.name.toLowerCase() < b.name.toLowerCase() ? -1 : 1;
        });

        if (state.dcGuild !== 'all' && !guildMap[state.dcGuild]) {
          state.dcGuild = 'all';
        }
        if (chanCountEl) chanCountEl.textContent = channels.length ? ('(' + channels.length + ')') : '';
        if (occBtn) {
          occBtn.textContent = totalOccupiedChans > 0 ? ('Com gente (' + totalOccupiedChans + ')') : 'Com gente';
          occBtn.className = state.dcOnlyOccupied ? 'dc-filter-pill dc-occ-btn active' : 'dc-filter-pill dc-occ-btn';
        }

        var query = String(state.dcSearch || '').replace(/^\s+|\s+$/g, '').toLowerCase();
        var expKeys = '';
        for (var ek in state.dcExpanded) {
          if (Object.prototype.hasOwnProperty.call(state.dcExpanded, ek)) {
            expKeys += ek + '=' + (state.dcExpanded[ek] ? 1 : 0) + ',';
          }
        }
        var chSig = (s.channelId || '') + '|' + state.dcGuild + '|' + (state.dcOnlyOccupied ? 1 : 0) + '|' + query + '|' + expKeys + '|';
        for (i = 0; i < channels.length; i++) {
          var sc = channels[i];
          var sp = (sc.people && sc.people.length) ? sc.people.length : 0;
          chSig += (sc.id || '') + ':' + sp + ';';
        }

        if (force || chSig !== state.dcSigChans) {
          state.dcSigChans = chSig;

          // Render Guild Filter Chips
          if (chipsBox) {
            if (guildOrder.length <= 1) {
              chipsBox.style.display = 'none';
              chipsBox.innerHTML = '';
            } else {
              chipsBox.style.display = 'block';
              var chipHtml = '<button type="button" class="dc-chip' + (state.dcGuild === 'all' ? ' active' : '') + '" data-dc-guild="all">Todos (' + guildOrder.length + ')</button>';
              for (i = 0; i < guildOrder.length; i++) {
                var go = guildOrder[i];
                var shortName = go.name.length > 18 ? (go.name.substring(0, 17) + '…') : go.name;
                chipHtml += '<button type="button" class="dc-chip' + (state.dcGuild === go.key ? ' active' : '') + '" data-dc-guild="' + escHtml(go.key) + '">'
                  + escHtml(shortName)
                  + (go.peopleTotal > 0 ? '<span class="dc-chip-cnt">' + go.peopleTotal + '</span>' : '')
                  + '</button>';
              }
              chipsBox.innerHTML = chipHtml;
              var chipBtns = chipsBox.getElementsByTagName('button');
              for (var cb = 0; cb < chipBtns.length; cb++) {
                chipBtns[cb].onclick = function () {
                  state.dcGuild = this.getAttribute('data-dc-guild') || 'all';
                  renderDiscord(state.dcLastData, true);
                };
              }
            }
          }

          // Render Collapsible Server Accordions
          if (chansBox) {
            if (!channels.length) {
              chansBox.innerHTML = '<span class="muted">Nenhum canal de voz disponivel.</span>';
            } else {
              var manyGuilds = guildOrder.length >= 3 || channels.length >= 14;
              var anyAutoExpanded = false;
              var accHtml = '';
              var totalMatched = 0;

              for (var gi = 0; gi < guildOrder.length; gi++) {
                var grp = guildOrder[gi];
                if (state.dcGuild !== 'all' && grp.key !== state.dcGuild) continue;

                var matchedChans = [];
                var gMatchesQuery = query && grp.name.toLowerCase().indexOf(query) !== -1;
                for (var ci = 0; ci < grp.channels.length; ci++) {
                  var cItem = grp.channels[ci];
                  var ppl = cItem.people || [];
                  var isCur = !!(s.channelId && cItem.id === s.channelId);
                  if (state.dcOnlyOccupied && ppl.length === 0 && !isCur) continue;
                  if (query && !gMatchesQuery) {
                    var hit = String(cItem.name || '').toLowerCase().indexOf(query) !== -1;
                    if (!hit) {
                      for (var pi = 0; pi < ppl.length; pi++) {
                        if (String(ppl[pi].name || '').toLowerCase().indexOf(query) !== -1) { hit = true; break; }
                      }
                    }
                    if (!hit) continue;
                  }
                  matchedChans.push(cItem);
                }
                if (!matchedChans.length) continue;
                totalMatched += matchedChans.length;

                // Sort channels within guild: current first, occupied first, then original order
                matchedChans.sort(function (a, b) {
                  var aCur = s.channelId && a.id === s.channelId;
                  var bCur = s.channelId && b.id === s.channelId;
                  if (aCur !== bCur) return aCur ? -1 : 1;
                  var aP = (a.people && a.people.length) ? a.people.length : 0;
                  var bP = (b.people && b.people.length) ? b.people.length : 0;
                  if ((bP > 0) !== (aP > 0)) return bP > 0 ? 1 : -1;
                  return 0;
                });

                var isExp;
                if (query || state.dcGuild !== 'all' || state.dcOnlyOccupied) {
                  isExp = true;
                } else if (typeof state.dcExpanded[grp.key] === 'boolean') {
                  isExp = state.dcExpanded[grp.key];
                } else if (!manyGuilds) {
                  isExp = true;
                } else {
                  isExp = grp.hasCurrent || grp.peopleTotal > 0 || (!anyAutoExpanded && gi === 0);
                }
                if (isExp) anyAutoExpanded = true;

                accHtml += '<div class="dc-guild-acc">'
                  + '<button type="button" class="dc-guild-head-btn clearfix" data-dc-toggle="' + escHtml(grp.key) + '">'
                  + '<span class="dc-guild-arrow">' + (isExp ? '▾' : '▸') + '</span>'
                  + '<span class="dc-guild-name-txt">' + escHtml(grp.name) + '</span>'
                  + '<span class="dc-guild-meta">' + matchedChans.length + ' canais'
                  + (grp.peopleTotal > 0 ? '<span class="dc-guild-active-pill">● ' + grp.peopleTotal + '</span>' : '')
                  + '</span></button>';

                if (isExp) {
                  accHtml += '<div class="dc-guild-body">';
                  for (var mci = 0; mci < matchedChans.length; mci++) {
                    var mc = matchedChans[mci];
                    var isCurRow = !!(s.channelId && mc.id === s.channelId);
                    var pList = mc.people || [];
                    var pNames = [];
                    var maxShownNames = 4;
                    for (var pn = 0; pn < pList.length && pn < maxShownNames; pn++) {
                      pNames.push(pList[pn].name);
                    }
                    var extraP = pList.length > maxShownNames ? (' +' + (pList.length - maxShownNames)) : '';
                    accHtml += '<div class="dc-chan-row clearfix' + (isCurRow ? ' dc-chan-cur' : '') + '">'
                      + '<div class="dc-chan-left">'
                      + '<div class="dc-chan-name" title="' + escHtml(mc.name || '') + '"># ' + escHtml(mc.name || 'Canal')
                      + (isCurRow ? ' <span class="pill-tag pill-busy">ATUAL</span>' : '') + '</div>'
                      + (pNames.length ? '<div class="dc-chan-people">' + pList.length + ' na sala: ' + escHtml(pNames.join(', ') + extraP) + '</div>' : '')
                      + '</div>'
                      + '<div class="dc-chan-right">'
                      + '<button type="button" class="mini-btn dc-join-btn' + (isCurRow ? ' active' : '') + '" data-dc-join="' + escHtml(mc.id) + '">' + (isCurRow ? 'Na sala' : 'Entrar') + '</button>'
                      + '</div></div>';
                  }
                  accHtml += '</div>';
                }
                accHtml += '</div>';
              }

              chansBox.innerHTML = totalMatched > 0
                ? accHtml
                : '<span class="muted">Nenhum canal encontrado para o filtro atual.</span>';

              var allBtns = chansBox.getElementsByTagName('button');
              for (var ab = 0; ab < allBtns.length; ab++) {
                var togKey = allBtns[ab].getAttribute('data-dc-toggle');
                var joinId = allBtns[ab].getAttribute('data-dc-join');
                if (togKey) {
                  allBtns[ab].onclick = function () {
                    var k = this.getAttribute('data-dc-toggle');
                    var curOpen = this.firstChild && this.firstChild.textContent === '▾';
                    state.dcExpanded[k] = !curOpen;
                    renderDiscord(state.dcLastData, true);
                  };
                } else if (joinId) {
                  allBtns[ab].onclick = function () {
                    var cid = this.getAttribute('data-dc-join');
                    if (cid) sendDiscordAction('join?channel=' + encodeURIComponent(cid));
                  };
                }
              }
            }
          }
          updateDiscordScrollBounds();
        }
      }

      function sendDiscordAction(act) {
        apiRequest('POST', '/api/discord/' + act, null, function () {
          showToast('Discord atualizado', 'ok');
          setTimeout(pollDiscord, 200);
        }, function (code, errMsg) {
          showToast(errMsg || 'Ative o controle do Discord nas configuracoes', 'error');
        });
      }

      function pollDiscord() {
        apiRequest('GET', '/api/discord', 'discord', function (d) {
          renderDiscord(d);
        });
      }

      // ==================== POLLING LOOPS (UNIFIED SINGLE-XHR SYNC) ====================
      function setOnline(ok) {
        var dot = $('conn-dot');
        var bar = $('offline-bar');
        if (ok) {
          state.failCount = 0;
          if (dot) dot.className = 'dot dot-ok';
          if (bar) addClass(bar, 'hidden');
        } else {
          state.failCount++;
          if (dot) dot.className = 'dot dot-err';
          if (bar && state.failCount >= 2) removeClass(bar, 'hidden');
        }
      }

      // ==================== FULL CONFIG, THEME & HACK 2.0 CYBERDECK ENGINE (PURE ES5) ====================
      var localTheme = 'default';
      var activeEffectiveTheme = 'default';
      var effectsEnabled = true;
      var lastThemeDisplay = null;
      var pendingHackBootOnClose = false;
      var pendingAnimeEntranceOnClose = false;
      var coldHackBootPlayed = false;

      // Fast xorshift32 PRNG (zero allocation)
      var hackRng = 0x5A71C3B9;
      function hackRand() {
        var x = hackRng | 0;
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        hackRng = x;
        return x < 0 ? -x : x;
      }

      function normalizeThemeId(id) {
        var k = String(id || 'default').toLowerCase();
        if (k === 'anime') return 'sakura';
        return k;
      }

      function isLivingWorldId(id) {
        var k = normalizeThemeId(id);
        return k === 'sakura' || k === 'lofi' || k === 'starfall' || k === 'miku';
      }

      function isCustomLocalThemeId(id) {
        var k = normalizeThemeId(id);
        return k === 'hack' || isLivingWorldId(k);
      }

      function isHackThemeActive() {
        return activeEffectiveTheme === 'hack';
      }

      function isAnimeThemeActive() {
        return isLivingWorldId(activeEffectiveTheme);
      }

      function activeLivingWorld() {
        var k = normalizeThemeId(activeEffectiveTheme);
        return isLivingWorldId(k) ? k : 'sakura';
      }

      // ---- 1. FULL-SCREEN MATRIX DIGITAL RAIN (3 depth tiers + radar pulse + glitch surge) ----
      var MATRIX_GLYPHS = '010101010123456789ABCDEFh4ck3r$#@%&*+=<>/?[]{}:;_|~^01X7Z9Q4W3E5R7T8Y0';
      var matrixTimer = null;
      var matrixCols = [];
      var matrixW = 0, matrixH = 0, matrixCellW = 24, matrixCellH = 16;
      var matrixTick = 0;
      var matrixRadarR = 0;
      var matrixSurgeUntil = 0;

      function initMatrixColumns(w, h) {
        matrixW = w;
        matrixH = h;
        var count = Math.max(10, Math.min(44, Math.round(w / 25)));
        matrixCellW = w / count;
        matrixCellH = 16;
        var rows = Math.max(12, Math.round(h / matrixCellH));
        matrixCols = [];
        for (var i = 0; i < count; i++) {
          var r = hackRand() % 65536;
          var tier = (r % 10 < 4) ? 0 : ((r % 10 < 8) ? 1 : 2);
          matrixCols.push({
            y: (hackRand() % (rows + 14)) - 8,
            speed: tier === 0 ? (0.22 + (r % 32) / 110) : (tier === 1 ? (0.42 + (r % 32) / 70) : (0.68 + (r % 32) / 55)),
            trail: tier === 0 ? (7 + r % 7) : (tier === 1 ? (9 + r % 8) : (11 + r % 9)),
            tier: tier,
            seed: hackRand()
          });
        }
      }

      function drawMatrixRainFrame(animateStep) {
        var canvas = $('hack-matrix-bg');
        if (!canvas || !canvas.getContext) return;
        var appEl = $('app');
        var w = (appEl && appEl.clientWidth) || window.innerWidth || 1024;
        var h = (appEl && appEl.clientHeight) || window.innerHeight || 552;
        if (w < 40 || h < 40) return;
        if (canvas.width !== w || canvas.height !== h || !matrixCols.length) {
          canvas.width = w;
          canvas.height = h;
          initMatrixColumns(w, h);
        }

        var ctx = canvas.getContext('2d');
        var now = new Date().getTime();
        var surge = now < matrixSurgeUntil;

        if (animateStep) {
          matrixTick++;
          var maxRows = (h / matrixCellH) + 2;
          var mul = surge ? 1.75 : 1.0;
          for (var i = 0; i < matrixCols.length; i++) {
            var c = matrixCols[i];
            c.y += c.speed * mul;
            if ((matrixTick + i) % (surge ? 2 : 5) === 0) {
              c.seed = (c.seed * 1103515245 + 12345) & 0x7fffffff;
            }
            if (c.y - c.trail > maxRows) {
              var rr = hackRand() % 65536;
              c.tier = (rr % 10 < 4) ? 0 : ((rr % 10 < 8) ? 1 : 2);
              c.speed = c.tier === 0 ? (0.22 + (rr % 32) / 110) : (c.tier === 1 ? (0.42 + (rr % 32) / 70) : (0.68 + (rr % 32) / 55));
              c.trail = c.tier === 0 ? (7 + rr % 7) : (c.tier === 1 ? (9 + rr % 8) : (11 + rr % 9));
              c.y = -(2 + (rr % 10));
              c.seed = hackRand();
            }
          }
          var maxR = Math.max(w, h) * 1.15;
          matrixRadarR += surge ? 9 : 3.2;
          if (matrixRadarR > maxR) matrixRadarR = -180;
        }

        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, w, h);

        // Subtle cyberdeck coordinate grid (+)
        ctx.strokeStyle = 'rgba(0, 255, 102, 0.12)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        for (var gx = 32; gx < w; gx += 64) {
          for (var gy = 32; gy < h; gy += 64) {
            ctx.moveTo(gx - 3, gy); ctx.lineTo(gx + 3, gy);
            ctx.moveTo(gx, gy - 3); ctx.lineTo(gx, gy + 3);
          }
        }
        ctx.stroke();

        // Ambient radar sweep ring
        if (animateStep && matrixRadarR > 10) {
          var maxRad = Math.max(w, h) * 1.15;
          var rAlpha = Math.max(0, (1 - Math.min(1, matrixRadarR / maxRad)) * 0.14);
          if (rAlpha > 0.01) {
            ctx.strokeStyle = 'rgba(0, 255, 102, ' + rAlpha.toFixed(3) + ')';
            ctx.lineWidth = 1.2;
            ctx.beginPath();
            ctx.arc(w * 0.85, h * 0.85, matrixRadarR, 0, Math.PI * 2, false);
            ctx.stroke();
          }
        }

        ctx.font = '13px Consolas, "Courier New", monospace';
        ctx.textAlign = 'center';
        var gLen = MATRIX_GLYPHS.length;
        for (var col = 0; col < matrixCols.length; col++) {
          var mc = matrixCols[col];
          var cx = (col + 0.5) * matrixCellW;
          var headRow = Math.floor(mc.y);
          var cyanCol = surge && ((col + matrixTick) % 5 === 0);
          var redCol = surge && !cyanCol && ((col * 3 + matrixTick) % 11 === 0);

          for (var t = 0; t < mc.trail; t++) {
            var row = headRow - t;
            var py = row * matrixCellH;
            if (py < -matrixCellH || py > h + matrixCellH) continue;
            var a;
            if (t === 0) {
              if (redCol) ctx.fillStyle = 'rgba(255, 85, 119, 0.92)';
              else if (cyanCol) ctx.fillStyle = 'rgba(184, 255, 255, 0.92)';
              else if (mc.tier === 2) ctx.fillStyle = 'rgba(238, 255, 245, 0.90)';
              else if (mc.tier === 1) ctx.fillStyle = 'rgba(153, 255, 204, 0.65)';
              else ctx.fillStyle = 'rgba(51, 187, 102, 0.36)';
            } else {
              var frac = 1 - (t / mc.trail);
              var maxA = mc.tier === 2 ? 0.62 : (mc.tier === 1 ? 0.42 : 0.22);
              if (!animateStep) maxA *= 0.45;
              a = Math.max(0.03, frac * frac * maxA);
              if (redCol) ctx.fillStyle = 'rgba(255, 42, 85, ' + a.toFixed(2) + ')';
              else if (cyanCol) ctx.fillStyle = 'rgba(0, 240, 255, ' + a.toFixed(2) + ')';
              else ctx.fillStyle = (t <= 2 ? 'rgba(0, 255, 102, ' : 'rgba(0, 187, 77, ') + a.toFixed(2) + ')';
            }
            var gi = Math.abs(mc.seed + row * 17 + col * 31 + (t === 0 ? matrixTick : 0)) % gLen;
            ctx.fillText(MATRIX_GLYPHS.charAt(gi), cx, py);
          }
        }
      }

      // ---- 2. FULL-SCREEN CRT & VISUAL BUG GLITCH OVERLAY ----
      var GLITCH_FLASH_LOGS = [
        '>> SYS_GLITCH :: 0x4F',
        '[PKT_INJ] R00T_SYNC',
        '> CRT_D3SYNC // 60Hz',
        '[0V3RR1D3] M3M_7F',
        '>> H4CK3D_51GN4L',
        '[!] BYP455_OK'
      ];
      var glitchTimer = null;
      var glitchBeamY = -50;
      var glitchBurstEnd = 0;
      var glitchNextAuto = 0;
      var glitchFrame = 0;
      var glitchSlices = [];
      var glitchBlocks = [];
      var glitchLogTxt = '';
      var glitchLogX = 16;
      var glitchLogY = 80;
      var tabWipeActive = false;
      var tabWipeY = 0;

      function clearUiJitter() {
        var vp = $('viewport');
        if (vp) {
          removeClass(vp, 'hack-jitter-l');
          removeClass(vp, 'hack-jitter-r');
        }
      }

      function regenGlitchGeometry(w, h) {
        glitchSlices = [];
        var sc = 2 + (hackRand() % 5);
        for (var i = 0; i < sc; i++) {
          glitchSlices.push({
            y: hackRand() % Math.max(1, h),
            h: 2 + (hackRand() % 7),
            dx: ((hackRand() & 1) === 0 ? 1 : -1) * (5 + (hackRand() % 14))
          });
        }
        glitchBlocks = [];
        var bc = 3 + (hackRand() % 6);
        var cols = ['rgba(0,255,102,0.40)', 'rgba(0,240,255,0.40)', 'rgba(255,42,85,0.38)'];
        for (var j = 0; j < bc; j++) {
          var edge = (j % 2 === 0);
          var bx = edge
            ? (((hackRand() & 1) === 0) ? (hackRand() % Math.max(1, Math.floor(w / 5))) : (w - (hackRand() % Math.max(1, Math.floor(w / 5))) - 24))
            : (hackRand() % Math.max(1, w));
          glitchBlocks.push({
            x: bx,
            y: hackRand() % Math.max(1, h),
            w: 8 + (hackRand() % 28),
            h: 2 + (hackRand() % 7),
            c: cols[hackRand() % 3]
          });
        }
      }

      function startGlitchBurst(intensity, durationMs) {
        var now = new Date().getTime();
        if (now + durationMs > glitchBurstEnd) glitchBurstEnd = now + durationMs;
        glitchFrame = 0;
        var canvas = $('hack-glitch-fx');
        var w = (canvas && canvas.width) || 1024;
        var h = (canvas && canvas.height) || 552;
        regenGlitchGeometry(w, h);
        if (intensity >= 1 && (hackRand() % 10 < 7)) {
          glitchLogTxt = GLITCH_FLASH_LOGS[hackRand() % GLITCH_FLASH_LOGS.length];
          glitchLogX = (hackRand() & 1) === 0 ? Math.max(14, w - 175) : 16;
          glitchLogY = 52 + (hackRand() % Math.max(1, Math.floor(h * 0.65)));
        }
        matrixSurgeUntil = Math.max(matrixSurgeUntil, now + durationMs + 240);
        spawnHackTextGlitchWave(intensity >= 2 ? 6 : 4);
      }

      function triggerHackTabGlitch() {
        if (!isHackThemeActive() || !effectsEnabled) return;
        tabWipeActive = true;
        tabWipeY = 0;
        startGlitchBurst(1, 200);
      }

      function drawGlitchOverlayFrame(animateStep) {
        var canvas = $('hack-glitch-fx');
        if (!canvas || !canvas.getContext) return;
        var appEl = $('app');
        var w = (appEl && appEl.clientWidth) || window.innerWidth || 1024;
        var h = (appEl && appEl.clientHeight) || window.innerHeight || 552;
        if (w < 40 || h < 40) return;
        if (canvas.width !== w || canvas.height !== h) {
          canvas.width = w;
          canvas.height = h;
        }
        var ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, w, h);

        var now = new Date().getTime();
        if (animateStep) {
          glitchBeamY += 4.5;
          if (glitchBeamY > h + 90) glitchBeamY = -90;
          if (tabWipeActive) {
            tabWipeY += Math.max(28, h * 0.20);
            if (tabWipeY > h + 24) tabWipeActive = false;
          }
          if (now >= glitchNextAuto && now > glitchBurstEnd) {
            startGlitchBurst(1, 220 + (hackRand() % 180));
            glitchNextAuto = now + 3400 + (hackRand() % 3800);
          }
          if (now < glitchBurstEnd) {
            glitchFrame++;
            regenGlitchGeometry(w, h);
            var vp = $('viewport');
            if (vp) {
              removeClass(vp, 'hack-jitter-l');
              removeClass(vp, 'hack-jitter-r');
              if (glitchFrame % 3 === 1) {
                addClass(vp, (hackRand() & 1) === 0 ? 'hack-jitter-l' : 'hack-jitter-r');
              }
            }
          } else if (glitchSlices.length || glitchBlocks.length || glitchLogTxt) {
            glitchSlices = [];
            glitchBlocks = [];
            glitchLogTxt = '';
            clearUiJitter();
          }
        }

        // Subtle CRT scanlines
        ctx.fillStyle = 'rgba(0, 255, 102, 0.04)';
        for (var sy = 0; sy < h; sy += 6) {
          ctx.fillRect(0, sy, w, 1);
        }

        // Ambient sweeping refresh beam
        if (animateStep && glitchBeamY > -70 && glitchBeamY < h + 70) {
          ctx.fillStyle = 'rgba(0, 255, 102, 0.05)';
          ctx.fillRect(0, glitchBeamY - 26, w, 52);
          ctx.fillStyle = 'rgba(0, 255, 102, 0.16)';
          ctx.fillRect(0, glitchBeamY - 1, w, 3);
        }

        // Tab-switch laser wipe
        if (tabWipeActive) {
          ctx.fillStyle = 'rgba(0, 240, 255, 0.34)';
          ctx.fillRect(0, tabWipeY - 12, w, 14);
          ctx.fillStyle = 'rgba(0, 255, 102, 0.82)';
          ctx.fillRect(0, tabWipeY, w, 2.5);
        }

        // RGB chromatic slice tears + corruption blocks + micro flash log
        for (var i = 0; i < glitchSlices.length; i++) {
          var sl = glitchSlices[i];
          ctx.fillStyle = 'rgba(0, 240, 255, 0.30)';
          ctx.fillRect(Math.max(0, sl.dx), sl.y, w, sl.h);
          ctx.fillStyle = 'rgba(255, 42, 85, 0.28)';
          ctx.fillRect(Math.max(0, -sl.dx), sl.y + 1, w, sl.h);
          if (i % 2 === 0) {
            ctx.fillStyle = 'rgba(0, 255, 102, 0.35)';
            ctx.fillRect(0, sl.y, w, Math.max(1, sl.h * 0.45));
          }
        }
        for (var j = 0; j < glitchBlocks.length; j++) {
          var b = glitchBlocks[j];
          ctx.fillStyle = b.c;
          ctx.fillRect(b.x, b.y, b.w, b.h);
        }
        if (glitchLogTxt) {
          ctx.font = 'bold 11px Consolas, monospace';
          var tw = ctx.measureText(glitchLogTxt).width + 12;
          ctx.fillStyle = 'rgba(2, 13, 7, 0.88)';
          ctx.fillRect(glitchLogX, glitchLogY - 14, tw, 18);
          ctx.fillStyle = '#ff2a55';
          ctx.fillRect(glitchLogX, glitchLogY - 14, 3, 18);
          ctx.fillStyle = '#00f0ff';
          ctx.fillText(glitchLogTxt, glitchLogX + 7, glitchLogY);
        }

        // Crisp cyberdeck viewport corner reticles
        ctx.strokeStyle = 'rgba(0, 255, 102, 0.62)';
        ctx.lineWidth = 1.5;
        var m = 3, cl = 12;
        ctx.beginPath();
        ctx.moveTo(m, m + cl); ctx.lineTo(m, m); ctx.lineTo(m + cl, m);
        ctx.moveTo(w - m - cl, m); ctx.lineTo(w - m, m); ctx.lineTo(w - m, m + cl);
        ctx.moveTo(m, h - m - cl); ctx.lineTo(m, h - m); ctx.lineTo(m + cl, h - m);
        ctx.moveTo(w - m - cl, h - m); ctx.lineTo(w - m, h - m); ctx.lineTo(w - m, h - m - cl);
        ctx.stroke();
      }

      // ---- 3. TOPBAR LIVE TYPEWRITER TERMINAL ----
      var termFeed = [
        '> h4ck3d_0s // R00T',
        '> m4tr1x_l1nk [0K]',
        '> cpu_l04d: --%',
        '> sy5_gl1tch :: 0x71',
        '> n3t_p1ng: --ms',
        '> r00t@hp71:~# _'
      ];
      var termTimer = null;
      var termMsgIdx = 0, termTyped = 0, termHold = 0, termTick = 0;

      function updateHackTerminalTelemetry(d) {
        if (!d) return;
        var pc = d.pc || {};
        var cpuRaw = typeof pc.cpu === 'number' ? pc.cpu : pc.cpuPct;
        var cpu = typeof cpuRaw === 'number' ? Math.round(cpuRaw) : 0;
        var ping = (d.ping && typeof d.ping.ms === 'number') ? Math.round(d.ping.ms) : -1;
        termFeed[1] = '> m4tr1x_l1nk [0K]';
        termFeed[2] = '> cpu_l04d: ' + cpu + '%';
        termFeed[4] = ping >= 0 ? ('> n3t_p1ng: ' + ping + 'ms') : '> n3t_p1ng: --ms';
      }

      function drawHackTopTerminal(animateStep) {
        var canvas = $('hack-tb-term');
        if (!canvas || !canvas.getContext) return;
        var w = canvas.clientWidth || 166;
        var h = canvas.clientHeight || 26;
        if (w < 30 || h < 14) return;
        if (canvas.width !== w) canvas.width = w;
        if (canvas.height !== h) canvas.height = h;
        var ctx = canvas.getContext('2d');

        if (animateStep) {
          termTick++;
          var cur = termFeed[termMsgIdx];
          if (termTyped < cur.length) {
            termTyped++;
            termHold = 0;
          } else {
            termHold++;
            if (termHold >= 24) {
              termMsgIdx = (termMsgIdx + 1) % termFeed.length;
              termTyped = 0;
              termHold = 0;
            }
          }
        }
        var line = termFeed[termMsgIdx];
        var show = animateStep ? Math.min(line.length, termTyped) : line.length;
        var sub = line.substring(0, show);
        var glitchFlash = animateStep && (termTick % 37 === 0 || termTick % 37 === 1);

        ctx.fillStyle = 'rgba(2, 14, 7, 0.92)';
        ctx.fillRect(0, 0, w, h);
        ctx.strokeStyle = glitchFlash ? '#00f0ff' : '#157a42';
        ctx.lineWidth = 1;
        ctx.strokeRect(0.5, 0.5, w - 1, h - 1);

        // Pulsing status LED
        var pulseOn = !animateStep || (Math.floor(termTick / 5) % 2 === 0);
        ctx.fillStyle = glitchFlash ? '#ff2a55' : (pulseOn ? '#00ff66' : '#0c522b');
        ctx.beginPath();
        ctx.arc(7, h * 0.5, 2.5, 0, Math.PI * 2, false);
        ctx.fill();

        ctx.save();
        ctx.beginPath();
        ctx.rect(13, 1, w - 16, h - 2);
        ctx.clip();
        ctx.font = 'bold 11px Consolas, "Courier New", monospace';
        ctx.textAlign = 'left';
        var tx = 14, ty = Math.round(h * 0.5 + 4);
        if (glitchFlash) {
          ctx.fillStyle = 'rgba(0, 240, 255, 0.85)';
          ctx.fillText(sub, tx + 2, ty);
          ctx.fillStyle = '#00ff66';
          ctx.fillText(sub, tx - 1, ty);
        } else {
          ctx.fillStyle = '#00ff66';
          ctx.fillText(sub, tx, ty);
        }
        if (!animateStep || (Math.floor(termTick / 4) % 2 === 0)) {
          var tw = ctx.measureText(sub).width;
          if (tx + tw + 6 < w) {
            ctx.fillStyle = glitchFlash ? '#00f0ff' : '#00ff66';
            ctx.fillRect(tx + tw + 2, ty - 9, 5, 11);
          }
        }
        ctx.restore();
      }

      // ---- 4. INTERACTIVE TYPEWRITER BREACH BOOT OVERLAY ----
      var BOOT_LINES = [
        '> [SYS_BREACH] BOOTING ZERO-DAY TERMINAL v7.1',
        '> h4ck3d_m0d3 :: PH0N3B0X_HP71 [R00T_OK]',
        '> loading m4tr1x_r41n + crt_gl1tch_c0r3...',
        '> overriding display_hud :: 1024x600 // WEB',
        '> @system.root :: ACCESS GRANTED'
      ];
      var BOOT_HEX = [
        '0x7F00: 4A 9C E1 00',
        '0x7F10: FF 3B 88 12',
        '0x7F20: 00 FF 66 A9',
        '0x7F30: C0 DE 13 37',
        '0x7F40: 91 0F 44 D2',
        '0x7F50: 71 HP 42 20'
      ];
      var bootTimer = null;
      var bootStartMs = 0;
      var BOOT_TOTAL_MS = 2450;

      function stopHackBoot() {
        if (bootTimer) { clearInterval(bootTimer); bootTimer = null; }
        var box = $('hack-boot');
        if (box) addClass(box, 'hidden');
      }

      function playHackBoot() {
        var box = $('hack-boot');
        var canvas = $('hack-boot-canvas');
        if (!box || !canvas || !canvas.getContext || !isHackThemeActive()) return;
        stopHackBoot();
        removeClass(box, 'hidden');
        bootStartMs = new Date().getTime();

        var renderBoot = function () {
          var elapsed = new Date().getTime() - bootStartMs;
          if (elapsed >= BOOT_TOTAL_MS) {
            stopHackBoot();
            startGlitchBurst(2, 340);
            return;
          }
          var w = box.clientWidth || window.innerWidth || 1024;
          var h = box.clientHeight || window.innerHeight || 552;
          if (canvas.width !== w || canvas.height !== h) {
            canvas.width = w;
            canvas.height = h;
          }
          var ctx = canvas.getContext('2d');
          ctx.clearRect(0, 0, w, h);

          var bgA = 0.96;
          if (elapsed > BOOT_TOTAL_MS - 380) {
            bgA = Math.max(0, 0.96 * (1 - (elapsed - (BOOT_TOTAL_MS - 380)) / 380));
          }
          ctx.fillStyle = 'rgba(0, 0, 0, ' + bgA.toFixed(2) + ')';
          ctx.fillRect(0, 0, w, h);

          // CRT scanlines
          ctx.fillStyle = 'rgba(0, 255, 102, 0.08)';
          for (var sy = 0; sy < h; sy += 5) ctx.fillRect(0, sy, w, 1);

          // Phase 1: CRT Power-on beam
          if (elapsed < 200) {
            var p = elapsed / 200;
            var cy = h * 0.5;
            var hh = Math.max(2, (h * 0.48) * p * p);
            ctx.fillStyle = 'rgba(0, 255, 102, 0.80)';
            ctx.fillRect(0, cy - hh, w, 3);
            ctx.fillRect(0, cy + hh - 3, w, 3);
            ctx.fillStyle = '#00f0ff';
            ctx.fillRect(0, cy - 2, w, 4);
            return;
          }

          var portrait = h > w;
          var padX = Math.max(22, Math.round(w * 0.08));
          var startY = Math.max(76, Math.round(h * 0.24));
          var stepY = 28;
          var fontSz = portrait ? 13 : 16;
          ctx.font = 'bold ' + fontSz + 'px Consolas, "Courier New", monospace';
          ctx.textAlign = 'left';

          var remaining = Math.floor(((elapsed - 200) * 98) / 1000);
          var curX = padX, curY = startY;
          for (var i = 0; i < BOOT_LINES.length; i++) {
            if (remaining <= 0) break;
            var ln = BOOT_LINES[i];
            var show = Math.min(ln.length, remaining);
            remaining -= ln.length + 5;
            var sub = ln.substring(0, show);
            var ly = startY + i * stepY;
            var isFinal = (i === BOOT_LINES.length - 1);
            if (isFinal && show === ln.length) {
              ctx.fillStyle = 'rgba(255, 42, 85, 0.65)';
              ctx.fillText(sub, padX - 2, ly);
              ctx.fillStyle = 'rgba(0, 255, 102, 0.75)';
              ctx.fillText(sub, padX + 2, ly);
            }
            ctx.fillStyle = isFinal ? '#00f0ff' : '#00ff66';
            ctx.fillText(sub, padX, ly);
            curX = padX + ctx.measureText(sub).width + 4;
            curY = ly;
          }

          // Blinking cursor block
          if (Math.floor(elapsed / 180) % 2 === 0) {
            ctx.fillStyle = '#00ff66';
            ctx.fillRect(curX, curY - fontSz + 2, 9, fontSz + 2);
          }

          // Segmented progress bar
          var barY = startY + (BOOT_LINES.length + 0.6) * stepY;
          var barW = Math.min(w - padX * 2, 440);
          var barH = 14;
          ctx.strokeStyle = '#1a6b3e';
          ctx.lineWidth = 1.5;
          ctx.strokeRect(padX, barY, barW, barH);

          var frac = Math.min(1, Math.max(0, (elapsed - 240) / 1650));
          var segs = 24;
          var lit = Math.round(segs * frac);
          var segW = (barW - 6) / segs;
          for (var s = 0; s < lit; s++) {
            ctx.fillStyle = (s === lit - 1) ? '#00f0ff' : '#00ff66';
            ctx.fillRect(padX + 3 + s * segW, barY + 3, Math.max(2, segW - 2.5), barH - 6);
          }
          ctx.font = '11px Consolas, monospace';
          ctx.fillStyle = '#2e8f57';
          ctx.fillText('SYNC ' + Math.round(frac * 100) + '%  [TOQUE OU CLIQUE PARA PULAR]', padX, barY + barH + 20);

          if (!portrait && w > 680) {
            var hx = w - 175;
            var shift = Math.floor(elapsed / 95);
            for (var k = 0; k < BOOT_HEX.length; k++) {
              ctx.fillText(BOOT_HEX[(k + shift) % BOOT_HEX.length], hx, startY + k * 20);
            }
          }

          if (elapsed > BOOT_TOTAL_MS - 380) {
            var ff = Math.floor(elapsed / 35);
            for (var gs = 0; gs < 5; gs++) {
              var gsy = (ff * 97 + gs * 131) % Math.max(1, h);
              ctx.fillStyle = (gs % 2 === 0) ? 'rgba(255, 42, 85, 0.55)' : 'rgba(0, 240, 255, 0.55)';
              ctx.fillRect(0, gsy, w, 4 + gs * 3);
            }
          }
        };
        renderBoot();
        bootTimer = setInterval(renderBoot, 38);
      }

      // ---- 5. GLOBAL LEGIBLE "h4ck3r" TEXT GLITCH ENGINE ----
      var LEET_MAP = {
        'A': '4', 'a': '4', 'Á': '4', 'á': '4', 'À': '4', 'à': '4', 'Ã': '4', 'ã': '4', 'Â': '4', 'â': '4',
        'E': '3', 'e': '3', 'É': '3', 'é': '3', 'Ê': '3', 'ê': '3',
        'I': '1', 'i': '1', 'Í': '1', 'í': '1',
        'O': '0', 'o': '0', 'Ó': '0', 'ó': '0', 'Õ': '0', 'õ': '0', 'Ô': '0', 'ô': '0',
        'S': '5', 's': '5', 'T': '7', 't': '7', 'B': '8', 'b': '8', 'G': '6', 'g': '6', 'Z': '2', 'z': '2'
      };
      var activeTextGlitches = [];
      var textGlitchTimer = null;
      var textNextWaveAt = 0;

      function isUnitCtx(str, idx) {
        if (idx > 0) {
          var prev = str.charAt(idx - 1);
          if (prev === '°' || (prev >= '0' && prev <= '9')) return true;
          if (prev === ' ' && idx >= 2) {
            var p2 = str.charAt(idx - 2);
            if (p2 >= '0' && p2 <= '9') {
              var end = idx;
              while (end < str.length && /[A-Za-z]/.test(str.charAt(end))) end++;
              if (end - idx <= 2) return true;
            }
          }
        }
        return false;
      }

      function mutateHackerText(str, surgeMode, seed) {
        var out = '';
        var changed = 0;
        for (var i = 0; i < str.length; i++) {
          var ch = str.charAt(i);
          var rep = ch;
          var leet = LEET_MAP[ch];
          if (leet && !isUnitCtx(str, i)) {
            var h = Math.abs((seed * 31 + i * 17) ^ (ch.charCodeAt(0) * 13));
            if (surgeMode) {
              if (h % 10 < 8) {
                if ((ch === 'A' || ch === 'a' || ch === 'Á' || ch === 'á' || ch === 'Ã' || ch === 'ã') && (h & 1) === 0) rep = '@';
                else if ((ch === 'S' || ch === 's') && (h & 1) === 0) rep = '$';
                else if ((ch === 'E' || ch === 'e' || ch === 'É' || ch === 'é') && (h % 3 === 0)) rep = '#';
                else if ((ch === 'I' || ch === 'i' || ch === 'Í' || ch === 'í') && (h & 1) === 0) rep = '!';
                else if ((ch === 'T' || ch === 't') && (h & 1) === 0) rep = '+';
                else rep = leet;
              }
            } else if (h % 10 < 7) {
              rep = leet;
            }
          }
          if (rep !== ch) changed++;
          out += rep;
        }
        if (changed === 0) {
          var forced = '';
          for (var k = 0; k < str.length; k++) {
            var c2 = str.charAt(k);
            if (changed < 2 && LEET_MAP[c2] && !isUnitCtx(str, k)) {
              forced += LEET_MAP[c2];
              changed++;
            } else {
              forced += c2;
            }
          }
          return forced;
        }
        return out;
      }

      function collectGlitchableNodes() {
        var scopes = [$('topbar'), $('navbar')];
        var vp = $('viewport');
        if (vp) {
          var secs = vp.getElementsByTagName('section');
          for (var s = 0; s < secs.length; s++) {
            if ((' ' + secs[s].className + ' ').indexOf(' active ') !== -1) {
              scopes.push(secs[s]);
              break;
            }
          }
        }
        var out = [];
        var skipTags = { INPUT: 1, TEXTAREA: 1, SELECT: 1, OPTION: 1, SCRIPT: 1, STYLE: 1, CANVAS: 1, SVG: 1, PATH: 1 };
        for (var i = 0; i < scopes.length; i++) {
          var root = scopes[i];
          if (!root) continue;
          var all = root.getElementsByTagName('*');
          for (var j = 0; j < all.length; j++) {
            var el = all[j];
            if (skipTags[el.tagName]) continue;
            if (el.children && el.children.length > 0) continue; // leaf text elements only
            if (el.offsetParent === null) continue;
            var txt = el.textContent || '';
            if (txt.length < 3 || txt.length > 48) continue;
            var cands = 0;
            for (var c = 0; c < txt.length; c++) {
              if (LEET_MAP[txt.charAt(c)] && !isUnitCtx(txt, c)) {
                cands++;
                if (cands >= 2) break;
              }
            }
            if (cands >= 2) out.push(el);
          }
        }
        return out;
      }

      function restoreTextGlitchEntry(item) {
        if (!item || !item.el) return;
        removeClass(item.el, 'hack-glitch-surge');
        removeClass(item.el, 'hack-glitch-leet');
        // Only restore if live telemetry polling didn't already replace the text with newer data
        if (item.el.textContent === item.lastWritten) {
          item.el.textContent = item.orig;
        }
      }

      function clearAllTextGlitches() {
        for (var i = 0; i < activeTextGlitches.length; i++) {
          restoreTextGlitchEntry(activeTextGlitches[i]);
        }
        activeTextGlitches = [];
      }

      function isElementCurrentlyGlitched(el) {
        for (var i = 0; i < activeTextGlitches.length; i++) {
          if (activeTextGlitches[i].el === el) return true;
        }
        return false;
      }

      function spawnHackTextGlitchWave(desiredCount) {
        if (!isHackThemeActive() || !effectsEnabled) return;
        var list = collectGlitchableNodes();
        if (!list.length) return;
        var now = new Date().getTime();
        var spawned = 0;
        var maxTry = Math.min(list.length * 2, desiredCount * 4);
        for (var a = 0; a < maxTry && spawned < desiredCount && activeTextGlitches.length < 7; a++) {
          var el = list[hackRand() % list.length];
          if (isElementCurrentlyGlitched(el)) continue;
          var orig = el.textContent || '';
          var seed = hackRand() % 32768;
          var surgeTxt = mutateHackerText(orig, true, seed);
          el.textContent = surgeTxt;
          addClass(el, 'hack-glitch-surge');
          activeTextGlitches.push({
            el: el,
            orig: orig,
            lastWritten: surgeTxt,
            phase: 0,
            seed: seed,
            nextAt: now + 105
          });
          spawned++;
        }
      }

      function stepHackTextGlitches() {
        if (!isHackThemeActive() || !effectsEnabled) return;
        var now = new Date().getTime();
        for (var i = activeTextGlitches.length - 1; i >= 0; i--) {
          var item = activeTextGlitches[i];
          if (!item.el || item.el.offsetParent === null || item.el.textContent !== item.lastWritten) {
            if (item.el) {
              removeClass(item.el, 'hack-glitch-surge');
              removeClass(item.el, 'hack-glitch-leet');
            }
            activeTextGlitches.splice(i, 1);
            continue;
          }
          if (now < item.nextAt) continue;
          if (item.phase === 0) {
            item.phase = 1;
            item.nextAt = now + 950 + (hackRand() % 650);
            removeClass(item.el, 'hack-glitch-surge');
            addClass(item.el, 'hack-glitch-leet');
            var leetTxt = mutateHackerText(item.orig, false, item.seed);
            item.el.textContent = leetTxt;
            item.lastWritten = leetTxt;
          } else if (item.phase === 1) {
            item.phase = 2;
            item.nextAt = now + 95;
            removeClass(item.el, 'hack-glitch-leet');
            addClass(item.el, 'hack-glitch-surge');
            var outTxt = mutateHackerText(item.orig, true, item.seed + 7);
            item.el.textContent = outTxt;
            item.lastWritten = outTxt;
          } else {
            restoreTextGlitchEntry(item);
            activeTextGlitches.splice(i, 1);
          }
        }
        if (now >= textNextWaveAt) {
          spawnHackTextGlitchWave(2 + (hackRand() % 3));
          textNextWaveAt = now + 1700 + (hackRand() % 1450);
        }
      }

      // ==================== LIVING WORLDS ENGINE (SAKURA / LOFI / STARFALL / MIKU — PURE ES5) ====================
      var CONST_PTS = [
        [0.14, 0.20, 0.24, 0.17, 0.21, 0.30, 0.18, 0.31, 0.15, 0.32, 0.26, 0.43, 0.12, 0.44],
        [0.56, 0.15, 0.63, 0.24, 0.70, 0.17, 0.77, 0.26, 0.85, 0.16],
        [0.36, 0.14, 0.42, 0.24, 0.48, 0.35, 0.31, 0.28, 0.53, 0.20]
      ];
      var CONST_EDGES = [
        [0, 1, 1, 2, 2, 3, 3, 4, 4, 0, 2, 5, 4, 6],
        [0, 1, 1, 2, 2, 3, 3, 4],
        [0, 1, 1, 2, 3, 1, 1, 4]
      ];

      var animeSkyTimer = null;
      var animeSparkleTimer = null;
      var animeRibbonTimer = null;
      var animeEntranceTimer = null;
      var animeSkyW = 0, animeSkyH = 0;
      var animeSceneWorld = '';
      var animeTime = 0;
      var animeStars = [];
      var animePetals = [];
      var animeRain = [];
      var animeEq = [];
      var animeEqPeak = [];
      var animeEdgePetals = [];
      var animeMeteor = { active: false, x: 0, y: 0, vx: 0, vy: 0, life: 0, nextAt: 0 };
      var lofiFlash = 0;
      var lofiNextFlash = 0;
      var animeBurstUntil = 0;
      var animeSweepActive = false;
      var animeSweepProg = 0;
      var animeSparkleTime = 0;
      var animeRibbonMsgs = ['SAKURA STORM', 'LINK TOKYO OK', 'CPU --%', 'PING --ms', 'HANAMI HUD'];
      var animeRibbonCpu = 0;
      var animeRibbonPing = -1;
      var animeRibbonTick = 0;
      var animeRibbonIdx = 0;
      var animeRibbonHold = 0;
      var animeEntranceStart = 0;
      var ANIME_ENTRANCE_MS = 2200;
      var mikuSpriteImg = null;
      var mikuSpriteReady = false;

      function ensureMikuSprite() {
        if (mikuSpriteImg) return;
        mikuSpriteImg = new Image();
        mikuSpriteImg.onload = function () { mikuSpriteReady = true; };
        mikuSpriteImg.src = '/legacy/miku_mascot.png';
      }

      function drawCanvasDiamondStar(ctx, cx, cy, r, fillStyle) {
        if (r <= 0.5) return;
        var inner = r * 0.27;
        ctx.fillStyle = fillStyle;
        ctx.beginPath();
        ctx.moveTo(cx, cy - r);
        ctx.lineTo(cx + inner, cy - inner);
        ctx.lineTo(cx + r, cy);
        ctx.lineTo(cx + inner, cy + inner);
        ctx.lineTo(cx, cy + r);
        ctx.lineTo(cx - inner, cy + inner);
        ctx.lineTo(cx - r, cy);
        ctx.lineTo(cx - inner, cy - inner);
        ctx.closePath();
        ctx.fill();
      }

      function drawCanvasSakuraPetal(ctx, cx, cy, s, deg, fillStyle, scaleX) {
        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(deg * Math.PI / 180);
        if (scaleX) ctx.scale(scaleX, 1);
        ctx.fillStyle = fillStyle;
        ctx.beginPath();
        ctx.moveTo(0, -s);
        ctx.quadraticCurveTo(s * 0.85, -s * 0.25, 0, s * 0.9);
        ctx.quadraticCurveTo(-s * 0.85, -s * 0.25, 0, -s);
        ctx.closePath();
        ctx.fill();
        ctx.restore();
      }

      function drawCanvasMusicNote(ctx, cx, cy, s, deg, col) {
        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(deg * Math.PI / 180);
        ctx.fillStyle = col;
        ctx.strokeStyle = col;
        ctx.lineWidth = Math.max(1.2, s * 0.24);
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.arc(-s * 0.15, s * 0.45, s * 0.42, 0, Math.PI * 2, false);
        ctx.fill();
        ctx.beginPath();
        ctx.moveTo(s * 0.22, s * 0.45);
        ctx.lineTo(s * 0.22, -s * 1.15);
        ctx.quadraticCurveTo(s * 1.12, -s * 0.82, s * 0.82, -s * 0.22);
        ctx.stroke();
        ctx.restore();
      }

      function drawRoundedBox(ctx, x, y, w, h, r) {
        var rr = Math.min(r, w * 0.5, h * 0.5);
        ctx.beginPath();
        ctx.moveTo(x + rr, y);
        ctx.lineTo(x + w - rr, y);
        ctx.quadraticCurveTo(x + w, y, x + w, y + rr);
        ctx.lineTo(x + w, y + h - rr);
        ctx.quadraticCurveTo(x + w, y + h, x + w - rr, y + h);
        ctx.lineTo(x + rr, y + h);
        ctx.quadraticCurveTo(x, y + h, x, y + h - rr);
        ctx.lineTo(x, y + rr);
        ctx.quadraticCurveTo(x, y, x + rr, y);
        ctx.closePath();
      }

      function initAnimeSky(w, h, world) {
        animeSkyW = w;
        animeSkyH = h;
        animeSceneWorld = world;
        if (world === 'miku') ensureMikuSprite();

        var starRgb = world === 'lofi'
          ? ['255,241,208', '255,179,138', '159,227,201', '255,136,188']
          : (world === 'starfall'
            ? ['255,255,255', '127,231,255', '109,255,176', '255,224,102', '181,124,255']
            : (world === 'miku'
              ? ['57,197,187', '127,231,255', '255,46,136', '238,255,254']
              : ['255,246,253', '255,213,107', '124,232,255', '255,184,232']));

        animeStars = [];
        var i;
        for (i = 0; i < 42; i++) {
          animeStars.push({
            x: hackRand() % Math.max(1, w),
            y: hackRand() % Math.max(1, Math.floor(h * 0.85)),
            size: 1.0 + (hackRand() % 18) * 0.12,
            phase: (hackRand() % 628) * 0.01,
            rgb: starRgb[i % starRgb.length],
            diamond: (i % 5 === 0)
          });
        }

        var partRgb = world === 'lofi'
          ? ['255,224,102', '255,179,138', '159,227,201', '184,236,255']
          : (world === 'starfall'
            ? ['220,255,120', '109,255,176', '127,231,255', '255,224,102']
            : (world === 'miku'
              ? ['57,197,187', '127,231,255', '255,46,136', '91,245,184']
              : ['255,126,212', '255,168,226', '255,209,240', '228,155,255']));

        animePetals = [];
        for (i = 0; i < 24; i++) {
          var p = { x: hackRand() % Math.max(1, w), y: hackRand() % Math.max(1, h), rgb: partRgb[i % partRgb.length], idx: i };
          resetAnimePetal(p, false, w, h, world);
          animePetals.push(p);
        }

        animeRain = [];
        for (i = 0; i < 34; i++) {
          animeRain.push({
            x: hackRand() % Math.max(1, w),
            y: hackRand() % Math.max(1, h),
            len: 10 + (hackRand() % 14),
            spd: 6.5 + (hackRand() % 7)
          });
        }

        animeEq = [];
        animeEqPeak = [];
        for (i = 0; i < 22; i++) {
          var lv = 0.25 + (i % 5) * 0.12;
          animeEq.push(lv);
          animeEqPeak.push(lv);
        }

        animeEdgePetals = [];
        for (i = 0; i < 6; i++) {
          var ep = {};
          resetAnimeEdgePetal(ep, i, w, h, false, world);
          animeEdgePetals.push(ep);
        }
      }

      function resetAnimePetal(p, respawnEdge, w, h, world) {
        var r = hackRand();
        var depth = 0.65 + (r % 10) * 0.08;
        p.phase = (r % 628) * 0.01;
        if (world === 'lofi') {
          p.size = 2.2 + (r % 4) * 0.7;
          p.vx = ((r & 1) === 0 ? 0.15 : -0.15);
          p.vy = p.idx < 14 ? (0.85 + (r % 6) * 0.25) : -(0.35 + (r % 4) * 0.12);
          p.angle = 0;
          p.spin = 0;
          if (respawnEdge) {
            p.x = r % Math.max(1, w);
            p.y = p.idx < 14 ? -8 : (h + 8);
          }
        } else if (world === 'starfall') {
          p.size = 2.0 + (r % 4) * 0.65;
          p.vx = ((r % 11) - 5) * 0.14;
          p.vy = -(0.22 + (r % 5) * 0.10);
          p.angle = 0;
          p.spin = 0;
          if (respawnEdge) {
            p.x = r % Math.max(1, w);
            p.y = h * (0.58 + (r % 38) * 0.01);
          } else {
            p.y = h * (0.48 + (r % 48) * 0.01);
          }
        } else if (world === 'miku') {
          p.size = (4.5 + (r % 4) * 1.1) * depth;
          p.vx = ((r % 7) - 3) * 0.18;
          p.vy = -(0.65 + (r % 6) * 0.20);
          p.angle = (r % 24) - 12;
          p.spin = 0;
          if (respawnEdge) {
            p.x = r % Math.max(1, w);
            p.y = h + 14;
          }
        } else {
          p.size = (4.8 + (r % 5) * 1.15) * depth;
          p.vx = (0.75 + (r % 7) * 0.18) * depth;
          p.vy = (0.95 + (r % 6) * 0.22) * depth;
          p.angle = r % 360;
          p.spin = ((r & 1) === 0 ? 1 : -1) * (1.3 + (r % 5) * 0.45);
          if (respawnEdge) {
            if ((r & 3) === 0) {
              p.x = -14;
              p.y = (r % Math.max(1, h)) * 0.7;
            } else {
              p.x = (r % Math.max(1, w)) - w * 0.15;
              p.y = -12;
            }
          }
        }
      }

      function resetAnimeEdgePetal(ep, idx, w, h, edgeSpawn, world) {
        var rightSide = (idx % 2 === 1);
        var marginW = Math.max(44, w * 0.16);
        var upward = world === 'miku' || world === 'starfall' || world === 'lofi';
        ep.x = rightSide ? (w - marginW + ((idx * 19) % marginW)) : ((idx * 23) % marginW);
        ep.y = edgeSpawn ? (upward ? (h + 10 + idx * 12) : (-10 - idx * 14)) : ((idx * 97) % Math.max(1, h));
        ep.vx = upward ? (rightSide ? -0.18 : 0.18) : (0.45 + (idx % 3) * 0.25);
        ep.vy = 0.72 + (idx % 4) * 0.20;
        ep.ang = (idx * 63) % 360;
        ep.size = 4.2 + (idx % 3) * 1.3;
      }

      function spawnAnimeMeteor(w, h) {
        animeMeteor.active = true;
        animeMeteor.life = 1.0;
        animeMeteor.x = w * (0.28 + (hackRand() % 55) * 0.01);
        animeMeteor.y = h * (0.04 + (hackRand() % 24) * 0.01);
        animeMeteor.vx = -(11.5 + (hackRand() % 6));
        animeMeteor.vy = 5.0 + (hackRand() % 4);
      }

      function triggerAnimeSakuraBurst() {
        var now = new Date().getTime();
        animeBurstUntil = now + 950;
        if (activeLivingWorld() === 'lofi') {
          lofiFlash = 0.55;
        } else if (!animeMeteor.active && animeSkyW > 0 && animeSkyH > 0) {
          spawnAnimeMeteor(animeSkyW, animeSkyH);
        }
      }

      function triggerAnimeTabFx() {
        if (!isAnimeThemeActive() || !effectsEnabled) return;
        animeSweepActive = true;
        animeSweepProg = 0;
        triggerAnimeSakuraBurst();
      }

      function drawStarsSet(ctx, animateStep, maxAlpha) {
        for (var i = 0; i < animeStars.length; i++) {
          var st = animeStars[i];
          var pulse = animateStep ? (0.45 + 0.55 * Math.sin(animeTime * 2.2 + st.phase)) : 0.65;
          var a = Math.max(0.14, Math.min(0.95, pulse * maxAlpha)).toFixed(2);
          var col = 'rgba(' + st.rgb + ',' + a + ')';
          if (st.diamond) {
            drawCanvasDiamondStar(ctx, st.x, st.y, st.size * (1.8 + pulse * 0.9), col);
          } else {
            ctx.fillStyle = col;
            ctx.beginPath();
            ctx.arc(st.x, st.y, st.size, 0, Math.PI * 2, false);
            ctx.fill();
          }
        }
      }

      function drawMeteorSet(ctx, outerRgb, innerRgb, headRgb) {
        if (!animeMeteor.active || animeMeteor.life <= 0) return;
        var tx = animeMeteor.x - animeMeteor.vx * 6.8;
        var ty = animeMeteor.y - animeMeteor.vy * 6.8;
        ctx.lineCap = 'round';
        ctx.strokeStyle = 'rgba(' + outerRgb + ',' + (animeMeteor.life * 0.45).toFixed(2) + ')';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(animeMeteor.x, animeMeteor.y);
        ctx.lineTo(tx, ty);
        ctx.stroke();
        ctx.strokeStyle = 'rgba(' + innerRgb + ',' + (animeMeteor.life * 0.94).toFixed(2) + ')';
        ctx.lineWidth = 1.8;
        ctx.beginPath();
        ctx.moveTo(animeMeteor.x, animeMeteor.y);
        ctx.lineTo((animeMeteor.x + tx) * 0.5, (animeMeteor.y + ty) * 0.5);
        ctx.stroke();
        drawCanvasDiamondStar(ctx, animeMeteor.x, animeMeteor.y, 5.2 * animeMeteor.life, 'rgba(' + headRgb + ',' + (animeMeteor.life * 0.95).toFixed(2) + ')');
      }

      function drawLanternCanvas(ctx, ax, ay, cordLen, phase, animateStep) {
        var sway = animateStep ? Math.sin(animeTime * 1.4 + phase) * 5 : 0;
        var lx = ax + sway;
        var ly = ay + cordLen;
        ctx.strokeStyle = 'rgba(140, 98, 156, 0.6)';
        ctx.lineWidth = 1.2;
        ctx.beginPath();
        ctx.moveTo(ax, ay);
        ctx.lineTo(lx, ly);
        ctx.stroke();
        var r = 7.5;
        ctx.fillStyle = 'rgba(255, 213, 107, 0.16)';
        ctx.beginPath();
        ctx.arc(lx, ly + r, r * 2.2, 0, Math.PI * 2, false);
        ctx.fill();
        ctx.fillStyle = 'rgba(255, 158, 104, 0.82)';
        ctx.beginPath();
        ctx.arc(lx, ly + r, r * 0.92, 0, Math.PI * 2, false);
        ctx.fill();
        ctx.fillStyle = 'rgba(255, 240, 184, 0.92)';
        ctx.beginPath();
        ctx.arc(lx, ly + r, r * 0.50, 0, Math.PI * 2, false);
        ctx.fill();
        ctx.fillStyle = 'rgba(46, 19, 58, 0.88)';
        ctx.fillRect(lx - r * 0.55, ly - 1.5, r * 1.1, 3);
        ctx.fillRect(lx - r * 0.55, ly + r * 1.85, r * 1.1, 3);
      }

      function drawAnimeSkyFrame(animateStep) {
        var canvas = $('anime-sky-bg');
        if (!canvas || !canvas.getContext) return;
        var appEl = $('app');
        var w = (appEl && appEl.clientWidth) || window.innerWidth || 1024;
        var h = (appEl && appEl.clientHeight) || window.innerHeight || 552;
        if (w < 40 || h < 40) return;
        var world = activeLivingWorld();
        if (canvas.width !== w || canvas.height !== h || !animeStars.length || animeSceneWorld !== world) {
          canvas.width = w;
          canvas.height = h;
          initAnimeSky(w, h, world);
        }
        var ctx = canvas.getContext('2d');
        var now = new Date().getTime();
        var i;

        if (animateStep) {
          animeTime += 0.05;
          var bursting = now < animeBurstUntil;
          var boost = bursting ? 2.1 : 1.0;

          if (world === 'lofi') {
            for (i = 0; i < animeRain.length; i++) {
              var rn = animeRain[i];
              rn.x -= rn.spd * 0.26;
              rn.y += rn.spd * (bursting ? 1.45 : 1.0);
              if (rn.y > h + 20 || rn.x < -20) {
                rn.x = hackRand() % Math.max(1, Math.floor(w * 1.15));
                rn.y = -(hackRand() % 60);
              }
            }
            for (i = 0; i < animePetals.length; i++) {
              var lp = animePetals[i];
              if (lp.idx < 14) {
                lp.y += lp.vy * (0.5 + 0.5 * Math.abs(Math.sin(animeTime * 1.4 + lp.phase)));
                lp.x += Math.sin(animeTime * 2.2 + lp.phase) * 0.22;
                if (lp.y > h + 14) resetAnimePetal(lp, true, w, h, world);
              } else {
                lp.x += Math.sin(animeTime * 1.3 + lp.phase) * 0.45;
                lp.y += lp.vy;
                if (lp.y < -14) resetAnimePetal(lp, true, w, h, world);
              }
            }
            if (lofiFlash > 0) lofiFlash = Math.max(0, lofiFlash - 0.07);
            else if (now >= lofiNextFlash) { lofiFlash = 0.45; lofiNextFlash = now + 9000 + (hackRand() % 9000); }
          } else if (world === 'starfall') {
            for (i = 0; i < animePetals.length; i++) {
              var ff = animePetals[i];
              ff.x += ff.vx + Math.sin(animeTime * 1.5 + ff.phase) * 0.45;
              ff.y += ff.vy + Math.cos(animeTime * 1.2 + ff.phase) * 0.25;
              if (ff.y < h * 0.35 || ff.x < -16 || ff.x > w + 16) resetAnimePetal(ff, true, w, h, world);
            }
            if (animeMeteor.active) {
              animeMeteor.x += animeMeteor.vx;
              animeMeteor.y += animeMeteor.vy;
              animeMeteor.life -= 0.06;
              if (animeMeteor.life <= 0 || animeMeteor.x < -70 || animeMeteor.y > h + 40) {
                animeMeteor.active = false;
                animeMeteor.nextAt = now + 2800 + (hackRand() % 3200);
              }
            } else if (now >= animeMeteor.nextAt) spawnAnimeMeteor(w, h);
          } else if (world === 'miku') {
            for (i = 0; i < animePetals.length; i++) {
              var mn = animePetals[i];
              mn.x += mn.vx + Math.sin(animeTime * 2.1 + mn.phase) * 0.5;
              mn.y += mn.vy * boost;
              mn.angle = Math.sin(animeTime * 2.0 + mn.phase) * 14;
              if (mn.y < -20) resetAnimePetal(mn, true, w, h, world);
            }
            for (i = 0; i < animeEq.length; i++) {
              var w1 = 0.48 + 0.42 * Math.sin(animeTime * 4.2 + i * 0.55);
              var w2 = 0.35 + 0.35 * Math.cos(animeTime * 2.7 - i * 0.38);
              var tgt = Math.min(1, (w1 * 0.65 + w2 * 0.35) * (bursting ? 1.25 : 1.0));
              animeEq[i] = animeEq[i] * 0.62 + tgt * 0.38;
              if (animeEq[i] > animeEqPeak[i]) animeEqPeak[i] = animeEq[i];
              else animeEqPeak[i] = Math.max(animeEq[i], animeEqPeak[i] - 0.025);
            }
          } else {
            var gust = Math.sin(animeTime * 0.7) * 0.55;
            for (i = 0; i < animePetals.length; i++) {
              var p = animePetals[i];
              p.x += (p.vx + gust) * boost + Math.sin(animeTime * 1.9 + p.phase) * 0.75;
              p.y += p.vy * (bursting ? 1.45 : 1.0);
              p.angle = (p.angle + p.spin * boost) % 360;
              if (p.y > h + 16 || p.x > w + 22) resetAnimePetal(p, true, w, h, world);
            }
            if (animeMeteor.active) {
              animeMeteor.x += animeMeteor.vx;
              animeMeteor.y += animeMeteor.vy;
              animeMeteor.life -= 0.06;
              if (animeMeteor.life <= 0 || animeMeteor.x < -70 || animeMeteor.y > h + 40) {
                animeMeteor.active = false;
                animeMeteor.nextAt = now + 3800 + (hackRand() % 3400);
              }
            } else if (now >= animeMeteor.nextAt) spawnAnimeMeteor(w, h);
          }
        }

        // ---- RENDER PER WORLD ----
        var grad = ctx.createLinearGradient(0, 0, 0, h);
        if (world === 'lofi') {
          grad.addColorStop(0, '#231134'); grad.addColorStop(0.42, '#180c26'); grad.addColorStop(0.80, '#0e0718'); grad.addColorStop(1, '#050209');
          ctx.fillStyle = grad; ctx.fillRect(0, 0, w, h);
          var lg = ctx.createRadialGradient(w * 0.86, h * 0.78, 8, w * 0.86, h * 0.78, Math.max(w, h) * 0.45);
          lg.addColorStop(0, 'rgba(255, 179, 138, 0.28)'); lg.addColorStop(0.45, 'rgba(255, 224, 102, 0.12)'); lg.addColorStop(1, 'rgba(19, 10, 30, 0)');
          ctx.fillStyle = lg; ctx.fillRect(0, 0, w, h);
          if (lofiFlash > 0.01) {
            ctx.fillStyle = 'rgba(216, 232, 255, ' + (lofiFlash * 0.28).toFixed(2) + ')';
            ctx.fillRect(0, 0, w, h);
          }
          drawStarsSet(ctx, animateStep, 0.55);
          var lmr = Math.min(w, h) * 0.062;
          ctx.fillStyle = 'rgba(255, 224, 102, 0.13)'; ctx.beginPath(); ctx.arc(w * 0.78, h * 0.22, lmr * 1.7, 0, Math.PI * 2, false); ctx.fill();
          ctx.fillStyle = 'rgba(255, 241, 208, 0.88)'; ctx.beginPath(); ctx.arc(w * 0.78, h * 0.22, lmr, 0, Math.PI * 2, false); ctx.fill();
          ctx.fillStyle = '#1d0e2d'; ctx.beginPath(); ctx.arc(w * 0.78 - lmr * 0.35, h * 0.22 - lmr * 0.16, lmr * 0.88, 0, Math.PI * 2, false); ctx.fill();
          // Skyline
          var bw = w / 12;
          for (i = 0; i < 12; i++) {
            var bh = h * (0.14 + ((i * 37) % 15) * 0.011);
            var bx = i * bw, by = h - bh;
            ctx.fillStyle = 'rgba(10, 5, 19, 0.62)'; ctx.fillRect(bx, by, bw + 1, bh);
            ctx.fillStyle = (i % 3 === 0) ? 'rgba(255, 179, 138, 0.34)' : 'rgba(255, 224, 102, 0.28)';
            ctx.fillRect(bx + bw * 0.24, by + 10, Math.max(2, bw * 0.14), 4);
            ctx.fillRect(bx + bw * 0.56, by + 22, Math.max(2, bw * 0.14), 4);
          }
          // Window mullions
          ctx.strokeStyle = 'rgba(77, 45, 90, 0.24)'; ctx.lineWidth = 3;
          ctx.beginPath(); ctx.moveTo(w * 0.64, 0); ctx.lineTo(w * 0.64, h * 0.86); ctx.moveTo(0, h * 0.86); ctx.lineTo(w, h * 0.86); ctx.stroke();
          // Fairy lights string
          ctx.strokeStyle = 'rgba(95, 61, 107, 0.35)'; ctx.lineWidth = 1.2;
          ctx.beginPath(); ctx.moveTo(0, h * 0.08); ctx.quadraticCurveTo(w * 0.25, h * 0.18, w * 0.50, h * 0.10); ctx.quadraticCurveTo(w * 0.75, h * 0.18, w, h * 0.08); ctx.stroke();
          var bCols = ['255,224,102', '255,179,138', '255,136,188', '159,227,201'];
          for (i = 0; i < 11; i++) {
            var ft = (i + 0.5) / 11;
            var fbx = w * ft;
            var fby = h * 0.115 + Math.abs(Math.sin(ft * Math.PI * 2)) * (h * 0.032);
            var fg = animateStep ? (0.65 + 0.35 * Math.sin(animeTime * 2.4 + i * 0.9)) : 0.85;
            ctx.fillStyle = 'rgba(' + bCols[i % 4] + ',' + (fg * 0.22).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(fbx, fby, 10, 0, Math.PI * 2, false); ctx.fill();
            ctx.fillStyle = 'rgba(' + bCols[i % 4] + ',' + (fg * 0.90).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(fbx, fby, 3, 0, Math.PI * 2, false); ctx.fill();
          }
          // Falling rain streaks
          ctx.strokeStyle = 'rgba(159, 227, 201, 0.28)'; ctx.lineWidth = 1.1;
          ctx.beginPath();
          for (i = 0; i < animeRain.length; i++) {
            var r2 = animeRain[i];
            ctx.moveTo(r2.x, r2.y); ctx.lineTo(r2.x - r2.len * 0.26, r2.y + r2.len);
          }
          ctx.stroke();
          // Glass droplets + warm floating motes
          for (i = 0; i < animePetals.length; i++) {
            var dp = animePetals[i];
            if (dp.idx < 14) {
              ctx.strokeStyle = 'rgba(196, 232, 255, 0.20)'; ctx.lineWidth = 1.4;
              ctx.beginPath(); ctx.moveTo(dp.x, dp.y - 10); ctx.lineTo(dp.x, dp.y); ctx.stroke();
              ctx.fillStyle = 'rgba(226, 244, 255, 0.54)'; ctx.beginPath(); ctx.arc(dp.x, dp.y, dp.size, 0, Math.PI * 2, false); ctx.fill();
            } else {
              var ma = animateStep ? (0.35 + 0.35 * Math.sin(animeTime * 1.8 + dp.phase)) : 0.45;
              ctx.fillStyle = 'rgba(' + dp.rgb + ',' + (ma * 0.65).toFixed(2) + ')';
              ctx.beginPath(); ctx.arc(dp.x, dp.y, dp.size * 1.25, 0, Math.PI * 2, false); ctx.fill();
            }
          }
          // Tea steam wisps
          ctx.strokeStyle = 'rgba(255, 235, 216, 0.20)'; ctx.lineWidth = 2;
          for (var st = 0; st < 3; st++) {
            var stx = w * 0.77 + (st - 1) * 10, sby = h * 0.88;
            var sw1 = Math.sin(animeTime * 1.8 + st * 1.4) * 9, sw2 = Math.cos(animeTime * 1.5 + st * 1.1) * 11;
            ctx.beginPath(); ctx.moveTo(stx, sby); ctx.bezierCurveTo(stx + sw1, sby - 26, stx - sw2, sby - 52, stx + sw1 * 0.6, sby - 78); ctx.stroke();
          }
          return;
        }

        if (world === 'starfall') {
          grad.addColorStop(0, '#020514'); grad.addColorStop(0.44, '#091432'); grad.addColorStop(0.82, '#070f26'); grad.addColorStop(1, '#010208');
          ctx.fillStyle = grad; ctx.fillRect(0, 0, w, h);
          // 2 Waving Aurora Borealis curtains
          var drawAurora = function (baseY, thick, col, ph) {
            ctx.fillStyle = col; ctx.beginPath();
            var k;
            for (k = 0; k <= 12; k++) {
              var ax = (w * k) / 12;
              var ay = baseY + Math.sin(animeTime * 0.85 + k * 0.55 + ph) * 20;
              if (k === 0) ctx.moveTo(ax, ay); else ctx.lineTo(ax, ay);
            }
            for (k = 12; k >= 0; k--) {
              var ax2 = (w * k) / 12;
              var ay2 = baseY + thick + Math.cos(animeTime * 0.70 + k * 0.48 + ph) * 16;
              ctx.lineTo(ax2, ay2);
            }
            ctx.closePath(); ctx.fill();
          };
          drawAurora(h * 0.24, h * 0.14, 'rgba(109, 255, 176, 0.20)', 0);
          drawAurora(h * 0.33, h * 0.12, 'rgba(181, 124, 255, 0.16)', 2.1);

          drawStarsSet(ctx, animateStep, 0.94);

          // Self-drawing cycling cyan constellations
          var cyc = (animeTime * 0.18) % CONST_PTS.length;
          var cIdx = Math.floor(cyc) % CONST_PTS.length;
          var cFrac = cyc - Math.floor(cyc);
          var cRev = cFrac < 0.75 ? Math.min(1, cFrac / 0.45) : Math.max(0, (1 - cFrac) / 0.25);
          if (cRev > 0.02) {
            var cPts = CONST_PTS[cIdx], cEd = CONST_EDGES[cIdx];
            var eCnt = cEd.length / 2, dEd = eCnt * cRev;
            ctx.strokeStyle = 'rgba(127, 231, 255, ' + (cRev * 0.54).toFixed(2) + ')';
            ctx.lineWidth = 1.3;
            for (var ce = 0; ce < eCnt; ce++) {
              if (ce > dEd) break;
              var sf = Math.min(1, dEd - ce);
              var a0 = cEd[ce * 2], b0 = cEd[ce * 2 + 1];
              var x1 = cPts[a0 * 2] * w, y1 = cPts[a0 * 2 + 1] * h;
              var x2 = cPts[b0 * 2] * w, y2 = cPts[b0 * 2 + 1] * h;
              ctx.beginPath(); ctx.moveTo(x1, y1); ctx.lineTo(x1 + (x2 - x1) * sf, y1 + (y2 - y1) * sf); ctx.stroke();
            }
            for (var cn = 0; cn < cPts.length / 2; cn++) {
              drawCanvasDiamondStar(ctx, cPts[cn * 2] * w, cPts[cn * 2 + 1] * h, 3.6, 'rgba(232,251,255,' + (cRev * 0.86).toFixed(2) + ')');
            }
          }

          drawMeteorSet(ctx, '255,224,102', '127,231,255', '255,255,255');

          // Horizon hills + Torii gate silhouette
          ctx.fillStyle = 'rgba(3, 7, 20, 0.84)';
          ctx.beginPath();
          ctx.moveTo(0, h); ctx.lineTo(0, h * 0.86);
          ctx.quadraticCurveTo(w * 0.24, h * 0.80, w * 0.50, h * 0.84);
          ctx.quadraticCurveTo(w * 0.76, h * 0.88, w, h * 0.81);
          ctx.lineTo(w, h); ctx.closePath(); ctx.fill();

          var tcx = w * 0.50, tby = h * 0.855, ts = Math.min(w, h) * 0.14;
          var tph = ts * 0.85, ths = ts * 0.42, tpw = Math.max(3, ts * 0.08);
          ctx.fillStyle = 'rgba(4, 8, 20, 0.92)';
          ctx.fillRect(tcx - ths - tpw * 0.5, tby - tph, tpw, tph + 6);
          ctx.fillRect(tcx + ths - tpw * 0.5, tby - tph, tpw, tph + 6);
          ctx.beginPath();
          ctx.moveTo(tcx - ths * 1.55, tby - tph - ts * 0.08);
          ctx.quadraticCurveTo(tcx, tby - tph + ts * 0.03, tcx + ths * 1.55, tby - tph - ts * 0.08);
          ctx.lineTo(tcx + ths * 1.42, tby - tph + ts * 0.06);
          ctx.quadraticCurveTo(tcx, tby - tph + ts * 0.14, tcx - ths * 1.42, tby - tph + ts * 0.06);
          ctx.closePath(); ctx.fill();
          ctx.fillRect(tcx - ths * 1.25, tby - tph * 0.72, ths * 2.5, tpw * 0.85);
          ctx.fillRect(tcx - tpw * 0.55, tby - tph * 0.92, tpw * 1.1, tph * 0.20);

          // Glowing fireflies
          for (i = 0; i < animePetals.length; i++) {
            var fp = animePetals[i];
            var fg2 = animateStep ? (0.35 + 0.65 * Math.abs(Math.sin(animeTime * 2.6 + fp.phase))) : 0.7;
            ctx.fillStyle = 'rgba(' + fp.rgb + ',' + (fg2 * 0.22).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(fp.x, fp.y, fp.size * 2.8, 0, Math.PI * 2, false); ctx.fill();
            ctx.fillStyle = 'rgba(' + fp.rgb + ',' + (fg2 * 0.92).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(fp.x, fp.y, fp.size, 0, Math.PI * 2, false); ctx.fill();
          }
          return;
        }

        if (world === 'miku') {
          grad.addColorStop(0, '#04171f'); grad.addColorStop(0.45, '#051c24'); grad.addColorStop(0.82, '#031015'); grad.addColorStop(1, '#010608');
          ctx.fillStyle = grad; ctx.fillRect(0, 0, w, h);
          var mg1 = ctx.createRadialGradient(w * 0.80, h * 0.55, 8, w * 0.80, h * 0.55, Math.max(w, h) * 0.48);
          mg1.addColorStop(0, 'rgba(57, 197, 187, 0.24)'); mg1.addColorStop(0.50, 'rgba(127, 231, 255, 0.11)'); mg1.addColorStop(1, 'rgba(3, 16, 21, 0)');
          ctx.fillStyle = mg1; ctx.fillRect(0, 0, w, h);
          var mg2 = ctx.createRadialGradient(w * 0.18, h * 0.28, 8, w * 0.18, h * 0.28, Math.max(w, h) * 0.40);
          mg2.addColorStop(0, 'rgba(255, 46, 136, 0.16)'); mg2.addColorStop(1, 'rgba(3, 16, 21, 0)');
          ctx.fillStyle = mg2; ctx.fillRect(0, 0, w, h);

          drawStarsSet(ctx, animateStep, 0.72);

          // Stage rings
          var mPulse = animateStep ? (1.0 + 0.06 * Math.sin(animeTime * 3.0)) : 1.0;
          var mR = Math.min(w, h) * 0.22;
          ctx.strokeStyle = 'rgba(57, 197, 187, 0.32)'; ctx.lineWidth = 1.6;
          ctx.beginPath(); ctx.arc(w * 0.80, h * 0.58, mR * mPulse, 0, Math.PI * 2, false); ctx.stroke();
          ctx.strokeStyle = 'rgba(255, 46, 136, 0.26)';
          ctx.beginPath(); ctx.arc(w * 0.80, h * 0.58, mR * 1.22 / mPulse, 0, Math.PI * 2, false); ctx.stroke();

          // Flowing dual synth waves
          var drawWave = function (cy, amp, freq, spd, col, lw) {
            ctx.strokeStyle = col; ctx.lineWidth = lw; ctx.beginPath();
            for (var s = 0; s <= 24; s++) {
              var wx = (w * s) / 24;
              var wy = cy + Math.sin(wx * freq + animeTime * spd) * amp;
              if (s === 0) ctx.moveTo(wx, wy); else ctx.lineTo(wx, wy);
            }
            ctx.stroke();
          };
          drawWave(h * 0.52, 20, 0.012, 2.2, 'rgba(57, 197, 187, 0.30)', 2.0);
          drawWave(h * 0.57, 15, 0.015, -1.7, 'rgba(255, 46, 136, 0.24)', 1.5);

          // 22-band bottom spectrum equalizer
          var eqSlot = w / animeEq.length;
          var eqGap = Math.max(2, eqSlot * 0.24);
          var maxEqH = h * 0.24;
          for (i = 0; i < animeEq.length; i++) {
            var ebh = Math.max(4, animeEq[i] * maxEqH);
            var ex = i * eqSlot + eqGap * 0.5;
            var ew = eqSlot - eqGap;
            ctx.fillStyle = (i % 5 === 2) ? 'rgba(255, 46, 136, 0.38)' : ((i % 3 === 0) ? 'rgba(127, 231, 255, 0.40)' : 'rgba(57, 197, 187, 0.42)');
            ctx.fillRect(ex, h - ebh, ew, ebh);
            var pkY = h - Math.max(6, animeEqPeak[i] * maxEqH);
            ctx.fillStyle = (i % 5 === 2) ? 'rgba(255, 102, 178, 0.80)' : 'rgba(154, 248, 255, 0.80)';
            ctx.fillRect(ex, pkY - 2.2, ew, 2.2);
          }

          // Floating musical eighth-notes
          var mna = animateStep ? '0.68' : '0.42';
          for (i = 0; i < animePetals.length; i++) {
            var np = animePetals[i];
            drawCanvasMusicNote(ctx, np.x, np.y, np.size, np.angle, 'rgba(' + np.rgb + ',' + mna + ')');
          }

          // Graceful translucent Hatsune Miku hologram cameo in bottom-right
          if (mikuSpriteReady && mikuSpriteImg) {
            var mh = Math.min(h * 0.48, 210);
            var mw = mh * (mikuSpriteImg.width / Math.max(1, mikuSpriteImg.height));
            var mbob = animateStep ? Math.sin(animeTime * 2.1) * 5.5 : 0;
            ctx.save();
            ctx.globalAlpha = 0.40;
            ctx.drawImage(mikuSpriteImg, w - mw - 14, h - mh - 6 + mbob, mw, mh);
            ctx.restore();
          }
          return;
        }

        // ---- SAKURA STORM (DEFAULT LIVING WORLD) ----
        grad.addColorStop(0, '#180c38'); grad.addColorStop(0.42, '#0e0824'); grad.addColorStop(0.80, '#070414'); grad.addColorStop(1, '#020108');
        ctx.fillStyle = grad; ctx.fillRect(0, 0, w, h);
        var neb = ctx.createRadialGradient(w * 0.82, h * 0.18, 8, w * 0.82, h * 0.18, Math.max(w, h) * 0.42);
        neb.addColorStop(0, 'rgba(255, 126, 212, 0.22)'); neb.addColorStop(0.48, 'rgba(199, 146, 255, 0.11)'); neb.addColorStop(1, 'rgba(9, 5, 22, 0)');
        ctx.fillStyle = neb; ctx.fillRect(0, 0, w, h);

        var moonR = Math.min(w, h) * 0.072;
        var mx = w * 0.87, my = h * 0.16;
        ctx.fillStyle = 'rgba(255, 213, 107, 0.13)'; ctx.beginPath(); ctx.arc(mx, my, moonR * 1.65, 0, Math.PI * 2, false); ctx.fill();
        ctx.fillStyle = 'rgba(255, 242, 204, 0.85)'; ctx.beginPath(); ctx.arc(mx, my, moonR, 0, Math.PI * 2, false); ctx.fill();
        ctx.fillStyle = '#150b30'; ctx.beginPath(); ctx.arc(mx - moonR * 0.36, my - moonR * 0.18, moonR * 0.88, 0, Math.PI * 2, false); ctx.fill();

        drawStarsSet(ctx, animateStep, 0.88);

        // Distant mountain ridge
        ctx.fillStyle = 'rgba(7, 3, 18, 0.65)';
        ctx.beginPath(); ctx.moveTo(0, h); ctx.lineTo(0, h * 0.85);
        ctx.quadraticCurveTo(w * 0.22, h * 0.74, w * 0.45, h * 0.83);
        ctx.quadraticCurveTo(w * 0.72, h * 0.90, w, h * 0.78);
        ctx.lineTo(w, h); ctx.closePath(); ctx.fill();

        // Silhouetted cherry branch + 2 swaying Japanese paper lanterns
        ctx.strokeStyle = 'rgba(42, 18, 56, 0.68)'; ctx.lineWidth = 4.5; ctx.lineCap = 'round';
        ctx.beginPath(); ctx.moveTo(-6, h * 0.12); ctx.quadraticCurveTo(w * 0.14, h * 0.17, w * 0.28, h * 0.10); ctx.stroke();
        ctx.lineWidth = 2.6;
        ctx.beginPath(); ctx.moveTo(w * 0.12, h * 0.14); ctx.quadraticCurveTo(w * 0.18, h * 0.22, w * 0.25, h * 0.20); ctx.stroke();
        drawLanternCanvas(ctx, w * 0.11, h * 0.145, 16, 0, animateStep);
        drawLanternCanvas(ctx, w * 0.23, h * 0.12, 22, 1.8, animateStep);

        drawMeteorSet(ctx, '255,126,212', '124,232,255', '255,248,231');

        var pAlpha = animateStep ? '0.68' : '0.42';
        for (i = 0; i < animePetals.length; i++) {
          var pt = animePetals[i];
          var flut = 0.42 + 0.58 * Math.abs(Math.cos(animeTime * 2.3 + pt.phase));
          drawCanvasSakuraPetal(ctx, pt.x, pt.y, pt.size, pt.angle, 'rgba(' + pt.rgb + ',' + pAlpha + ')', flut);
        }
      }

      function drawAnimeSparkleFrame(animateStep) {
        var canvas = $('anime-sparkle-fx');
        if (!canvas || !canvas.getContext) return;
        var appEl = $('app');
        var w = (appEl && appEl.clientWidth) || window.innerWidth || 1024;
        var h = (appEl && appEl.clientHeight) || window.innerHeight || 552;
        if (w < 40 || h < 40) return;
        var world = activeLivingWorld();
        if (canvas.width !== w || canvas.height !== h || animeSceneWorld !== world) {
          canvas.width = w;
          canvas.height = h;
          if (!animeEdgePetals.length || animeSceneWorld !== world) initAnimeSky(w, h, world);
        }
        var ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, w, h);

        var upward = world === 'miku' || world === 'starfall' || world === 'lofi';
        var i;
        if (animateStep) {
          animeSparkleTime += 0.06;
          for (i = 0; i < animeEdgePetals.length; i++) {
            var ep = animeEdgePetals[i];
            ep.x += ep.vx + Math.sin(animeSparkleTime * 2.1 + i * 1.7) * 0.55;
            ep.y += upward ? -ep.vy : ep.vy;
            ep.ang = (ep.ang + 2.2) % 360;
            if (upward) {
              if (ep.y < -18 || ep.x < -18 || ep.x > w + 18) resetAnimeEdgePetal(ep, i, w, h, true, world);
            } else {
              if (ep.y > h + 16 || ep.x > w + 16) resetAnimeEdgePetal(ep, i, w, h, true, world);
            }
          }
          if (animeSweepActive) {
            animeSweepProg += 0.11;
            if (animeSweepProg >= 1.0) { animeSweepActive = false; animeSweepProg = 0; }
          }
        }

        if (animateStep) {
          for (i = 0; i < animeEdgePetals.length; i++) {
            var ep2 = animeEdgePetals[i];
            if (world === 'lofi') {
              var lp2 = 0.55 + 0.45 * Math.sin(animeSparkleTime * 2.2 + i);
              ctx.fillStyle = (i % 2 === 0 ? 'rgba(255,224,102,' : 'rgba(255,179,138,') + (lp2 * 0.38).toFixed(2) + ')';
              ctx.beginPath(); ctx.arc(ep2.x, ep2.y, ep2.size * 0.72, 0, Math.PI * 2, false); ctx.fill();
            } else if (world === 'starfall') {
              var sp2 = 0.5 + 0.5 * Math.sin(animeSparkleTime * 2.8 + i * 1.3);
              drawCanvasDiamondStar(ctx, ep2.x, ep2.y, ep2.size * 0.85, (i % 2 === 0 ? 'rgba(220,255,120,' : 'rgba(127,231,255,') + (sp2 * 0.58).toFixed(2) + ')');
            } else if (world === 'miku') {
              drawCanvasMusicNote(ctx, ep2.x, ep2.y, ep2.size * 0.92, 0, i % 2 === 0 ? 'rgba(57,197,187,0.52)' : 'rgba(255,46,136,0.48)');
            } else {
              drawCanvasSakuraPetal(ctx, ep2.x, ep2.y, ep2.size, ep2.ang, (i % 2 === 0) ? 'rgba(255,156,224,0.44)' : 'rgba(255,214,244,0.38)', 1);
            }
          }
        }

        var cCol = world === 'lofi' ? '255,179,138' : (world === 'starfall' ? '127,231,255' : (world === 'miku' ? '57,197,187' : '255,126,212'));
        var sCol = world === 'lofi' ? '255,224,102' : (world === 'starfall' ? '109,255,176' : (world === 'miku' ? '255,46,136' : '255,213,107'));
        var coreCol = world === 'miku' ? '255,92,168' : (world === 'starfall' ? '228,251,255' : '255,242,204');

        if (animeSweepActive) {
          var sx = w * animeSweepProg;
          var fade = Math.max(0, 1 - Math.abs(animeSweepProg - 0.5) * 1.7);
          ctx.fillStyle = 'rgba(' + cCol + ',' + (fade * 0.30).toFixed(2) + ')';
          ctx.fillRect(sx - 24, 0, 36, h);
          ctx.fillStyle = 'rgba(' + coreCol + ',' + (fade * 0.84).toFixed(2) + ')';
          ctx.fillRect(sx - 2, 0, 4, h);
          var starCol = 'rgba(' + sCol + ',' + (fade * 0.85).toFixed(2) + ')';
          for (var s = 0; s < 8; s++) {
            var sy = ((s + 0.5) / 8) * h;
            var ox = sx + ((s % 2 === 0 ? 1 : -1) * 16);
            drawCanvasDiamondStar(ctx, ox, sy, (3.5 + (s % 3) * 2) * fade, starCol);
          }
        }

        ctx.strokeStyle = 'rgba(' + cCol + ',0.64)';
        ctx.lineWidth = 1.4;
        ctx.lineCap = 'round';
        var m = 4, arm = 12;
        ctx.beginPath();
        ctx.moveTo(m, m + arm); ctx.lineTo(m, m); ctx.lineTo(m + arm, m);
        ctx.moveTo(w - m - arm, m); ctx.lineTo(w - m, m); ctx.lineTo(w - m, m + arm);
        ctx.moveTo(m, h - m - arm); ctx.lineTo(m, h - m); ctx.lineTo(m + arm, h - m);
        ctx.moveTo(w - m - arm, h - m); ctx.lineTo(w - m, h - m); ctx.lineTo(w - m, h - m - arm);
        ctx.stroke();

        var twinkle = animateStep ? (0.65 + 0.35 * Math.sin(animeSparkleTime * 2.8)) : 0.85;
        var cornerStar = 'rgba(' + sCol + ',' + (twinkle * 0.88).toFixed(2) + ')';
        drawCanvasDiamondStar(ctx, m + 1.5, m + 1.5, 4, cornerStar);
        drawCanvasDiamondStar(ctx, w - m - 1.5, h - m - 1.5, 4, cornerStar);
      }

      function refreshAnimeRibbonMsgs() {
        var world = activeLivingWorld();
        var pingTxt = animeRibbonPing >= 0 ? ('PING ' + animeRibbonPing + 'ms') : 'PING --ms';
        if (world === 'lofi') {
          animeRibbonMsgs = ['LOFI 72 BPM', 'CHUVA NA JANELA', 'CPU ' + animeRibbonCpu + '% · CHILL', pingTxt, 'STUDY BEATS'];
        } else if (world === 'starfall') {
          animeRibbonMsgs = ['STARFALL AURORA', 'CONSTELACAO OK', 'CPU ' + animeRibbonCpu + '% · NOVA', pingTxt, 'METEOR SHOWER'];
        } else if (world === 'miku') {
          animeRibbonMsgs = ['MIKU 01 STAGE', 'VOCAL SYNTH OK', 'CPU ' + animeRibbonCpu + '% · 39HZ', pingTxt, '01 LIVE SYNC'];
        } else {
          animeRibbonMsgs = ['SAKURA STORM', 'LINK TOKYO OK', 'CPU ' + animeRibbonCpu + '% · SAKURA', pingTxt, 'HANAMI HUD'];
        }
      }

      function updateAnimeRibbonTelemetry(d) {
        if (!d) return;
        var pc = d.pc || {};
        var cpuRaw = typeof pc.cpu === 'number' ? pc.cpu : pc.cpuPct;
        animeRibbonCpu = typeof cpuRaw === 'number' ? Math.round(cpuRaw) : 0;
        animeRibbonPing = (d.ping && typeof d.ping.ms === 'number') ? Math.round(d.ping.ms) : -1;
        refreshAnimeRibbonMsgs();
      }

      function drawAnimeTopRibbon(animateStep) {
        var canvas = $('anime-tb-ribbon');
        if (!canvas || !canvas.getContext) return;
        var w = canvas.clientWidth || 166;
        var h = canvas.clientHeight || 26;
        if (w < 30 || h < 14) return;
        if (canvas.width !== w) canvas.width = w;
        if (canvas.height !== h) canvas.height = h;
        var ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, w, h);

        var world = activeLivingWorld();
        if (animateStep) {
          animeRibbonTick++;
          animeRibbonHold++;
          if (animeRibbonHold >= 42) {
            animeRibbonHold = 0;
            animeRibbonIdx = (animeRibbonIdx + 1) % animeRibbonMsgs.length;
          }
        }

        var rBg = world === 'lofi' ? 'rgba(41, 22, 56, 0.90)' : (world === 'starfall' ? 'rgba(10, 27, 61, 0.90)' : (world === 'miku' ? 'rgba(7, 34, 43, 0.90)' : 'rgba(37, 20, 73, 0.90)'));
        var rBrd = world === 'lofi' ? 'rgba(255, 179, 138, 0.80)' : (world === 'starfall' ? 'rgba(127, 231, 255, 0.80)' : (world === 'miku' ? 'rgba(57, 197, 187, 0.85)' : 'rgba(255, 126, 212, 0.80)'));
        var rStar = world === 'lofi' ? '#ffe066' : (world === 'starfall' ? '#6dffb0' : (world === 'miku' ? '#39c5bb' : '#ffd56b'));
        var rSec = world === 'lofi' ? '#9fe3c9' : (world === 'starfall' ? 'rgba(220,255,120,0.65)' : (world === 'miku' ? '#ff2e88' : 'rgba(255,126,212,0.42)'));
        var rTxt = world === 'lofi' ? '255,244,236' : (world === 'starfall' ? '240,251,255' : (world === 'miku' ? '238,255,254' : '255,245,253'));

        drawRoundedBox(ctx, 1, 1, w - 2, h - 2, h * 0.48);
        ctx.fillStyle = rBg;
        ctx.fill();
        ctx.strokeStyle = rBrd;
        ctx.lineWidth = 1.1;
        ctx.stroke();

        var textLeft = 21;
        if (world === 'lofi' || world === 'miku') {
          var baseY = h * 0.74, maxH = h * 0.46;
          for (var b = 0; b < 4; b++) {
            var lvl = animateStep ? (0.32 + 0.68 * Math.abs(Math.sin(animeRibbonTick * 0.24 + b * 1.15))) : (0.45 + (b % 2) * 0.35);
            ctx.fillStyle = (b === 2) ? rSec : rStar;
            ctx.fillRect(8 + b * 3.6, baseY - maxH * lvl, 2.2, maxH * lvl);
          }
          textLeft = 25;
        } else {
          var pulse = animateStep ? (0.75 + 0.25 * Math.sin(animeRibbonTick * 0.22)) : 0.9;
          drawCanvasDiamondStar(ctx, 12, h * 0.5, 4.6 * pulse, rStar);
          if (animateStep) {
            var px = w - 10 - ((animeRibbonTick * 1.1) % Math.max(16, w * 0.42));
            var py = h * 0.5 + Math.sin(animeRibbonTick * 0.2) * (h * 0.22);
            if (world === 'starfall') {
              ctx.fillStyle = rSec; ctx.beginPath(); ctx.arc(px, py, 2.2, 0, Math.PI * 2, false); ctx.fill();
            } else {
              drawCanvasSakuraPetal(ctx, px, py, 3.2, animeRibbonTick * 4, rSec, 1);
            }
          }
        }

        var alpha = 1;
        if (animateStep) {
          if (animeRibbonHold < 5) alpha = animeRibbonHold / 5;
          else if (animeRibbonHold > 37) alpha = (42 - animeRibbonHold) / 5;
        }
        ctx.save();
        ctx.beginPath();
        ctx.rect(textLeft - 1, 1, w - textLeft - 5, h - 2);
        ctx.clip();
        ctx.font = 'bold 10.5px Roboto, "Segoe UI", Arial, sans-serif';
        ctx.textAlign = 'left';
        ctx.fillStyle = 'rgba(' + rTxt + ',' + Math.max(0.15, alpha).toFixed(2) + ')';
        ctx.fillText(animeRibbonMsgs[animeRibbonIdx % animeRibbonMsgs.length], textLeft, Math.round(h * 0.5 + 3.5));
        ctx.restore();
      }

      function stopAnimeEntrance() {
        if (animeEntranceTimer) { clearInterval(animeEntranceTimer); animeEntranceTimer = null; }
        var box = $('anime-entrance');
        if (box) addClass(box, 'hidden');
      }

      function playAnimeEntrance() {
        var box = $('anime-entrance');
        var canvas = $('anime-entrance-canvas');
        if (!box || !canvas || !canvas.getContext || !isAnimeThemeActive()) return;
        var world = activeLivingWorld();
        if (world === 'miku') ensureMikuSprite();
        stopHackBoot();
        stopAnimeEntrance();
        removeClass(box, 'hidden');
        animeEntranceStart = new Date().getTime();

        var drawEntTitle = function (ctx, w, h, cx, cy, elapsed, fade, title, sub, tRgb, accRgb, lnRgb, hintRgb) {
          var ta = Math.min(1, Math.max(0, (elapsed - 160) / 360)) * fade;
          var port = h > w;
          ctx.textAlign = 'center';
          ctx.font = 'bold ' + (port ? 21 : 27) + 'px Roboto, "Segoe UI", Arial, sans-serif';
          ctx.fillStyle = 'rgba(' + accRgb + ',' + (0.45 * ta).toFixed(2) + ')';
          ctx.fillText(title, cx, cy + 18);
          ctx.fillStyle = 'rgba(' + tRgb + ',' + ta.toFixed(2) + ')';
          ctx.fillText(title, cx, cy + 16);

          var lh = Math.min(w * 0.30, 145) * Math.min(1, Math.max(0, (elapsed - 260) / 400));
          ctx.strokeStyle = 'rgba(' + lnRgb + ',' + (0.82 * ta).toFixed(2) + ')';
          ctx.lineWidth = 1.5;
          ctx.beginPath(); ctx.moveTo(cx - lh, cy + 28); ctx.lineTo(cx + lh, cy + 28); ctx.stroke();

          ctx.font = 'bold ' + (port ? 12 : 13.5) + 'px Roboto, "Segoe UI", Arial, sans-serif';
          ctx.fillStyle = 'rgba(' + accRgb + ',' + ta.toFixed(2) + ')';
          ctx.fillText(sub, cx, cy + 48);

          ctx.font = '11.5px Roboto, "Segoe UI", Arial, sans-serif';
          ctx.fillStyle = 'rgba(' + hintRgb + ',' + (0.72 * ta).toFixed(2) + ')';
          ctx.fillText('Toque ou clique para entrar', cx, cy + 74);

          var sp = 0.7 + 0.3 * Math.sin(elapsed * 0.014);
          var gCol = 'rgba(' + lnRgb + ',' + ta.toFixed(2) + ')';
          drawCanvasDiamondStar(ctx, cx - lh - 10, cy + 28, 6.2 * sp, gCol);
          drawCanvasDiamondStar(ctx, cx + lh + 10, cy + 28, 6.2 * sp, gCol);
        };

        var renderEnt = function () {
          var elapsed = new Date().getTime() - animeEntranceStart;
          if (elapsed >= ANIME_ENTRANCE_MS) {
            stopAnimeEntrance();
            triggerAnimeTabFx();
            return;
          }
          var w = box.clientWidth || window.innerWidth || 1024;
          var h = box.clientHeight || window.innerHeight || 552;
          if (canvas.width !== w || canvas.height !== h) {
            canvas.width = w;
            canvas.height = h;
          }
          var ctx = canvas.getContext('2d');
          ctx.clearRect(0, 0, w, h);

          var progress = Math.min(1, elapsed / ANIME_ENTRANCE_MS);
          var fade = 1;
          if (elapsed > ANIME_ENTRANCE_MS - 420) {
            fade = Math.max(0, 1 - ((elapsed - (ANIME_ENTRANCE_MS - 420)) / 420));
          }
          var minDim = Math.min(w, h);
          var ease = 1 - (1 - progress) * (1 - progress) * (1 - progress);
          var cx = w * 0.5, cy = h * 0.47;
          var i;

          if (world === 'lofi') {
            ctx.fillStyle = 'rgba(19, 10, 30, ' + (0.96 * fade).toFixed(2) + ')';
            ctx.fillRect(0, 0, w, h);
            var vr = Math.max(24, minDim * 0.088) * Math.min(1, elapsed / 300);
            var vy = cy - 54;
            ctx.fillStyle = 'rgba(255, 179, 138, ' + (0.18 * fade).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(cx, vy, vr * 1.5, 0, Math.PI * 2, false); ctx.fill();
            ctx.fillStyle = 'rgba(30, 17, 44, ' + (0.94 * fade).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(cx, vy, vr, 0, Math.PI * 2, false); ctx.fill();
            ctx.strokeStyle = 'rgba(255, 179, 138, ' + (0.65 * fade).toFixed(2) + ')'; ctx.lineWidth = 1.5; ctx.stroke();
            ctx.strokeStyle = 'rgba(255, 224, 102, ' + (0.38 * fade).toFixed(2) + ')'; ctx.lineWidth = 1;
            ctx.beginPath(); ctx.arc(cx, vy, vr * 0.72, 0, Math.PI * 2, false); ctx.stroke();
            ctx.beginPath(); ctx.arc(cx, vy, vr * 0.52, 0, Math.PI * 2, false); ctx.stroke();
            ctx.fillStyle = 'rgba(255, 179, 138, ' + (0.92 * fade).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(cx, vy, vr * 0.30, 0, Math.PI * 2, false); ctx.fill();
            ctx.fillStyle = 'rgba(19, 10, 30, ' + (0.96 * fade).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(cx, vy, vr * 0.09, 0, Math.PI * 2, false); ctx.fill();
            var spin = progress * Math.PI * 3.5;
            ctx.strokeStyle = 'rgba(255, 242, 204, ' + (0.72 * fade).toFixed(2) + ')'; ctx.lineWidth = 2.2;
            ctx.beginPath(); ctx.moveTo(cx + Math.cos(spin) * vr * 0.40, vy + Math.sin(spin) * vr * 0.40); ctx.lineTo(cx + Math.cos(spin) * vr * 0.86, vy + Math.sin(spin) * vr * 0.86); ctx.stroke();
            var bStart = cx - (14 * 12) * 0.5;
            for (i = 0; i < 14; i++) {
              var ebh = (6 + 18 * (0.5 + 0.5 * Math.sin(elapsed * 0.012 + i * 0.7))) * ease;
              ctx.fillStyle = 'rgba(' + (i % 3 === 0 ? '159,227,201' : (i % 2 === 0 ? '255,179,138' : '255,224,102')) + ',' + (0.74 * fade).toFixed(2) + ')';
              ctx.fillRect(bStart + i * 12, cy + 104 - ebh, 7, ebh);
            }
            drawEntTitle(ctx, w, h, cx, cy, elapsed, fade, 'PHONEBOX  LOFI', 'COZY RAINY ROOM  //  72 BPM', '255,244,236', '255,179,138', '255,224,102', '201,166,196');
            return;
          }

          if (world === 'starfall') {
            ctx.fillStyle = 'rgba(2, 5, 18, ' + (0.96 * fade).toFixed(2) + ')';
            ctx.fillRect(0, 0, w, h);
            ctx.strokeStyle = 'rgba(109, 255, 176, ' + (0.52 * fade).toFixed(2) + ')'; ctx.lineWidth = 2.6;
            ctx.beginPath(); ctx.arc(cx, cy, minDim * (0.14 + 0.36 * ease), 0, Math.PI * 2, false); ctx.stroke();
            ctx.strokeStyle = 'rgba(181, 124, 255, ' + (0.48 * fade).toFixed(2) + ')'; ctx.lineWidth = 1.5;
            ctx.beginPath(); ctx.arc(cx, cy, minDim * (0.10 + 0.28 * ease), 0, Math.PI * 2, false); ctx.stroke();
            var ay = cy - 54, spn = Math.min(w * 0.28, 110);
            var cxs = [cx - spn, cx - spn * 0.45, cx, cx + spn * 0.45, cx + spn];
            var cys = [ay + 12, ay - 14, ay + 4, ay - 16, ay + 10];
            var drw = 4 * Math.min(1, progress * 1.8);
            ctx.strokeStyle = 'rgba(127, 231, 255, ' + (0.80 * fade).toFixed(2) + ')'; ctx.lineWidth = 1.6;
            for (i = 0; i < 4; i++) {
              if (i > drw) break;
              var sf2 = Math.min(1, drw - i);
              ctx.beginPath(); ctx.moveTo(cxs[i], cys[i]); ctx.lineTo(cxs[i] + (cxs[i + 1] - cxs[i]) * sf2, cys[i] + (cys[i + 1] - cys[i]) * sf2); ctx.stroke();
            }
            for (i = 0; i < 5; i++) {
              drawCanvasDiamondStar(ctx, cxs[i], cys[i], 4.2 + (i === 2 ? 2.5 : 0), 'rgba(' + (i % 2 === 0 ? '109,255,176' : '255,224,102') + ',' + (0.92 * fade).toFixed(2) + ')');
            }
            drawEntTitle(ctx, w, h, cx, cy, elapsed, fade, 'PHONEBOX  STARFALL', 'AURORA & CONSTELLATIONS  //  HP 7.1', '240,251,255', '127,231,255', '109,255,176', '136,180,220');
            return;
          }

          if (world === 'miku') {
            ctx.fillStyle = 'rgba(3, 16, 21, ' + (0.96 * fade).toFixed(2) + ')';
            ctx.fillRect(0, 0, w, h);
            var port = h > w;
            var tcx2 = port ? (w * 0.5) : (w * 0.40);
            var tcy2 = port ? (h * 0.54) : (h * 0.48);
            var rcx = port ? (w * 0.5) : (w * 0.72);
            var rcy = port ? (h * 0.30) : (h * 0.50);
            ctx.strokeStyle = 'rgba(57, 197, 187, ' + (0.60 * fade).toFixed(2) + ')'; ctx.lineWidth = 2.4;
            ctx.beginPath(); ctx.arc(rcx, rcy, minDim * (0.16 + 0.24 * ease), 0, Math.PI * 2, false); ctx.stroke();
            ctx.strokeStyle = 'rgba(255, 46, 136, ' + (0.54 * fade).toFixed(2) + ')'; ctx.lineWidth = 1.5;
            ctx.beginPath(); ctx.arc(rcx, rcy, minDim * (0.12 + 0.19 * ease), 0, Math.PI * 2, false); ctx.stroke();
            if (mikuSpriteReady && mikuSpriteImg) {
              var mh2 = Math.min(h * (port ? 0.36 : 0.68), 260);
              var mw2 = mh2 * (mikuSpriteImg.width / Math.max(1, mikuSpriteImg.height));
              var rise = (1 - ease) * 26, bob = Math.sin(elapsed * 0.008) * 4;
              ctx.save();
              ctx.globalAlpha = Math.min(1, progress * 2.2) * fade;
              ctx.drawImage(mikuSpriteImg, rcx - mw2 * 0.5, rcy - mh2 * 0.5 + rise + bob, mw2, mh2);
              ctx.restore();
            }
            var slt = w / 18;
            for (i = 0; i < 18; i++) {
              var mbh = (10 + 34 * (0.5 + 0.5 * Math.sin(elapsed * 0.014 + i * 0.6))) * ease;
              ctx.fillStyle = 'rgba(' + (i % 5 === 2 ? '255,46,136' : (i % 3 === 0 ? '127,231,255' : '57,197,187')) + ',' + (0.58 * fade).toFixed(2) + ')';
              ctx.fillRect(i * slt + 3, h - mbh, slt - 6, mbh);
            }
            drawEntTitle(ctx, w, h, tcx2, tcy2, elapsed, fade, 'HATSUNE MIKU  01', 'CYBER VOCAL STAGE  //  HP 7.1', '238,255,254', '57,197,187', '255,46,136', '121,196,199');
            return;
          }

          // Sakura Storm entrance
          ctx.fillStyle = 'rgba(10, 6, 26, ' + (0.95 * fade).toFixed(2) + ')';
          ctx.fillRect(0, 0, w, h);
          var ringR = minDim * (0.14 + 0.38 * ease);
          ctx.strokeStyle = 'rgba(255, 126, 212, ' + (0.55 * fade * (1 - progress * 0.4)).toFixed(2) + ')'; ctx.lineWidth = 2.2;
          ctx.beginPath(); ctx.arc(cx, cy, ringR, 0, Math.PI * 2, false); ctx.stroke();
          ctx.strokeStyle = 'rgba(124, 232, 255, ' + (0.45 * fade).toFixed(2) + ')'; ctx.lineWidth = 1.2;
          ctx.beginPath(); ctx.arc(cx, cy, ringR * 0.72, 0, Math.PI * 2, false); ctx.stroke();
          var moonR = Math.max(18, minDim * 0.068) * Math.min(1, elapsed / 320);
          var my = cy - 48;
          ctx.fillStyle = 'rgba(255, 242, 204, ' + (0.94 * fade).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(cx, my, moonR, 0, Math.PI * 2, false); ctx.fill();
          ctx.fillStyle = 'rgba(10, 6, 26, ' + (0.96 * fade).toFixed(2) + ')'; ctx.beginPath(); ctx.arc(cx - moonR * 0.38, my - moonR * 0.16, moonR * 0.86, 0, Math.PI * 2, false); ctx.fill();
          for (i = 0; i < 16; i++) {
            var ang = (i * Math.PI * 2 / 16) + progress * 2.4;
            var dist = minDim * (0.08 + 0.42 * ease) * (0.82 + (i % 3) * 0.14);
            var pCol = (i % 2 === 0 ? 'rgba(255,126,212,' : 'rgba(255,209,240,') + (0.82 * fade).toFixed(2) + ')';
            drawCanvasSakuraPetal(ctx, cx + Math.cos(ang) * dist, cy + Math.sin(ang) * dist * 0.75, 6.5 + (i % 3) * 1.6, (ang * 180 / Math.PI) + 45, pCol, 1);
          }
          drawEntTitle(ctx, w, h, cx, cy, elapsed, fade, 'PHONEBOX  SAKURA', 'SAKURA STORM EDITION  //  HP 7.1', '255,245,253', '255,126,212', '255,213,107', '192,162,232');
        };
        renderEnt();
        animeEntranceTimer = setInterval(renderEnt, 36);
      }

      // ---- 6. MASTER LIFECYCLE CONTROLLER FOR HACK & ANIME THEMES ----
      function stopAllThemeLoops() {
        if (matrixTimer !== null) { clearInterval(matrixTimer); matrixTimer = null; }
        if (glitchTimer !== null) { clearInterval(glitchTimer); glitchTimer = null; }
        if (termTimer !== null) { clearInterval(termTimer); termTimer = null; }
        if (textGlitchTimer !== null) { clearInterval(textGlitchTimer); textGlitchTimer = null; }
        if (animeSkyTimer !== null) { clearInterval(animeSkyTimer); animeSkyTimer = null; }
        if (animeSparkleTimer !== null) { clearInterval(animeSparkleTimer); animeSparkleTimer = null; }
        if (animeRibbonTimer !== null) { clearInterval(animeRibbonTimer); animeRibbonTimer = null; }
        clearAllTextGlitches();
        clearUiJitter();
      }

      function refreshThemeEffects() {
        stopAllThemeLoops();
        var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        var visible = !document.hidden && !document.webkitHidden;
        var shouldAnimate = effectsEnabled && !reduce && visible;

        if (isHackThemeActive()) {
          stopAnimeEntrance();
          drawMatrixRainFrame(false);
          drawGlitchOverlayFrame(false);
          drawHackTopTerminal(false);
          if (shouldAnimate) {
            glitchNextAuto = new Date().getTime() + 2600 + (hackRand() % 2400);
            textNextWaveAt = new Date().getTime() + 900;
            matrixTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) drawMatrixRainFrame(true);
            }, 55);
            glitchTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) drawGlitchOverlayFrame(true);
            }, 60);
            termTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) drawHackTopTerminal(true);
            }, 85);
            textGlitchTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) stepHackTextGlitches();
            }, 90);
          }
          return;
        }

        stopHackBoot();
        if (isAnimeThemeActive()) {
          refreshAnimeRibbonMsgs();
          drawAnimeSkyFrame(false);
          drawAnimeSparkleFrame(false);
          drawAnimeTopRibbon(false);
          if (shouldAnimate) {
            animeMeteor.nextAt = new Date().getTime() + 1600;
            lofiNextFlash = new Date().getTime() + 6500;
            animeSkyTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) drawAnimeSkyFrame(true);
            }, 50);
            animeSparkleTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) drawAnimeSparkleFrame(true);
            }, 60);
            animeRibbonTimer = setInterval(function () {
              if (!document.hidden && !document.webkitHidden) drawAnimeTopRibbon(true);
            }, 70);
          }
          return;
        }

        stopAnimeEntrance();
      }

      function selectLocalTheme(theme) {
        var prevTheme = activeEffectiveTheme;
        var norm = normalizeThemeId(theme);
        localTheme = isCustomLocalThemeId(norm) ? norm : 'default';
        var choices = document.getElementsByName('panel-theme');
        for (var i = 0; i < choices.length; i++) choices[i].checked = choices[i].value === localTheme;
        if (!state.isPreview) storeSet('localTheme', localTheme);
        applyThemeAndAccent(lastThemeDisplay);
        refreshThemeEffects();
        redrawCharts();

        var settingsEl = $('theme-settings');
        var modalOpen = settingsEl && !(/(^|\s)hidden(\s|$)/).test(settingsEl.className);
        var changed = activeEffectiveTheme !== prevTheme;
        if (isHackThemeActive() && changed && effectsEnabled && !state.isPreview) {
          if (modalOpen) {
            pendingHackBootOnClose = true;
            pendingAnimeEntranceOnClose = false;
          } else {
            coldHackBootPlayed = true;
            playHackBoot();
          }
        } else if (isAnimeThemeActive() && changed && effectsEnabled && !state.isPreview) {
          if (modalOpen) {
            pendingAnimeEntranceOnClose = true;
            pendingHackBootOnClose = false;
          } else {
            coldHackBootPlayed = true;
            playAnimeEntrance();
          }
        }
      }

      function applyThemeAndAccent(disp) {
        var d = disp || {};
        lastThemeDisplay = disp;
        var serverTheme = normalizeThemeId(d.theme);
        var themeKey = localTheme !== 'default' ? localTheme : (THEMES[serverTheme] ? serverTheme : 'default');
        var prevEff = activeEffectiveTheme;
        activeEffectiveTheme = themeKey;
        var th = THEMES[themeKey] || THEMES['default'];
        var isHack = !!(th && th.isHack);
        var isAnime = !!(th && th.isAnime);
        var isSpecial = isHack || isAnime;
        var visualCls = isHack ? 'theme-hack' : (isAnime ? ('theme-anime world-' + (th.world || themeKey)) : 'theme-default');
        if ($('app')) $('app').className = visualCls;
        var ts = $('theme-settings');
        if (ts) ts.className = 'modal ' + visualCls + ((/(^|\s)hidden(\s|$)/).test(ts.className) ? ' hidden' : '');
        var tbArt = $('anime-tb-art');
        if (tbArt && isAnime && th.art && tbArt.getAttribute('src') !== th.art) {
          tbArt.setAttribute('src', th.art);
        }
        if (themeKey === 'miku') ensureMikuSprite();

        var accent = th.accent || ((typeof d.accent === 'string' && /^#[0-9a-fA-F]{6}$/.test(d.accent)) ? d.accent : '#8bd0ff');
        state.accentColor = accent;
        state.themeColors = th;

        var ap = d.appearance || {};
        var rad = isHack ? 0 : (isAnime ? 12 : (typeof ap.radius === 'number' ? Math.max(0, Math.min(32, ap.radius)) : null));
        var borderCol = (!isSpecial && ap.border === false) ? 'transparent' : th.border;
        var cardBg = (!isSpecial && ap.style === 'borderless') ? 'transparent' : ((!isSpecial && ap.style === 'seamless') ? th.bg : th.card);

        var css = ''
          + 'html, body, #app { background-color: ' + th.bg + '; color: ' + th.text + '; }\n'
          + '#topbar, #navbar { background-color: ' + th.bar + '; border-color: ' + th.line + '; }\n'
          + '.card, .pr-kpi-card, .wx-gauge-card, .wx-topbar, .dk-topbar, .cp-head { background-color: ' + cardBg + '; border-color: ' + borderCol + '; }\n'
          + '.card.card-clock, .card.wx-hero-box, .card.pr-list-card, .card.dk-grid-card, .card.dc-top-card { background-color: ' + ((!isSpecial && (ap.style === 'borderless' || ap.style === 'seamless')) ? cardBg : (th.focal || th.cardAlt)) + '; }\n'
          + '.wx-desk-card, .chart-canvas, .q-provider, .dc-call-banner:not(.in-call), .dc-member:not(.dc-speaking), .dc-guild-acc, .dc-notif, .dc-search-inp, .wx-h-canvas, .wx-h-slot-in:not(.now-slot), .cw-wc-in:not(.cw-wc-local) { background-color: ' + (th.inset || th.cardAlt) + '; border-color: ' + borderCol + '; }\n'
          + '.proc-row:nth-child(even) { background-color: ' + (th.stripe || th.card) + '; }\n'
          + '.muted, .card-title, .m-label, .pr-kpi-head, .kv-k { color: ' + th.muted + '; }\n'
          + '.c-teal, .clock-sec, .night-sec, .wx-t-sec, .media-app, .loc-pill, .cw-loc-badge, .mixer-pct-col, .nav-btn.active, .tb-btn.active, .tb-btn-apk, .modal-head, .night-media { color: ' + accent + '; }\n'
          + '.bg-teal, .vol-fill, .m-btn-play, .wx-subtab.active, .dc-subtab.active, .dc-chip.active, .mini-btn.active, .dim-opt.active, .cw-cal-today, .cp-check-done .cp-check-box { background-color: ' + accent + '; border-color: ' + accent + '; }\n'
          + '.nav-btn.active { background-color: ' + (th.focal || th.card) + '; color: ' + th.text + '; border-top-color: ' + accent + '; }\n'
          + '.nav-top .nav-btn.active { border-top-color: transparent; border-bottom-color: ' + accent + '; }\n'
          + '.loc-pill, .cw-loc-badge, .tb-btn.active, .tb-btn-apk, .sc-btn.sc-on, .toast, .modal-card { border-color: ' + accent + '; }\n';

        if (themeKey !== 'default') {
          css += '.theme-settings-card, .tb-btn, .m-btn:not(.m-btn-play), .v-btn, .mini-btn:not(.active), .wx-subtab:not(.active), .dc-btn:not(.dc-btn-leave), .bar-bg, .vol-track, .mix-ico-btn, .sc-btn, .pr-app-ico, .media-art-wrap { background-color: ' + th.cardAlt + '; border-color: ' + th.border + '; color: ' + th.text + '; }\n';
        }
        if (rad !== null) {
          css += '.card, .pr-kpi-card, .wx-gauge-card, .wx-topbar, .dk-topbar, .cp-head { -webkit-border-radius: ' + rad + 'px; border-radius: ' + rad + 'px; }\n';
        }

        var styleEl = $('pb-theme-style');
        if (!styleEl) {
          styleEl = document.createElement('style');
          styleEl.id = 'pb-theme-style';
          styleEl.type = 'text/css';
          (document.head || document.getElementsByTagName('head')[0]).appendChild(styleEl);
        }
        if (styleEl.styleSheet) styleEl.styleSheet.cssText = css;
        else styleEl.textContent = css;

        if (prevEff !== activeEffectiveTheme) {
          refreshThemeEffects();
          redrawCharts();
        }
      }

      function applyWidgetOptions(cfg) {
        var procOpts = findWidgetOptions(cfg, 'processes');
        var procsPage = $('page-procs');
        if (procsPage) {
          if (procOpts && procOpts.allowKill === false) addClass(procsPage, 'pr-nokill');
          else removeClass(procsPage, 'pr-nokill');
        }

        var dcOpts = findWidgetOptions(cfg, 'discord');
        var dcCtrl = $('dc-controls');
        if (dcCtrl) dcCtrl.style.display = (dcOpts && dcOpts.control === false) ? 'none' : '';
        var dcTabNotifs = $('dc-tab-notifs');
        var dcTabChans = $('dc-tab-chans');
        if (dcTabNotifs) dcTabNotifs.style.display = (dcOpts && dcOpts.notifications === false) ? 'none' : '';
        if (dcTabChans) dcTabChans.style.display = (dcOpts && dcOpts.channels === false) ? 'none' : '';
        if (dcOpts && dcOpts.channels === false && dcOpts.notifications !== false) setDiscordSubTab('notifs');
        else if (dcOpts && dcOpts.notifications === false) setDiscordSubTab('chans');

        var clkOpts = findWidgetOptions(cfg, 'clock');
        if ($('home-date')) $('home-date').style.display = (clkOpts && clkOpts.date === false) ? 'none' : '';
        if ($('home-weather')) $('home-weather').style.display = (clkOpts && clkOpts.weather === false) ? 'none' : '';
      }

      function tickCustomPageClock(now, f, ss, showSec, dateStr) {
        var cur = activePageObj();
        if (!cur || cur.viewIdx !== -1) return;
        var root = $(cur.domId);
        if (!root) return;
        var divs = root.getElementsByTagName('div');
        for (var i = 0; i < divs.length; i++) {
          var role = divs[i].getAttribute('data-dyn');
          if (role === 'clock') divs[i].textContent = f.hm + (showSec ? ':' + ss : '') + f.suffix;
          else if (role === 'date') divs[i].textContent = dateStr;
          else if (role === 'wordclock') divs[i].textContent = wordClockPt(now.getHours(), now.getMinutes());
          else if (role === 'countdown') {
            var tgt = divs[i].getAttribute('data-target') || '';
            divs[i].textContent = fmtCountdown(tgt, now);
          }
        }
      }

      function fmtCountdown(targetStr, now) {
        if (!targetStr) return '--';
        var parsed = Date.parse(targetStr);
        if (isNaN(parsed)) {
          var parts = String(targetStr).split(/[-T :]/);
          if (parts.length >= 3) {
            parsed = new Date(parseInt(parts[0], 10) || 2026, (parseInt(parts[1], 10) || 1) - 1, parseInt(parts[2], 10) || 1, parseInt(parts[3], 10) || 0, parseInt(parts[4], 10) || 0).getTime();
          }
        }
        if (isNaN(parsed)) return targetStr;
        var diff = Math.floor((parsed - now.getTime()) / 1000);
        if (diff <= 0) return 'Chegou!';
        var d = Math.floor(diff / 86400);
        var h = Math.floor((diff % 86400) / 3600);
        var m = Math.floor((diff % 3600) / 60);
        return (d > 0 ? d + 'd ' : '') + h + 'h ' + m + 'm';
      }

      function pollCustomPageWidgets(pgObj) {
        if (!pgObj || !pgObj.pageCfg || !pgObj.pageCfg.widgets) return;
        var ws = pgObj.pageCfg.widgets;
        var needCal = false, needFeeds = false, needObs = false, needDocker = false, needSteam = false, needBat = false;
        var haIds = [];
        for (var i = 0; i < ws.length; i++) {
          var w = ws[i];
          if (!w) continue;
          if (w.type === 'calendar') needCal = true;
          else if (w.type === 'feeds') needFeeds = true;
          else if (w.type === 'obs' || w.type.indexOf('obs-') === 0) needObs = true;
          else if (w.type === 'docker') needDocker = true;
          else if (w.type === 'steam' || w.type === 'steam-activity') needSteam = true;
          else if (w.type === 'batteries') needBat = true;
          else if (w.type === 'ha' || w.type === 'homeassistant') {
            var ents = (w.options && w.options.entities) ? w.options.entities : [];
            for (var e = 0; e < ents.length; e++) {
              var eid = typeof ents[e] === 'string' ? ents[e].split('=')[0] : (ents[e] && ents[e].id);
              if (eid) haIds.push(eid);
            }
          }
        }
        function onData(k, d) {
          state.customCache[k] = d;
          if (activePageObj().id === pgObj.id) renderCustomPage(pgObj);
        }
        if (needCal) apiRequest('GET', '/api/calendar', 'cp_cal', function (d) { onData('calendar', d); });
        if (needFeeds) apiRequest('GET', '/api/feeds', 'cp_feeds', function (d) { onData('feeds', d); });
        if (needObs) apiRequest('GET', '/api/obs', 'cp_obs', function (d) { onData('obs', d); });
        if (needDocker) apiRequest('GET', '/api/docker', 'cp_docker', function (d) { onData('docker', d); });
        if (needSteam) apiRequest('GET', '/api/steam', 'cp_steam', function (d) { onData('steam', d); });
        if (needBat) apiRequest('GET', '/api/batteries', 'cp_bat', function (d) { onData('batteries', d); });
        if (haIds.length) apiRequest('GET', '/api/ha/states?ids=' + encodeURIComponent(haIds.join(',')), 'cp_ha', function (d) { onData('ha', d); });
      }

      function renderCustomWidgetCard(w, pgId, wIdx) {
        var o = w.options || {};
        var st = state.lastState || {};
        var pc = st.pc || {};
        var gpu = pc.gpu || {};
        var now = new Date();
        var disp = (state.config && state.config.display) ? state.config.display : {};
        var showSec = disp.showSeconds !== false;
        var use12h = disp.hour12 === true || disp.hour12 === 'true';
        var f = formatHourMin(now.getHours(), now.getMinutes(), use12h);
        var dateStr = DAYS_PT[now.getDay()] + ', ' + now.getDate() + ' de ' + MONTHS_PT[now.getMonth()];

        if (w.type === 'clock' || w.type === 'analog') {
          return '<div class="card"><div class="card-title">RELOGIO</div>'
            + '<div class="cp-big-val" data-dyn="clock">' + f.hm + (showSec ? ':' + pad2(now.getSeconds()) : '') + f.suffix + '</div>'
            + '<div class="muted" data-dyn="date">' + escHtml(dateStr) + '</div></div>';
        }
        if (w.type === 'wordclock') {
          return '<div class="card"><div class="card-title">HORA POR EXTENSO</div>'
            + '<div class="cp-big-val" style="font-size:22px" data-dyn="wordclock">' + escHtml(wordClockPt(now.getHours(), now.getMinutes())) + '</div></div>';
        }
        if (w.type === 'weather' || w.type === 'forecast' || w.type === 'air' || w.type === 'sun') {
          var wx = state.weather;
          if (!wx || !wx.ok) return '<div class="card"><div class="card-title">CLIMA</div><div class="muted">Carregando clima...</div></div>';
          var u = '°' + (wx.unit || 'C');
          return '<div class="card"><div class="card-title">CLIMA · ' + escHtml((wx.city || '').toUpperCase()) + '</div>'
            + '<div class="clearfix"><div style="float:left;margin-right:10px">' + getWeatherSvg(wx.code, wx.isDay !== false, 44) + '</div>'
            + '<div><div class="cp-big-val" style="font-size:28px">' + Math.round(wx.temp) + u + '</div>'
            + '<div class="muted">' + escHtml(WMO_PT[wx.code] || '') + ' · Min ' + Math.round(wx.lo) + '° / Max ' + Math.round(wx.hi) + '° · Chuva ' + (wx.rainProb || 0) + '%</div></div></div></div>';
        }
        if (w.type === 'note') {
          return '<div class="card"><div class="card-title">NOTA</div><div class="cp-note-body">' + escHtml(o.text || '(nota vazia)') + '</div></div>';
        }
        if (w.type === 'label') {
          return '<div class="card"><div style="font-size:16px;font-weight:bold;color:#f8fafc">' + escHtml(o.text || '') + '</div></div>';
        }
        if (w.type === 'quote') {
          var qs = o.quotes && o.quotes.length ? o.quotes : ['Mantenha o foco.'];
          return '<div class="card"><div class="card-title">FRASE</div><div style="font-size:14px;font-style:italic;color:#d9e6fa">"' + escHtml(qs[0]) + '"</div></div>';
        }
        if (w.type === 'countdown') {
          return '<div class="card"><div class="card-title">CONTAGEM · ' + escHtml((o.label || 'EVENTO').toUpperCase()) + '</div>'
            + '<div class="cp-big-val c-sky" data-dyn="countdown" data-target="' + escHtml(o.target || '') + '">' + escHtml(fmtCountdown(o.target || '', now)) + '</div>'
            + '<div class="muted">Alvo: ' + escHtml(o.target || '--') + '</div></div>';
        }
        if (w.type === 'checklist') {
          var items = o.items || [];
          var clKey = 'cl_' + pgId + '_' + wIdx;
          var savedMap = {};
          try { savedMap = JSON.parse(storeGet(clKey, '{}')) || {}; } catch (e) { }
          var clHtml = '<div class="card"><div class="card-title">' + escHtml((o.title || 'CHECKLIST').toUpperCase()) + '</div>';
          for (var ci = 0; ci < items.length; ci++) {
            var done = !!savedMap[ci];
            clHtml += '<div class="cp-check-row' + (done ? ' cp-check-done' : '') + '" data-cl-key="' + escHtml(clKey) + '" data-cl-i="' + ci + '">'
              + '<span class="cp-check-box">' + (done ? '✓' : '') + '</span>' + escHtml(items[ci]) + '</div>';
          }
          return clHtml + '</div>';
        }
        if (w.type === 'pomodoro' || w.type === 'timer') {
          var mins = Math.floor(state.pomo.secLeft / 60);
          var secs = state.pomo.secLeft % 60;
          return '<div class="card"><div class="card-title">' + escHtml((o.label || 'FOCO / POMODORO').toUpperCase()) + '</div>'
            + '<div class="cp-pomo-time" id="cp-pomo-disp">' + pad2(mins) + ':' + pad2(secs) + '</div>'
            + '<div class="cp-btn-row">'
            + '<button type="button" class="mini-btn' + (state.pomo.running ? ' active' : '') + '" data-pomo="toggle">' + (state.pomo.running ? 'Pausar' : 'Iniciar') + '</button>'
            + '<button type="button" class="mini-btn" data-pomo="reset">Reiniciar 25m</button>'
            + '</div></div>';
        }
        if (w.type === 'push') {
          var pKey = o.key || '';
          var itemsObj = (state.lastPush && state.lastPush.items) ? state.lastPush.items : {};
          var entry = pKey ? itemsObj[pKey] : null;
          var fv = entry ? formatPushValue(entry.data !== undefined ? entry.data : entry.value) : { main: '--', sub: 'Aguardando POST /api/push/' + (pKey || 'key') };
          return '<div class="card"><div class="card-title">' + escHtml((o.title || pKey || 'WEBHOOK').toUpperCase()) + '</div>'
            + '<div class="cp-big-val c-teal">' + escHtml(fv.main) + '</div>'
            + '<div class="muted">' + escHtml(fv.sub) + '</div></div>';
        }
        if (w.type === 'stat' || w.type === 'gauge' || w.type === 'gpu') {
          var m = o.metric || (w.type === 'gpu' ? 'gpu' : 'cpu');
          var mVal = '--', mPct = 0, mLbl = m.toUpperCase();
          if (m === 'cpu') { mPct = Math.round(pc.cpu || 0); mVal = mPct + '%'; }
          else if (m === 'ram') { mPct = pc.ramTotalGb ? Math.round((pc.ramUsedGb / pc.ramTotalGb) * 100) : 0; mVal = mPct + '%'; }
          else if (m === 'gpu') { mPct = Math.round(gpu.util || 0); mVal = mPct + '%'; }
          else if (m === 'temp' || m === 'gputemp') { mPct = Math.min(100, Math.round(gpu.temp || 0)); mVal = Math.round(gpu.temp || 0) + '°C'; }
          else if (m === 'ping') { mVal = (st.ping && typeof st.ping.ms === 'number') ? (Math.round(st.ping.ms) + ' ms') : '-- ms'; mPct = 20; }
          return '<div class="card"><div class="card-title">' + escHtml(mLbl) + '</div>'
            + '<div class="cp-big-val c-teal">' + escHtml(mVal) + '</div>'
            + '<div class="bar-bg"><div class="bar-fill bg-teal" style="width:' + Math.min(100, Math.max(0, mPct)) + '%"></div></div></div>';
        }
        if (w.type === 'button') {
          var scId = o.id || '';
          var sc = null;
          var scs = (state.config && state.config.shortcuts) ? state.config.shortcuts : [];
          for (var bi = 0; bi < scs.length; bi++) { if (scs[bi].id === scId) { sc = scs[bi]; break; } }
          var bLbl = sc ? sc.label : (scId || 'Atalho');
          return '<div class="card"><div class="card-title">BOTAO RAPIDO</div>'
            + '<button type="button" class="dc-btn" style="width:100%" data-cp-run="' + escHtml(scId) + '" data-cp-lbl="' + escHtml(bLbl) + '">' + escHtml(bLbl) + '</button></div>';
        }
        if (w.type === 'calendar') {
          var cal = state.customCache.calendar;
          var evs = (cal && (cal.events || cal.items)) ? (cal.events || cal.items) : [];
          var calH = '<div class="card"><div class="card-title">AGENDA</div>';
          if (!evs.length) return calH + '<div class="muted">Nenhum evento proximo configurado.</div></div>';
          for (var ei = 0; ei < Math.min(6, evs.length); ei++) {
            calH += '<div class="item-row"><div class="item-title">' + escHtml(evs[ei].title || evs[ei].summary || 'Evento') + '</div>'
              + '<div class="item-sub">' + escHtml(evs[ei].when || evs[ei].start || '') + '</div></div>';
          }
          return calH + '</div>';
        }
        if (w.type === 'feeds') {
          var fd = state.customCache.feeds;
          var fitems = (fd && fd.items) ? fd.items : [];
          var fH = '<div class="card"><div class="card-title">NOTICIAS / FEEDS</div>';
          if (!fitems.length) return fH + '<div class="muted">Nenhum feed RSS configurado em General > News feeds.</div></div>';
          for (var fi2 = 0; fi2 < Math.min(6, fitems.length); fi2++) {
            fH += '<div class="cp-feed-row" data-feed-open="' + escHtml(fitems[fi2].id || '') + '">'
              + '<div class="item-title">' + escHtml(fitems[fi2].title || '') + '</div>'
              + '<div class="item-sub">' + escHtml(fitems[fi2].source || '') + '</div></div>';
          }
          return fH + '</div>';
        }
        if (w.type === 'obs' || w.type.indexOf('obs-') === 0) {
          var obs = state.customCache.obs;
          var os = (obs && obs.snapshot) ? obs.snapshot : {};
          var oH = '<div class="card"><div class="card-title">OBS STUDIO</div>';
          if (!obs || !obs.enabled) return oH + '<div class="muted">Ative o OBS Studio em General > OBS Studio.</div></div>';
          if (!os.connected) return oH + '<div class="muted">OBS desconectado.</div></div>';
          oH += '<div class="kv-row clearfix"><span class="kv-k">Cena</span><span class="kv-v">' + escHtml(os.scene || '--') + '</span></div>'
            + '<div class="kv-row clearfix"><span class="kv-k">Status</span><span class="kv-v">' + (os.streaming ? 'AO VIVO' : 'Offline') + (os.recording ? ' · GRAVANDO' : '') + '</span></div>';
          if (obs.control) {
            oH += '<div class="cp-btn-row">'
              + '<button type="button" class="mini-btn" data-obs-act="stream">' + (os.streaming ? 'Parar Live' : 'Iniciar Live') + '</button>'
              + '<button type="button" class="mini-btn" data-obs-act="record">' + (os.recording ? 'Parar Grav.' : 'Gravar') + '</button></div>';
          }
          return oH + '</div>';
        }
        if (w.type === 'docker') {
          var dk = state.customCache.docker;
          var cs = (dk && dk.snapshot && dk.snapshot.containers) ? dk.snapshot.containers : [];
          var dH = '<div class="card"><div class="card-title">DOCKER CONTAINERS</div>';
          if (!cs.length) return dH + '<div class="muted">Nenhum container encontrado.</div></div>';
          for (var di = 0; di < Math.min(8, cs.length); di++) {
            var c = cs[di];
            var run = String(c.state || '').toLowerCase() === 'running';
            dH += '<div class="cp-ha-row clearfix"><span>' + escHtml(c.name || c.id) + ' <span class="muted">(' + escHtml(c.state || '') + ')</span></span>'
              + (dk.control ? '<button type="button" class="mini-btn right" data-dk-id="' + escHtml(c.id) + '" data-dk-act="' + (run ? 'stop' : 'start') + '">' + (run ? 'Parar' : 'Iniciar') + '</button>' : '')
              + '</div>';
          }
          return dH + '</div>';
        }
        if (w.type === 'ha' || w.type === 'homeassistant') {
          var ha = state.customCache.ha;
          var hList = (ha && (ha.entities || ha.states)) ? (ha.entities || ha.states) : (ha && Object.prototype.toString.call(ha) === '[object Array]' ? ha : []);
          var hH = '<div class="card"><div class="card-title">HOME ASSISTANT</div>';
          if (!hList.length) return hH + '<div class="muted">Configure entidades em General > Home Assistant.</div></div>';
          for (var hi = 0; hi < hList.length; hi++) {
            var en = hList[hi];
            hH += '<div class="cp-ha-row clearfix"><span>' + escHtml(en.name || en.id) + '</span>'
              + '<button type="button" class="mini-btn right' + (en.state === 'on' ? ' active' : '') + '" data-ha-toggle="' + escHtml(en.id) + '">' + escHtml(en.state || '--') + '</button></div>';
          }
          return hH + '</div>';
        }
        if (w.type === 'batteries') {
          var bt = state.customCache.batteries;
          var devs = (bt && bt.devices) ? bt.devices : [];
          var bH = '<div class="card"><div class="card-title">BATERIAS SEM FIO</div>';
          if (!devs.length) return bH + '<div class="muted">Nenhum dispositivo com bateria detectado.</div></div>';
          for (var bti = 0; bti < devs.length; bti++) {
            var dv = devs[bti];
            var bp = Math.max(0, Math.min(100, Math.round(dv.percent || dv.level || 0)));
            bH += '<div class="disk-item"><div class="disk-top clearfix"><span>' + escHtml(dv.name || 'Dispositivo') + '</span><span class="right">' + bp + '%</span></div>'
              + '<div class="bar-bg"><div class="bar-fill ' + (bp <= 20 ? 'bg-red' : 'bg-green') + '" style="width:' + bp + '%"></div></div></div>';
          }
          return bH + '</div>';
        }
        if (w.type === 'steam' || w.type === 'steam-activity') {
          var stm = state.customCache.steam;
          var frs = (stm && stm.snapshot && stm.snapshot.friends) ? stm.snapshot.friends : [];
          var sH = '<div class="card"><div class="card-title">STEAM</div>';
          if (!stm || !stm.enabled) return sH + '<div class="muted">Ative a integracao Steam em General > Steam.</div></div>';
          if (!frs.length) return sH + '<div class="muted">Nenhum amigo online no Steam.</div></div>';
          for (var sfi = 0; sfi < Math.min(8, frs.length); sfi++) {
            sH += '<div class="item-row"><div class="item-title">' + escHtml(frs[sfi].name || '') + '</div>'
              + '<div class="item-sub">' + escHtml(frs[sfi].game || frs[sfi].state || '') + '</div></div>';
          }
          return sH + '</div>';
        }
        return '<div class="card"><div class="card-title">' + escHtml(String(w.type || 'WIDGET').toUpperCase()) + '</div>'
          + '<div class="muted">Widget configurado em http://127.0.0.1:8765</div></div>';
      }

      function renderCustomPage(pgObj) {
        if (!pgObj || pgObj.viewIdx !== -1) return;
        var root = $(pgObj.domId);
        if (!root) return;
        var ws = (pgObj.pageCfg && pgObj.pageCfg.widgets) ? pgObj.pageCfg.widgets : [];
        var html = '<div class="cp-head clearfix"><span class="cw-loc-badge">' + escHtml((pgObj.title || 'PAGINA').toUpperCase()) + '</span>'
          + '<button type="button" class="mini-btn right" data-cp-refresh="1">Atualizar</button></div>';
        if (!ws.length) {
          html += '<div class="card"><div class="muted">Pagina vazia — adicione widgets em http://127.0.0.1:8765</div></div>';
        } else {
          html += '<div class="cp-grid clearfix">';
          for (var i = 0; i < ws.length; i++) {
            var colCls = (ws[i].w >= 8 || ws.length === 1) ? 'cp-col-full' : 'cp-col-half';
            html += '<div class="' + colCls + '">' + renderCustomWidgetCard(ws[i], pgObj.id, i) + '</div>';
          }
          html += '</div>';
        }
        root.innerHTML = html;

        var btns = root.getElementsByTagName('button');
        var j;
        for (j = 0; j < btns.length; j++) {
          btns[j].onclick = function () {
            if (this.getAttribute('data-cp-refresh')) { pollCustomPageWidgets(pgObj); showToast('Pagina atualizada'); return; }
            var runId = this.getAttribute('data-cp-run');
            if (runId) { runShortcut(runId, this.getAttribute('data-cp-lbl') || runId); return; }
            var pomoAct = this.getAttribute('data-pomo');
            if (pomoAct === 'toggle') { state.pomo.running = !state.pomo.running; renderCustomPage(pgObj); return; }
            if (pomoAct === 'reset') { state.pomo.running = false; state.pomo.secLeft = state.pomo.totalSec; renderCustomPage(pgObj); return; }
            var obsAct = this.getAttribute('data-obs-act');
            if (obsAct) { apiRequest('POST', '/api/obs/' + encodeURIComponent(obsAct), null, function () { pollCustomPageWidgets(pgObj); }); return; }
            var dkId = this.getAttribute('data-dk-id');
            if (dkId) { apiRequest('POST', '/api/docker/' + encodeURIComponent(dkId) + '/' + encodeURIComponent(this.getAttribute('data-dk-act')), null, function () { pollCustomPageWidgets(pgObj); }); return; }
            var haId = this.getAttribute('data-ha-toggle');
            if (haId) { apiRequest('POST', '/api/ha/toggle/' + encodeURIComponent(haId), null, function () { pollCustomPageWidgets(pgObj); }); return; }
          };
        }
        var divs = root.getElementsByTagName('div');
        for (j = 0; j < divs.length; j++) {
          if (divs[j].getAttribute('data-cl-key')) {
            divs[j].onclick = function () {
              var k = this.getAttribute('data-cl-key');
              var idx = this.getAttribute('data-cl-i');
              var map = {};
              try { map = JSON.parse(storeGet(k, '{}')) || {}; } catch (e) { }
              map[idx] = !map[idx];
              storeSet(k, JSON.stringify(map));
              renderCustomPage(pgObj);
            };
          } else if (divs[j].getAttribute('data-feed-open')) {
            divs[j].onclick = function () {
              var fid = this.getAttribute('data-feed-open');
              if (fid) apiRequest('POST', '/api/feeds/open?id=' + encodeURIComponent(fid), null, function () { showToast('Artigo aberto no PC'); });
            };
          }
        }
      }

      function renderConfiguredExtraWidgets(cfg) {
        var box = $('cw-custom-widgets');
        if (!box) return;
        if (!cfg || !cfg.pages) { box.innerHTML = ''; return; }
        var html = '';
        for (var i = 0; i < cfg.pages.length; i++) {
          var p = cfg.pages[i];
          if (!p || p.enabled === false || !p.widgets) continue;
          for (var j = 0; j < p.widgets.length; j++) {
            var w = p.widgets[j];
            if (!w) continue;
            var opts = w.options || {};
            if (w.type === 'note' && opts.text) {
              html += '<div class="card"><div class="card-title">NOTA (' + escHtml(p.title || 'Config') + ')</div>'
                + '<div style="white-space:pre-wrap;font-size:13px;color:#fef08a">' + escHtml(opts.text) + '</div></div>';
            } else if (w.type === 'checklist' && opts.items && opts.items.length) {
              html += '<div class="card"><div class="card-title">' + escHtml((opts.title || 'CHECKLIST').toUpperCase()) + '</div>';
              for (var c = 0; c < opts.items.length; c++) {
                html += '<div class="kv-row clearfix"><span>• ' + escHtml(opts.items[c]) + '</span></div>';
              }
              html += '</div>';
            } else if (w.type === 'countdown' && opts.target) {
              html += '<div class="card"><div class="card-title">CONTAGEM REGRESSIVA · ' + escHtml((opts.label || '').toUpperCase()) + '</div>'
                + '<div style="font-size:14px;font-weight:bold;color:#38bdf8">Alvo: ' + escHtml(opts.target) + '</div></div>';
            } else if (w.type === 'quote' && opts.quotes && opts.quotes.length) {
              html += '<div class="card"><div class="card-title">FRASE CONFIGURADA</div>'
                + '<div style="font-size:13px;font-style:italic;color:#d9e6fa">' + escHtml(opts.quotes[0]) + '</div></div>';
            }
          }
        }
        box.innerHTML = html;
      }

      function applyConfig(cfg) {
        if (!cfg) return;
        state.config = cfg;
        try { applyThemeAndAccent(cfg.display); } catch (e) { }
        try { syncPagesFromConfig(cfg); } catch (e) { }
        try { applyWidgetOptions(cfg); } catch (e) { }
        try {
          if (cfg.display && cfg.display.orientation && !state.manualOrient && !state.isPreview) {
            var o = cfg.display.orientation;
            if (o === 'landscape' || o === 'portrait' || o === 'auto') {
              state.orientMode = o;
            }
          }
          updateLayout();
        } catch (e) { }
        try { renderShortcuts(cfg); } catch (e) { }
        try { renderWeather(state.weather); } catch (e) { }
        try { updateClock(); } catch (e) { }
        try { renderConfiguredExtraWidgets(cfg); } catch (e) { }
        try { if (state.lastQuotas) renderQuotas(state.lastQuotas); } catch (e) { }
        try { if (activeViewIdx() === -1) renderCustomPage(activePageObj()); } catch (e) { }
      }

      function handleLiveEvents(st) {
        if (!st) return;
        var events = st.events || [];
        if (typeof st.evSeq === 'number' && st.evSeq > state.evSeq) {
          var prevSeq = state.evSeq;
          state.evSeq = st.evSeq;
          if (prevSeq === 0) return; // ignore historical events on initial page load
        }
        for (var i = 0; i < events.length; i++) {
          var ev = events[i];
          if (!ev) continue;
          var t = ev.kind || ev.type || '';
          var data = ev.data || {};
          if (t === 'goto') {
            var pg = String(data.page || '');
            var targetIdx = findTabByPageId(pg);
            if (targetIdx >= 0) {
              state.lastTouchAt = new Date().getTime();
              setPage(targetIdx);
              showToast('Pagina aberta: ' + (state.activePages[targetIdx].title || pg), 'ok');
            }
          } else if (t === 'back') {
            setPage(state.prevPage || 0);
          } else if (t === 'notify') {
            var msg = (data.title ? data.title + ' — ' : '') + (data.text || '');
            var dur = (typeof data.seconds === 'number' && data.seconds > 0) ? Math.min(15000, data.seconds * 1000) : 4500;
            if (msg) showToast(msg, data.level || 'info', dur);
            if (data.page) {
              var nIdx = findTabByPageId(data.page);
              if (nIdx >= 0) setPage(nIdx);
            }
          } else if (t === 'reload') {
            if (state.isPreview) showToast('Recarregamento recebido (preview)', 'info');
            else { try { window.location.reload(); } catch (e) { } }
          } else if (t === 'refresh') {
            if (!state.previewConfig) pollConfig();
            pollWeather();
            pollSync(activeViewIdx() === 4);
            showToast('Painel atualizado', 'ok');
          }
        }
      }

      function applySyncPayload(d) {
        if (!d) return;
        if (!state.previewConfig && d.config && d.config.config) {
          var prevVer = state.cfgVersion;
          state.cfgVersion = d.config.version || state.cfgVersion;
          applyConfig(d.config.config);
          if (prevVer && prevVer !== state.cfgVersion) {
            showToast('Configuracoes sincronizadas do PC', 'ok');
          }
        }
        if (d.weather) {
          try { renderWeather(d.weather); } catch (e) { }
        }
        if (d.state) {
          try { handleLiveEvents(d.state); } catch (e) { }
          try { renderState(d.state); } catch (e) { }
        }
        if (d.approvals) {
          try { renderApprovals(d.approvals); } catch (e) { }
        }
        if (d.controls) {
          try { renderMixer(d.controls); } catch (e) { }
        }
        if (d.procs) {
          try { renderProcs(d.procs); } catch (e) { }
        }
        if (d.quotas) {
          try { renderQuotas(d.quotas); } catch (e) { }
        }
        if (d.work) {
          try { renderWork(d.work); } catch (e) { }
        }
        if (d.push) {
          try { renderPush(d.push); } catch (e) { }
        }
        if (d.discord) {
          try { renderDiscord(d.discord); } catch (e) { }
        }
      }

      function pollSync(liveAi) {
        var cfgParam = state.config ? encodeURIComponent(state.cfgVersion || '') : '';
        var evParam = state.evSeq > 0 ? '&ev=' + state.evSeq : '';
        var vIdx = activeViewIdx();
        var url = '/api/legacy/sync?page=' + vIdx + '&cfg=' + cfgParam + evParam + (liveAi ? '&liveAi=1' : '');
        var key = liveAi ? 'sync_ai' : 'sync';
        apiRequest('GET', url, key, function (d) {
          setOnline(true);
          applySyncPayload(d);
        }, function () {
          setOnline(false);
        });
      }

      function pollState() {
        pollSync(false);
      }

      function pollConfig() {
        if (state.previewConfig) return;
        apiRequest('GET', '/api/config', 'config', function (d) {
          if (!d || !d.config) return;
          state.cfgVersion = d.version || state.cfgVersion;
          applyConfig(d.config);
        });
      }

      function pollWeather() {
        apiRequest('GET', '/api/legacy/weather', 'weather', function (w) {
          renderWeather(w);
        });
      }

      function pollControls() {
        apiRequest('GET', '/api/controls/state', 'controls', function (d) {
          renderMixer(d);
        });
      }

      function pollProcs() {
        apiRequest('GET', '/api/procs?top=30&sort=' + encodeURIComponent(state.sortProcs || 'ram'), 'procs', function (d) {
          renderProcs(d);
        });
      }

      function pollAiWork() {
        if (activeViewIdx() !== 4) return;
        pollSync(true);
      }

      function pollApprovals() {
        apiRequest('GET', '/api/approvals', 'approvals', function (d) { renderApprovals(d); });
      }

      // ==================== CONTROLS BINDINGS ====================
      function sendMedia(action) {
        apiRequest('POST', '/api/media/' + action, null, function (d) {
          if (d && d.media) renderMedia(d.media);
          setTimeout(pollState, 250);
        });
      }

      function sendVolume(qs) {
        apiRequest('POST', '/api/volume?' + qs, null, function (d) {
          if (d) renderAudio(d);
        });
      }

      function markUserActivity() {
        state.lastTouchAt = new Date().getTime();
        if (state.idleDimmed) {
          state.idleDimmed = false;
          applyDimmer(0, true);
        }
      }

      function bindEvents() {
        document.addEventListener('touchstart', markUserActivity, false);
        document.addEventListener('mousedown', markUserActivity, false);

        var settings = $('theme-settings');
        $('btn-settings').onclick = function () {
          pendingHackBootOnClose = false;
          pendingAnimeEntranceOnClose = false;
          removeClass(settings, 'hidden');
          $('close-settings').focus();
        };
        $('close-settings').onclick = function () {
          addClass(settings, 'hidden');
          $('btn-settings').focus();
          if (pendingHackBootOnClose && isHackThemeActive() && effectsEnabled) {
            pendingHackBootOnClose = false;
            playHackBoot();
          } else if (pendingAnimeEntranceOnClose && isAnimeThemeActive() && effectsEnabled) {
            pendingAnimeEntranceOnClose = false;
            playAnimeEntrance();
          }
        };
        if ($('btn-hack-test')) {
          $('btn-hack-test').onclick = function () {
            effectsEnabled = true;
            if ($('theme-effects')) $('theme-effects').checked = true;
            if (!state.isPreview) storeSet('themeEffects', '1');
            pendingHackBootOnClose = false;
            pendingAnimeEntranceOnClose = false;
            if (isHackThemeActive() || localTheme === 'hack') {
              selectLocalTheme('hack');
              addClass(settings, 'hidden');
              playHackBoot();
            } else {
              var targetWorld = isLivingWorldId(localTheme) ? normalizeThemeId(localTheme) : (isLivingWorldId(activeEffectiveTheme) ? activeLivingWorld() : 'sakura');
              selectLocalTheme(targetWorld);
              addClass(settings, 'hidden');
              playAnimeEntrance();
            }
          };
        }
        if ($('hack-boot')) {
          $('hack-boot').onclick = function () {
            stopHackBoot();
            startGlitchBurst(1, 240);
          };
        }
        if ($('anime-entrance')) {
          $('anime-entrance').onclick = function () {
            stopAnimeEntrance();
            triggerAnimeTabFx();
          };
        }
        settings.onclick = function (event) {
          if (event.target === settings) $('close-settings').click();
        };
        document.addEventListener('keydown', function (event) {
          if (event.keyCode === 27 && !(/(^|\s)hidden(\s|$)/).test(settings.className)) $('close-settings').click();
        }, false);
        var choices = document.getElementsByName('panel-theme');
        for (var choice = 0; choice < choices.length; choice++) {
          choices[choice].onchange = function () { if (this.checked) selectLocalTheme(this.value); };
        }
        $('theme-effects').onchange = function () {
          effectsEnabled = this.checked;
          storeSet('themeEffects', effectsEnabled ? '1' : '0');
          refreshThemeEffects();
        };
        document.addEventListener('visibilitychange', refreshThemeEffects, false);
        document.addEventListener('webkitvisibilitychange', refreshThemeEffects, false);

        // Bottom nav
        var navBtns = $('navbar').getElementsByTagName('button');
        for (var i = 0; i < navBtns.length; i++) {
          navBtns[i].onclick = function () {
            setPage(parseInt(this.getAttribute('data-page'), 10));
          };
        }

        // Home metric / clock card click -> jump by semantic view index
        var cards = $('page-home').getElementsByTagName('div');
        for (var c = 0; c < cards.length; c++) {
          if (cards[c].getAttribute('data-goto') !== null) {
            cards[c].onclick = function () {
              var vIdx = parseInt(this.getAttribute('data-goto'), 10);
              var tIdx = findTabByViewIdx(vIdx);
              if (tIdx >= 0) setPage(tIdx);
            };
          }
        }

        // Top bar actions
        $('btn-orient').onclick = function () {
          state.manualOrient = true;
          if (state.orientMode === 'auto') state.orientMode = 'landscape';
          else if (state.orientMode === 'landscape') state.orientMode = 'portrait';
          else state.orientMode = 'auto';
          if (!state.isPreview) storeSet('orient', state.orientMode);
          updateLayout();
          if (state.config) renderShortcuts(state.config);
          showToast('Proporcao: ' + (state.orientMode === 'auto' ? 'Automatica' : (state.orientMode === 'landscape' ? 'Horizontal' : 'Vertical')));
        };

        $('btn-dim').onclick = function () {
          var next = state.dimLevel < 0.1 ? 0.45 : (state.dimLevel < 0.65 ? 0.78 : 0);
          applyDimmer(next);
        };

        $('btn-full').onclick = toggleFullscreen;

        // Media & Volume (Home only)
        if ($('m-prev')) $('m-prev').onclick = function () { sendMedia('prev'); };
        if ($('m-play')) $('m-play').onclick = function () { sendMedia('playpause'); };
        if ($('m-next')) $('m-next').onclick = function () { sendMedia('next'); };

        if ($('v-mute')) $('v-mute').onclick = function () { sendVolume('op=mute'); };
        if ($('v-down')) $('v-down').onclick = function () { sendVolume('op=down'); };
        if ($('v-up')) $('v-up').onclick = function () { sendVolume('op=up'); };

        // Direct tap on volume bar
        if ($('v-track')) $('v-track').onclick = function (e) {
          var rect = this.getBoundingClientRect();
          var x = (e.clientX || 0) - rect.left;
          var pct = Math.max(0, Math.min(100, Math.round((x / (rect.width || 1)) * 100)));
          sendVolume('level=' + pct);
        };

        // Procs sort, limit & refresh
        if ($('sort-ram')) $('sort-ram').onclick = function () {
          state.sortProcs = 'ram';
          addClass($('sort-ram'), 'active');
          removeClass($('sort-cpu'), 'active');
          if (state.lastProcs) renderProcs(state.lastProcs);
          else pollProcs();
        };
        if ($('sort-cpu')) $('sort-cpu').onclick = function () {
          state.sortProcs = 'cpu';
          addClass($('sort-cpu'), 'active');
          removeClass($('sort-ram'), 'active');
          if (state.lastProcs) renderProcs(state.lastProcs);
          else pollProcs();
        };
        if ($('btn-procs-limit')) $('btn-procs-limit').onclick = function () {
          state.procsLimit = state.procsLimit === 15 ? 30 : 15;
          this.textContent = state.procsLimit === 15 ? 'Top 15' : 'Todos 30';
          if (state.lastProcs) renderProcs(state.lastProcs);
          else pollProcs();
        };
        if ($('btn-procs-refresh')) $('btn-procs-refresh').onclick = pollProcs;
        if ($('btn-mixer-refresh')) $('btn-mixer-refresh').onclick = pollControls;
        if ($('btn-ai-refresh')) $('btn-ai-refresh').onclick = pollAiWork;
        if ($('btn-weather-refresh')) $('btn-weather-refresh').onclick = function () {
          pollWeather();
          showToast('Clima atualizado');
        };
        if ($('btn-dc-refresh')) $('btn-dc-refresh').onclick = pollDiscord;
        if ($('dc-mute')) $('dc-mute').onclick = function () { sendDiscordAction('mute'); };
        if ($('dc-deafen')) $('dc-deafen').onclick = function () { sendDiscordAction('deafen'); };
        if ($('dc-inputmode')) $('dc-inputmode').onclick = function () { sendDiscordAction('inputmode'); };
        if ($('dc-leave')) $('dc-leave').onclick = function () { sendDiscordAction('leave'); };
        if ($('dc-tab-chans')) $('dc-tab-chans').onclick = function () { setDiscordSubTab('chans'); };
        if ($('dc-tab-notifs')) $('dc-tab-notifs').onclick = function () { setDiscordSubTab('notifs'); };
        if ($('dc-only-occupied')) $('dc-only-occupied').onclick = function () {
          state.dcOnlyOccupied = !state.dcOnlyOccupied;
          renderDiscord(state.dcLastData, true);
        };
        if ($('dc-only-speaking')) $('dc-only-speaking').onclick = function () {
          state.dcOnlySpeaking = !state.dcOnlySpeaking;
          renderDiscord(state.dcLastData, true);
        };
        if ($('dc-search')) {
          var onDcSearch = function () {
            state.dcSearch = $('dc-search').value || '';
            var clr = $('dc-search-clear');
            if (clr) {
              if (state.dcSearch.length > 0) removeClass(clr, 'hidden');
              else addClass(clr, 'hidden');
            }
            renderDiscord(state.dcLastData, true);
          };
          $('dc-search').oninput = onDcSearch;
          $('dc-search').onkeyup = onDcSearch;
        }
        if ($('dc-search-clear')) $('dc-search-clear').onclick = function () {
          state.dcSearch = '';
          if ($('dc-search')) $('dc-search').value = '';
          addClass(this, 'hidden');
          renderDiscord(state.dcLastData, true);
        };

        // Night dimmer options & Weather sub-view tabs
        var allBtns = document.getElementsByTagName('button');
        for (var b = 0; b < allBtns.length; b++) {
          if (allBtns[b].getAttribute('data-dim') !== null) {
            allBtns[b].onclick = function () {
              applyDimmer(this.getAttribute('data-dim'));
            };
          }
          if (allBtns[b].getAttribute('data-wxtab') !== null) {
            allBtns[b].onclick = function () {
              var tabIdx = parseInt(this.getAttribute('data-wxtab'), 10) || 0;
              for (var k = 0; k < 3; k++) {
                var vEl = $('wx-view-' + k);
                if (vEl) vEl.className = (k === tabIdx) ? 'wx-subview active' : 'wx-subview';
              }
              var tbBtns = document.getElementsByTagName('button');
              for (var m = 0; m < tbBtns.length; m++) {
                var attr = tbBtns[m].getAttribute('data-wxtab');
                if (attr !== null) {
                  tbBtns[m].className = (parseInt(attr, 10) === tabIdx) ? 'wx-subtab active' : 'wx-subtab';
                }
              }
              if (tabIdx === 0 && state.weather && state.weather.hourly) {
                setTimeout(function () {
                  drawHourlyWeatherCanvas(state.weather.hourly, '°' + (state.weather.unit || 'C'));
                }, 30);
              }
            };
          }
        }

        // Approval modal buttons
        $('ap-allow').onclick = function () { sendApproval('allow'); };
        $('ap-deny').onclick = function () { sendApproval('deny'); };

        // Resize / Orientation listeners (with Android 4.2.2 delayed reflow fix)
        window.addEventListener('resize', function () {
          updateLayout();
          if (state.config) renderShortcuts(state.config);
        }, false);
        window.addEventListener('orientationchange', function () {
          updateLayout();
          setTimeout(function () { updateLayout(); if (state.config) renderShortcuts(state.config); }, 250);
          setTimeout(function () { updateLayout(); if (state.config) renderShortcuts(state.config); }, 600);
        }, false);

        setupSwipe();
      }

      // ==================== LIVE PREVIEW BRIDGE (SETTINGS.HTML IFRAME) ====================
      function setupPreviewBridge() {
        if (!state.isPreview) return;
        window.addEventListener('message', function (e) {
          if (e.origin !== window.location.origin || !e.data || e.data.type !== 'phonebox-preview') return;
          state.previewConfig = true;
          if (e.data.orient === 'landscape' || e.data.orient === 'portrait') {
            state.orientMode = e.data.orient;
            updateLayout();
          }
          if (e.data.config) {
            applyConfig(e.data.config);
          }
          var targetIdx = -1;
          if (e.data.pageId) {
            targetIdx = findTabByPageId(e.data.pageId);
          }
          if (targetIdx < 0 && typeof e.data.page === 'number' && e.data.config && e.data.config.pages) {
            var rawP = e.data.config.pages[e.data.page];
            if (rawP) targetIdx = findTabByPageId(rawP.id || ('p' + e.data.page));
          }
          if (targetIdx >= 0 && targetIdx !== state.page) {
            setPage(targetIdx);
          }
        }, false);

        try {
          if (window.parent && window.parent !== window) {
            window.parent.postMessage({ type: 'phonebox-preview-ready' }, window.location.origin);
          }
        } catch (e) { }
      }

      // ==================== BOOT ====================
      function init() {
        state.isPreview = window.location.search.indexOf('preview=1') !== -1;
        state.orientMode = state.isPreview ? 'landscape' : storeGet('orient', 'auto');
        var savedPage = state.isPreview ? 0 : parseInt(storeGet('page', '0'), 10);
        if (isNaN(savedPage) || savedPage < 0 || savedPage >= state.activePages.length) savedPage = 0;

        bindEvents();
        applyDimmer(state.isPreview ? 0 : storeGet('dim', '0'), true);
        updateLayout();
        setPage(savedPage);
        effectsEnabled = storeGet('themeEffects', '1') !== '0';
        $('theme-effects').checked = effectsEnabled;
        selectLocalTheme(state.isPreview ? 'default' : storeGet('localTheme', 'default'));
        if (!coldHackBootPlayed && effectsEnabled && !state.isPreview) {
          if (isHackThemeActive()) {
            coldHackBootPlayed = true;
            playHackBoot();
          } else if (isAnimeThemeActive()) {
            coldHackBootPlayed = true;
            playAnimeEntrance();
          }
        }
        updateClock();
        setupPreviewBridge();

        pollSync(activeViewIdx() === 4);

        setInterval(updateClock, 1000);
        setInterval(function () {
          pollSync(false);
        }, 1500);

        // Try to hide browser address bar on Android 4.2.2
        if (!state.isPreview) {
          setTimeout(function () { try { window.scrollTo(0, 1); } catch (e) { } }, 400);
        }
      }

            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', init, false);
            } else {
              init();
            }
          }) ();

