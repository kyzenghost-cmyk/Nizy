'use strict';
const urls = text => String(text || '').match(/https?:\/\/[^\s<>()]+/gi) || [];
function inspectText(text) {
  const s = String(text || '').toLowerCase();
  const flags = [];
  if (/(c[oó]digo de verifica[cç][aã]o|senha|password|token|c[oó]digo de 6 d[ií]gitos)/i.test(s)) flags.push('pedido de código/senha');
  if (/(pix).{0,70}(urgente|agora|imediat|devolv|pr[eê]mio)/i.test(s) || /(urgente|agora).{0,70}pix/i.test(s)) flags.push('pedido Pix com urgência');
  if (/(ganhou|pr[eê]mio|sorteio).{0,80}(clique|link|resgate)/i.test(s)) flags.push('possível isca de prêmio');
  for (const x of urls(text)) {
    try {
      const u = new URL(x), h = u.hostname.toLowerCase();
      if (u.protocol === 'http:') flags.push('link sem HTTPS');
      if (/^(bit\.ly|tinyurl\.com|t\.co|cutt\.ly|is\.gd|rb\.gy)$/i.test(h)) flags.push('encurtador de link');
      if (h.startsWith('xn--') || /^\d{1,3}(\.\d{1,3}){3}$/.test(h)) flags.push('domínio incomum');
    } catch { flags.push('URL malformada'); }
  }
  return [...new Set(flags)];
}
module.exports = { inspectText };