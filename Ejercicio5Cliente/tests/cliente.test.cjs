const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const vm=require('node:vm');
const {JSDOM,VirtualConsole}=require('jsdom');
const base=path.resolve('src/main/resources');
const user={nombre:'Usuario QA',correo_electronico:'qa@example.test',imagen_usuario:'#123456'};
const task={id_tarea:18,id_proyecto:17,nombre:"Tarea 'QA' <segura>",descripcion:'Descripcion',fecha_vencimiento:'2026-11-25',estado:'warning',lista:'pendientes'};
const project={id_proyecto:17,nombre:"Proyecto 'QA' <seguro>",descripcion:'Descripcion <texto>',fecha_inicio:'2026-11-19',fecha_finalizacion:'2026-11-25',estado_proyecto:'En proceso',fondo:'/Images/blanco.png'};
async function page(template,scripts,options={}){
 const errors=[];const virtualConsole=new VirtualConsole();virtualConsole.on('jsdomError',e=>errors.push(e.message));
 const html=template.startsWith('<')?template:fs.readFileSync(path.join(base,'templates',template),'utf8');
 const dom=new JSDOM(html,{url:'http://localhost:8082/proyecto?id_proyecto=17',runScripts:'outside-only',pretendToBeVisual:true,virtualConsole});
 const w=dom.window;w.localStorage.setItem('usuario',JSON.stringify(user));
 if(options.noUser)w.localStorage.removeItem('usuario');
 const requests=[];const modals=[];const alerts=[];const toasts=[];
 w.alert=m=>alerts.push(m);
 w.bootstrap={Toast:class{constructor(el){this.el=el;}show(){toasts.push(this.el.id);}}};
 new vm.Script(fs.readFileSync(path.join(base,'static/js/jquery-3.7.1.min.js'),'utf8'),{filename:path.join(base,'static/js/jquery-3.7.1.min.js')}).runInContext(dom.getInternalVMContext());
 const $=w.$;$.fn.modal=function(action){modals.push([this.attr('id'),action]);return this;};
 $.ajax=opts=>{
  requests.push(opts);
  const data=opts.data?JSON.parse(opts.data):{};
  const responses={
   '/consultarProyectos':[project],'/consultarUsuarios':[user],
   '/obtenerTareas':[task,{...task,id_tarea:19,nombre:'En curso',lista:'encurso'},{...task,id_tarea:20,nombre:'Terminado',lista:'terminado'}],
   '/actualizarProyecto':project,'/actualizarTarea':task,
   '/obtenerComentarios':[{id_usuario:'Usuario QA',fecha_hora:'2026-11-19 10:00:00',texto_comentario:'Texto <seguro>'}],
   '/verificarLogin':options.badLogin?{nombre:'000'}:user,
   '/verificarTarea':1,'/saveComentario':1,'/eliminarTarea':1,'/eliminarProyecto':1
  };
  if(options.failure){opts.error?.({status:500});return;}
  opts.success?.(responses[opts.url]??1);
 };
 for(const name of scripts){const f=path.join(base,'static/js',name);new vm.Script(fs.readFileSync(f,'utf8'),{filename:f}).runInContext(dom.getInternalVMContext());}
 await new Promise(resolve=>setTimeout(resolve,40));
 return {dom,w,$,requests,modals,alerts,toasts,errors,close:()=>w.close()};
}

test('login: valida vacios, correo invalido y credenciales antes de navegar',async()=>{
 const p=await page('login.html',['login.js']);try{
  p.w.validarLogin();assert.equal(p.toasts.at(-1),'alerta1');
  p.$('#exampleInputPassword1').val('QA');p.w.validarLogin();assert.equal(p.toasts.at(-1),'alerta2');
  p.$('#exampleInputEmail1').val('incorrecto');p.w.validarLogin();assert.equal(p.toasts.at(-1),'alerta3');
  p.$('#exampleInputEmail1').val('qa@example.test');p.$('#exampleInputPassword1').val('');p.w.validarLogin();assert.equal(p.toasts.at(-1),'alerta4');
  p.$('#exampleInputPassword1').val('QA');p.w.validarLogin();
  const request=p.requests.at(-1);assert.equal(request.url,'/verificarLogin');assert.equal(JSON.parse(request.data).correo_electronico,'qa@example.test');
  assert.equal(JSON.parse(p.w.localStorage.getItem('usuario')).nombre,'Usuario QA');
 }finally{p.close();}
});
test('login: usuario incorrecto no reemplaza la sesion',async()=>{
 const p=await page('login.html',['login.js'],{badLogin:true});try{p.$('#exampleInputEmail1').val('qa@example.test');p.$('#exampleInputPassword1').val('mala');p.w.validarLogin();assert.equal(p.toasts.at(-1),'alerta5');}finally{p.close();}
});
test('registro: valida formulario y asigna color de avatar',async()=>{
 const p=await page('register.html',['register.js']);try{
  const form=p.w.document.querySelector('form');let event=new p.w.Event('submit',{cancelable:true});form.dispatchEvent(event);assert.equal(event.defaultPrevented,true);assert.ok(form.classList.contains('was-validated'));
  for(const input of form.querySelectorAll('input[required]'))input.value=input.type==='email'?'qa@example.test':'QA';form.querySelector('select').value='Estudiante';
  event=new p.w.Event('submit',{cancelable:true});form.dispatchEvent(event);assert.equal(event.defaultPrevented,false);
  p.w.getRandomColor();assert.match(p.w.document.getElementById('imagenUsuario').value,/^#[0-9A-F]{6}$/);
 }finally{p.close();}
});
test('portada: registro opcional funciona con campos completos e incompletos',async()=>{
 const p=await page('<form id="form-registro"><input id="nombre"><input id="email"><input id="password"><p id="mensaje-error"></p></form>',['pplmproyecto.js']);try{
  const form=p.w.document.querySelector('form');let submitted=0;form.submit=()=>submitted++;
  form.dispatchEvent(new p.w.Event('submit',{cancelable:true}));assert.match(p.w.document.getElementById('mensaje-error').innerText,/complete todos/);assert.equal(submitted,0);
  for(const id of ['nombre','email','password'])p.w.document.getElementById(id).value='QA';form.dispatchEvent(new p.w.Event('submit',{cancelable:true}));assert.equal(submitted,1);
 }finally{p.close();}
});
test('portada: no requiere que exista un formulario de registro',async()=>{const p=await page('home.html',['pplmproyecto.js']);try{assert.equal(p.errors.length,0);}finally{p.close();}});

test('inicio: tarjetas, edicion, fondos y borrado usan datos correctos y texto escapado',async()=>{
 const p=await page('inicio.html',['inicio.js']);try{
  assert.equal(p.$('#cardContainer .card-title').text(),project.nombre);assert.equal(p.$('.card-title seguro').length,0);
  p.w.guardarRutaImagen('/Images/azulReal.png');assert.equal(p.$('#fondo').val(),'/Images/azulReal.png');
  p.$('#fondo').val('');p.w.asignarFondo();assert.equal(p.$('#fondo').val(),'/Images/blanco.png');
  p.$('#editar_fondo').val('');p.w.asignarFondoEditar();assert.equal(p.$('#editar_fondo').val(),'/Images/blanco.png');
  p.$('#editar_fondo').val('/Images/rosa.png');p.w.asignarFondoEditar();assert.equal(p.$('#editar_fondo').val(),'/Images/rosa.png');
  p.w.actualizarProyecto(17);assert.equal(p.$('#editarNombre').val(),project.nombre);assert.equal(p.$('#editar_fondo').val(),project.fondo);
  p.w.obtenerInfo(17,'hiddenId',project.nombre);assert.equal(p.$('#hiddenId').val(),'17');assert.ok(p.$('#textAlerta').get(0).innerText.includes(project.nombre));
  p.w.botonEliminar();assert.equal(JSON.parse(p.requests.at(-1).data).id_proyecto,'17');
  const dialog=p.w.document.getElementById('imagenesFondo');let closed=0;dialog.close=()=>closed++;p.w.cerrarFondos();assert.equal(closed,1);
 }finally{p.close();}
});
test('inicio: comunica fallos de API sin crear tarjetas ficticias',async()=>{const p=await page('inicio.html',['inicio.js'],{failure:true});try{assert.equal(p.$('#cardContainer').children().length,0);p.w.actualizarProyecto(17);assert.equal(p.modals.length,0);}finally{p.close();}});

test('tablero: separa listas, carga usuario y escapa comentarios',async()=>{
 const p=await page('proyecto.html',['proyecto.js']);try{
  assert.equal(p.$('#Nombre').text(),'Usuario QA');assert.equal(p.$('#IdProyectoURL').val(),'17');
  assert.equal(p.$('#tareasPendientes button').first().text(),task.nombre);assert.equal(p.$('#enCurso button').first().text(),'En curso');assert.equal(p.$('#trabajoTerminado button').first().text(),'Terminado');
  assert.equal(p.$('#tareasPendientes segura').length,0);assert.equal(p.$('#offcanvasScrollingLabel').text(),project.nombre);
  p.w.actualizarTarea(18);assert.equal(p.$('#descripcionT').val(),task.descripcion);assert.equal(p.$('#id_tarea').val(),'18');assert.equal(p.$('#inputFechas').val(),task.fecha_vencimiento);
  p.w.comentarios(18);assert.ok(p.$('#contenedorComentario').text().includes('Texto <seguro>'));assert.equal(p.$('#contenedorComentario seguro').length,0);
  p.w.obtenerInfo(18,'hiddenId',task.nombre);p.w.botonEliminar();assert.equal(JSON.parse(p.requests.at(-1).data).id_tarea,'18');
  p.w.IrInicio();
 }finally{p.close();}
});
test('tablero: controles alternan visibilidad y crean tareas en cada lista',async()=>{
 const p=await page('proyecto.html',['proyecto.js']);try{
  for(const [show,save,id,list] of [['newTarea','validarTarea','inputTarea','pendientes'],['newTarea2','validarTarea2','inputTarea2','encurso'],['newTarea3','validarTarea3','inputTarea3','terminado']]){
   const el=p.w.document.getElementById(id);const btn=p.w.document.getElementById(id.replace('input','btn'));el.style.display='none';btn.style.display='none';
   p.w[show]();assert.equal(el.style.display,'block');p.w[show]();assert.equal(el.style.display,'none');
   const before=p.requests.length;p.w[save]();assert.equal(p.requests.length,before);
   el.value='Nueva QA';p.w[save]();const data=JSON.parse(p.requests.at(-1).data);assert.equal(data.nombre,'Nueva QA');assert.equal(data.lista,list);assert.equal(data.id_proyecto,17);
  }
  const date=p.w.document.getElementById('inputFechas');date.style.display='none';p.w.Fechas();assert.equal(date.style.display,'block');p.w.Fechas();assert.equal(date.style.display,'none');
 }finally{p.close();}
});
test('comentarios: rechaza vacios y envia fecha ISO y autor de sesion',async()=>{
 const p=await page('proyecto.html',['proyecto.js']);try{
  p.$('#id_tarea').val('18');const before=p.requests.length;p.$('#comentarioT').val('  ');p.w.guardarComentario();assert.equal(p.requests.length,before);
  p.$('#comentarioT').val('Texto QA');p.w.guardarComentario();const data=JSON.parse(p.requests.at(-1).data);assert.equal(data.texto_comentario,'Texto QA');assert.equal(data.id_usuario,'Usuario QA');assert.match(data.fecha_hora,/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:00$/);
 }finally{p.close();}
});
test('tablero: maneja API fallida y sesion ausente',async()=>{
 const p=await page('proyecto.html',['proyecto.js'],{failure:true});try{p.w.actualizarTarea(18);p.w.llenarBarraLateral(17);p.$('#comentarioT').val('QA');p.w.guardarComentario();assert.equal(p.modals.length,0);}finally{p.close();}
 const q=await page('proyecto.html',['proyecto.js'],{noUser:true});try{assert.equal(q.requests.length,0);}finally{q.close();}
});
