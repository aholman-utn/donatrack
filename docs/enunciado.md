# DonaTrack - Sistema de Gestión y Trazabilidad de Donaciones

**Trabajo Práctico Anual Integrador - 2026**
**Cátedra:** Diseño de Sistemas de Información (UTN.BA)
**Curso:** K 3002 - Viernes Turno Mañana

---

## Tabla de contenido
- Marco General Trabajo Práctico Anual
- Prólogo
- Decálogo de Consideraciones para el Trabajo Práctico Anual
- Contexto general
    - Marco Institucional
    - Contexto
    - Nuestro sistema
- Requerimientos mínimos de UI/UX del TP Anual
    - Acceso
    - Persona donante
    - Entidad beneficiaria
    - Persona administradora
- ENTREGA 1: Arquitectura y Modelado en Objetos - Parte I
- ENTREGA 2: Arquitectura y Modelado en Objetos - Parte II
- ENTREGA 3: Arquitectura y Modelado en Objetos - Parte III
- ENTREGA 4: Persistencia, Integración y Despliegue
- ENTREGA 5: Persistencia
- ENTREGA 6: Interfaz de Usuario y Arquitectura MVC
- ENTREGA 7: Observabilidad y Seguridad
- Anexo - Links de interés

---

## Marco General Trabajo Práctico Anual

### Prólogo
El Trabajo Práctico Anual Integrador (TPA) constituye el eje central de la cursada y tiene como objetivo principal acompañar al estudiantado en el diseño, construcción y evolución de un sistema de mediana complejidad, representativo de escenarios reales de desarrollo profesional.
El Trabajo Práctico 2026 se inscribe en una línea de continuidad pedagógica y conceptual iniciada en años anteriores, manteniendo una estructura común que permite que, año a año, los trabajos prácticos compartan una misma identidad, profundidad y coherencia, independientemente del dominio específico abordado.
El enfoque adoptado propone el análisis, diseño e implementación de un sistema distribuido, concebido desde su origen como un conjunto de componentes o servicios que interactúan entre sí, y que evolucionan de manera incremental a lo largo de las distintas entregas. Esta evolución no es meramente funcional, sino que busca reflejar cómo los sistemas reales crecen, se complejizan y deben adaptarse a nuevas necesidades, restricciones y contextos operativos.
A lo largo del TP se pondrá especial énfasis en el modelado del dominio, la toma de decisiones de diseño justificadas, la separación de responsabilidades y el análisis de trade-offs arquitectónicos. Las tecnologías utilizadas se consideran un medio y no un fin: su selección y uso deberán responder a decisiones de diseño previamente fundamentadas.
Asimismo, el trabajo práctico introduce de manera progresiva problemáticas propias de sistemas reales, tales como la arquitectura distribuida, la comunicación entre componentes, y la diferenciación entre procesos sincrónicos y asincrónicos, permitiendo reflexionar sobre consistencia, latencia, fallos parciales, escalabilidad y disponibilidad.
El TP está estructurado en entregas incrementales que incorporan gradualmente nuevas responsabilidades: persistencia, integración, exposición de servicios, interfaces de usuario, despliegue, observabilidad y seguridad. Cada etapa se apoya en las decisiones tomadas anteriormente, reforzando la idea de aprendizaje acumulativo y de diseño evolutivo.
Finalmente, el Trabajo Práctico Anual busca que las y los estudiantes no solo logren un sistema funcional, sino que desarrollen una mirada sistémica y profesional del diseño de sistemas de información, comprendiendo que diseñar implica justificar, anticipar consecuencias y asumir compromisos técnicos en contextos de incertidumbre.

### Decálogo de Consideraciones para el Trabajo Práctico Anual
1. **Mantener un dominio rico, realista y con impacto social**
   Justificación: Un dominio verosímil, con actores reales (ONGs, Estado, ciudadanía, empresas), favorece decisiones de diseño no triviales y discusiones auténticas. El impacto social agrega motivación y obliga a considerar aspectos éticos, legales y de responsabilidad del software.
2. **Diseñar el TP como un sistema evolutivo e incremental**
   Justificación: El TP debe construirse por capas, donde cada entrega amplía o tensiona decisiones previas. Se refuerza así la idea de que el software evoluciona y que las decisiones tempranas condicionan el crecimiento del sistema.
3. **Separar claramente dominio, arquitectura y tecnología**
   Justificación: El foco debe estar en el modelado del dominio y las decisiones arquitectónicas. La tecnología aparece como consecuencia del diseño y no como punto de partida, evitando que el TP se reduzca a un ejercicio puramente técnico.
4. **Proponer decisiones de diseño de requisitos no triviales**
   Justificación: Requisitos como consistencia, tolerancia a fallos, seguridad o integración obligan a aplicar patrones, principios de diseño y análisis de trade-offs, evitando soluciones lineales o simplistas.
5. **Incorporar explícitamente Arquitectura Distribuida**
   Justificación: El TP debe concebirse como un sistema que se ejecuta en múltiples nodos o servicios. Esto habilita el análisis de problemas reales como latencia, fallos parciales, consistencia eventual, desacoplamiento y comunicación entre componentes.
6. **Modelar y justifycar procesos sincrónicos y asincrónicos**
   Justificación: El diseño debe contemplar qué operaciones requieren respuesta inmediata y cuáles pueden resolverse de forma diferida. Esto permite introducir colas de mensajería, tareas programadas y flujos asincrónicos como decisiones conscientes y justificadas.
7. **Introducir gradualmente preocupaciones transversales**
   Justificación: Aspectos como seguridad, observabilidad, performance y escalabilidad deben aparecer progresivamente, acompañando la madurez del sistema y reflejando cómo emergen estos problemas en escenarios reales.
8. **Exigir justificaciones explícitas de diseño en cada entrega**
   Justificación: No se evalúa solo el resultado funcional, sino la capacidad de argumentar decisiones técnicas, explicar alternativas descartadas y comprender los compromisos asumidos en cada etapa.
9. **Permitir variabilidad controlada sin romper la estructura**
   Justificación: El TP debe habilitar alternativas tecnológicas o de implementación (protocolos, frameworks, estilos de API) sin perder una base común que garantice coherencia pedagógica y comparabilidad entre trabajos.
10. **Visión sistémica y profesional**
    Justificación: La entrega final debe integrar diseño, despliegue, operación y monitoreo, mostrando que el sistema es coherente, desplegable y operable, reforzando una mirada profesional del desarrollo de sistemas.

---

## Contexto general

### Marco Institucional
UTN Solidaria es una iniciativa de la Subsecretaría de Asuntos Estudiantiles de nuestra Facultad, orientada a acompañar y asistir a personas en situación de vulnerabilidad a través de distintas acciones solidarias organizadas por estudiantes y voluntarios. Se constituye como un espacio de articulación entre estudiantes, docentes y graduados, con el propósito de canalizar acciones solidarias que generen un impacto positivo y sostenible en la sociedad.
El área busca complementar la formación académica de los futuros profesionales, impulsando valores como la responsabilidad social, la empatía y el trabajo colaborativo. En este marco, UTN Solidaria se posiciona como un puente entre la universidad y la comunidad, promoviendo la participación activa frente a problemáticas sociales y fomentando el desarrollo de soluciones concretas desde una perspectiva interdisciplinaria.
Entre sus principales líneas de acción se destaca el reparto de viandas a personas en situación de vulnerabilidad en los alrededores de la sede Medrano y de donaciones recibidas de alimentos no perecederos, ropa, abrigo, colchones y frazadas, el acompañamiento a instituciones educativas y comunitarias como el Hospital Gandulfo y un hogar de niños, y la generación de espacios de voluntariado que permitan a los participantes involucrarse de manera directa con diversos contextos sociales. Su trabajo se sostiene gracias al compromiso de los voluntarios y a las donaciones de quienes confían y acompañan cada una de las propuestas.
En el marco del presente trabajo práctico de grado, se propone analizar y modelar los distintos procesos que conforman UTN Solidaria, utilizando esta iniciativa como caso de estudio, y llevarla a la práctica mediante el diseño y desarrollo de una solución tecnológica que integre herramientas y enfoques propios de la Ingeniería en Sistemas de Información.
De este modo, se busca articular la formación tecnológica con el compromiso social, contribuyendo a la formación de profesionales no solo altamente capacitados, sino también conscientes de su rol en la sociedad y comprometidos con la transformación de su entorno.

### Contexto
Una organización sin fines de lucro dedicada a la recolección y distribución de donaciones de bienes materiales enfrenta dificultades para gestionar y dar trazabilidad a los recursos que recibe. La falta de un sistema unificado provoca desorden, duplicación de registros y poca claridad sobre el destino de las donaciones, perjudicando la transparencia y eficiencia.

### Nuestro sistema
A partir de la problemática identificada y presentada en la sección anterior, se propone el diseño y desarrollo de DonaTrack: una solución digital orientada a organizar, registrar y monitorear las donaciones desde su recepción en el depósito de la organización hasta su entrega a entidades beneficiarias que las requieran. El objetivo del sistema será mejorar la distribución de recursos y, al mismo tiempo, fortalecer la confianza de personas donantes y entidades beneficiarias de las donaciones.

En este marco, diseñaremos e implementaremos una arquitectura distribuida en servicios:
- **un servicio de gestión de personas donantes, entidades beneficiarias y donaciones** (en adelante, Servicio de Donaciones), que permitirá el alta, baja y modificación de personas donantes (humanas o jurídicas), registro de donaciones con trazabilidad y auditoría de estados y de entidades beneficiarias.
- **un servicio de logística y gestión de camiones y entregas** (en adelante, Servicio de Logística), que garantizará la generación de rutas de los camiones de la organización para la entrega de las donaciones y la trazabilidad de las mismas durante este proceso.
- **un servicio de incentivos a donantes y misiones** (en adelante, Servicio de Incentivos), que integrará funcionalidades de analítica para procesar la actividad de donación, calcular el progreso de los donantes y otorgar incentivos simbólicos como insignias y ascensos de categoría.
- **un servicio de notificaciones**, que permitirá envío de notificaciones por distintos medios ante cambios de estado de las donaciones, asignaciones, entregas de donaciones realizadas y recompensas desbloqueadas.
- **un servicio de autenticación**;
- **un componente de front para server-side rendering** (se explicará en la entrega VI).

---

## Requerimientos mínimos de UI/UX del TP Anual

### Acceso
1. El sistema debe contar con una landing page accesible públicamente que permita:
   - Presentar el propósito y objetivos de DonaTrack.
   - Mostrar ejemplos destacados de donaciones realizadas en el último mes.
   - Incluir enlaces de acceso a:
     i. Visualización de fotos de donaciones entregadas sin login.
     ii. Registro o inicio de sesión para personas donantes y beneficiarias.
     iii. Información legal y de privacidad.
2. El sistema debe mostrar las donaciones ya entregadas a entidades beneficiarias en un mapa interactivo y permitir ver sus detalles al hacer clic en cada marcador.
3. El sistema debe permitir el registro de personas donantes y entidades beneficiarias.
4. El sistema debe permitir el inicio de sesión de personas donantes, entidades beneficiarias y personas administradoras.

### Persona donante
5. La persona donante debe poder filtrar sus donaciones realizadas por estado y categoría o subcategoría/s de bienes entregados.
6. La persona donante debe poder navegar por las entidades beneficiarias registradas.
7. La persona humana donante debe poder acceder a una sección de Incentivos y consultar misiones a realizar e insignias obtenidas.
8. La persona donante debe recibir notificaciones cuando una donación realizada por ella es asignada, cuando cumplió una misión, cuando sube de categoría o cuando la entidad beneficiaria asignada recibe la donación realizada.
9. La persona donante debe poder seguir en un mapa las entregas activas de las donaciones que realizó, visualizando el/los camiones asociados con ubicación y última actualización.

### Entidad beneficiaria
10. La entidad beneficiaria debe poder registrar necesidades materiales concretas.
11. La entidad beneficiaria debe poder ver el estado de las donaciones asignadas a ella.
12. La entidad beneficiaria debe poder confirmar la recepción de una donación, cargando fotos de la misma.
13. La entidad beneficiaria debe recibir notificaciones en base a la asignación de donaciones y confirmación de recepción de entrega.
14. La entidad beneficiaria debe poder seguir en un mapa las entregas activas de las donaciones que debe recibir, visualizando el/los camiones asociados con ubicación y última actualización.

### Persona administradora
15. La persona administradora debe registrar personas donantes y donaciones que haya recibido en el depósito, asociándolas a la persona donante correspondiente.
16. La persona administradora debe poder actualizar el estado de las donaciones disponibles en el depósito, en caso de que estén vencidas.
17. La persona administradora debe poder seleccionar la entidad beneficiaria final de una donación pendiente de asignación, a partir del resultado de ejecución de los algoritmos de selección.
18. La persona administradora debe poder administrar los camiones disponibles.
19. La persona administradora debe poder visualizar el ranking mensual de donantes más activos y el historial de rankings anteriores.
20. La persona administradora debe poder importar personas donantes ya registradas para otorgarles un usuario desde archivos CSV con más de 10.000 filas.

---

## ENTREGA 1: Arquitectura y Modelado en Objetos - Parte I

### Objetivos de la entrega
- Entrar en contacto con el dominio y sus principales abstracciones.
- Incorporar de forma paulatina conceptos y principios de Diseño.
- Familiarizarse con el entorno de desarrollo y las principales tecnologías a ser aplicadas a lo largo del Trabajo Práctico.
- Familiarizarse con la arquitectura del sistema.
- Incorporar nociones de Diseño UI/UX.

### Unidades del Programa Vinculadas
- Unidad 2: Herramientas de Concepción y Comunicación del Diseño.
- Unidad 3: Diseño con Objetos.
- Unidad 4: Diseño de Interfaz de Usuario.
- Unidad 6: Diseño de Arquitectura.
- Unidad 8: Validación del Diseño.

### Alcance
- Servicio de Donaciones - Gestión de Donantes y Donaciones.
- Servicio de Donaciones - Importación masiva de donantes mediante archivo CSV.
- Servicio de Donaciones - Gestión de Entidades Beneficiarias y Necesidades.
- Bocetos de interfaz de usuario.

### Dominio

**Servicio de Donaciones - Donantes**
Las personas donantes son personas humanas o jurídicas que desean aportar a las entidades beneficiarias. Estas podrán registrarse en la plataforma previo a llevar una donación al depósito. A las personas humanas se les solicita nombre, apellido, edad, número de documento, género y dirección y al menos un medio de contacto (en forma obligatoria correo electrónico y en forma opcional teléfono y/o WhatsApp). A su vez, podrá determinar cuál de ellos será su medio de contacto predeterminado para recibir notificaciones del sistema.
Por otra parte, las personas jurídicas deben indicar razón social, su tipo (Gubernamental, ONG, Empresa, Institución), rubro y al menos un medio de contacto. Cada organización tendrá personas representantes habilitadas a operar en su nombre.

**Servicio de Donaciones - Registro de personas donantes**
Las personas donantes (o en caso de las jurídicas, un representante) se acercarán al depósito con el objetivo de ingresar una donación. En caso de que no tengan un usuario en la plataforma, una persona administradora les pedirá los datos de registro y se les enviará un correo electrónico de bienvenida para que accedan por primera vez.

**Servicio de Donaciones - Donaciones y segmentación**
Una vez registrada la persona donante, o confirmado que ya cuenta con un usuario, la persona administradora que esté en el depósito en ese momento completará el formulario de la donación en su nombre.
Se debe incluir una descripción general y los bienes que contiene. De cada bien se conoce una descripción y, opcionalmente, se le asocia una foto. Estos pertenecen a una categoría (mobiliario, alimentos, vestimenta, etc.). Para cada categoría existen múltiples subcategorías.
La subcategoría constituye la unidad mínima de asignación dentro del sistema, permitiendo identificar con precisión qué bien se necesita o se dona.
Se registrará la totalidad de los bienes a donar en una única carga dentro del sistema, a partir de la cual el sistema realizará automáticamente una segmentación interna, generando múltiples posibles donaciones independientes agrupadas obligatoriamente por subcategorías. De este modo, cada donación resultante quedará asociada a una única subcategoría, constituyendo la unidad mínima de asignación.
En el caso de bienes perecederos, el sistema podrá generar donaciones separadas cuando existan diferencias en la fecha de vencimiento. Asimismo, para aquellos bienes cuyo estado sea relevante (como mobiliario o vestimenta) deberá consignarse si se trata de artículos nuevos o usados, a fin de asegurar una correcta evaluación y asignación posterior.

**Servicio de Donaciones - Entidades beneficiarias y necesidades**
Las entidades beneficiarias son organizaciones sin fines de lucro que se registran para recibir donaciones. De cada entidad se conoce su razón social, dirección completa, teléfono y correos de las personas representantes designadas.
Las entidades beneficiarias podrán registrar sus necesidades materiales indicando subcategoría y una breve descripción. La plataforma distinguirá entre necesidades recurrentes (consumos habituales) y extraordinarias (situaciones puntuales o emergencias).
- **Necesidades extraordinarias:** surgen ante situaciones excepcionales (como mudanzas, inundaciones, incendios o vencimientos próximos de insumos). La cantidad solicitada puede cubrirse mediante donaciones parciales.
- **Necesidades recurrentes:** están vinculadas al funcionamiento habitual de la organización e implican bienes de consumo periódico. Se satisfacen dentro del período correspondiente (por ejemplo, mensual), según la cantidad objetivo definida para ese período.

**Servicio de Donaciones - Importación masiva de personas donantes por CSV**
Dado que la ONG ya cuenta con un histórico de personas donantes, se debe permitir la migración de su información en forma masiva importando un archivo .csv. Cada línea representará una persona donante (humana o jurídica) e incluirá los campos mínimos para identificarla y contactarla. En caso de que un registro ya exista (esto quiere decir que el correo electrónico ya se encuentra registrado en el servicio) se deberá actualizar su información; en caso contrario, se lo deberá crear el usuario y enviarle sus credenciales de acceso.

| Tipo Persona | TipoDoc | Documento | Nombre/Razón Social | Email | Teléfono |
|---|---|---|---|---|---|
| HUMANA | DNI | 12345678 | Ana Pérez | ana@mail.com | +54 11 5555-5555 |
| JURIDICA | CUIT | 30-12345678-9 | Arcos Plateados S.A. | contacto@empresa.com | +54 11 4444-4444 |

**Bocetos de interfaz de usuario**
Nuestro sistema contará con una interfaz web accesible para personas donantes, entidades beneficiarias y personas administradoras de la plataforma. Con el objetivo de familiarizarse con ella, sus flujos, y usuarios finales, se solicita realizar bocetos de las principales interfaces de usuario del sistema.

### Requerimientos detallados
**Requerimientos de dominio**
1. El sistema deberá permitir la gestión de personas donantes.
2. El sistema deberá permitir la gestión de las donaciones resultantes.
3. El sistema deberá permitir la gestión de entidades beneficiarias y sus necesidades.
4. El sistema deberá permitir la importación masiva de donantes en CSV.

**Requerimientos de implementación**
5. El sistema deberá implementarse como una aplicación multimódulo.

### Entregables
1. Modelo del Dominio: diagrama de clases inicial que contemple las funcionalidades requeridas. Se solicita realizar un diagrama de clases por servicio.
2. Diagramas de Arquitectura: diagrama de despliegue, componentes y/o cualquier otro tipo de diagrama que refleje la arquitectura física y lógica de la entrega actual.
3. Justificaciones de Diseño Iniciales.
4. Diagrama General de Casos de Uso.
5. Bocetos de interfaz de usuario que cumplan con los requerimientos detallados (se sugiere la utilización de IA).
6. Implementación de los requerimientos.

---

## ENTREGA 2: Arquitectura y Modelado en Objetos - Parte II

### Objetivos de la entrega
- Diseñar e implementar, de manera incremental, las nuevas funcionalidades.
- Incorporar nociones de ejecuciones de tareas asincrónicas y/o calendarizadas.
- Familiarizarse con el concepto de APIs como mecanismo de integración y su consumo, y exponer servicios a través de un protocolo de red.

### Unidades del Programa Vinculadas
- Unidad 2: Herramientas de Concepción y Comunicación del Diseño.
- Unidad 3: Diseño con Objetos.
- Unidad 6: Diseño de Arquitectura.
- Unidad 7: Integración de Sistemas.
- Unidad 8: Validación del Diseño.

### Alcance
- Servicio de Donaciones - Trazabilidad de las donaciones.
- Servicio de Notificaciones - Integración concreta con distintos medios de notificación.
- Servicio de Donaciones - Asignación de Necesidades Materiales.
- Servicio de Incentivos - Analítica de Donantes y Sistema de Recompensas.
- Exposición REST de los servicios.

### Dominio

**Servicio de Donaciones - Estados de las donaciones**
El sistema deberá garantizar la trazabilidad y auditoría de los estados de cada donación. Al momento de registrarse, la donación quedará en estado "En depósito".
Al día siguiente, cuando se ejecute el algoritmo de asignación, si la donación es asignada a una entidad beneficiaria quedará en estado "Asignación realizada", en caso contrario permanece en el depósito.
La donación pasará a estar "Lista para entregar" cuando se haya planificado una ruta que incluya la donación.
Pasará a "En traslado" cuando el camión asignado para la entrega haya iniciado el recorrido. Finalmente, cuando la entidad beneficiaria haya confirmado su entrega, quedará en estado "Entregada".
Si no se la pudo entregar el día estipulado, quedará en estado "Entrega fallida" y volverá al depósito. Si esto sucediera, se solicita registrar una justificación.
Las personas administradoras encargadas del depósito podrán cambiar el estado de una donación a "Vencida".

**Servicio de Donaciones - Asignación de las donaciones**
Por cada una de las donaciones cuyo estado sea "En Depósito", el sistema debe ejecutar un proceso de matchmaking con el objetivo de asignar el destino de las mismas, es decir, a qué entidad beneficiaria será entregada.
Se cuenta con dos algoritmos que proponen hasta diez entidades beneficiarias:
- **Algoritmo de Compatibilidad Semántica:** analiza la correspondencia entre las características del bien donado y las necesidades declaradas por cada entidad beneficiaria.
- **Algoritmo de Prioridad a sub-atendidos:** asigna prioridad a organizaciones que hayan recibido menos donaciones en el último trimestre.
A partir de los resultados, este componente puede filtrar automáticamente las entidades beneficiarias que hayan aparecido en la ejecución de ambos algoritmos para que una persona administradora confirme el destino final. Para asegurarse de que la ejecución de los algoritmos no degrade el desempeño del sistema, se solicita que el mismo se realice en horarios de baja carga.

**Servicio de Incentivos - Analítica de donantes**
Este servicio tiene como objetivo consolidar y analizar los datos de donaciones realizadas en nuestro sistema para generar métricas de impacto y comportamiento, y a partir de ellas implementar un sistema de incentivos basado en misiones y categorías.
La persona donante podrá observar en su perfil sus totales históricos de donaciones, gráficos de evolución por período, comparaciones mensuales y el número total de organizaciones que ha ayudado. También podrá visualizar su posición en el ranking de donantes activos.

**Servicio de Incentivos - Recompensas**
Se establecen tres categorías de donante: Colaborador, Sostenedor y Transformador. Para subir de categoría, las personas donantes deberán completar una serie de misiones específicas. Las misiones se completan en orden secuencial dentro de cada categoría.
Cada misión completada le otorga a la persona donante una insignia.
Algunos ejemplos de misiones son:
- Racha: realizar una donación durante X meses consecutivos.
- Completitud: realizar donaciones de X categorías distintas.
- Hábil Donador: donación que supere X cantidad de bienes.
- Donaciones Exitosas: lograr X donaciones que sean recibidas exitosamente por una entidad beneficiaria.
El donante deberá poder visualizar en todo momento el progreso de su misión actual y la distancia restante respecto del objetivo establecido.

**Servicio de Incentivos - Difusión de Insignias y ranking mensual**
Cada vez que una persona donante obtenga una insignia, el sistema activará automáticamente un flujo de procesamiento. Este flujo recibirá un texto descriptivo y generará una imagen asociada a la insignia obtenida. Posteriormente, publicará el contenido en una red social (utilizando n8n u otra herramienta).
Al cierre de cada mes, un flujo programado ejecutará un proceso de ranking mensual de donantes más activos (destacando a los 3 primeros).

**Servicio de Notificaciones y Eventos**
El sistema deberá enviar notificaciones a personas donantes y entidades beneficiarias cuando ocurra un evento relevante.
Para esta entrega, se requiere notificar:
- A una persona donante, cuando no registre interacción con la plataforma durante más de 20 días.
- A una entidad beneficiaria, cuando se le asigna una donación en base a sus necesidades recurrentes o extraordinarias.
- A una persona donante, cuando haya realizado una donación que acaba de ser asignada.
- A una persona donante, cuando haya cumplido una misión.
- A una persona donante, cuando haya cambiado de categoría.
En esta iteración, se solicita simular la llamada a servicios externos para enviar por correo, SMS o WhatsApp.

**Exposición REST de los servicios - Parte I**
Se deberán exponer las siguientes operaciones en una API REST:
*Servicio de Donaciones - Gestión de Personas Donantes y Donaciones:*
- Operaciones CRUD sobre las donaciones.
- Operaciones CRUD sobre las personas donantes (jurídicas y humanas).
- Cambios de estado de una donación, garantizando trazabilidad y auditoría.
*Servicio de Donaciones - Gestión de Entidades Beneficiarias y Necesidades:*
- Operaciones CRUD sobre las entidades beneficiarias.
- Operaciones CRUD sobre las necesidades materiales (recurrentes y extraordinarias).
- Obtención del ranking generado por los algoritmos de asignación y selección de entidad beneficiaria final.
- Ejecución a demanda de los algoritmos de asignación de necesidades.
*Servicio de Incentivos:*
- Obtención de métricas de actividad de una persona donante.
- Obtención de las misiones disponibles para una persona donante.
- Obtención de las insignias para una persona donante.

### Requerimientos detallados
1. El sistema debe garantizar la trazabilidad de los estados de las donaciones, desde su recepción hasta su entrega.
2. El sistema debe garantizar el envío de notificaciones por distintos medios.
3. El sistema debe garantizar la ejecución asincrónica de los algoritmos de asignación de donaciones presentes en el depósito.
4. El sistema debe permitir la gestión de incentivos a donantes.
5. El sistema debe permitir la realización de operaciones a través de los endpoints solicitados, mediante el desarrollo de una API REST.
6. El sistema debe incorporar un mecanismo de automatización para la publicación en redes sociales cuando una persona donante recibe una insignia.

### Entregables
1. Modelo del Dominio: diagrama de clases que contemple las funcionalidades requeridas.
2. Diagrama de despliegue y componentes.
3. Justificaciones de Diseño: Documento y Diagramas Complementarios que considere el equipo.
4. Implementación de requerimientos de la entrega actual.
5. Implementación del flujo automatizado de publicación y difusión de insignias.

---

## ENTREGA 3: Arquitectura y Modelado en Objetos - Parte III

### Objetivos de la entrega
- Diseñar e implementar, de manera incremental, las nuevas funcionalidades.
- Exponer un servicio a través de un protocolo de red.
- Incorporar flujos de trabajo asincrónicos.

### Alcance
- Servicio de Logística - Entrega y planificación de rutas.
- Servicio de Logística - Monitoreo de camiones en tiempo real.
- Exposición REST de los servicios.

### Revisión general de aspectos destacados de Entrega 1 y 2
Es importante que para esta entrega los requerimientos y decisiones de entregas 1 y 2 estén maduros:
- Separación de donaciones originales y segmentadas.
- Importación masiva de donantes en CSV funcional.
- Proceso de asignación a entidades utilizando Strategy o similar.
- Correcta exposición de API para Servicio de Donaciones.
- Pérdida de misiones en Servicio de Incentivos.
- Workflow de publicación con N8N.
- Impacto de las donaciones en el cálculo de progreso de las misiones.
- Notificaciones configuradas por diversos medios usando Strategy o similar.

### Dominio

**Servicio de Logística - Entrega y planificación de rutas**
Para realizar las entregas, la organización cuenta con una flota de camiones. De cada uno se conoce la patente, la capacidad en volumen (m³), la altura (m) y la capacidad de carga (kg). La plataforma contará con la integración de un componente externo encargado de generar las rutas de reparto del día siguiente. Este componente recibirá, por ejecución, un conjunto de donaciones en estado "Asignación Realizada" junto con la información de los camiones disponibles. Devolverá, por cada camión, una lista ordenada de destinos con las entregas que debe realizar en cada una.

**Servicio de Logística - Trazabilidad de las entregas de las donaciones**
Antes de iniciar el recorrido, el chofer deberá indicar en el sistema que dará por inicio su ruta. Automáticamente, las entregas asignadas pasarán al estado "En traslado". Cuando el camión entregue la donación en la sede de la entidad beneficiaria, esta deberá confirmar la recepción en el sistema. La entrega pasará al estado "Entregada". Luego, la entidad deberá cargar fotos de la donación recibida.
Si la entidad informa que no recibió la entrega en el día correspondiente, la misma se marcará como "No recibida" y el caso será revisado. Si la donación regresa al depósito, la entrega volverá al estado "Pendiente".

**Servicio de Donaciones - Notificaciones de Eventos por Logística**
Para esta entrega, se requiere notificar en los siguientes casos:
- **Inicio de ruta:** se notificará a todas las entidades beneficiarias y a los donantes cuyas entregas formen parte de la ruta. Incluir un enlace al mapa interactivo.
- **Entrega realizada con éxito:** cuando la entidad beneficiaria confirme la recepción satisfactoria de la donación. Incluir un comprobante de entrega.
- **Entrega no satisfactoria:** en caso de que la entrega no pueda concretarse, se notificará a la entidad beneficiaria, a la persona donante y a personas administradoras.

### Requerimientos detallados
**Requerimientos de dominio:**
1. Realización de operaciones a través de API REST.
2. Generación (en horarios de baja carga) de planes de rutas para los camiones, integrándose con el componente externo provisto.
3. Permitir a los choferes de los camiones informar del comienzo de su ruta.
4. Gestionar la recepción de la entrega por parte de las entidades beneficiarias.

**Requerimientos de implementación:**
1. El sistema deberá exponer una URL de callback donde el componente externo de planificación podrá notificar el resultado.
2. Realizar solicitudes al proveedor en lotes (máximo 100 donaciones).
3. El servicio de logística no debe invocar los servicios de donaciones ni incentivos sino dejar disponible la información.
4. El servicio de logística no debe comunicarse con el servicio de notificaciones.

**Documentación e implementación sobre el sistema en general:**
- Presentar en Postman (o similar) los diferentes endpoints.
- Documentar en Swagger (o similar).
- Generar contenedores (Docker o similar) y desplegar allí cada uno de los servicios.

### Entregables
1. Modelo del Dominio: diagrama de clases por servicio que contemple las funcionalidades.
2. Justificaciones de Diseño.
3. Implementación de los requerimientos de esta entrega.
4. Diagrama de despliegue actualizado (incluir integraciones concretas entre los servicios).

---

## ENTREGA 4: Persistencia, Integración y Despliegue

### Objetivos de la entrega
- Incorporar nociones de persistencia de datos en un medio relacional.
- Incorporar nociones de la técnica de mapeo objeto - relacional.
- Incorporar nociones respecto a diferentes estrategias y patrones de integración.

### Requerimientos de Integración
**Cola de mensajes para notificaciones**
La integración entre los servicios de dominio y el Servicio de Notificaciones deberá realizarse de forma asincrónica, a través de una cola de mensajes, a fin de no afectar la disponibilidad del sistema ante picos de carga o fallas transitorias.

**Broker de Integración con Logística**
Para la integración entre el servicio de donaciones y el servicio de logística deberá implementarse un broker. Este broker deberá permitir seleccionar entre más de 1 servicio de logística disponible.

### Requerimiento de Despliegue
**Despliegue de servicio de logística**
El servicio de logística deberá ser desplegado para ser accesible vía web a través de sus URIs. El despliegue puede encontrarse pausado para reducir consumos hasta que sea presentado en la defensa.

### Entregables
1. Modelo de Clases actualizado para cada Servicio.
2. Modelo de datos: Diagrama de entidad-relación físico por cada Servicio.
3. Justificaciones de Diseño: Documento y Diagramas Complementarios.
4. Diagrama de Componentes actualizado incluyendo los componentes de integración.
5. Documento explicativo de la arquitectura utilizada sin detalle de los componentes, pero justificando las decisiones.
6. Implementación de los requerimientos de integración de la presente entrega.
7. Despliegue de servicio de logística.

---

## ENTREGA 5: Persistencia

### Objetivos de la entrega
- Incorporar nociones de persistencia de datos en un medio relacional.
- Incorporar nociones de la técnica de mapeo objeto - relacional.
- Incorporar nociones de desnormalizaciones del modelo relacional.

### Alcance
- Persistencia del modelo de objetos previamente generado por Servicio.
- Estrategia de persistencia políglota.

### Requerimientos detallados
1. Se deberán persistir las entidades del modelo planteado para cada servicio.
2. El Servicio de Logística deberá utilizar una base de datos documental (ODM), mientras que los servicios restantes deberán ser persistidos en una base de datos relacional (ORM).
3. Se deberán contemplar estrategias de desnormalización cuando resulte necesario para optimizar consultas de lectura.

### Entregables
1. Actualizaciones de Modelo del Dominio, Modelo de Datos y Justificaciones de Diseño.
2. Actualizaciones de Diagrama de Componentes.
3. Implementación de los requerimientos de la presente entrega en código y bases de datos.

---

## ENTREGA 6: Interfaz de Usuario y Arquitectura MVC

### Objetivos de la entrega
- Incorporar nociones de Diseño UI/UX.
- Incorporar nociones de maquetado Web a través de HTML5 y CSS.
- Incorporar patrones de arquitectura MVC a través de la implementación de un Cliente Liviano.
- Aplicar principios de diseño centrado en el usuario (UI/UX) para la construcción de interfaces.

### Alcance
- Diseño y maquetado de interfaces de usuario.
- Implementación de un Cliente Liviano desacoplado.
- Servicio de Autenticación.

### Requerimientos
**Implementación de un Cliente Liviano desacoplado**
Se deberá crear un nuevo proyecto independiente para la implementación de la interfaz gráfica de usuario. El cliente será desacoplado de la lógica de negocio, consumiendo las APIs REST. Para la generación de las interfaces de usuario se deberá utilizar un motor de plantillas (template engine).

**Servicio de Autenticación**
Se deberá incorporar un servicio de Autenticación, encargado de la gestión de tokens de autorización desde el servidor frontend hacia los servicios de aplicación.

### Entregables
1. Implementación de los bocetos de interfaz de usuario planteados en la Entrega 1.
2. Implementación de un cliente liviano desacoplado, renderizando las vistas generadas.
3. Implementación de un SSO con soporte de Social-Login.
4. Maquetado e implementación en HTML de las interfaces de usuario requeridas, aplicando estilos.

### Consideraciones para el Maquetado
- **UI orientada a la usabilidad:** priorizando una curva de aprendizaje mínima.
- **Arquitectura de navegación optimizada:** profundidad máxima de tres niveles.
- **Familiaridad del diseño:** adherirse a design patterns establecidos (Material Design, etc.).
- **Soporte de microcopy y ayudas visuales:** etiquetas, tooltips, placeholders, onboarding hints.
- **Gestión de estados e interacciones:** notificaciones no intrusivas (toasts, modals).
- **Indicadores asincrónicos:** loaders, skeleton screens o spinners para procesos > 300ms.
- **Layout adaptable (responsive):** utilizando media queries, flexbox o grillas.
- **Integridad funcional cross-device:** accesible y operable sin distorsión en distintas pantallas.
- **Cumplimiento de normas WCAG:** mínimo nivel AA, soporte ARIA.
- **Interacción móvil optimizada:** hit área mínima de 48x48px para elementos clickeables.
- **Estilo visual unificado:** sistema que asegure coherencia visual en tokens de diseño.

---

## ENTREGA 7: Observabilidad y Seguridad

### Objetivos de la entrega
- Incorporar herramientas de monitoreo, observabilidad y seguridad.
- Familiarizarse con diferentes estrategias de integración.
- Familiarizarse con la Arquitectura de microservicios y sus componentes asociados.

### Alcance
- Herramientas de observabilidad, monitoreo, seguridad y control.
- Nuevas estrategias de diseño de APIs Web.

### Requerimientos detallados
1. Se deberán implementar soluciones de observabilidad que permitan recolectar, visualizar y analizar métricas, trazas y logs del sistema.
2. Se deberá incorporar herramientas de monitoreo proactivo que supervisen la salud de los servicios y permitan configurar políticas de autorestart ante fallos.
3. Se deberá implementar un sistema de limitación de solicitudes (Rate Limiting) que regule el tráfico entrante.
4. Se deberá implementar:
   a. **Opción 1:** el protocolo gRPC como reemplazo de HTTP para las comunicaciones entre servicios que requieren actualizaciones en tiempo real.
   b. **Opción 2:** una interfaz GraphQL en el Servicio de Donaciones o en el Servicio de Logística, para proporcionar flexibilidad en las consultas.

### Entregables
1. Diagrama de Componentes y Despliegue de la solución actual.
2. Implementación de Herramientas de Observabilidad, Monitoreo, Seguridad y Control.
3. Implementación de un Web Service utilizando gRPC o GraphQL.

---

## Anexo - Links de interés
- Modelado UML (Diagrama de Casos de Uso, Diagrama de Clases, Diagrama de Componentes, Diagrama de Despliegue)
- StarUML
- Metodología de Trabajo (Git Flow)
- Diseño y Maquetado Web (Leyes, Diseño y Maquetado Web - Parte I / Parte II, Seminario UX/UI, Heurísticas de Nielsen, Laws of UX, Web Content Accessibility Guidelines)
- Ley de Accesibilidad en las Páginas Web
- Ley de Protección de los Datos Personales
