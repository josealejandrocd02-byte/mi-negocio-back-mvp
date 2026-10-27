'use strict';

// RF-18: CRUD de usuarios globales (SUPER_ADMIN), asignaciones y bloqueo.
module.exports = {
  name: '03 Usuarios admin (RF-18)',
  cases: [
    {
      name: 'RF-18 crear usuario con email duplicado -> 409',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: { fullName: 'Duplicado', email: 'qa.admin.a.{{runId}}@minegocio.com', password: '{{qaPassword}}' },
      expect: 409,
    },
    {
      name: 'RF-18 email duplicado case-insensitive -> 409',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: { fullName: 'Duplicado', email: 'QA.ADMIN.A.{{runId}}@MINEGOCIO.COM', password: '{{qaPassword}}' },
      expect: 409,
    },
    {
      name: 'RF-18 email invalido -> 400',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: { fullName: 'Invalido', email: 'sin-arroba', password: '{{qaPassword}}' }, expect: 400,
    },
    {
      name: 'RF-18 password corta -> 400',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: { fullName: 'Corta', email: 'qa.corta.{{runId}}@minegocio.com', password: '123' }, expect: 400,
    },
    {
      name: 'RF-18 fullName ausente -> 400',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: { email: 'qa.sinnombre.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 400,
    },
    {
      name: 'RF-18 listar usuarios',
      method: 'GET', path: '/api/admin/users', auth: 'super',
      expect: 200, checks: [{ lengthAtLeast: 5 }, { idsIncludeVar: 'userA' }, { idsIncludeVar: 'userB' }, { idsIncludeVar: 'userTemp' }],
    },
    {
      name: 'RF-18 consultar usuario A',
      method: 'GET', path: '/api/admin/users/{{userA}}', auth: 'super',
      expect: 200, checks: [{ eq: 'email', value: 'qa.admin.a.{run}@minegocio.com' }, { eq: 'platformRole', value: 'USER' }],
    },
    {
      name: 'RF-18 consultar usuario inexistente -> 404',
      method: 'GET', path: '/api/admin/users/00000000-0000-0000-0000-000000000095', auth: 'super', expect: 404,
    },
    {
      name: 'RF-18 editar usuario temporal',
      method: 'PUT', path: '/api/admin/users/{{userTemp}}', auth: 'super',
      body: { fullName: 'QA Temp Editado {{runId}}', email: 'qa.temp.{{runId}}@minegocio.com' },
      expect: 200, checks: [{ eq: 'fullName', value: 'QA Temp Editado {run}' }],
    },
    {
      name: 'RF-18 editar email a uno existente -> 409',
      method: 'PUT', path: '/api/admin/users/{{userA}}', auth: 'super',
      body: { fullName: 'QA Admin A {{runId}}', email: 'qa.admin.b.{{runId}}@minegocio.com' }, expect: 409,
    },
    {
      name: 'RF-18 reset password de usuario temporal',
      method: 'PATCH', path: '/api/admin/users/{{userTemp}}/password', auth: 'super',
      body: { newPassword: '{{newQaPassword}}' }, expect: 204,
    },
    {
      name: 'RF-18 login con password reseteada',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.temp.{{runId}}@minegocio.com', password: '{{newQaPassword}}' },
      expect: 200, save: { tokenTemp: 'token' },
    },
    {
      name: 'RF-18 asignar temp a sucursal A como EMPLOYEE',
      method: 'POST', path: '/api/admin/users/{{userTemp}}/stores', auth: 'super',
      body: { storeId: '{{storeA}}', role: 'EMPLOYEE' }, expect: 200,
      checks: [{ findEq: ['stores', 'storeId', 'storeA', 'role', 'EMPLOYEE'] }],
    },
    {
      name: 'RF-18 usuario asignado lee catalogo de A -> 200',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'temp', expect: 200,
    },
    {
      name: 'RF-18 restringir acceso de temp a A',
      method: 'PATCH', path: '/api/admin/users/{{userTemp}}/stores/{{storeA}}/active', auth: 'super',
      body: { active: false }, expect: 200,
      checks: [{ findEq: ['stores', 'storeId', 'storeA', 'active', false] }],
    },
    {
      name: 'RF-18 membresia inactiva no accede -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'temp', expect: 403,
    },
    {
      name: 'RF-18 devolver acceso de temp a A',
      method: 'PATCH', path: '/api/admin/users/{{userTemp}}/stores/{{storeA}}/active', auth: 'super',
      body: { active: true }, expect: 200, checks: [{ findEq: ['stores', 'storeId', 'storeA', 'active', true] }],
    },
    {
      name: 'RF-18 temp recupera acceso a A',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'temp', expect: 200,
    },
    {
      name: 'RF-18 desasignar temp de A',
      method: 'DELETE', path: '/api/admin/users/{{userTemp}}/stores/{{storeA}}', auth: 'super',
      expect: 200, checks: [{ findAbsent: ['stores', 'storeId', 'storeA'] }],
    },
    {
      name: 'RF-18 sin membresia no accede a A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'temp', expect: 403,
    },
    {
      name: 'RF-18 bloquear usuario temporal',
      method: 'PATCH', path: '/api/admin/users/{{userTemp}}/active', auth: 'super',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-18 login de temp bloqueado -> 401',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.temp.{{runId}}@minegocio.com', password: '{{newQaPassword}}' }, expect: 401,
    },
    {
      name: 'RF-18 desbloquear usuario temporal',
      method: 'PATCH', path: '/api/admin/users/{{userTemp}}/active', auth: 'super',
      body: { active: true }, expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-18 login de temp desbloqueado',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.temp.{{runId}}@minegocio.com', password: '{{newQaPassword}}' },
      expect: 200, save: { tokenTemp: 'token' },
    },
    {
      name: 'RF-18 eliminar usuario temporal',
      method: 'DELETE', path: '/api/admin/users/{{userTemp}}', auth: 'super', expect: 204,
    },
    {
      name: 'RF-18 usuario eliminado -> 404',
      method: 'GET', path: '/api/admin/users/{{userTemp}}', auth: 'super', expect: 404,
    },
    {
      name: 'RF-18 no eliminar dueno de sucursal -> 409',
      method: 'DELETE', path: '/api/admin/users/{{userA}}', auth: 'super', expect: 409,
    },
    {
      name: 'RF-18 no modificar SUPER_ADMIN -> 403',
      method: 'PUT', path: '/api/admin/users/{{saId}}', auth: 'super',
      body: { fullName: 'Hack', email: 'hack.{{runId}}@minegocio.com' }, expect: 403,
    },
    {
      name: 'RF-18 no bloquear SUPER_ADMIN -> 403',
      method: 'PATCH', path: '/api/admin/users/{{saId}}/active', auth: 'super',
      body: { active: false }, expect: 403,
    },
    {
      name: 'RF-18 no eliminar SUPER_ADMIN -> 403',
      method: 'DELETE', path: '/api/admin/users/{{saId}}', auth: 'super', expect: 403,
    },
    {
      name: 'RF-18 no asignar sucursal a SUPER_ADMIN -> 403',
      method: 'POST', path: '/api/admin/users/{{saId}}/stores', auth: 'super',
      body: { storeId: '{{storeA}}', role: 'STORE_ADMIN' }, expect: 403,
    },
    {
      name: 'RF-18 rol de sucursal invalido -> 400',
      method: 'POST', path: '/api/admin/users/{{empBId}}/stores', auth: 'super',
      body: { storeId: '{{storeB}}', role: 'MANAGER' }, expect: 400,
    },
    {
      name: 'RF-18 asignar sucursal inexistente -> 404',
      method: 'POST', path: '/api/admin/users/{{empBId}}/stores', auth: 'super',
      body: { storeId: '00000000-0000-0000-0000-000000000094', role: 'EMPLOYEE' }, expect: 404,
    },
    {
      name: 'RF-18 no restringir acceso del dueno -> 409',
      method: 'PATCH', path: '/api/admin/users/{{userA}}/stores/{{storeA}}/active', auth: 'super',
      body: { active: false }, expect: 409,
    },
    {
      name: 'RF-18 no desasignar al dueno -> 409',
      method: 'DELETE', path: '/api/admin/users/{{userA}}/stores/{{storeA}}', auth: 'super', expect: 409,
    },
    {
      name: 'RF-18 cerrar B temporalmente',
      method: 'DELETE', path: '/api/admin/stores/{{storeB}}', auth: 'super', expect: 204,
    },
    {
      name: 'RF-18 no asignar usuario a sucursal cerrada -> 409',
      method: 'POST', path: '/api/admin/users/{{empBId}}/stores', auth: 'super',
      body: { storeId: '{{storeB}}', role: 'EMPLOYEE' }, expect: 409,
    },
    {
      name: 'RF-18 reabrir B',
      method: 'PATCH', path: '/api/admin/stores/{{storeB}}/status', auth: 'super',
      body: { status: 'ACTIVE' }, expect: 200,
    },
    {
      name: 'RF-18 crear usuario con sucursal inexistente -> 404',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: {
        fullName: 'QA Store Invalida {{runId}}', email: 'qa.storeinv.{{runId}}@minegocio.com', password: '{{qaPassword}}',
        stores: [{ storeId: '00000000-0000-0000-0000-000000000093', role: 'EMPLOYEE' }],
      },
      expect: 404,
    },
    {
      name: 'RF-18 crear usuario con sucursal repetida -> 400',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: {
        fullName: 'QA Store Repetida {{runId}}', email: 'qa.storerep.{{runId}}@minegocio.com', password: '{{qaPassword}}',
        stores: [
          { storeId: '{{storeA}}', role: 'EMPLOYEE' },
          { storeId: '{{storeA}}', role: 'STORE_ADMIN' },
        ],
      },
      expect: 400,
    },
    {
      name: 'RF-18 platformRole inyectado en body se ignora (siempre USER)',
      method: 'POST', path: '/api/admin/users', auth: 'super',
      body: {
        fullName: 'QA Inyectado {{runId}}', email: 'qa.inject.{{runId}}@minegocio.com', password: '{{qaPassword}}',
        platformRole: 'SUPER_ADMIN', active: false,
      },
      expect: 201, save: { userInj: 'id' },
      checks: [{ eq: 'platformRole', value: 'USER' }, { eq: 'active', value: true }],
    },
  ],
};