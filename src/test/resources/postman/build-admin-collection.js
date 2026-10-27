/*
 * Genera una coleccion Postman v2.1.0 a partir de la especificacion QA.
 * Uso: node build-collection.js [runId]
 * Salidas:
 *   AdminCuenta-QA.postman_collection.json
 *   admin-cuenta.local.postman_environment.json
 */
'use strict';

const fs = require('fs');
const path = require('path');

const runId = process.argv[2] || ('r' + new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14));

const folderFiles = [
  '00-admin-setup',
  '05-categories',
  '06-products',
  '07-isolation-identity',
  '09-errors',
  '10-admin-teardown',
];

// Placeholders en nombres/valores: {run} se sustituye por el runId en runtime.
function jsExpr(value) {
  if (typeof value !== 'string') return JSON.stringify(value);
  if (value.includes('{run}')) {
    return value.split('{run}').map(p => JSON.stringify(p)).join(' + __run + ');
  }
  return JSON.stringify(value);
}

function varExpr(name) {
  return `pm.collectionVariables.get(${JSON.stringify(name)})`;
}

function fixture(name) {
  return path.resolve(__dirname, '..', 'fixtures', name);
}

const AUTH_VARS = {
  super: 'tokenSuper',
  adminA: 'tokenAdminA',
  adminB: 'tokenAdminB',
  empA: 'tokenEmpA',
  empB: 'tokenEmpB',
  empA2: 'tokenEmpA2',
  noMem: 'tokenNoMem',
  temp: 'tokenTemp',
  seedAdmin: 'tokenSeedAdmin',
  seedEmp: 'tokenSeedEmp',
};

const PREAMBLE = [
  'const __run = pm.collectionVariables.get("runId");',
  'const __code = pm.response.code;',
  'const __raw = typeof pm.response.text === "function" ? pm.response.text() : "";',
  'let __json = null;',
  'try { __json = pm.response.json(); } catch (e) { __json = null; }',
  'function __get(obj, p) {',
  '  if (p === null || p === undefined || p === "") return obj;',
  '  const parts = String(p).split(".");',
  '  let cur = obj;',
  '  for (const part of parts) {',
  '    if (cur === null || cur === undefined) return undefined;',
  '    cur = (/^\\d+$/.test(part)) ? cur[Number(part)] : cur[part];',
  '  }',
  '  return cur;',
  '}',
  'function __names(arr) { return (Array.isArray(arr) ? arr : []).map(x => x && x.name); }',
  'function __ids(arr) { return (Array.isArray(arr) ? arr : []).map(x => x && x.id && String(x.id)); }',
];

function buildTestScript(req) {
  const lines = PREAMBLE.slice();

  lines.push(`pm.test("status esperado ${req.expect}", function () { pm.expect(__code).to.eql(${req.expect}); });`);

  const checks = req.checks || [];
  for (const c of checks) {
    if (c.eq !== undefined) {
      const eqPath = c.path || c.eq;
      const actual = `__get(__json, ${JSON.stringify(eqPath)})`;
      lines.push(`pm.test(${JSON.stringify('campo ' + (eqPath || 'body') + ' === ' + JSON.stringify(c.value))}, function () { pm.expect(${actual}).to.eql(${jsExpr(c.value)}); });`);
    }
    if (c.eqVar !== undefined) {
      const actual = `__get(__json, ${JSON.stringify(c.eqVar[0])})`;
      lines.push(`pm.test(${JSON.stringify('campo ' + c.eqVar[0] + ' === variable ' + c.eqVar[1])}, function () { pm.expect(String(${actual})).to.eql(String(${varExpr(c.eqVar[1])})); });`);
    }
    if (c.exists !== undefined) {
      lines.push(`pm.test(${JSON.stringify('existe ' + c.exists)}, function () { const v = __get(__json, ${JSON.stringify(c.exists)}); pm.expect(v !== undefined && v !== null).to.eql(true); });`);
    }
    if (c.null !== undefined) {
      lines.push(`pm.test(${JSON.stringify(c.null + ' es null')}, function () { pm.expect(__get(__json, ${JSON.stringify(c.null)})).to.eql(null); });`);
    }
    if (c.regex !== undefined) {
      lines.push(`pm.test(${JSON.stringify('formato de ' + c.path)}, function () { pm.expect(String(__get(__json, ${JSON.stringify(c.path)}))).to.match(new RegExp(${JSON.stringify(c.regex)})); });`);
    }
    if (c.lengthAtLeast !== undefined) {
      lines.push(`pm.test(${JSON.stringify('length >= ' + c.lengthAtLeast)}, function () { pm.expect(Array.isArray(__json)).to.eql(true); pm.expect(__json.length).to.be.at.least(${c.lengthAtLeast}); });`);
    }
    if (c.lengthEq !== undefined) {
      lines.push(`pm.test(${JSON.stringify('length === ' + c.lengthEq)}, function () { pm.expect(Array.isArray(__json)).to.eql(true); pm.expect(__json.length).to.eql(${c.lengthEq}); });`);
    }
    if (c.empty === true) {
      lines.push(`pm.test("array vacio", function () { pm.expect(Array.isArray(__json)).to.eql(true); pm.expect(__json.length).to.eql(0); });`);
    }
    if (c.sortedBy !== undefined) {
      lines.push(`pm.test(${JSON.stringify('ordenado por ' + c.sortedBy)}, function () {`);
      lines.push('  pm.expect(Array.isArray(__json)).to.eql(true);');
      lines.push('  for (let i = 1; i < __json.length; i++) {');
      lines.push(`    pm.expect(String(__json[i-1][${JSON.stringify(c.sortedBy)}]).localeCompare(String(__json[i][${JSON.stringify(c.sortedBy)}]))).to.be.at.most(0);`);
      lines.push('  }');
      lines.push('});');
    }
    if (c.namesInclude !== undefined) {
      lines.push(`pm.test(${JSON.stringify('nombres incluyen ' + c.namesInclude.join(', '))}, function () {`);
      lines.push('  const names = __names(__json);');
      for (const n of c.namesInclude) lines.push(`  pm.expect(names).to.include(${jsExpr(n)});`);
      lines.push('});');
    }
    if (c.namesExclude !== undefined) {
      lines.push(`pm.test(${JSON.stringify('nombres excluyen ' + c.namesExclude.join(', '))}, function () {`);
      lines.push('  const names = __names(__json);');
      for (const n of c.namesExclude) lines.push(`  pm.expect(names).to.not.include(${jsExpr(n)});`);
      lines.push('});');
    }
    if (c.namesAllContain !== undefined) {
      lines.push(`pm.test(${JSON.stringify('todos los nombres contienen ' + c.namesAllContain)}, function () {`);
      lines.push('  const names = __names(__json);');
      lines.push('  pm.expect(names.length).to.be.at.least(1);');
      lines.push(`  for (const n of names) { pm.expect(String(n).toLowerCase()).to.include(String(${jsExpr(c.namesAllContain)}).toLowerCase()); }`);
      lines.push('});');
    }
    if (c.allEq !== undefined) {
      const field = c.allEq[0];
      lines.push(`pm.test(${JSON.stringify('todos ' + field + ' iguales')}, function () {`);
      lines.push('  pm.expect(Array.isArray(__json)).to.eql(true);');
      lines.push('  pm.expect(__json.length).to.be.at.least(1);');
      if (c.allEqVar) {
        lines.push(`  for (const it of __json) { pm.expect(String(it[${JSON.stringify(field)}])).to.eql(String(${varExpr(c.allEqVar)})); }`);
      } else {
        lines.push(`  for (const it of __json) { pm.expect(String(it[${JSON.stringify(field)}])).to.eql(String(${jsExpr(c.allEq[1])})); }`);
      }
      lines.push('});');
    }
    if (c.fieldIncludes !== undefined) {
      const [field, values] = c.fieldIncludes;
      lines.push(`pm.test(${JSON.stringify('campo ' + field + ' incluye ' + values.join(', '))}, function () {`);
      lines.push(`  const vals = (Array.isArray(__json) ? __json : []).map(x => x && x[${JSON.stringify(field)}]);`);
      for (const val of values) lines.push(`  pm.expect(vals).to.include(${jsExpr(val)});`);
      lines.push('});');
    }
    if (c.fieldExcludes !== undefined) {
      const [field, values] = c.fieldExcludes;
      lines.push(`pm.test(${JSON.stringify('campo ' + field + ' excluye ' + values.join(', '))}, function () {`);
      lines.push(`  const vals = (Array.isArray(__json) ? __json : []).map(x => x && x[${JSON.stringify(field)}]);`);
      for (const val of values) lines.push(`  pm.expect(vals).to.not.include(${jsExpr(val)});`);
      lines.push('});');
    }
    if (c.findEq !== undefined) {
      const [arrPath, keyField, varName, checkKey, checkVal] = c.findEq;
      lines.push(`pm.test(${JSON.stringify('elemento ' + keyField + ' === ' + varName + (checkKey ? ' y ' + checkKey + ' === ' + checkVal : ''))}, function () {`);
      lines.push(`  const arr = __get(__json, ${JSON.stringify(arrPath)}) || [];`);
      lines.push(`  const item = arr.find(x => x && String(x[${JSON.stringify(keyField)}]) === String(${varExpr(varName)}));`);
      lines.push('  pm.expect(item).to.not.eql(undefined);');
      if (checkKey) lines.push(`  pm.expect(item[${JSON.stringify(checkKey)}]).to.eql(${jsExpr(checkVal)});`);
      lines.push('});');
    }
    if (c.findAbsent !== undefined) {
      const [arrPath, keyField, varName] = c.findAbsent;
      lines.push(`pm.test(${JSON.stringify('no existe elemento ' + keyField + ' === ' + varName)}, function () {`);
      lines.push(`  const arr = __get(__json, ${JSON.stringify(arrPath)}) || [];`);
      lines.push(`  const item = arr.find(x => x && String(x[${JSON.stringify(keyField)}]) === String(${varExpr(varName)}));`);
      lines.push('  pm.expect(item).to.eql(undefined);');
      lines.push('});');
    }
    if (c.idsIncludeVar !== undefined) {
      lines.push(`pm.test(${JSON.stringify('ids incluyen ' + c.idsIncludeVar)}, function () { const ids = __ids(__json); pm.expect(ids).to.include(String(${varExpr(c.idsIncludeVar)})); });`);
    }
    if (c.idsExcludeVar !== undefined) {
      lines.push(`pm.test(${JSON.stringify('ids excluyen ' + c.idsExcludeVar)}, function () { const ids = __ids(__json); pm.expect(ids).to.not.include(String(${varExpr(c.idsExcludeVar)})); });`);
    }
    if (c.idEqualsVar !== undefined) {
      lines.push(`pm.test(${JSON.stringify('id === variable ' + c.idEqualsVar[1])}, function () { pm.expect(String(__get(__json, ${JSON.stringify(c.idEqualsVar[0])}))).to.eql(String(${varExpr(c.idEqualsVar[1])})); });`);
    }
    if (c.keysAbsent !== undefined) {
      lines.push(`pm.test(${JSON.stringify('sin campos ' + c.keysAbsent.join(', '))}, function () {`);
      lines.push('  const items = Array.isArray(__json) ? __json : [__json];');
      for (const k of c.keysAbsent) {
        lines.push(`  for (const it of items) { if (it && typeof it === "object") pm.expect(Object.prototype.hasOwnProperty.call(it, ${JSON.stringify(k)})).to.eql(false); }`);
      }
      lines.push('});');
    }
    if (c.fixturesInactive !== undefined) {
      lines.push(`pm.test(${JSON.stringify('fixtures desactivados: ' + c.fixturesInactive.join(', '))}, function () {`);
      lines.push('  pm.expect(Array.isArray(__json)).to.eql(true);');
      lines.push('  const byId = {}; for (const it of __json) { byId[String(it.id)] = it; }');
      for (const v of c.fixturesInactive) {
        lines.push(`  { const id = String(${varExpr(v)}); pm.expect(byId[id], 'presente ' + ${JSON.stringify(v)}).to.not.eql(undefined); pm.expect(byId[id] && byId[id].active, 'activo ' + ${JSON.stringify(v)}).to.eql(false); }`);
      }
      lines.push('});');
    }    if (c.notContains !== undefined) {
      lines.push(`pm.test(${JSON.stringify('body no contiene ' + c.notContains.join(', '))}, function () {`);
      lines.push('  const body = String(__raw).toLowerCase();');
      for (const k of c.notContains) lines.push(`  pm.expect(body).to.not.include(${JSON.stringify(k.toLowerCase())});`);
      lines.push('});');
    }
  }

  const save = req.save || {};
  for (const [variable, jsonPath] of Object.entries(save)) {
    lines.push(`{ const v = __get(__json, ${JSON.stringify(jsonPath)}); if (v !== undefined && v !== null) pm.collectionVariables.set(${JSON.stringify(variable)}, String(v)); }`);
  }

  return lines;
}

function buildRequest(req) {
  const headers = [];
  if (req.auth && req.auth !== 'none') {
    if (req.auth === 'invalid') {
      headers.push({ key: 'Authorization', value: 'Bearer not.a.valid.token' });
    } else if (req.auth === 'empty') {
      headers.push({ key: 'Authorization', value: '' });
    } else {
      const v = AUTH_VARS[req.auth];
      if (!v) throw new Error('auth desconocido: ' + req.auth);
      headers.push({ key: 'Authorization', value: 'Bearer {{' + v + '}}' });
    }
  }

  let body;
  if (req.form) {
    body = {
      mode: 'formdata',
      formdata: [{ key: 'file', type: 'file', src: fixture(req.form) }],
    };
  } else if (req.formOther) {
    body = {
      mode: 'formdata',
      formdata: [{ key: 'other', type: 'text', value: 'sin archivo' }],
    };
  } else if (req.raw !== undefined) {
    body = { mode: 'raw', raw: req.raw, options: { raw: { language: 'text' } } };
    headers.push({ key: 'Content-Type', value: req.contentType || 'application/json' });
  } else if (req.body !== undefined) {
    body = { mode: 'raw', raw: JSON.stringify(req.body), options: { raw: { language: 'json' } } };
    headers.push({ key: 'Content-Type', value: 'application/json' });
  }

  if (req.headers) {
    for (const [k, v] of Object.entries(req.headers)) headers.push({ key: k, value: v });
  }

  return {
    name: req.name,
    request: {
      method: req.method,
      header: headers,
      url: '{{baseUrl}}' + req.path,
      ...(body ? { body } : {}),
    },
    event: [{ listen: 'test', script: { type: 'text/javascript', exec: buildTestScript(req) } }],
  };
}

const collection = {
  info: {
    _postman_id: '9a6b1f42-6d6f-4a1e-9d0b-6d2f0f2f7a11',
    name: 'Mi Negocio - Catalogo cuenta real admin@ (RF-19..23, RF-36..38)',
    description:
      'Suite caja negra contra la app en {{baseUrl}}. Datos efimeros con runId. ' +
      'Corrida con la cuenta real admin@minegocio.com sobre Tienda Central. ' +
      'QA-B y QA-C son desechables (aislamiento y estados). ' +
      'RF-24..35 (pedidos/ventas) estan fuera de alcance: no implementados.',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
  },
  item: [],
  variable: [{ key: 'runId', value: runId }, { key: 'storeA', value: '44ac42b1-59c0-4929-88ea-9ca9faac29fd' }],
};

for (const file of folderFiles) {
  const spec = require('./spec/' + file);
  collection.item.push({ name: spec.name, item: spec.cases.map(buildRequest) });
}

const environment = {
  id: 'a5f3a7b0-6c1e-4d2a-8f5e-3b9c7d1e2f40',
  name: 'Mi Negocio - Admin real (local)',
  values: [
    { key: 'baseUrl', value: 'http://localhost:9080', type: 'default', enabled: true },
    { key: 'runId', value: runId, type: 'default', enabled: true },
    { key: 'seedSuperEmail', value: 'superadmin@minegocio.com', type: 'default', enabled: true },
    { key: 'seedPassword', value: 'Admin123*', type: 'default', enabled: true },
    { key: 'qaPassword', value: 'Qa12345*', type: 'default', enabled: true },
    { key: 'adminEmail', value: 'admin@minegocio.com', type: 'default', enabled: true },
    { key: 'adminPassword', value: 'Admin123*', type: 'default', enabled: true },
    { key: 'centralStoreId', value: '44ac42b1-59c0-4929-88ea-9ca9faac29fd', type: 'default', enabled: true },
  ],
  _postman_variable_scope: 'environment',
};

const outDir = __dirname;
fs.writeFileSync(path.join(outDir, 'AdminCuenta-QA.postman_collection.json'), JSON.stringify(collection, null, 2));
fs.writeFileSync(path.join(outDir, 'admin-cuenta.local.postman_environment.json'), JSON.stringify(environment, null, 2));

const total = collection.item.reduce((n, f) => n + f.item.length, 0);
console.log('runId=' + runId);
console.log('requests=' + total);
console.log('collection=' + path.join(outDir, 'AdminCuenta-QA.postman_collection.json'));
console.log('environment=' + path.join(outDir, 'admin-cuenta.local.postman_environment.json'));