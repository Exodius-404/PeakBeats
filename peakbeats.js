// Mobile nav
const navToggle = document.querySelector('.nav-toggle');
const navLinks = document.querySelector('.nav-links');
const burger = document.querySelector('.burger');

if (navToggle) {
  navToggle.addEventListener('click', () => {
    const expanded = navToggle.getAttribute('aria-expanded') === 'true';
    navToggle.setAttribute('aria-expanded', String(!expanded));
    navLinks.classList.toggle('open');
    burger.classList.toggle('open');
  });
}

// Smooth scroll for in-page links
document.querySelectorAll('a[href^="#"]').forEach(a => {
  a.addEventListener('click', e => {
    const id = a.getAttribute('href').slice(1);
    const el = document.getElementById(id);
    if (el) {
      e.preventDefault();
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      navLinks?.classList.remove('open');
      burger?.classList.remove('open');
      navToggle?.setAttribute('aria-expanded', 'false');
    }
  });
});

// Pakete -> Vorauswahl im Formular
document.querySelectorAll('[data-package]').forEach(btn => {
  btn.addEventListener('click', () => {
    const select = document.querySelector('select[name="package"]');
    if (select) select.value = btn.dataset.package;
  });
});

// Produktfilter
const filterButtons = document.querySelectorAll('.filter-btn');
const products = document.querySelectorAll('.product-card');
filterButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    filterButtons.forEach(b => b.classList.remove('is-active'));
    btn.classList.add('is-active');
    const f = btn.dataset.filter;
    products.forEach(card => {
      const show = f === 'all' || card.dataset.category === f;
      card.style.display = show ? '' : 'none';
    });
  });
});

// Preis-Schätzung inkl. optionaler Lieferung/Techniker
const estimateEl = document.getElementById('estimateValue');
const form = document.querySelector('.form');
const peopleEl = form?.querySelector('input[name="people"]');
const packageEl = form?.querySelector('select[name="package"]');
const deliveryEl = form?.querySelector('select[name="delivery"]');
const techEl = form?.querySelector('select[name="tech"]');

function deliveryPrice(val){
  if(val === 'zone1') return 39;
  if(val === 'zone2') return 59;
  if(val === 'zone3') return 89;
  return 0;
}
function techPrice(val){
  if(val === 'setup') return 69;
  if(val === 'full') return 199;
  return 0;
}

function basePrice(pkg){
  if (pkg === 'Small Gig') return 149;
  if (pkg === 'Party Pro') return 299;
  if (pkg === 'Open Air') return 749;
  return 0;
}

function calcEstimate() {
  if (!estimateEl) return;
  const people = Number(peopleEl?.value || 0);
  const pkg = packageEl?.value || '';
  let total = basePrice(pkg);

  // kleine Anpassung nach Personen
  const mod = Math.max(0, Math.ceil((people - 80) / 50)) * 45;
  if(total) total += mod;

  // optionale Add-ons
  total += deliveryPrice(deliveryEl?.value);
  total += techPrice(techEl?.value);

  estimateEl.textContent = total ? `${total.toFixed(0)}€ / Tag` : '–';
}
['input','change'].forEach(evt => {
  peopleEl?.addEventListener(evt, calcEstimate);
  packageEl?.addEventListener(evt, calcEstimate);
  deliveryEl?.addEventListener(evt, calcEstimate);
  techEl?.addEventListener(evt, calcEstimate);
});

// Form Validation (front-end)
form?.addEventListener('submit', e => {
  e.preventDefault();
  let ok = true;
  form.querySelectorAll('.error').forEach(el => el.textContent = '');
  const required = [
    ['name', 'Bitte Name angeben.'],
    ['email', 'Bitte gültige E-Mail angeben.'],
    ['date', 'Bitte Datum wählen.'],
    ['people', 'Bitte Personenanzahl eingeben.'],
    ['liability', 'Bitte Zustimmung zur Selbstbeteiligung bestätigen.']
  ];

  required.forEach(([name, msg]) => {
    const field = form.querySelector(`[name="${name}"]`);
    if ((field?.type === 'checkbox' && !field.checked) || !field?.value) {
      ok = false;
      field?.closest('.field, .policy-box')?.querySelector('.error')?.append(msg);
    }
  });

  // simple email check
  const email = form.querySelector('[name="email"]')?.value || '';
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ok = false;
    form.querySelector('[name="email"]').closest('.field')?.querySelector('.error')?.append(' Format ungültig.');
  }

  if (!ok) return;

  // Simulierter Versand
  form.querySelector('.form-success').hidden = false;
  form.reset();
  calcEstimate();
});

// year in footer
document.getElementById('year').textContent = new Date().getFullYear();

/* ===== Admin Backdoor (clientseitig) =====
 * Zweck: Produkte schnell ausgrauen/sperren und Preise ändern
 * Öffnen: ALT + A oder URL-Parameter ?admin=peakbeats
 * Sicherheit: einfache Passwortabfrage
*/

const ADMIN_PARAM = 'peakbeats';
const ADMIN_STORAGE_KEY = 'pb_blockedSkus';
const PRICE_STORAGE_KEY = 'pb_prices';

const adminDialog = document.getElementById('adminPanel');
const adminList = adminDialog?.querySelector('.admin-list');
const adminResetBtn = document.getElementById('adminReset');

// Passwort-Hash für "PeakBeats2025!" (SHA-256, hex)
const ADMIN_HASH = '64a3b461d4f1f12bcf63c1274b6822049a4561521232ad470e0c4a5e93f27547';

async function sha256(str){
  const buf = new TextEncoder().encode(str);
  const hashBuffer = await crypto.subtle.digest('SHA-256', buf);
  return Array.from(new Uint8Array(hashBuffer))
    .map(b => b.toString(16).padStart(2,'0'))
    .join('');
}

// ===== Blocked/ausgegraute Produkte =====
function getBlocked(){
  try { return JSON.parse(localStorage.getItem(ADMIN_STORAGE_KEY) || '[]'); }
  catch { return []; }
}
function setBlocked(arr){
  localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(arr));
}
function applyBlockedUI(){
  const blocked = new Set(getBlocked());
  document.querySelectorAll('.product-card').forEach(card => {
    const sku = card.dataset.sku;
    card.classList.toggle('is-blocked', blocked.has(sku));
    if(blocked.has(sku)){
      card.setAttribute('aria-disabled','true');
      card.style.pointerEvents = 'none';
    } else {
      card.removeAttribute('aria-disabled');
      card.style.pointerEvents = '';
    }
  });
}

// ===== Preise speichern/ändern =====
function getPrices(){
  try { return JSON.parse(localStorage.getItem(PRICE_STORAGE_KEY) || '{}'); }
  catch { return {}; }
}
function setPrices(prices){
  localStorage.setItem(PRICE_STORAGE_KEY, JSON.stringify(prices));
}
function applyPricesUI(){
  const prices = getPrices();
  document.querySelectorAll('.product-card').forEach(card => {
    const sku = card.dataset.sku;
    const priceEl = card.querySelector('.price');
    if(priceEl && prices[sku] != null){
      priceEl.textContent = prices[sku] + '€ / Tag';
    }
  });
}

// ===== Admin-Panel aufbauen =====
function buildAdminList(){
  if(!adminList) return;
  adminList.innerHTML = '';
  const blocked = new Set(getBlocked());
  const prices = getPrices();

  document.querySelectorAll('.product-card').forEach(card => {
    const sku = card.dataset.sku;
    const title = card.querySelector('h3')?.textContent?.trim() || sku;
    const row = document.createElement('div');
    row.className = 'admin-row';
    row.innerHTML = `
      <div>
        <div><strong>${title}</strong></div>
        <div class="sku">SKU: ${sku}</div>
      </div>
      <label>
        Sperren <input type="checkbox" class="switch" ${blocked.has(sku)?'checked':''} aria-label="${title} sperren" />
      </label>
      <label>
        Preis <input type="number" class="price-input" value="${prices[sku] ?? ''}" /> €
      </label>
    `;
    // Sperren toggle
    const toggle = row.querySelector('input.switch');
    toggle.addEventListener('change', () => {
      const current = new Set(getBlocked());
      if(toggle.checked){ current.add(sku); } else { current.delete(sku); }
      setBlocked([...current]);
      applyBlockedUI();
    });
    // Preis ändern
    const priceInput = row.querySelector('.price-input');
    priceInput.addEventListener('input', () => {
      const p = getPrices();
      p[sku] = Number(priceInput.value) || 0;
      setPrices(p);
      applyPricesUI();
    });

    adminList.appendChild(row);
  });
}

// Admin öffnen
async function openAdmin(){
  const pw = prompt('Admin-Passwort eingeben:');
  if(!pw) return;
  const hash = await sha256(pw);
  if(hash !== ADMIN_HASH){
    alert('Falsches Passwort!');
    return;
  }
  buildAdminList();
  adminDialog?.showModal();
}

// ALT + A shortcut
document.addEventListener('keydown', (e) => {
  if(e.altKey && (e.key === 'a' || e.key === 'A')){
    openAdmin();
  }
});

// URL Parameter ?admin=peakbeats
(function(){
  const params = new URLSearchParams(location.search);
  if(params.get('admin') === ADMIN_PARAM){ openAdmin(); }
})();

// Alle Sperren zurücksetzen
adminResetBtn?.addEventListener('click', () => {
  if(confirm('Alle Sperren zurücksetzen?')){
    localStorage.removeItem(ADMIN_STORAGE_KEY);
    localStorage.removeItem(PRICE_STORAGE_KEY);
    applyBlockedUI();
    applyPricesUI();
    buildAdminList();
  }
});

// Beim Laden aktuelle Sperren & Preise anwenden
applyBlockedUI();
applyPricesUI();
