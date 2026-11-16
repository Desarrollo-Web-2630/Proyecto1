# Despliegue en Kubernetes (MicroK8s)

El sistema se despliega con dos repositorios, cada uno con su workflow de GitHub Actions
(`.github/workflows/deploy.yml`): el job 1 construye la imagen en un runner de GitHub y la
publica en GHCR; el job 2 corre en el runner self-hosted del servidor y la aplica en MicroK8s.

```
Internet ─▶ Ingress (<grupo>.inphotech.co)
              ├─ /api, /swagger-ui, /v3/api-docs ─▶ Service procesos-backend:8080 ─▶ Pod Spring Boot (JWT)
              └─ /                                ─▶ Service procesos-frontend:80  ─▶ Pods nginx + Angular (x2)
                                                     Service procesos-db:5432      ─▶ Pod PostgreSQL 16 + PVC 1Gi
```

| Repositorio | Manifiestos | Recursos |
|---|---|---|
| proyecto1 (backend) | `thymeleaf/k8s/postgres.yaml`, `thymeleaf/k8s/backend.yaml` | PVC, Deployment y Service de PostgreSQL; Deployment y Service del backend |
| proyecto2 (frontend) | `front/k8s/frontend.yaml`, `front/k8s/ingress.yaml` | Deployment y Service del frontend; Ingress |

Todos los recursos van en el mismo namespace. Las imagenes quedan como
`ghcr.io/<owner>/procesos-backend` y `ghcr.io/<owner>/procesos-frontend`, con tag `latest`
y tag del commit (7 caracteres) para poder volver a una version exacta.

## 1. Configuracion unica (una sola vez)

### 1.1 Datos del grupo en los dos workflows

En `.github/workflows/deploy.yml` de **ambos** repositorios:

```yaml
env:
  NAMESPACE: CAMBIAR-namespace      # namespace asignado al grupo
  DOMINIO: CAMBIAR.inphotech.co     # dominio del Ingress
```

### 1.2 Servidor (MicroK8s)

```bash
sudo microk8s enable dns ingress hostpath-storage
sudo microk8s kubectl create namespace <namespace>      # si el profesor no lo creo ya
```

### 1.3 Runner self-hosted con etiqueta `main`

Registrarlo **a nivel de organizacion** (Settings de la organizacion → Actions → Runners)
para que sirva a los dos repositorios. Si se registra en un solo repositorio, los jobs del
otro quedan en *Waiting for a runner*. Ver la guia *Action Runners* del curso
(`./config.sh ... --labels main`, `sudo ./svc.sh install && sudo ./svc.sh start`).

El usuario del runner debe poder ejecutar `sudo microk8s` sin contrasena.

### 1.4 Secret para bajar las imagenes de GHCR

Los paquetes de GHCR son privados por defecto. Con un token de GitHub (permiso `read:packages`):

```bash
sudo microk8s kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=<usuario_github> \
  --docker-password=<token_read_packages> \
  -n <namespace>
```

Alternativa: hacer publicos los dos paquetes (Packages → Package settings → Change visibility).

### 1.5 Secret de la aplicacion (credenciales)

Nunca se sube al repositorio (`k8s/secret.example.yaml` es solo el ejemplo):

```bash
sudo microk8s kubectl create secret generic procesos-secrets -n <namespace> \
  --from-literal=DB_NAME=procesos \
  --from-literal=DB_USER=procesos \
  --from-literal=DB_PASSWORD="$(openssl rand -base64 24)" \
  --from-literal=JWT_SECRET="$(openssl rand -base64 48)" \
  --from-literal=DEMO_ADMIN_PASSWORD='<clave del admin demo>'
```

| Clave | Uso |
|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Crean la base en PostgreSQL y conectan el backend |
| `JWT_SECRET` | Firma de los tokens. Obligatorio: sin el, cada pod usaria su propia clave |
| `DEMO_ADMIN_PASSWORD` | Contrasena de `admin@demo.com`, que el inicializador crea en la base vacia |

Si se cambia un valor: `sudo microk8s kubectl rollout restart deployment/procesos-backend -n <namespace>`.

## 2. Desplegar

1. Push a `main` del **backend** (o *Run workflow* en la pestana Actions). Crea la base y el backend.
2. Cuando termine en verde, push a `main` del **frontend**. Crea el frontend y el Ingress.

El orden importa solo la primera vez: nginx del frontend necesita que exista el Service
`procesos-backend`. Despues cada repositorio se despliega solo con su push.

El job 2 termina en rojo si un pod no queda listo (`rollout status`), asi un despliegue roto
no se reporta como exitoso.

## 3. Verificar

```bash
sudo microk8s kubectl get pods,svc,ingress,pvc -n <namespace>   # todo Running y 1/1 READY
curl -i http://<dominio>/api/v1/procesos                         # 401: la API esta protegida
curl -i -X POST http://<dominio>/api/auth/login -H 'Content-Type: application/json' \
  -d '{"correo":"admin@demo.com","password":"<DEMO_ADMIN_PASSWORD>"}'   # 200 con token
```

En el navegador: `http://<dominio>` (aplicacion) y `http://<dominio>/swagger-ui.html` (API).
Monitor del curso: https://javeriana.inphotech.co/k8smonitor

## 4. Operacion

```bash
NS=<namespace>
sudo microk8s kubectl logs -f deployment/procesos-backend -n $NS        # logs
sudo microk8s kubectl describe pod <pod> -n $NS                          # por que no arranca
sudo microk8s kubectl rollout restart deployment/procesos-backend -n $NS # reiniciar
sudo microk8s kubectl scale deployment/procesos-backend --replicas=0 -n $NS  # detener
sudo microk8s kubectl scale deployment/procesos-backend --replicas=1 -n $NS  # iniciar
```

### Volver a una version anterior (rollback)

```bash
sudo microk8s kubectl rollout undo deployment/procesos-backend -n $NS
# o fijar una version exacta por commit:
sudo microk8s kubectl set image deployment/procesos-backend \
  procesos-backend=ghcr.io/<owner>/procesos-backend:<sha7> -n $NS
```

### Respaldo y restauracion de la base

```bash
sudo microk8s kubectl exec deployment/procesos-db -n $NS -- \
  sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > respaldo_$(date +%F).sql
sudo microk8s kubectl exec -i deployment/procesos-db -n $NS -- \
  sh -c 'psql -U "$POSTGRES_USER" "$POSTGRES_DB"' < respaldo_AAAA-MM-DD.sql
```

Los datos viven en el PVC `procesos-db-datos`: sobreviven a reinicios y redespliegues del pod.
Borrar el PVC borra la base.

## 5. Problemas frecuentes

| Sintoma | Causa habitual |
|---|---|
| Job en *Waiting for a runner* | Runner apagado, etiqueta distinta de `main`, o registrado solo en el otro repositorio |
| `ImagePullBackOff` | Falta `ghcr-secret` en el namespace o el paquete es privado |
| `CreateContainerConfigError` | Falta el Secret `procesos-secrets` o una de sus claves |
| Backend en `CrashLoopBackOff` | Revisar `logs`: casi siempre la conexion a la base o `JWT_SECRET` con menos de 32 caracteres |
| `procesos-db` en `Pending` | El PVC no se creo: falta `microk8s enable hostpath-storage` |
| Frontend en `CrashLoopBackOff` (`host not found in upstream`) | Se desplego antes que el backend: desplegar primero el backend |
| 404 desde el Ingress | `DOMINIO` del workflow no coincide con la URL pedida |
| 503 desde el Ingress | Pods aun no listos, o etiquetas del Service distintas a las del pod |
| El sitio sigue con el codigo viejo | Revisar que el job corrio `rollout restart` (con `:latest` es necesario) |
