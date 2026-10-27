'use strict';

// Teardown de la corrida con la cuenta real admin@minegocio.com.
// - Tienda Central NUNCA se suspende ni se cierra: solo se desactiva el catalogo QA.
// - No se borra fisicamente ningun producto/categoria (RF-21): quedan inactivos por runId.
// - QA-B y QA-C son desechables: se cierran y sus usuarios QA se bloquean.
module.exports = {
  name: '10 Admin teardown (cuenta real + sucursales QA)',
  cases: [
    // ---------- QA-C: asegurar estado activo antes de limpiar ----------
    {
      name: 'QA teardown: reactivar QA-C si quedo inactiva',
      method: 'PATCH', path: '/api/admin/stores/{{storeC}}/status', auth: 'super',
      body: { status: 'ACTIVE' }, expect: 200,
      checks: [{ eq: 'store.status', value: 'ACTIVE' }],
    },
    // ---------- Tienda Central: desactivar catalogo QA ----------
    { name: 'QA teardown: desactivar Alfa', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar Alfajor', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfajor}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar Beta', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodBeta}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar Inactivo', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodInactive}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar ProdCatInactiva', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodCatInactive}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar StockIgnorado', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodStock}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar TEMPUPDATE', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodTemp}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar categoria A-Alfa', method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catAa}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar categoria M-BETA', method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catM}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar categoria Z-Zeta', method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catZ}}/active', auth: 'adminA', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    // ---------- QA-B: desactivar catalogo QA ----------
    { name: 'QA teardown: desactivar producto B', method: 'PATCH', path: '/api/stores/{{storeB}}/products/{{prodB}}/active', auth: 'adminB', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar producto B2', method: 'PATCH', path: '/api/stores/{{storeB}}/products/{{prodB2}}/active', auth: 'adminB', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar categoria B', method: 'PATCH', path: '/api/stores/{{storeB}}/categories/{{catB}}/active', auth: 'adminB', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: desactivar categoria homonima B', method: 'PATCH', path: '/api/stores/{{storeB}}/categories/{{catBSame}}/active', auth: 'adminB', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    // ---------- Tienda Central: desvincular empleados QA ----------
    { name: 'QA teardown: desvincular empleado A de Tienda Central', method: 'DELETE', path: '/api/stores/{{storeA}}/employees/{{empAId}}', auth: 'adminA', expect: 204 },
    { name: 'QA teardown: desvincular empleado A2 de Tienda Central', method: 'DELETE', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}', auth: 'adminA', expect: 204 },
    // ---------- Bloquear usuarios QA (nunca admin@ ni carlos) ----------
    { name: 'QA teardown: bloquear empleado A', method: 'PATCH', path: '/api/admin/users/{{empAId}}/active', auth: 'super', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: bloquear empleado A2', method: 'PATCH', path: '/api/admin/users/{{empA2Id}}/active', auth: 'super', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: bloquear admin B', method: 'PATCH', path: '/api/admin/users/{{userB}}/active', auth: 'super', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: bloquear empleado B', method: 'PATCH', path: '/api/admin/users/{{empBId}}/active', auth: 'super', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: bloquear usuario sin membresia', method: 'PATCH', path: '/api/admin/users/{{userNoMem}}/active', auth: 'super', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: bloquear dueno QA-C', method: 'PATCH', path: '/api/admin/users/{{userOwnerC}}/active', auth: 'super', body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: verificar empleado A bloqueado', method: 'GET', path: '/api/admin/users/{{empAId}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: verificar admin B bloqueado', method: 'GET', path: '/api/admin/users/{{userB}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: verificar empleado B bloqueado', method: 'GET', path: '/api/admin/users/{{empBId}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: verificar noMem bloqueado', method: 'GET', path: '/api/admin/users/{{userNoMem}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: verificar dueno QA-C bloqueado', method: 'GET', path: '/api/admin/users/{{userOwnerC}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
    // ---------- QA-C: desvincular admin@ y cerrar ----------
    {
      name: 'QA teardown: desvincular admin@ de QA-C',
      method: 'DELETE', path: '/api/admin/stores/{{storeC}}/members/{{userA}}', auth: 'super',
      expect: 200,
      checks: [
        { findAbsent: ['members', 'userId', 'userA'] },
        { findEq: ['members', 'userId', 'userOwnerC', 'role', 'STORE_ADMIN'] },
      ],
    },
    { name: 'QA teardown: cerrar QA-C', method: 'DELETE', path: '/api/admin/stores/{{storeC}}', auth: 'super', expect: 204 },
    { name: 'QA teardown: verificar QA-C CLOSED', method: 'GET', path: '/api/admin/stores/{{storeC}}', auth: 'super', expect: 200, checks: [{ eq: 'store.status', value: 'CLOSED' }] },
    { name: 'QA teardown: cerrar QA-B', method: 'DELETE', path: '/api/admin/stores/{{storeB}}', auth: 'super', expect: 204 },
    { name: 'QA teardown: verificar QA-B CLOSED', method: 'GET', path: '/api/admin/stores/{{storeB}}', auth: 'super', expect: 200, checks: [{ eq: 'store.status', value: 'CLOSED' }] },
    // ---------- Tienda Central y admin@ intactos ----------
    { name: 'QA teardown: verificar Tienda Central ACTIVE', method: 'GET', path: '/api/admin/stores/{{storeA}}', auth: 'super', expect: 200, checks: [{ eq: 'store.name', value: 'Tienda Central' }, { eq: 'store.status', value: 'ACTIVE' }] },
    { name: 'QA teardown: verificar admin@ activo', method: 'GET', path: '/api/admin/users/{{userA}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: true }, { eq: 'platformRole', value: 'USER' }, { eq: 'email', value: 'admin@minegocio.com' }] },
    {
      name: 'QA teardown: admin@ sigue listando categorias de Tienda Central',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      expect: 200,
      checks: [{ fixturesInactive: ['catAa', 'catM', 'catZ'] }],
    },
    {
      name: 'QA teardown: admin@ sigue listando productos de Tienda Central',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      expect: 200,
      checks: [{ fixturesInactive: ['prodAlfa', 'prodAlfajor', 'prodBeta', 'prodInactive', 'prodCatInactive', 'prodStock', 'prodTemp'] }],
    },
    {
      name: 'QA teardown: admin@ sigue pudiendo iniciar sesion',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: '{{adminEmail}}', password: '{{adminPassword}}' },
      expect: 200,
      checks: [{ eq: 'email', value: 'admin@minegocio.com' }, { eq: 'platformRole', value: 'USER' }, { exists: 'token' }],
    },
  ],
};