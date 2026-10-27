'use strict';

// RF-15..RF-18: gestion del equipo por el STORE_ADMIN de la sucursal.
module.exports = {
  name: '04 Equipo de sucursal (RF-15..18)',
  cases: [
    {
      name: 'RF-15 admin A lista solo empleados de A',
      method: 'GET', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      expect: 200,
      checks: [
        { lengthAtLeast: 2 },
        { fieldIncludes: ['email', ['qa.emp.a.{run}@minegocio.com', 'qa.emp.a2.{run}@minegocio.com']] },
        { fieldExcludes: ['email', ['qa.admin.a.{run}@minegocio.com']] },
      ],
    },
    {
      name: 'RF-15 crear empleado con email duplicado -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      body: { fullName: 'Dup', email: 'qa.emp.a.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 409,
    },
    {
      name: 'RF-15 crear empleado con email de admin B -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      body: { fullName: 'Dup', email: 'qa.admin.b.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 409,
    },
    {
      name: 'RF-15 crear empleado con email de usuario sin membresia -> 409',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      body: { fullName: 'Dup', email: 'qa.nomember.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 409,
    },
    {
      name: 'RF-15 crear empleado con email invalido -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      body: { fullName: 'Invalido', email: 'sin-arroba', password: '{{qaPassword}}' }, expect: 400,
    },
    {
      name: 'RF-15 crear empleado con password corta -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      body: { fullName: 'Corta', email: 'qa.cortaemp.{{runId}}@minegocio.com', password: '123' }, expect: 400,
    },
    {
      name: 'RF-15 crear empleado sin fullName -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      body: { email: 'qa.sinnombreemp.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 400,
    },
    {
      name: 'RF-16 EMPLOYEE no lista el equipo -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/employees', auth: 'empA', expect: 403,
    },
    {
      name: 'RF-16 EMPLOYEE no crea empleados -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'empA',
      body: { fullName: 'X', email: 'qa.x.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 403,
    },
    {
      name: 'RF-16 admin B no gestiona equipo de A -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/employees', auth: 'adminB', expect: 403,
    },
    {
      name: 'RF-16 admin B no crea empleados en A -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/employees', auth: 'adminB',
      body: { fullName: 'X', email: 'qa.x2.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 403,
    },
    {
      name: 'RF-16 admin A no gestiona equipo de B -> 403',
      method: 'GET', path: '/api/stores/{{storeB}}/employees', auth: 'adminA', expect: 403,
    },
    {
      name: 'RF-16 admin A no crea empleados en B -> 403',
      method: 'POST', path: '/api/stores/{{storeB}}/employees', auth: 'adminA',
      body: { fullName: 'X', email: 'qa.x3.{{runId}}@minegocio.com', password: '{{qaPassword}}' }, expect: 403,
    },
    {
      name: 'RF-17 admin A edita empleado A',
      method: 'PUT', path: '/api/stores/{{storeA}}/employees/{{empAId}}', auth: 'adminA',
      body: { fullName: 'QA Empleado A Editado {{runId}}', email: 'qa.emp.a.{{runId}}@minegocio.com' },
      expect: 200, checks: [{ eq: 'fullName', value: 'QA Empleado A Editado {run}' }, { eq: 'role', value: 'EMPLOYEE' }],
    },
    {
      name: 'RF-17 editar email de empleado a uno existente -> 409',
      method: 'PUT', path: '/api/stores/{{storeA}}/employees/{{empAId}}', auth: 'adminA',
      body: { fullName: 'QA Empleado A', email: 'qa.emp.b.{{runId}}@minegocio.com' }, expect: 409,
    },
    {
      name: 'RF-17 editar email invalido de empleado -> 400',
      method: 'PUT', path: '/api/stores/{{storeA}}/employees/{{empAId}}', auth: 'adminA',
      body: { fullName: 'QA Empleado A', email: 'sin-arroba' }, expect: 400,
    },
    {
      name: 'RF-17 no editar a un STORE_ADMIN via employees -> 404',
      method: 'PUT', path: '/api/stores/{{storeA}}/employees/{{userA}}', auth: 'adminA',
      body: { fullName: 'X', email: 'qa.x4.{{runId}}@minegocio.com' }, expect: 404,
    },
    {
      name: 'RF-17 reset password de empleado A',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empAId}}/password', auth: 'adminA',
      body: { newPassword: '{{newQaPassword}}' }, expect: 204,
    },
    {
      name: 'RF-17 login empleado A con password reseteada',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.emp.a.{{runId}}@minegocio.com', password: '{{newQaPassword}}' },
      expect: 200, save: { tokenEmpA: 'token' },
    },
    {
      name: 'RF-15 restringir acceso de empleado A a la sucursal',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empAId}}/active', auth: 'adminA',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-15 empleado restringido no opera -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA', expect: 403,
    },
    {
      name: 'RF-15 cuenta de empleado restringido sigue pudiendo loguearse',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.emp.a.{{runId}}@minegocio.com', password: '{{newQaPassword}}' },
      expect: 200, save: { tokenEmpA: 'token' },
    },
    {
      name: 'RF-15 devolver acceso de empleado A',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empAId}}/active', auth: 'adminA',
      body: { active: true }, expect: 200, checks: [{ eq: 'active', value: true }],
    },
    {
      name: 'RF-15 empleado recupera acceso',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA', expect: 200,
    },
    {
      name: 'RF-16 EMPLOYEE no se autogestiona -> 403',
      method: 'PUT', path: '/api/stores/{{storeA}}/employees/{{empAId}}', auth: 'empA',
      body: { fullName: 'X', email: 'qa.emp.a.{{runId}}@minegocio.com' }, expect: 403,
    },
    {
      name: 'RF-15 desvincular empleado A2',
      method: 'DELETE', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}', auth: 'adminA', expect: 204,
    },
    {
      name: 'RF-15 empleado desvinculado no opera -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA2', expect: 403,
    },
    {
      name: 'RF-15 SUPER_ADMIN revincula empleado A2',
      method: 'POST', path: '/api/admin/users/{{empA2Id}}/stores', auth: 'super',
      body: { storeId: '{{storeA}}', role: 'EMPLOYEE' }, expect: 200,
      checks: [{ findEq: ['stores', 'storeId', 'storeA', 'role', 'EMPLOYEE'] }],
    },
    {
      name: 'RF-15 empleado revinculado recupera acceso',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA2', expect: 200,
    },
    {
      name: 'RF-15 admin A lista nuevamente a A2',
      method: 'GET', path: '/api/stores/{{storeA}}/employees', auth: 'adminA',
      expect: 200, checks: [{ fieldIncludes: ['email', ['qa.emp.a2.{run}@minegocio.com']] }],
    },
    {
      name: 'RF-15 restringir acceso de A2',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}/active', auth: 'adminA',
      body: { active: false }, expect: 200, checks: [{ eq: 'active', value: false }],
    },
    {
      name: 'RF-15 A2 restringido no opera -> 403',
      method: 'GET', path: '/api/stores/{{storeA}}/products', auth: 'empA2', expect: 403,
    },
    {
      name: 'RF-15 devolver acceso de A2',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}/active', auth: 'adminA',
      body: { active: true }, expect: 200,
    },
    {
      name: 'RF-17 admin A edita empleado A2',
      method: 'PUT', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}', auth: 'adminA',
      body: { fullName: 'QA Empleado A2 Editado {{runId}}', email: 'qa.emp.a2.{{runId}}@minegocio.com' },
      expect: 200, checks: [{ eq: 'fullName', value: 'QA Empleado A2 Editado {run}' }],
    },
    {
      name: 'RF-17 reset password de A2',
      method: 'PATCH', path: '/api/stores/{{storeA}}/employees/{{empA2Id}}/password', auth: 'adminA',
      body: { newPassword: '{{newQaPassword}}' }, expect: 204,
    },
    {
      name: 'RF-17 login A2 con nueva password',
      method: 'POST', path: '/api/auth/login', auth: 'none',
      body: { email: 'qa.emp.a2.{{runId}}@minegocio.com', password: '{{newQaPassword}}' },
      expect: 200, save: { tokenEmpA2: 'token' },
    },
  ],
};