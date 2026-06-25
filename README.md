# java21-multicluster

## Cluster Availability Service

Backend interno en Java 21 y Spring Boot 3 para publicar, con baja latencia, el estado operativo de un cluster. El endpoint productivo responde siempre desde memoria; MongoDB se consulta solamente por sincronizacion programada.

## Arquitectura

La aplicacion sigue arquitectura hexagonal:

```text
com.company.clusteravailability
|-- domain
|   |-- model
|   |-- exception
|   `-- port
|-- application
|   |-- port
|   `-- service
`-- infrastructure
    |-- adapter.in.rest
    |-- adapter.in.schedule
    |-- adapter.out.persistence
    |-- inmemory
    |-- config
    `-- config.property
```

Reglas principales:

- `domain` no depende de Spring, MongoDB ni infraestructura.
- `application` solo conoce puertos y dominio.
- REST vive en `infrastructure.adapter.in.rest`.
- MongoDB vive en `infrastructure.adapter.out.persistence`.
- El controller es delgado y no accede a repositorios.
- El estado en memoria se guarda con `AtomicReference` y lectura O(1).

## Flujo de Datos

1. La aplicacion carga properties tipadas desde `cluster.*`.
2. Al iniciar, el scheduler ejecuta una sincronizacion inicial.
3. El caso de uso consulta la coleccion `cluster_status` por alias usando el puerto de repositorio.
4. El adaptador Mongo aplica cache y circuit breaker.
5. El mapper transforma la entidad a modelo de dominio.
6. El dominio valida alias, estado, intervalo y fecha.
7. El store en memoria se actualiza con el ultimo estado valido.
8. `GET /api/v1/cluster/status` responde desde memoria, sin consultar MongoDB.
9. El scheduler ajusta dinamicamente el proximo intervalo con `polling_interval_seconds`.

## Coleccion Esperada

```json
{
  "_id": "primary-cluster",
  "cluster_alias": "primary-cluster",
  "active": true,
  "polling_interval_seconds": 30,
  "updated_at": "2026-06-19T10:15:00Z",
  "created_at": "2026-06-19T10:00:00Z"
}
```

## Configuracion

Variables relevantes:

```yaml
cluster:
  alias: primary-cluster
  scheduler:
    default-polling-interval-seconds: 30
    initial-delay-seconds: 5
  cache:
    ttl-seconds: 60
    collection-name: cluster_cache
```

MongoDB por variables de entorno:

```text
SPRING_DATA_MONGODB_URI
CLUSTER_ALIAS
CLUSTER_SCHEDULER_DEFAULT_POLLING_INTERVAL_SECONDS
CLUSTER_SCHEDULER_INITIAL_DELAY_SECONDS
CLUSTER_CACHE_TTL_SECONDS
CLUSTER_CACHE_COLLECTION_NAME
```

## API

### GET `/api/v1/cluster/status`

Respuesta 200:

```json
{
  "metadata": {
    "timestamp": "2026-06-19T10:15:30Z",
    "service": "cluster-availability-service",
    "version": "1.0.0"
  },
  "data": {
    "alias": "primary-cluster",
    "active": true,
    "pollingIntervalSeconds": 30,
    "updatedAt": "2026-06-19T10:15:00Z"
  },
  "errors": []
}
```

Respuesta 503 cuando no hay estado cargado:

```json
{
  "metadata": {
    "timestamp": "2026-06-19T10:15:30Z",
    "service": "cluster-availability-service",
    "version": "1.0.0"
  },
  "data": null,
  "errors": [
    {
      "code": "CLUSTER_STATUS_UNAVAILABLE",
      "message": "Cluster status is not currently available"
    }
  ]
}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Scheduler Dinamico

`DynamicClusterStatusScheduler` evita ejecuciones concurrentes con `AtomicBoolean`. Despues de cada sincronizacion consulta la configuracion liviana de polling por alias y actualiza el intervalo. Si no puede obtener un valor valido, conserva el default configurado.

## Cache y Circuit Breaker

El adaptador `ClusterStatusPersistenceAdapter` usa:

- Cache Mongo para `clusterStatusByAlias`.
- Cache Mongo para `clusterPollingConfigByAlias`.
- Circuit breaker `clusterStatusRepository`.

La cache Mongo se implementa con un `CacheManager` de Spring que guarda entradas serializadas en la coleccion configurable `cluster_cache`.

Comportamiento de cache:

- Usa circuit breaker `mongoCache`.
- Falla en modo fail-open: si Mongo cache no responde, se trata como cache miss y no rompe el flujo.
- Crea indice TTL real sobre `expires_at` al iniciar la aplicacion.
- Crea indice por `cache_name` y `cache_key`.
- Los errores tecnicos de persistencia se propagan como `ClusterStatusRepositoryException` y no se cachean.

Configuracion Mongo recomendada para alta carga:

```yaml
cluster:
  mongo:
    connect-timeout-ms: 500
    read-timeout-ms: 1000
    server-selection-timeout-ms: 1000
    max-connection-pool-size: 100
    min-connection-pool-size: 0
    max-wait-time-ms: 500
```

## Ejecucion Local

Requisitos:

- Java 21.
- Maven 3.9+.
- MongoDB accesible.

Comandos:

```powershell
mvn clean test
mvn spring-boot:run
```

## Observabilidad

Incluye Spring Boot Actuator:

```text
/actuator/health
/actuator/info
```

El servicio registra eventos de inicio, fin, duracion, alias, resultado, errores tecnicos, datos invalidos y cambios de intervalo. Los valores externos se sanitizan antes de escribirse en logs.

## OpenShift

El `Dockerfile` usa Java 21 runtime. Para OpenShift:

- Inyectar la URI de MongoDB mediante `Secret`.
- Inyectar alias e intervalos mediante `ConfigMap`.
- Usar `/actuator/health/liveness` y `/actuator/health/readiness`.
- No hardcodear credenciales.
- Construir el jar con `mvn clean package` antes de crear la imagen.
