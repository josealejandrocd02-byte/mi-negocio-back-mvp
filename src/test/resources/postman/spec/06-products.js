'use strict';

// RF-19..RF-23: productos, validaciones, filtros, visibilidad y no-stock.
module.exports = {
  name: '06 Productos (RF-19..23)',
  cases: [
    {
      name: 'RF-19 admin A lista productos (activos e inactivos)',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      expect: 200,
      checks: [
        { lengthAtLeast: 7 }, { sortedBy: 'name' },
        { idsIncludeVar: 'prodAlfa' }, { idsIncludeVar: 'prodInactive' }, { idsExcludeVar: 'prodB' },
      ],
    },
    {
      name: 'RF-22 empleado A solo ve productos activos con categoria activa',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      expect: 200,
      checks: [
        { namesInclude: ['Alfa-{run}'] },
        { namesExclude: ['Inactivo-{run}', 'ProdCatInactiva-{run}'] },
        { keysAbsent: ['stockQuantity', 'stock', 'quantity', 'availability'] },
      ],
    },
    {
      name: 'RF-19 detalle de producto activo',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA',
      expect: 200,
      checks: [
        { eqVar: ['categoryId', 'catAa'] }, { eq: 'categoryName', value: 'A-Alfa-Cat-{run}' },
        { eq: 'price', value: 10.5 }, { eq: 'active', value: true },
        { null: 'imageUrl' }, { exists: 'createdAt' },
        { keysAbsent: ['stockQuantity', 'stock', 'quantity', 'availability'] },
      ],
    },
    {
      name: 'RF-19 admin ve producto inactivo',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodInactive}}', auth: 'adminA',
      expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-19 producto inexistente -> 404',
      method: 'GET', path: '/api/stores/{{storeA}}/products/00000000-0000-0000-0000-000000000090', auth: 'adminA', expect: 404,
    },
    {
      name: 'RF-19 precio 0 -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'PrecioCero-{{runId}}', price: 0 }, expect: 400,
    },
    {
      name: 'RF-19 precio negativo -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'PrecioNeg-{{runId}}', price: -5 }, expect: 400,
    },
    {
      name: 'RF-19 precio ausente -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'PrecioAusente-{{runId}}' }, expect: 400,
    },
    {
      name: 'RF-19 precio null -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'PrecioNull-{{runId}}', price: null }, expect: 400,
    },
    {
      name: 'RF-19 nombre de producto vacio -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: '', price: 1 }, expect: 400,
    },
    {
      name: 'RF-19 nombre de producto > 150 -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'P'.repeat(151), price: 1 }, expect: 400,
    },
    {
      name: 'RF-23 producto con categoria de B -> 404',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'CatB-{{runId}}', categoryId: '{{catB}}', price: 1 }, expect: 404,
    },
    {
      name: 'RF-19 producto con categoria inexistente -> 404',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'CatX-{{runId}}', categoryId: '00000000-0000-0000-0000-000000000089', price: 1 }, expect: 404,
    },
    {
      name: 'RF-19 producto duplicado exacto -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'Alfa-{{runId}}', price: 1 }, expect: 409,
    },
    {
      name: 'RF-19 producto duplicado case-insensitive -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      body: { name: 'alfa-{{runId}}', price: 1 }, expect: 409,
    },
    {
      name: 'RF-23 mismo nombre de producto permitido en B',
      method: 'POST', path: '/api/stores/{{storeB}}/products', auth: 'adminB',
      body: { name: 'Alfajor-{{runId}}', price: 88 }, expect: 201, save: { prodB2: 'id' },
    },
    {
      name: 'RF-21 producto con stockQuantity no devuelve campos de stock',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodStock}}', auth: 'adminA',
      expect: 200,
      checks: [
        { keysAbsent: ['stockQuantity', 'stock', 'quantity', 'availability'] },
        { notContains: ['stockQuantity', 'availability'] },
      ],
    },
    {
      name: 'RF-19 editar producto: nombre, categoria y precio',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodTemp}}', auth: 'adminA',
      body: { name: 'TempUpdate-{{runId}}-R', categoryId: '{{catM}}', price: 7.77 },
      expect: 200,
      checks: [
        { eq: 'name', value: 'TempUpdate-{run}-R' },
        { eqVar: ['categoryId', 'catM'] },
        { eq: 'categoryName', value: 'M-BETA-CAT-{run}-RENAMED' },
        { eq: 'price', value: 7.77 }, { eq: 'active', value: true },
      ],
    },
    {
      name: 'RF-19 editar producto a nombre duplicado -> 409',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodTemp}}', auth: 'adminA',
      body: { name: 'Alfa-{{runId}}', categoryId: '{{catM}}', price: 1 }, expect: 409,
    },
    {
      name: 'RF-19 cambiar solo mayusculas del propio producto -> 200',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodTemp}}', auth: 'adminA',
      body: { name: 'TEMPUPDATE-{{runId}}-R', categoryId: '{{catM}}', price: 7.77 },
      expect: 200, checks: [{ eq: 'name', value: 'TEMPUPDATE-{run}-R' }],
    },
    {
      name: 'RF-23 editar producto con categoria de B -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodTemp}}', auth: 'adminA',
      body: { name: 'TEMPUPDATE-{{runId}}-R', categoryId: '{{catB}}', price: 1 }, expect: 404,
    },
    {
      name: 'RF-19 editar producto con categoria inexistente -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodTemp}}', auth: 'adminA',
      body: { name: 'TEMPUPDATE-{{runId}}-R', categoryId: '00000000-0000-0000-0000-000000000088', price: 1 }, expect: 404,
    },
    {
      name: 'RF-23 producto de B via path de A -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodB}}', auth: 'adminA',
      body: { name: 'Intruso-{{runId}}', price: 1 }, expect: 404,
    },
    {
      name: 'RF-19 editar producto inexistente -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/00000000-0000-0000-0000-000000000087', auth: 'adminA',
      body: { name: 'NoExiste-{{runId}}', price: 1 }, expect: 404,
    },
    {
      name: 'RF-19 editar producto con precio 0 -> 400',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodTemp}}', auth: 'adminA',
      body: { name: 'TEMPUPDATE-{{runId}}-R', price: 0 }, expect: 400,
    },
    {
      name: 'RF-21 admin desactiva producto Alfa',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'adminA',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-22 Alfa desactivado desaparece para el empleado',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      expect: 200, checks: [{ namesExclude: ['Alfa-{run}'] }],
    },
    {
      name: 'RF-21 el admin sigue viendo Alfa desactivado',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      expect: 200, checks: [{ idsIncludeVar: 'prodAlfa' }],
    },
    {
      name: 'RF-21 reactivar Alfa',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'adminA',
      body: { active: true }, expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-22 Alfa reactivado reaparece para el empleado',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      expect: 200, checks: [{ namesInclude: ['Alfa-{run}'] }],
    },
    {
      name: 'RF-19 PATCH producto sin active -> 400',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'adminA',
      body: {}, expect: 400,
    },
    {
      name: 'RF-19 PATCH producto UUID invalido -> 400',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/no-es-uuid/active', auth: 'adminA',
      body: { active: false }, expect: 400,
    },
    {
      name: 'RF-21 DELETE producto no permitido -> 405',
      method: 'DELETE', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA', expect: 405,
    },
    {
      name: 'RF-21 producto sigue existiendo tras DELETE',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA', expect: 200,
    },
    {
      name: 'RF-19 filtro q=alfa case-insensitive',
      method: 'GET', path: '/api/stores/{{storeA}}/products?q=alfa', auth: 'adminA',
      expect: 200, checks: [{ namesAllContain: 'alfa' }, { namesInclude: ['Alfa-{run}', 'Alfajor-{run}'] }],
    },
    {
      name: 'RF-19 filtro q=ALFA mayusculas',
      method: 'GET', path: '/api/stores/{{storeA}}/products?q=ALFA', auth: 'adminA',
      expect: 200, checks: [{ namesAllContain: 'alfa' }, { namesInclude: ['Alfa-{run}'] }],
    },
    {
      name: 'RF-19 filtro q sin coincidencias',
      method: 'GET', path: '/api/stores/{{storeA}}/products?q=zzz-no-match', auth: 'adminA',
      expect: 200, checks: [{ empty: true }],
    },
    {
      name: 'RF-19 filtro categoryId',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId={{catAa}}', auth: 'adminA',
      expect: 200, checks: [
        { allEq: ['categoryId'], allEqVar: 'catAa' },
        { idsIncludeVar: 'prodAlfa' }, { idsExcludeVar: 'prodBeta' },
      ],
    },
    {
      name: 'RF-19 filtro categoryId + q combinados',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId={{catAa}}&q=alfa', auth: 'adminA',
      expect: 200, checks: [{ namesAllContain: 'alfa' }, { namesInclude: ['Alfa-{run}'] }],
    },
    {
      name: 'RF-19 filtro categoryId inexistente -> lista vacia',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId=00000000-0000-0000-0000-000000000086', auth: 'adminA',
      expect: 200, checks: [{ empty: true }],
    },
    {
      name: 'RF-19 filtro categoryId malformado -> 400',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId=no-es-uuid', auth: 'adminA', expect: 400,
    },
    {
      name: 'RF-22 EMPLOYEE no crea productos -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      body: { name: 'EmpProd-{{runId}}', price: 1 }, expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE no edita productos -> 403',
      method: 'PUT', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'empA',
      body: { name: 'EmpEdit-{{runId}}', price: 1 }, expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE no desactiva productos -> 403',
      method: 'PATCH', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}/active', auth: 'empA',
      body: { active: false }, expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE no ve producto inactivo -> 404',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodInactive}}', auth: 'empA', expect: 404,
    },
    {
      name: 'RF-22 EMPLOYEE no ve producto de categoria inactiva -> 404',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodCatInactive}}', auth: 'empA', expect: 404,
    },
    {
      name: 'RF-22 EMPLOYEE ve producto activo -> 200',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'empA',
      expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-22 admin desactiva categoria M-BETA',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catM}}/active', auth: 'adminA',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-22 categoria inactiva desaparece para el empleado',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'empA',
      expect: 200, checks: [{ namesExclude: ['M-BETA-CAT-{run}-RENAMED'] }],
    },
    {
      name: 'RF-22 productos de categoria inactiva desaparecen para el empleado',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      expect: 200, checks: [{ namesExclude: ['Alfajor-{run}'] }],
    },
    {
      name: 'RF-22 detalle de producto de categoria inactiva -> 404 empleado',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfajor}}', auth: 'empA', expect: 404,
    },
    {
      name: 'RF-21 el admin sigue viendo el producto de categoria inactiva',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfajor}}', auth: 'adminA',
      expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-22 reactivar categoria M-BETA',
      method: 'PATCH', path: '/api/stores/{{storeA}}/categories/{{catM}}/active', auth: 'adminA',
      body: { active: true }, expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-22 el producto reaparece para el empleado al reactivar la categoria',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA',
      expect: 200, checks: [{ namesInclude: ['Alfajor-{run}'] }],
    },
    {
      name: 'RF-22 filtro por categoria inactiva no expone productos al empleado',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId={{catZ}}', auth: 'empA',
      expect: 200, checks: [{ empty: true }],
    },
    {
      name: 'RF-19 el admin si ve el producto de categoria inactiva con filtro',
      method: 'GET', path: '/api/stores/{{storeA}}/products?categoryId={{catZ}}', auth: 'adminA',
      expect: 200, checks: [{ idsIncludeVar: 'prodCatInactive' }],
    },
    {
      name: 'RF-21 listado de productos sin campos de stock',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      expect: 200,
      checks: [
        { keysAbsent: ['stockQuantity', 'stock', 'quantity', 'availability'] },
        { notContains: ['stockQuantity', 'availability'] },
      ],
    },
  ],
};