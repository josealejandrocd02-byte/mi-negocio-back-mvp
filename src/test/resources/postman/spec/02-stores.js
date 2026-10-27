'use strict';

// RF-02, RF-06..RF-14: administracion de sucursales y ciclo de vida.
module.exports = {
  name: '02 Sucursales (RF-02, RF-06..14)',
  cases: [
    {
      name: 'RF-08 listar sucursales (SUPER_ADMIN)',
      method: 'GET', path: '/api/admin/stores', auth: 'super',
      expect: 200, checks: [{ lengthAtLeast: 2 }, { namesInclude: ['QA-A-{run}', 'QA-B-{run}'] }],
    },
    {
      name: 'RF-08 listar sucursales ACTIVE',
      method: 'GET', path: '/api/admin/stores?status=ACTIVE', auth: 'super',
      expect: 200, checks: [{ namesInclude: ['QA-A-{run}', 'QA-B-{run}'] }],
    },
    {
      name: 'RF-07 consultar sucursal A con su equipo',
      method: 'GET', path: '/api/admin/stores/{{storeA}}', auth: 'super',
      expect: 200,
      checks: [
        { eq: 'store.status', value: 'ACTIVE' },
        { eqVar: ['store.ownerId', 'userA'] },
        { findEq: ['members', 'userId', 'userA', 'role', 'STORE_ADMIN'] },
        { findEq: ['members', 'userId', 'empAId', 'role', 'EMPLOYEE'] },
      ],
    },
    {
      name: 'RF-07 consultar sucursal B (dueno B)',
      method: 'GET', path: '/api/admin/stores/{{storeB}}', auth: 'super',
      expect: 200, checks: [{ eqVar: ['store.ownerId', 'userB'] }, { eq: 'store.status', value: 'ACTIVE' }],
    },
    {
      name: 'RF-06 editar sucursal A',
      method: 'PUT', path: '/api/admin/stores/{{storeA}}', auth: 'super',
      body: { name: 'QA-A-{{runId}}', address: 'Calle A 99', phone: '5559999' },
      expect: 200, checks: [{ eq: 'store.address', value: 'Calle A 99' }, { eq: 'store.phone', value: '5559999' }],
    },
    {
      name: 'RF-06 editar sucursal A con nombre vacio -> 400',
      method: 'PUT', path: '/api/admin/stores/{{storeA}}', auth: 'super',
      body: { name: '', address: 'x' }, expect: 400,
    },
    {
      name: 'RF-06 crear sucursal duplicada para el mismo dueno -> 409',
      method: 'POST', path: '/api/admin/stores', auth: 'super',
      body: { name: 'qa-a-{{runId}}', ownerUserId: '{{userA}}' }, expect: 409,
    },
    {
      name: 'RF-06 SUPER_ADMIN no puede ser dueno -> 409',
      method: 'POST', path: '/api/admin/stores', auth: 'super',
      body: { name: 'QA-SA-{{runId}}', ownerUserId: '{{saId}}' }, expect: 409,
    },
    {
      name: 'RF-06 dueno inexistente -> 404',
      method: 'POST', path: '/api/admin/stores', auth: 'super',
      body: { name: 'QA-X-{{runId}}', ownerUserId: '00000000-0000-0000-0000-000000000099' }, expect: 404,
    },
    {
      name: 'RF-06 crear sucursal sin dueno -> 400',
      method: 'POST', path: '/api/admin/stores', auth: 'super',
      body: { name: 'QA-X-{{runId}}' }, expect: 400,
    },
    {
      name: 'RF-06 crear sucursal con nombre vacio -> 400',
      method: 'POST', path: '/api/admin/stores', auth: 'super',
      body: { name: '', ownerUserId: '{{userB}}' }, expect: 400,
    },
    {
      name: 'RF-06 vincular admin A como STORE_ADMIN de B',
      method: 'POST', path: '/api/admin/stores/{{storeB}}/admins', auth: 'super',
      body: { userId: '{{userA}}' }, expect: 200,
      checks: [{ findEq: ['members', 'userId', 'userA', 'role', 'STORE_ADMIN'] }],
    },
    {
      name: 'RF-06 desvincular admin A de B',
      method: 'DELETE', path: '/api/admin/stores/{{storeB}}/members/{{userA}}', auth: 'super',
      expect: 200, checks: [{ findAbsent: ['members', 'userId', 'userA'] }],
    },
    {
      name: 'RF-06 no desvincular al dueno de A -> 409',
      method: 'DELETE', path: '/api/admin/stores/{{storeA}}/members/{{userA}}', auth: 'super',
      expect: 409,
    },
    {
      name: 'RF-09 cambiar dueno de B a admin A',
      method: 'PUT', path: '/api/admin/stores/{{storeB}}/owner', auth: 'super',
      body: { userId: '{{userA}}' }, expect: 200,
      checks: [
        { eqVar: ['store.ownerId', 'userA'] },
        { findEq: ['members', 'userId', 'userA', 'role', 'STORE_ADMIN'] },
      ],
    },
    {
      name: 'RF-09 devolver dueno de B a admin B',
      method: 'PUT', path: '/api/admin/stores/{{storeB}}/owner', auth: 'super',
      body: { userId: '{{userB}}' }, expect: 200, checks: [{ eqVar: ['store.ownerId', 'userB'] }],
    },
    {
      name: 'RF-09 limpiar membresia temporal de admin A en B',
      method: 'DELETE', path: '/api/admin/stores/{{storeB}}/members/{{userA}}', auth: 'super',
      expect: 200, checks: [{ findAbsent: ['members', 'userId', 'userA'] }],
    },
    {
      name: 'RF-09 cambiar al mismo dueno -> 409',
      method: 'PUT', path: '/api/admin/stores/{{storeB}}/owner', auth: 'super',
      body: { userId: '{{userB}}' }, expect: 409,
    },
    {
      name: 'RF-09 SUPER_ADMIN no puede ser dueno de sucursal -> 409',
      method: 'PUT', path: '/api/admin/stores/{{storeB}}/owner', auth: 'super',
      body: { userId: '{{saId}}' }, expect: 409,
    },
    {
      name: 'RF-09 cambiar a dueno inexistente -> 404',
      method: 'PUT', path: '/api/admin/stores/{{storeB}}/owner', auth: 'super',
      body: { userId: '00000000-0000-0000-0000-000000000098' }, expect: 404,
    },
    {
      name: 'RF-12 suspender sucursal A',
      method: 'PATCH', path: '/api/admin/stores/{{storeA}}/status', auth: 'super',
      body: { status: 'SUSPENDED' }, expect: 200, checks: [{ eq: 'store.status', value: 'SUSPENDED' }],
    },
    {
      name: 'RF-12 STORE_ADMIN no opera en sucursal suspendida -> 409',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA', expect: 409,
    },
    {
      name: 'RF-12 EMPLOYEE no opera en sucursal suspendida -> 409',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA', expect: 409,
    },
    {
      name: 'RF-12 escritura de catalogo en sucursal suspendida -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/categories', auth: 'adminA',
      body: { name: 'Suspendida-{{runId}}' }, expect: 409,
    },
    {
      name: 'RF-12 gestion de equipo en sucursal suspendida -> 409',
      method: 'GET', path: '/api/stores/{{storeA}}/employees', auth: 'adminA', expect: 409,
    },
    {
      name: 'RF-12 SUPER_ADMIN sigue consultando sucursal suspendida -> 200',
      method: 'GET', path: '/api/admin/stores/{{storeA}}', auth: 'super',
      expect: 200, checks: [{ eq: 'store.status', value: 'SUSPENDED' }],
    },
    {
      name: 'RF-12 listar sucursales suspendidas',
      method: 'GET', path: '/api/admin/stores?status=SUSPENDED', auth: 'super',
      expect: 200, checks: [{ namesInclude: ['QA-A-{run}'] }],
    },
    {
      name: 'RF-12 reactivar sucursal A',
      method: 'PATCH', path: '/api/admin/stores/{{storeA}}/status', auth: 'super',
      body: { status: 'ACTIVE' }, expect: 200, checks: [{ eq: 'store.status', value: 'ACTIVE' }],
    },
    {
      name: 'RF-12 admin A recupera acceso tras reactivar',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA', expect: 200,
    },
    {
      name: 'RF-12 empleado A recupera acceso tras reactivar',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA', expect: 200,
    },
    {
      name: 'RF-13 cerrar sucursal B (DELETE)',
      method: 'DELETE', path: '/api/admin/stores/{{storeB}}', auth: 'super', expect: 204,
    },
    {
      name: 'RF-13 sucursal B queda CLOSED',
      method: 'GET', path: '/api/admin/stores/{{storeB}}', auth: 'super',
      expect: 200, checks: [{ eq: 'store.status', value: 'CLOSED' }],
    },
    {
      name: 'RF-13 admin B no opera en sucursal cerrada -> 409',
      method: 'GET', path: '/api/stores/{{storeB}}/products', auth: 'adminB', expect: 409,
    },
    {
      name: 'RF-13 no editar sucursal cerrada -> 409',
      method: 'PUT', path: '/api/admin/stores/{{storeB}}', auth: 'super',
      body: { name: 'QA-B-{{runId}}' }, expect: 409,
    },
    {
      name: 'RF-13 no vincular admin en sucursal cerrada -> 409',
      method: 'POST', path: '/api/admin/stores/{{storeB}}/admins', auth: 'super',
      body: { userId: '{{userA}}' }, expect: 409,
    },
    {
      name: 'RF-13 reactivar sucursal cerrada',
      method: 'PATCH', path: '/api/admin/stores/{{storeB}}/status', auth: 'super',
      body: { status: 'ACTIVE' }, expect: 200, checks: [{ eq: 'store.status', value: 'ACTIVE' }],
    },
    {
      name: 'RF-13 admin B recupera acceso tras reactivar B',
      method: 'GET', path: '/api/stores/{{storeB}}/products', auth: 'adminB', expect: 200,
    },
    {
      name: 'RF-13 status invalido -> 400',
      method: 'PATCH', path: '/api/admin/stores/{{storeB}}/status', auth: 'super',
      body: { status: 'INVALID' }, expect: 400,
    },
    {
      name: 'RF-13 status ausente -> 400',
      method: 'PATCH', path: '/api/admin/stores/{{storeB}}/status', auth: 'super',
      body: {}, expect: 400,
    },
    {
      name: 'RF-07 sucursal inexistente -> 404',
      method: 'GET', path: '/api/admin/stores/00000000-0000-0000-0000-000000000097', auth: 'super', expect: 404,
    },
    {
      name: 'RF-14 cerrar sucursal inexistente -> 404',
      method: 'DELETE', path: '/api/admin/stores/00000000-0000-0000-0000-000000000096', auth: 'super', expect: 404,
    },
  ],
};