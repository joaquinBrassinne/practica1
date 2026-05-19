# Sistema de Gestión de Estacionamiento

## Descripción general

Este proyecto es una aplicación de escritorio en Java para administrar una playa de estacionamiento.
Permite registrar entradas de vehículos, calcular cobros de salidas, anular tickets y generar reportes.

La solución está construida con:
- Java 17
- Swing para la interfaz gráfica
- JPA/Hibernate para persistencia de datos
- MySQL (u otra base de datos compatible) para almacenamiento
- Maven para compilación y dependencias

## Objetivo del proyecto

El sistema gestiona la operación diaria de un estacionamiento mediante:
- Registro de ingreso de vehículos
- Control de ocupación y lugares disponibles
- Cálculo automático de tarifa de estacionamiento
- Cierre y cobro del ticket
- Anulación de tickets cuando corresponde
- Reportes operativos y exportación a CSV

## Estructura del proyecto

### Paquetes principales

- `com.parking.ui`: contiene la interfaz gráfica.
- `com.parking.service`: contiene la lógica de negocio.
- `com.parking.repository`: contiene el acceso a datos a través de JPA.
- `com.parking.domain`: contiene las entidades y enumeraciones del modelo.
- `com.parking.persistence`: contiene la utilería JPA.

### Archivos de configuración

- `src/main/resources/appconfig.properties`: parámetros de tarifa, capacidad y conexión a base de datos.
- `src/main/resources/META-INF/persistence.xml`: configuración de JPA/Hibernate.

## Flujo de ejecución

### Inicio de la aplicación

El punto de entrada es `com.parking.ui.MainApp`.
Desde el IDE ejecutas la clase `MainApp`, que abre la ventana principal `MainFrame`.

### Panel principal

La ventana principal tiene dos pestañas:
- `Entradas/Salidas`
- `Reportes`

En la parte superior aparece un encabezado con la ocupación actual y los espacios disponibles.

## Pestaña Entradas/Salidas

Esta pestaña es la interfaz operativa principal.

### Controles y botones

1. `Patente` (campo de texto)
   - Aquí se ingresa la matrícula del vehículo.

2. `Tipo` (combo box)
   - Selecciona el tipo de vehículo: `AUTO`, `MOTO` o `CAMIONETA`.

3. `Registrar Entrada` (botón)
   - Crea un nuevo ticket de estadía.
   - Valida que la patente no esté vacía.
   - Valida que el tipo sea seleccionado.
   - Verifica si hay capacidad disponible.
   - Verifica que no exista ya un ticket abierto para la misma patente.
   - Registra la hora de entrada e inserta el ticket en la base de datos.

4. Tabla de tickets abiertos
   - Muestra los tickets cuyo estado es `ABIERTO`.
   - Para cada registro muestra información básica de la entrada.

5. `Registrar Salida/Cobro` (botón)
   - Debe seleccionarse un ticket abierto en la tabla.
   - Abre un diálogo de cobro (`CheckoutDialog`).
   - Muestra:
     - patente
     - duración total en minutos
     - minutos cobrables
     - cantidad de bloques
     - tarifa aplicada
     - importe final
   - Permite seleccionar la forma de pago: `EFECTIVO`, `TARJETA` o `TRANSFERENCIA`.
   - Al confirmar, se cierra el ticket y se registra la salida con el importe calculado.

6. `Anular Ticket` (botón)
   - Debe seleccionarse un ticket en la tabla.
   - Abre un diálogo (`AnulacionDialog`) para ingresar el motivo de anulación.
   - Cambia el estado del ticket a `ANULADO`.
   - Se puede anular un ticket abierto o cerrado del mismo día.

## Pestaña Reportes

Esta pestaña permite consultar y analizar tickets existentes.

### Controles y botones

1. `Desde` / `Hasta`
   - Fechas y horas para filtrar el rango de búsqueda.

2. `Patente`
   - Filtro por texto parcial de patente.

3. `Estado`
   - Filtra por estado del ticket: `ABIERTO`, `CERRADO`, `ANULADO`.

4. `Tipo`
   - Filtra por tipo de vehículo.

5. `Imp.Min` / `Imp.Max`
   - Filtran por importe calculado.

6. `Buscar` (botón)
   - Ejecuta la consulta usando los filtros seleccionados.
   - Muestra resultados en una tabla.
   - Actualiza KPIs en la parte inferior.

7. `Exportar CSV` (botón)
   - Permite guardar el listado actual de tickets filtrados en un archivo CSV.

### Indicadores (KPIs)

El panel de reportes muestra valores clave de hoy:
- Ocupación actual y disponibles.
- Recaudación neta del día.
- Totales agrupados por forma de pago.
- Cantidad de tickets por estado.

## Lógica de negocio

### TicketService

`com.parking.service.TicketService` realiza las operaciones principales:
- `registrarEntrada(patente, tipo)`
- `listarAbiertos()`
- `registrarSalida(idTicket, formaPago)`
- `anular(idTicket, motivo)`

### Reglas importantes

- `capacidad` se obtiene desde `Config.getCapacidad()`.
- No se permite más de un ticket abierto por patente.
- El ticket abierto se cierra con hora de salida y monto calculado.
- El ticket anulado queda en `ANULADO` y no se considera recaudación.
- Si la salida ocurre antes de la entrada, el sistema arroja error.
- La forma de pago es obligatoria al cerrar un ticket.

### Cálculo de tarifa

La tarifa se calcula en `CalculoTarifaService` a partir de:
- tipo de vehículo
- hora de entrada
- hora de salida
- minutos de gracia
- bloques de minutos
- tope diario
- potencial tarifa nocturna

La configuración se lee de `src/main/resources/appconfig.properties`.

## Datos y persistencia

### Entidad principal

`TicketEstadia` representa un ticket con campos como:
- `id`
- `nroTicket`
- `patente`
- `tipoVehiculo`
- `horaEntrada`
- `horaSalida`
- `minutosEstadia`
- `tarifaAplicada`
- `importeCalculado`
- `formaPago`
- `estado` (`ABIERTO`, `CERRADO`, `ANULADO`)
- `motivoAnulacion`
- `creadoEn`
- `cerradoEn`

### Repositorio

`TicketEstadiaRepository` contiene los métodos JPA para:
- guardar y actualizar tickets
- buscar tickets abiertos
- contar ocupación actual
- buscar tickets con filtros
- calcular totales y recaudación por día

### Base de datos

La configuración por defecto está en `appconfig.properties`:
- URL: `jdbc:mysql://localhost:3306/parking?serverTimezone=UTC`
- usuario: `root`
- contraseña: `root`

Eso puede editarse según el entorno local.

El archivo `persistence.xml` configura JPA/Hibernate con:
- `hibernate.hbm2ddl.auto=update` para crear/actualizar el esquema según la entidad.

## Cómo ejecutar

### Requisitos

- Java 17
- Maven
- Base de datos MySQL (o equivalente) configurada

### Pasos

1. Abrir el proyecto en el IDE.
2. Confirmar la conexión a la base de datos en `src/main/resources/appconfig.properties`.
3. Ejecutar `com.parking.ui.MainApp`.

También se puede compilar con Maven:

```bash
mvn clean compile
```

Y ejecutar desde el IDE o configurando el classpath con `target/classes` más dependencias.

