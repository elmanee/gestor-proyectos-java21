from pathlib import Path
from PIL import Image,ImageDraw,ImageFont,ImageOps
import json,textwrap
R=Path(__file__).resolve().parents[1]; out=R/'tmp/video';out.mkdir(parents=True,exist_ok=True)
scenes=[
('Práctica 3 | Java 21 y CI','De dos proyectos heredados a una entrega verificable', ['Cliente: Java 17 → 21','API: Java 11 → 21','Jenkins: JDK 21 + Maven 3.9.11'],None,
'Este video resume la práctica de actualización y automatización del gestor de proyectos. Se utilizan capturas reales de Jenkins y SonarQube y resultados exportados de las ejecuciones. No es una grabación continua. La narración es sintética y sirve como apoyo para que el estudiante revise y explique su propio aprendizaje. El código está publicado en la cuenta elmanee, repositorio gestor proyectos java veintiuno.'),
('1. Actualización de los dos proyectos','Los POM originales se conservan como evidencia',['Spring Boot 3.5.16 y Jakarta Persistence','Compiler release 21; Surefire y JaCoCo','Sonar Maven: 5.5.0.6356'],None,
'El cliente utilizaba Java diecisiete y la API Java once. Los dos proyectos se configuraron para Java veintiuno. No bastó con cambiar el JDK del IDE: se actualizaron los archivos POM, el compilador y las herramientas de Jenkins. La API migró de javax a jakarta y conserva las columnas autoincrementales y las fechas de MariaDB. Se entregan dos POM originales y dos actualizados, junto con la explicación técnica.'),
('2. Ejecuciones reales de Jenkins','Evidencia del inicio de los pipelines',['Checkout desde el repositorio del estudiante','Compilación, pruebas y cobertura','Trivy → SonarQube → Quality Gate'],'jenkins-ejecutando.jpg',
'La captura muestra el inicio de ejecuciones reales. Cada pipeline descarga el código desde GitHub, muestra las versiones de Java y Maven, compila, ejecuta pruebas y verifica la cobertura. Después analiza la fuente y el inventario de dependencias con Trivy, envía resultados a SonarQube y espera el Quality Gate. Los resultados JUnit y los reportes se guardan como artefactos. El pipeline de base de datos añade una prueba contra MariaDB aislada.'),
('3. Los tres jobs terminaron en SUCCESS','Captura real de las ejecuciones #2',['gestor-frontend: SUCCESS','gestor-backend: SUCCESS','gestor-database: SUCCESS'],'jenkins-tres-success.jpg',
'En esta captura los tres jobs finalizaron correctamente: frontend, backend y base de datos. La primera ejecución del frontend había fallado por cobertura insuficiente del código JavaScript. La solución fue añadir pruebas reales e importar su reporte LCOV, conservando el código propio dentro del análisis. No se redujo el umbral del Quality Gate. Las consolas completas permiten revisar el commit, las etapas y el resultado de cada ejecución.'),
('4. Pruebas y cobertura','Las métricas se interpretan por separado',['52 pruebas Java del cliente; 16 de API','11 pruebas JavaScript; 100% líneas','Java: 100% métodos evaluables'],None,
'Las pruebas verifican altas, consultas, modificaciones y borrados, fechas, contraseñas, comentarios y errores de API. El cliente tiene cincuenta y dos pruebas Java y la API dieciséis. Once pruebas adicionales cubren el JavaScript propio con cien por ciento de líneas y funciones. JaCoCo alcanza cien por ciento de métodos Java evaluables. Solo se excluye la clase de arranque. La cobertura de ramas se informa por separado y no equivale a la cobertura de métodos. También se conservan ochenta y siete comprobaciones funcionales HTTP.'),
('5. SonarQube y seguridad','Los tres Quality Gates están aprobados',['Cliente: 99.7% de cobertura combinada','API: 94.8%; persistencia: 100%','Los hallazgos pendientes permanecen visibles'],'sonar-tres-aprobados-final.jpg',
'Esta captura muestra los tres Quality Gates aprobados y cobertura combinada de noventa y nueve punto siete por ciento en frontend. Un Quality Gate aprobado no significa que no exista deuda técnica. Los reportes conservan y clasifican los avisos. El inventario original detectaba noventa y dos hallazgos de dependencias en cliente y ciento dieciocho en API. Las actualizaciones redujeron cada servicio a dos avisos residuales de Spring. Sus condiciones requieren XSLT o eventos de servidor con fragmentos de vistas, funciones ausentes en esta configuración. No se ocultan los avisos ni se afirma que la biblioteca esté corregida.'),
('6. Persistencia y límites del laboratorio','La base original no se recrea para ejecutar CI',['dbgestor: puerto 3307; esquema validado','gestor_ci: MariaDB aparte en 13307','BCrypt nuevo; secretos fuera de Git'],None,
'La base original dbgestor se conserva en el puerto tres mil trescientos siete. Hibernate valida su esquema y no lo recrea. Las pruebas SQL trabajan en una MariaDB separada, con la base gestor CI, en el puerto trece mil trescientos siete. La prueba rechaza otra base. Las contraseñas nuevas utilizan BCrypt y no aparecen en el JSON. Se documentan las cuentas antiguas y la autorización pendiente por usuario o rol. Es un laboratorio educativo y no una declaración de preparación para producción.'),
('7. Entrega y explicación del aprendizaje','Revisa el guion y añade tu reflexión personal',['4 POM + informe PDF + ligas GitHub','JaCoCo, JUnit/Failsafe, Trivy y Sonar','Consolas, capturas y este video de apoyo'],None,
'La entrega incluye cuatro POM, el informe técnico, las ligas del cliente y la API, reportes de cobertura y pruebas, inventarios y análisis de seguridad, consolas de Jenkins, capturas y este video de apoyo. La principal idea técnica es distinguir compilación, pruebas, cobertura y análisis de seguridad. Un porcentaje alto no sustituye pruebas con aserciones ni una revisión de riesgos. Antes de entregar, el estudiante debe revisar el guion, comprobar que comprende los cambios y añadir con sus propias palabras qué problemas encontró y qué aprendió.')]
font=lambda n,b=False:ImageFont.truetype('C:/Windows/Fonts/'+('segoeuib.ttf' if b else 'segoeui.ttf'),n)
for i,(title,sub,bullets,shot,speech) in enumerate(scenes):
 im=Image.new('RGB',(1920,1080),'#101f33');d=ImageDraw.Draw(im);d.rectangle((0,0,1920,12),fill='#3dc9b0');d.text((70,48),title,font=font(48,True),fill='white');d.text((72,115),sub,font=font(28),fill='#bcd5e7')
 if shot:
  pic=Image.open(R/'entrega/parte2/capturas'/shot).convert('RGB');pic.thumbnail((1220,730));im.paste(pic,(60,205+(730-pic.height)//2));x,y,width=1320,245,31
 else:x,y,width=115,260,62
 for line in bullets:
  d.ellipse((x,y+12,x+12,y+24),fill='#3dc9b0')
  for seg in textwrap.wrap(line,width=width):d.text((x+32,y),seg,font=font(36,True),fill='white');y+=57
  y+=42
 d.line((70,975,1850,975),fill='#35506d',width=2);d.text((72,1000),'Evidencia real + explicación de apoyo | Voz sintética | 19 noviembre 2026',font=font(23),fill='#bcd5e7');d.text((1770,1000),f'{i+1}/8',font=font(24),fill='#bcd5e7')
 im.save(out/f'escena-{i+1}.png');(out/f'voz-{i+1}.txt').write_text(speech,encoding='utf-8')
(out/'escenas.json').write_text(json.dumps(scenes,ensure_ascii=False,indent=2),encoding='utf-8')
print('8 escenas y textos preparados')

(out/"narracion.json").write_text(json.dumps([x[4] for x in scenes],ensure_ascii=False),encoding="utf-8")

