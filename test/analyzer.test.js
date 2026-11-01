'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const { inspectText } = require('../lib/analyzer');

test('texto normal não gera alerta', () => assert.deepEqual(inspectText('Oi, tudo bem?'), []));
test('detecta pedido de senha/código', () => assert.ok(inspectText('Me envie seu código de verificação').includes('pedido de código/senha')));
test('detecta pressão envolvendo Pix sem classificar Pix sozinho', () => {
  assert.deepEqual(inspectText('Fiz um Pix para você'), []);
  assert.ok(inspectText('Faça o Pix agora urgente').includes('pedido Pix com urgência'));
});
test('detecta encurtador e HTTP', () => {
  const flags = inspectText('Acesse http://bit.ly/abc');
  assert.ok(flags.includes('link sem HTTPS'));
  assert.ok(flags.includes('encurtador de link'));
});
test('detecta domínio punycode e IP literal', () => {
  assert.ok(inspectText('https://xn--exemplo-9za.com').includes('domínio incomum'));
  assert.ok(inspectText('https://192.0.2.1/login').includes('domínio incomum'));
});
test('não duplica alertas', () => assert.equal(inspectText('http://bit.ly/a http://bit.ly/b').filter(x => x === 'link sem HTTPS').length, 1));
