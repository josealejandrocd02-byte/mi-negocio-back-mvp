'use strict';

// Teardown: desactivar catalogo, retirar imagenes, cerrar sucursales QA y bloquear usuarios QA.
// No toca datos seed ni sucursales reales.
module.exports = {
  name: '10 Teardown QA',
  cases: [
    { name: 'QA teardown: desactivar Alfa', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar Alfajor', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfajor}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar Beta', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodBeta}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar Inactivo', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodInactive}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar ProdCatInactiva', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodCatInactive}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar StockIgnorado', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodStock}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar TEMPUPDATE', method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodTemp}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar producto B', method: 'PATCH', path: '/api/stores/{{storeB}}/products/{{prodB}}/active', auth: 'adminB', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar producto B2', method: 'PATCH', path: '/api/stores/{{storeB}}/products/{{prodB2}}/active', auth: 'adminB', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar categoria A-Alfa', method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catAa}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar categoria M-BETA', method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catM}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar categoria Z-Zeta', method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catZ}}/active', auth: 'adminA', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar categoria B', method: 'PATCH', path: '/api/stores/{{storeB}}/categories/{{catB}}/active', auth: 'adminB', body: { active: false }, expect: 200 },
    { name: 'QA teardown: desactivar categoria homonima B', method: 'PATCH', path: '/api/stores/{{storeB}}/categories/{{catBSame}}/active', auth: 'adminB', body: { active: false }, expect: 200 },
    { name: 'QA teardown: retirar foto de Alfa', method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo', auth: 'adminA', expect: 204 },
    { name: 'QA teardown: retirar logo de A', method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/logo', auth: 'adminA', expect: 204 },
    { name: 'QA teardown: cerrar sucursal A', method: 'DELETE', path: '/api/admin/stores/{{storeA}}', auth: 'super', expect: 204 },
    { name: 'QA teardown: cerrar sucursal B', method: 'DELETE', path: '/api/admin/stores/{{storeB}}', auth: 'super', expect: 204 },
    { name: 'QA teardown: verificar A CLOSED', method: 'GET', path: '/api/admin/stores/{{storeA}}', auth: 'super', expect: 200, checks: [{ eq: 'store.status', value: 'CLOSED' }] },
    { name: 'QA teardown: verificar B CLOSED', method: 'GET', path: '/api/admin/stores/{{storeB}}', auth: 'super', expect: 200, checks: [{ eq: 'store.status', value: 'CLOSED' }] },
    { name: 'QA teardown: bloquear admin A', method: 'PATCH', path: '/api/admin/users/{{userA}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: bloquear admin B', method: 'PATCH', path: '/api/admin/users/{{userB}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: bloquear usuario sin membresia', method: 'PATCH', path: '/api/admin/users/{{userNoMem}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: bloquear empleado A', method: 'PATCH', path: '/api/admin/users/{{empAId}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: bloquear empleado B', method: 'PATCH', path: '/api/admin/users/{{empBId}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: bloquear empleado A2', method: 'PATCH', path: '/api/admin/users/{{empA2Id}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: bloquear usuario inyectado', method: 'PATCH', path: '/api/admin/users/{{userInj}}/active', auth: 'super', body: { active: false }, expect: 200 },
    { name: 'QA teardown: verificar admin A bloqueado', method: 'GET', path: '/api/admin/users/{{userA}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
    { name: 'QA teardown: verificar admin B bloqueado', method: 'GET', path: '/api/admin/users/{{userB}}', auth: 'super', expect: 200, checks: [{ eq: 'active', value: false }] },
  ],
};