'use strict';

// RF-10/RF-20: logo de sucursal y foto de producto (Cloudinary).
// Si Cloudinary no responde, la suite lo refleja como fallo de entorno (502).
module.exports = {
  name: '08 Fotos y logo (RF-10/20)',
  cases: [
    {
      name: 'RF-20 admin A sube foto de producto Alfa',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminA', form: 'valid-120.png', expect: 200,
      checks: [{ exists: 'url' }, { path: 'url', regex: '^https?://' }],
    },
    {
      name: 'RF-20 GET producto muestra imageUrl',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA',
      expect: 200, checks: [{ exists: 'imageUrl' }],
    },
    {
      name: 'RF-20 admin A borra foto de producto',
      method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminA', expect: 204,
    },
    {
      name: 'RF-20 GET producto muestra imageUrl null tras borrar',
      method: 'GET', path: '/api/stores/{{storeA}}/products/{{prodAlfa}}', auth: 'adminA',
      expect: 200, checks: [{ null: 'imageUrl' }],
    },
    {
      name: 'RF-10 admin A sube logo de sucursal',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/logo',
      auth: 'adminA', form: 'valid-120.png', expect: 200,
      checks: [{ exists: 'url' }, { path: 'url', regex: '^https?://' }],
    },
    {
      name: 'RF-10 GET sucursal muestra logoUrl',
      method: 'GET', path: '/api/admin/stores/{{storeA}}', auth: 'super',
      expect: 200, checks: [{ exists: 'store.logoUrl' }],
    },
    {
      name: 'RF-10 admin A borra logo',
      method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/logo', auth: 'adminA', expect: 204,
    },
    {
      name: 'RF-10 GET sucursal muestra logoUrl null',
      method: 'GET', path: '/api/admin/stores/{{storeA}}', auth: 'super',
      expect: 200, checks: [{ null: 'store.logoUrl' }],
    },
    {
      name: 'RF-20 EMPLOYEE no sube foto de producto -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'empA', form: 'valid-120.png', expect: 403,
    },
    {
      name: 'RF-10 EMPLOYEE no sube logo -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/logo',
      auth: 'empA', form: 'valid-120.png', expect: 403,
    },
    {
      name: 'RF-20 SUPER_ADMIN no sube foto de producto -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'super', form: 'valid-120.png', expect: 403,
    },
    {
      name: 'RF-10 SUPER_ADMIN si puede subir logo',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/logo',
      auth: 'super', form: 'valid-120.png', expect: 200, checks: [{ exists: 'url' }],
    },
    {
      name: 'RF-10 SUPER_ADMIN borra logo',
      method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/logo', auth: 'super', expect: 204,
    },
    {
      name: 'RF-20 foto de producto de otra sucursal -> 404',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodB}}/photo',
      auth: 'adminA', form: 'valid-120.png', expect: 404,
    },
    {
      name: 'RF-20 imagen 1x1 rechazada por dimensiones -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminA', form: 'pixel.png', expect: 400,
    },
    {
      name: 'RF-20 archivo que no es imagen -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminA', form: 'fake-image.png', expect: 400,
    },
    {
      name: 'RF-20 multipart sin campo file -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminA', formOther: true, expect: 400,
    },
    {
      name: 'RF-20 borrar foto inexistente es idempotente -> 204',
      method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminA', expect: 204,
    },
    {
      name: 'RF-10 borrar logo inexistente es idempotente -> 204',
      method: 'DELETE', path: '/api/stores/{{storeA}}/foto-config/logo', auth: 'adminA', expect: 204,
    },
    {
      name: 'RF-20 admin B no sube foto en A -> 403',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/products/{{prodAlfa}}/photo',
      auth: 'adminB', form: 'valid-120.png', expect: 403,
    },
    {
      name: 'RF-10 archivo invalido como logo -> 400',
      method: 'POST', path: '/api/stores/{{storeA}}/foto-config/logo',
      auth: 'adminA', form: 'fake-image.png', expect: 400,
    },
  ],
};