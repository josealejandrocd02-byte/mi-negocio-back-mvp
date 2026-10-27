'use strict';

// RF-19, RF-22, RF-23: categorias del catalogo.
module.exports = {
  name: '05 Categorias (RF-19/22/23)',
  cases: [
    {
      name: 'RF-19 admin A lista categorias (activas e inactivas) ordenadas',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      expect: 200,
      checks: [
        { lengthAtLeast: 3 }, { sortedBy: 'name' },
        { namesInclude: ['A-Alfa-Cat-{run}', 'M-Beta-Cat-{run}', 'Z-Zeta-Cat-{run}'] },
      ],
    },
    {
      name: 'RF-22 empleado A solo ve categorias activas',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'empA',
      expect: 200, checks: [
        { sortedBy: 'name' },
        { namesInclude: ['A-Alfa-Cat-{run}', 'M-Beta-Cat-{run}'] },
        { namesExclude: ['Z-Zeta-Cat-{run}'] },
      ],
    },
    {
      name: 'RF-19 crear categoria con nombre vacio -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      body: { name: '' }, expect: 400,
    },
    {
      name: 'RF-19 crear categoria con nombre > 100 -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      body: { name: 'C'.repeat(101) }, expect: 400,
    },
    {
      name: 'RF-19 crear categoria duplicada exacta -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      body: { name: 'A-Alfa-Cat-{{runId}}' }, expect: 409,
    },
    {
      name: 'RF-19 crear categoria duplicada case-insensitive -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      body: { name: 'a-alfa-cat-{{runId}}' }, expect: 409,
    },
    {
      name: 'RF-23 mismo nombre de categoria permitido en B',
      method: 'POST', path: '/api/stores/{{storeB}}/categories', auth: 'adminB',
      body: { name: 'A-Alfa-Cat-{{runId}}', active: true }, expect: 201, save: { catBSame: 'id' },
    },
    {
      name: 'RF-19 admin A edita nombre de categoria',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catM}}', auth: 'adminA',
      body: { name: 'M-Beta-Cat-{{runId}}-Renamed' }, expect: 200,
      checks: [{ eq: 'name', value: 'M-Beta-Cat-{run}-Renamed' }, { eq: 'active', value: true }],
    },
    {
      name: 'RF-19 editar categoria a nombre duplicado -> 409',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catM}}', auth: 'adminA',
      body: { name: 'A-Alfa-Cat-{{runId}}' }, expect: 409,
    },
    {
      name: 'RF-19 cambiar solo mayusculas del propio nombre -> 200',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catM}}', auth: 'adminA',
      body: { name: 'M-BETA-CAT-{{runId}}-RENAMED' }, expect: 200,
      checks: [{ eq: 'name', value: 'M-BETA-CAT-{run}-RENAMED' }, { eq: 'active', value: true }],
    },
    {
      name: 'RF-19 editar categoria con nombre vacio -> 400',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catM}}', auth: 'adminA',
      body: { name: '' }, expect: 400,
    },
    {
      name: 'RF-19 editar categoria con nombre > 100 -> 400',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catM}}', auth: 'adminA',
      body: { name: 'C'.repeat(101) }, expect: 400,
    },
    {
      name: 'RF-23 categoria de B via path de A -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catB}}', auth: 'adminA',
      body: { name: 'Intruso-{{runId}}' }, expect: 404,
    },
    {
      name: 'RF-19 categoria inexistente -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/00000000-0000-0000-0000-000000000092', auth: 'adminA',
      body: { name: 'NoExiste-{{runId}}' }, expect: 404,
    },
    {
      name: 'RF-19 activar categoria inactiva',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catZ}}/active', auth: 'adminA',
      body: { active: true }, expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-22 empleado ve la categoria al activarla',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'empA',
      expect: 200, checks: [{ namesInclude: ['Z-Zeta-Cat-{run}'] }],
    },
    {
      name: 'RF-19 desactivar categoria',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catZ}}/active', auth: 'adminA',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-22 empleado deja de ver la categoria inactiva',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'empA',
      expect: 200, checks: [{ namesExclude: ['Z-Zeta-Cat-{run}'] }],
    },
    {
      name: 'RF-19 PATCH sin active -> 400',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catZ}}/active', auth: 'adminA',
      body: {}, expect: 400,
    },
    {
      name: 'RF-19 PATCH con UUID invalido -> 400',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/no-es-uuid/active', auth: 'adminA',
      body: { active: false }, expect: 400,
    },
    {
      name: 'RF-21 DELETE categoria no permitido -> 405',
      method: 'DELETE', path: '/api/stores/{{storeA}}/categories/{{catZ}}', auth: 'adminA', expect: 405,
    },
    {
      name: 'RF-21 categoria sigue existiendo tras DELETE',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      expect: 200, checks: [{ namesInclude: ['Z-Zeta-Cat-{run}'] }],
    },
    {
      name: 'RF-23 admin B no lista categorias de A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'adminB', expect: 403,
    },
    {
      name: 'RF-04 SUPER_ADMIN no lista categorias de sucursal -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'super', expect: 403,
    },
    {
      name: 'RF-23 admin A con sucursal inexistente -> 403',
      method: 'GET', path: '/api/stores/00000000-0000-0000-0000-000000000091/categories', auth: 'adminA', expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE no crea categoria -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'empA',
      body: { name: 'EmpCat-{{runId}}' }, expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE no edita categoria -> 403',
      method: 'PUT', path: '/api/stores/{{storeA}}/categories/{{catAa}}', auth: 'empA',
      body: { name: 'EmpEdit-{{runId}}' }, expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE no desactiva categoria -> 403',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catAa}}/active', auth: 'empA',
      body: { active: false }, expect: 403,
    },
  ],
};