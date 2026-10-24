'use strict';

// RF-01..05: autenticacion JWT, roles de plataforma y bloqueo de cuenta.
module.exports = {
  name: '01 Auth y roles (RF-01..05)',
  cases: [
    {
      name: 'RF-01 login SUPER_ADMIN valido',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: '{{seedSuperEmail}}', password: '{{seedPassword}}' },
      expect: 200, save: { tokenSuper: 'token' },
      checks: [
        { eq: 'platformRole', value: 'SUPER_ADMIN' },
        { eq: 'tokenType', value: 'Bearer' },
        { exists: 'token' }, { exists: 'userId' },
        { path: 'token', regex: '^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$' },
      ],
    },
    {
      name: 'RF-01 login STORE_ADMIN valido (A)',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.admin.a.{{runId}}@minegocio.com', password: '{{qaPassword}}' },
      expect: 200, checks: [{ eq: 'platformRole', value: 'USER' }, { exists: 'token' }],
    },
    {
      name: 'RF-01 login EMPLOYEE valido (A)',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.emp.a.{{runId}}@minegocio.com', password: '{{qaPassword}}' },
      expect: 200, checks: [{ eq: 'platformRole', value: 'USER' }, { exists: 'token' }],
    },
    {
      name: 'RF-02 login password incorrecta -> 401',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.admin.a.{{runId}}@minegocio.com', password: 'Incorrecta123*' },
      expect: 401,
    },
    {
      name: 'RF-02 login email inexistente -> 401',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'noexiste.{{runId}}@minegocio.com', password: '{{qaPassword}}' },
      expect: 401,
    },
    {
      name: 'RF-02 login email invalido -> 400',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'no-es-email', password: '{{qaPassword}}' },
      expect: 400,
    },
    {
      name: 'RF-02 login sin password -> 400',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.admin.a.{{runId}}@minegocio.com' },
      expect: 400,
    },
    {
      name: 'RF-03 endpoint protegido sin token -> 401',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'none',
      expect: 401,
    },
    {
      name: 'RF-03 token invalido -> 401',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'invalid',
      expect: 401,
    },
    {
      name: 'RF-03 JWT con firma falsa -> 401',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'none',
      headers: { Authorization: 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ4In0.firmafalsa' },
      expect: 401,
    },
    {
      name: 'RF-03 STORE_ADMIN en /api/admin/users -> 403',
      method: 'GET', path: '/api/admin/users', auth: 'adminA', expect: 403,
    },
    {
      name: 'RF-04 EMPLOYEE en /api/admin/users -> 403',
      method: 'GET', path: '/api/admin/users', auth: 'empA', expect: 403,
    },
    {
      name: 'RF-03 SUPER_ADMIN en /api/admin/users -> 200',
      method: 'GET', path: '/api/admin/users', auth: 'super',
      expect: 200, checks: [{ lengthAtLeast: 5 }, { idsIncludeVar: 'userA' }],
    },
    {
      name: 'RF-03 SUPER_ADMIN bloquea a admin A',
      method: 'PATCH', path: '/api/admin/users/{{userA}}/active', auth: 'super',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-03 login de usuario bloqueado -> 401',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.admin.a.{{runId}}@minegocio.com', password: '{{qaPassword}}' },
      expect: 401,
    },
    {
      name: 'RF-03 token vigente de cuenta bloqueada -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA', expect: 403,
    },
    {
      name: 'RF-03 SUPER_ADMIN desbloquea a admin A',
      method: 'PATCH', path: '/api/admin/users/{{userA}}/active', auth: 'super',
      body: { active: true }, expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-03 token previo recupera acceso tras desbloquear',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'adminA',
      expect: 200, checks: [{ lengthAtLeast: 1 }],
    },
    {
      name: 'RF-01 login admin A tras desbloquear',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.admin.a.{{runId}}@minegocio.com', password: '{{qaPassword}}' },
      expect: 200, save: { tokenAdminA: 'token' }, checks: [{ eq: 'platformRole', value: 'USER' }],
    },
    {
      name: 'RF-01 login seed carlos (STORE_ADMIN)',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'carlos@minegocio.com', password: '{{seedPassword}}' },
      expect: 200, checks: [{ eq: 'platformRole', value: 'USER' }],
    },
    {
      name: 'RF-01 login seed maria (EMPLOYEE)',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'maria@minegocio.com', password: '{{seedPassword}}' },
      expect: 200, save: { tokenSeedEmp: 'token' }, checks: [{ eq: 'platformRole', value: 'USER' }],
    },
    {
      name: 'RF-03 STORE_ADMIN seed no entra a /api/admin/users -> 403',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'carlos@minegocio.com', password: '{{seedPassword}}' },
      expect: 200, save: { tokenSeedAdmin: 'token' },
    },
    {
      name: 'RF-03 STORE_ADMIN seed en /api/admin/users -> 403',
      method: 'GET', path: '/api/admin/users', auth: 'seedAdmin', expect: 403,
    },
    {
      name: 'RF-22 EMPLOYEE seed lee catalogo de su sucursal -> 200',
      method: 'GET', path: '/api/stores/{{seedStoreId}}/categories', auth: 'seedEmp', expect: 200,
    },
    {
      name: 'RF-04 SUPER_ADMIN no opera catalogo de sucursal -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/categories', auth: 'super', expect: 403,
    },
  ],
};