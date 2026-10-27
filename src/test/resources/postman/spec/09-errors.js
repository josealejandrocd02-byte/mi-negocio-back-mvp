'use strict';

// Regresion del manejo de errores: los errores de framework NO deben enmascararse como 401.
module.exports = {
  name: '09 Errores de framework',
  cases: [
    {
      name: 'UUID invalido en path -> 400 (no 401)',
      method: 'GET', path: '/api/stores/{{storeA}}/products/no-es-uuid', auth: 'adminA', expect: 400,
    },
    {
      name: 'UUID invalido en query -> 400 (no 401)',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId=no-es-uuid', auth: 'adminA', expect: 400,
    },
    {
      name: 'DELETE producto no soportado -> 405 (no 401)',
      method: 'DELETE', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA', expect: 405,
    },
    {
      name: 'DELETE categoria no soportado -> 405 (no 401)',
      method: 'DELETE', path: '/api/stores/{{storeA}}/categories/{{catAa}}', auth: 'adminA', expect: 405,
    },
    {
      name: 'JSON malformado en categoria -> 400 (no 401)',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      raw: '{"name":', contentType: 'application/json', expect: 400,
    },
    {
      name: 'JSON malformado en producto -> 400 (no 401)',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      raw: '{"name":', contentType: 'application/json', expect: 400,
    },
    {
      name: 'JSON malformado en login -> 400 (no 401)',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      raw: '{"email":', contentType: 'application/json', expect: 400,
    },
    {
      name: 'Ruta inexistente con token -> 404 (no 401)',
      method: 'GET', path: '/api/ruta-inexistente', auth: 'super', expect: 404,
    },
    {
      name: 'Metodo PATCH en login -> 405 (no 401)',
      method: 'PATCH', path: '/api/auth/login', auth: 'none', expect: 405,
    },
    {
      name: 'Content-Type text/plain en login -> 415',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      raw: 'email=x&password=y', contentType: 'text/plain', expect: 415,
    },
    {
      name: 'Content-Type text/plain en categoria -> 415',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      raw: 'name=x', contentType: 'text/plain', expect: 415,
    },
    {
      name: 'status de sucursal invalido -> 400',
      method: 'GET', path: '/api/admin/stores?status=INVALID', auth: 'super', expect: 400,
    },
    {
      name: 'UUID invalido en admin stores -> 400',
      method: 'GET', path: '/api/admin/stores/no-es-uuid', auth: 'super', expect: 400,
    },
    {
      name: 'UUID invalido en admin users -> 400',
      method: 'GET', path: '/api/admin/users/no-es-uuid', auth: 'super', expect: 400,
    },
    {
      name: 'UUID invalido como categoryId en body -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'BadUuid-{{runId}}', categoryId: 'no-es-uuid', price: 1 }, expect: 400,
    },
    {
      name: 'POST producto sin body -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA', expect: 400,
    },
    {
      name: 'Ruta inexistente sin token -> 401 (protegida)',
      method: 'GET', path: '/api/ruta-inexistente', auth: 'none', expect: 401,
    },
    {
      name: 'GET /api/auth/login no permitido -> 405',
      method: 'GET', path: '/api/auth/login', auth: 'none', expect: 405,
    },
  ],
};