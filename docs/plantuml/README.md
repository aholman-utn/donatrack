

# PlantUML


PlantUML es una herramienta muy versátil que facilita la creación rápida y directa de una amplia gama de diagramas.


En esta carpeta se encontrarán los diagramas relacionados con el proyecto de Donatrack.


---


## Links


* https://plantuml.com/es/
* [Guia](https://plantuml.com/es/guide)
* [Playground](https://plantuml.github.io/plantuml/js-plantuml/index.html)
* [Extenciones para el navegador](https://github.com/plantuml/plantuml-for-github)


---


## ¿Cómo generar los diagramas?


    Recorda estar sobre esta carpeta y usar rutas relativas


Se generan utilizando `plantuml.jar` de la siguiente forma:


``` BASH
java -jar plantuml.jar ./miDiagrama.puml
```


De esta forma se obtiene, al menos, un archivo `.png` del diagrama.


Si el diagrama es muy grande, es recomendable generarlo como un `.svg`. Para obtener un diagrama en este formato se debe agregar el parámetro `-tsvg`.


``` BASH
java -jar plantuml.jar -tsvg ./miDiagrama.puml
```


---


## ¿Cómo sé cuántos archivos genere?


Para saber cuantos archivos se crearon o modificaron se puede agregar el parámetro `-verbose`, este detalla el proceso al generar el diagrama y al finalizar menciona el número de archivos generados.


``` BASH
java -jar plantuml.jar -verbose ./miDiagrama.puml
```


---


## ¿Dónde están los diagramas que generé?


Los diagramas se generan en la misma ubicación que su respectivo archivo `.puml`, ten en cuenta que se reescriben cuando generas uno nuevo.


---

## ¿Cómo imprimir un diagrama muy grande?

1. Generar el diagrama
2. Arrastrar / copiar la imagen del diagrama en ***draw.io***
3. Ajustar tamaño de hoja en ***draw.io***
4. Ajustar imagen
5. Archivo > Imprimir... > Vista de la página > Imprimir

---

## Estructura de la carpeta


```
docs/plantuml/
├── servicio-donaciones/                # Diagramas del Servicio de Donaciones
│   ├── Objetos.puml
│   └── Componentes.puml
├── servicio-incentivos/                # Diagramas del Servicio de Incentivos
│   ├── Objetos.puml
│   └── Componentes.puml
├── servicio-notificaciones/            # Diagramas del Servicio de Notificaciones
│   ├── Objetos.puml
│   └── Componentes.puml
└── Componentes.puml                    # Diagrama general de componentes
```
---


## Desarrollo


Es recomendable usar alguna de las siguientes integraciones para tu IDE de confianza para facilitar más el desarrollo.


* [IntelliJ IDEA](https://plugins.jetbrains.com/plugin/7017-plantuml-integration): "plantuml4idea"
* [VS Code](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml): "PlantUML".
* Eclipse: https://plantuml.com/es/eclipse


---


## Recomendaciones


- Mantener un único objetivo por diagrama.
- Evitar incluir todo el sistema en un mismo diagrama.
- Utilizar paquetes para agrupar clases relacionadas.
- Dividir diagramas demasiado grandes en varios archivos.
- Preferir SVG para diagramas grandes.


---


## Ejemplos


    Es recomendable verlos en el playground o usar una extensión del navegador, ambos en la sección de links.
    También existe una extensión para markdown.


Ejemplo base:


````plantuml
@startuml
class A
class B   
@enduml
````


Al cambiar el orden de la definición cambia la posición en el diagrama.


````plantuml
@startuml
class B   
class A
@enduml
````


Una relacion con solo una línea o punto va a ser perpendicular a la dirección del diagrama


````plantuml
@startuml


left to right direction


class A
class B 
class C 


A - B
B . C
D -|> E
@enduml
````


````plantuml
@startuml


top to bottom direction


class A
class B 
class C 


A - B
B . C
D -|> E
@enduml
````


El orden de la relación afecta al diagrama (comparado con el segundo ejemplo)


````plantuml
@startuml
class B   
class A


A - B
@enduml
````


Al agregar más líneas o puntos la relación se alarga y está en la misma dirección que el diagrama


````plantuml
@startuml


left to right direction


class A
class B   


A -- B
C .. D


@enduml
````


````plantuml
@startuml


top to bottom direction


class A
class B   


A -- B
C .. D


@enduml
````


````plantuml
@startuml
class A
class B   


A --- B
C ...|> D
@enduml
````


Para hacer más cosas, como: cambiar el color de las clases, atributos, relaciones, flechas; agregar notas; usar paquetes; agregar json; modificar forma de las flechas (rectas, curvas, etc.) y más cosas, leer la **Guía** de la sección de links o buscar en google

