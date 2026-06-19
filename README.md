# Cluster Availability Service

Backend interno en Java 21 y Spring Boot 3 para publicar, con baja latencia, el estado operativo de un cluster. El endpoint productivo responde siempre desde memoria; la base SQL Server se consulta solamente por sincronizacion programada.

## Arquitectura

La aplicacion sigue arquitectura hexagonal:

```text
com.company.clusteravailability
├── domain
│   ├── model
│   ├── exception
│   └── port
├── application
│   ├── port
│   └── service
└── infrastructure
    ├── adapter.in.rest
    ├── adapter.in.schedule
    ├── adapter.out.persistence
    ├── inmemory
    ├── config
    └── config.property
```

Reglas principales:

- `domain` no depende de Spring, JPA ni infraestructura.
- `application` solo conoce puertos y dominio.
- REST vive en `infrastructure.adapter.in.rest`.
- JPA vive en `infrastructure.adapter.out.persistence`.
- El controller es delgado y no accede a repositorios.
- El estado en memoria se guarda con `AtomicReference` y lectura O(1).

## Flujo de Datos

1. La aplicacion carga properties tipadas desde `cluster.*`.
2. Al iniciar, el scheduler ejecuta una sincronizacion inicial.
3. El caso de uso consulta `dbo.cluster_status` por alias usando el puerto de repositorio.
4. El adaptador JPA aplica cache y circuit breaker.
5. El mapper transforma la entidad a modelo de dominio.
6. El dominio valida alias, estado, intervalo y fecha.
7. El store en memoria se actualiza con el ultimo estado valido.
8. `GET /api/v1/cluster/status` responde desde memoria, sin consultar SQL Server.
9. El scheduler ajusta dinamicamente el proximo intervalo con `polling_interval_seconds`.

## Tabla Esperada

```sql
CREATE TABLE dbo.cluster_status (
    id BIGINT NOT NULL PRIMARY KEY,
    cluster_alias VARCHAR(120) NOT NULL,
    active BIT NOT NULL,
    polling_interval_seconds BIGINT NOT NULL,
    updated_at DATETIME2 NOT NULL,
    created_at DATETIME2 NOT NULL
);
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
```

Datasource por variables de entorno:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
CLUSTER_ALIAS
CLUSTER_SCHEDULER_DEFAULT_POLLING_INTERVAL_SECONDS
CLUSTER_SCHEDULER_INITIAL_DELAY_SECONDS
CLUSTER_CACHE_TTL_SECONDS
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

- Cache JCache/Ehcache para `clusterStatusByAlias`.
- Cache JCache/Ehcache para `clusterPollingConfigByAlias`.
- Circuit breaker `clusterStatusRepository`.

Los errores tecnicos se propagan como `ClusterStatusRepositoryException` y no se cachean.

## Ejecucion Local

Requisitos:

- Java 21.
- Maven 3.9+.
- SQL Server accesible.

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

- Inyectar secretos de datasource mediante `Secret`.
- Inyectar alias e intervalos mediante `ConfigMap`.
- Usar `/actuator/health/liveness` y `/actuator/health/readiness`.
- No hardcodear credenciales.
- Construir el jar con `mvn clean package` antes de crear la imagen.
