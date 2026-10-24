'use strict';

// RF-23 y RF-36/37: aislamiento por sucursal e identidad tomada del JWT.
module.exports = {
  name: '07 Aislamiento e identidad (RF-23/36/37)',
  cases: [
    {
      name: 'RF-23 admin A no lista productos de B -> 403',
      method: 'GET', path: '/api/stores/{{storeB}}/products', auth: 'adminA', expect: 403,
    },
    {
      name: 'RF-23 admin A no lista categorias de B -> 403',
      method: 'GET', path: '/api/stores/{{storeB}}/categories', auth: 'adminA', expect: 403,
    },
    {
      name: 'RF-23 empleado A no lista productos de B -> 403',
      method: 'GET', path: '/api/stores/{{storeB}}/products', auth: 'empA', expect: 403,
    },
    {
      name: 'RF-23 empleado A no lista categorias de B -> 403',
      method: 'GET', path: '/api/stores/{{storeB}}/categories', auth: 'empA', expect: 403,
    },
    {
      name: 'RF-23 admin B no lista productos de A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminB', expect: 403,
    },
    {
      name: 'RF-23 admin B no edita producto de A -> 403',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminB',
      body: { name: 'Hack-{{runId}}', price: 1 }, expect: 403,
    },
    {
      name: 'RF-23 admin B no desactiva categoria de A -> 403',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catAa}}/active', auth: 'adminB',
      body: { active: false }, expect: 403,
    },
    {
      name: 'RF-23 obtener producto B via path de A -> 404',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodB}}', auth: 'adminA', expect: 404,
    },
    {
      name: 'RF-23 desactivar producto B via path de A -> 404',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodB}}/active', auth: 'adminA',
      body: { active: false }, expect: 404,
    },
    {
      name: 'RF-23 editar categoria B via path de A -> 404',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catB}}/active', auth: 'adminA',
      body: { active: false }, expect: 404,
    },
    {
      name: 'RF-23 crear producto en A con categoria de B -> 404',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'Cruce-{{runId}}', categoryId: '{{catB}}', price: 1 }, expect: 404,
    },
    {
      name: 'RF-23 listado de A no incluye producto de B',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      expect: 200, checks: [{ idsExcludeVar: 'prodB' }],
    },
    {
      name: 'RF-23 listado de B no incluye producto de A',
      method: 'GET', path: '/api/stores/{{storeB}}/products', auth: 'adminB',
      expect: 200, checks: [{ idsExcludeVar: 'prodAlfa' }],
    },
    {
      name: 'RF-23 categorias de A no incluyen la categoria de B',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      expect: 200, checks: [{ idsExcludeVar: 'catBSame' }],
    },
    {
      name: 'RF-23 categorias de B incluyen su propia categoria homonima',
      method: 'GET', path: '/api/stores/{{storeB}}/categories', auth: 'adminB',
      expect: 200, checks: [{ idsIncludeVar: 'catBSame' }],
    },
    {
      name: 'RF-23 mismo nombre de producto, ids distintos por sucursal',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA',
      expect: 200, checks: [{ idEqualsVar: ['id', 'prodAlfa'] }, { eq: 'name', value: 'Alfa-{run}' }],
    },
    {
      name: 'RF-23 producto homonimo en B es otro registro',
      method: 'GET', path: '/api/stores/{{storeB}}/products/{{prodB}}', auth: 'adminB',
      expect: 200, checks: [{ idEqualsVar: ['id', 'prodB'] }, { eq: 'name', value: 'Alfa-{run}' }],
    },
    {
      name: 'RF-23 STORE_ADMIN no accede a /api/admin/stores -> 403',
      method: 'GET', path: '/api/admin/stores', auth: 'adminB', expect: 403,
    },
    {
      name: 'RF-36 usuario sin membresia no lista categorias de A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'noMem', expect: 403,
    },
    {
      name: 'RF-36 usuario sin membresia no lista productos de A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'noMem', expect: 403,
    },
    {
      name: 'RF-36 usuario sin membresia no lista equipo de A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/employees', auth: 'noMem', expect: 403,
    },
    {
      name: 'RF-36 usuario sin membresia no crea categoria en A -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'noMem',
      body: { name: 'NoMem-{{runId}}' }, expect: 403,
    },
    {
      name: 'RF-36 usuario sin membresia no accede a /api/admin/users -> 403',
      method: 'GET', path: '/api/admin/users', auth: 'noMem', expect: 403,
    },
    {
      name: 'RF-37 EMPLOYEE enviando userId de admin sigue siendo EMPLOYEE -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      body: { name: 'Identidad-{{runId}}', price: 1, userId: '{{userA}}' }, expect: 403,
    },
    {
      name: 'RF-37 EMPLOYEE enviando userId de admin no crea categoria -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'empA',
      body: { name: 'IdentidadCat-{{runId}}', userId: '{{userA}}' }, expect: 403,
    },
    {
      name: 'RF-37 EMPLOYEE enviando userId de admin no desactiva producto -> 403',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'empA',
      body: { active: false, userId: '{{userA}}' }, expect: 403,
    },
    {
      name: 'RF-36 restringir acceso de A2 a la sucursal',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}/active', auth: 'adminA',
      body: { active: false }, expect: 200,
    },
    {
      name: 'RF-36 membresia inactiva no opera -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA2', expect: 403,
    },
    {
      name: 'RF-36 devolver acceso de A2',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}/active', auth: 'adminA',
      body: { active: true }, expect: 200,
    },
    {
      name: 'RF-36 token de A no sirve para B (empleado) -> 403',
      method: 'GET', path: '/api/stores/{{storeB}}/products', auth: 'empA', expect: 403,
    },
  ],
};