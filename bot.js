'use strict';
require('dotenv').config();
const fs=require('node:fs');
const path=require('node:path');
const pino=require('pino');
const TelegramBot=require('node-telegram-bot-api');
const {default:makeWASocket,useMultiFileAuthState,DisconnectReason}=require('@whiskeysockets/baileys');

const TOKEN=process.env.BOT_TOKEN;
const ADMIN=String(process.env.TELEGRAM_ADMIN_ID||'');
if(!TOKEN||!ADMIN){console.error('Configure BOT_TOKEN e TELEGRAM_ADMIN_ID no .env');process.exit(1);}
const ROOT=__dirname, AUTH=path.join(ROOT,'auth','whatsapp'), DATA=path.join(ROOT,'data');
fs.mkdirSync(AUTH,{recursive:true});fs.mkdirSync(DATA,{recursive:true});
const files={trusted:path.join(DATA,'trusted.json'),blocked:path.join(DATA,'blocked.json'),incidents:path.join(DATA,'incidents.json'),state:path.join(DATA,'state.json')};
function read(file,fallback){try{return JSON.parse(fs.readFileSync(file,'utf8'));}catch{return fallback;}}
function write(file,value){const tmp=file+'.tmp';fs.writeFileSync(tmp,JSON.stringify(value,null,2));fs.renameSync(tmp,file);}
let trusted=read(files.trusted,[]),blocked=read(files.blocked,[]),incidents=read(files.incidents,[]),state=read(files.state,{paused:false,connection:'starting'});
let sock=null,connecting=false;
const tg=new TelegramBot(TOKEN,{polling:true});
const admin=String(ADMIN);
const send=async text=>{try{await tg.sendMessage(admin,text,{disable_web_page_preview:true});}catch(e){console.error('Telegram:',e.message);}};
const numberOf=id=>String(id||'').split('@')[0].split(':')[0].replace(/\D/g,'');
function incident(kind,from,detail){const item={at:new Date().toISOString(),kind,from:numberOf(from),detail:String(detail||'').slice(0,500)};incidents.unshift(item);incidents=incidents.slice(0,300);write(files.incidents,incidents);send('🛡 WA-GUARD\n'+kind+'\nNúmero: '+item.from+'\n'+item.detail);}
const {inspectText}=require('./lib/analyzer');
function textOf(m){const x=m.message||{};return x.conversation||x.extendedTextMessage?.text||x.imageMessage?.caption||x.videoMessage?.caption||x.documentMessage?.caption||'';}
async function connect(){if(connecting)return;connecting=true;try{const {state:auth,saveCreds}=await useMultiFileAuthState(AUTH);sock=makeWASocket({auth,logger:pino({level:'silent'}),printQRInTerminal:true,browser:['WA-GUARD','Chrome','1.0.0']});sock.ev.on('creds.update',saveCreds);sock.ev.on('connection.update',async u=>{if(u.connection==='open'){state.connection='online';write(files.state,state);await send('✅ WhatsApp conectado. WA-GUARD ativo.');}if(u.connection==='close'){state.connection='offline';write(files.state,state);const code=u.lastDisconnect?.error?.output?.statusCode;if(code!==DisconnectReason.loggedOut){setTimeout(connect,5000);}else await send('⚠️ Sessão desconectada pelo WhatsApp. Não apague auth/whatsapp; será necessário parear novamente.');}});sock.ev.on('messages.upsert',async({messages,type})=>{if(type!=='notify')return;for(const m of messages){if(!m.message||m.key.fromMe||m.key.remoteJid==='status@broadcast')continue;const jid=m.key.remoteJid,from=numberOf(jid);if(jid.endsWith('@g.us'))continue;if(blocked.includes(from)){if(!state.paused){try{await sock.updateBlockStatus(jid,'block');}catch(e){incident('Falha ao bloquear',jid,e.message);}}continue;}const body=textOf(m),flags=inspectText(body);if(!trusted.includes(from))incident('Contato desconhecido',jid,body||'Mensagem recebida; conteúdo não textual.');if(flags.length)incident('Possível golpe/link suspeito',jid,flags.join(', ')+' | '+body);const file=m.message.documentMessage||m.message.imageMessage||m.message.videoMessage||m.message.audioMessage;if(file){const name=file.fileName||file.mimetype||'anexo sem nome';const ext=String(file.fileName||'').toLowerCase();if(/\.(apk|exe|bat|cmd|scr|js|vbs|ps1|msi|jar|com)$/i.test(ext))incident('Anexo potencialmente perigoso',jid,'Metadados: '+name+' (não baixado nem aberto)');}}});}catch(e){console.error(e);state.connection='error';write(files.state,state);setTimeout(connect,8000);}finally{connecting=false;}}
function authorized(msg){return String(msg.chat.id)===admin;}
tg.onText(/^\/(status|pause|resume|incidents)(?:@\w+)?$/,async msg=>{if(!authorized(msg))return;const cmd=msg.text.split(/[ @]/)[0].slice(1);if(cmd==='status')return tg.sendMessage(admin,'WA-GUARD\nWhatsApp: '+state.connection+'\nModo emergência: '+(state.paused?'PAUSADO':'normal')+'\nContatos confiáveis: '+trusted.length+'\nBloqueados manualmente: '+blocked.length+'\nIncidentes: '+incidents.length);if(cmd==='pause'){state.paused=true;write(files.state,state);return send('⏸ Monitoramento continua; ações automáticas pausadas.');}if(cmd==='resume'){state.paused=false;write(files.state,state);return send('▶️ Ações automáticas retomadas.');}if(cmd==='incidents')return tg.sendMessage(admin,incidents.slice(0,15).map(x=>x.at+' | '+x.kind+' | '+x.from+'\n'+x.detail).join('\n\n')||'Nenhum incidente.');});
tg.onText(/^\/(trust|block)\s+(\+?[0-9]{8,15})$/,async(msg,match)=>{if(!authorized(msg))return;const cmd=match[1],n=numberOf(match[2]);if(cmd==='trust'){trusted=[...new Set([...trusted,n])];blocked=blocked.filter(x=>x!==n);write(files.trusted,trusted);write(files.blocked,blocked);return send('Contato confiável: '+n);}blocked=[...new Set([...blocked,n])];trusted=trusted.filter(x=>x!==n);write(files.blocked,blocked);write(files.trusted,trusted);if(!state.paused&&sock){try{await sock.updateBlockStatus(n+'@s.whatsapp.net','block');}catch(e){return send('Salvo na lista, mas o bloqueio no WhatsApp falhou: '+e.message);}}return send('Número adicionado à lista de bloqueio: '+n);});
tg.on('polling_error',e=>console.error('Telegram polling:',e.message));
process.on('SIGINT',()=>{state.connection='stopped';write(files.state,state);tg.stopPolling();process.exit(0);});
process.on('SIGTERM',()=>{state.connection='stopped';write(files.state,state);tg.stopPolling();process.exit(0);});
connect();
