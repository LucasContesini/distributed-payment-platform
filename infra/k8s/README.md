# Kubernetes Manifests

Production-ready Kubernetes manifests for the FinFlow platform.

## Structure

```
infra/k8s/
├── namespace.yml          # finflow namespace
├── configmap.yml          # Shared non-sensitive configuration
├── secret.yml             # Sensitive values (DB password, JWT secret)
├── api-gateway.yml        # Deployment + Service + Ingress + HPA + PDB
├── auth-service.yml       # Deployment + Service + PDB
├── payment-service.yml    # Deployment + Service + HPA + PDB
├── wallet-service.yml     # Deployment + Service + HPA + PDB
├── fraud-service.yml      # Deployment + Service + HPA
├── notification-service.yml # Deployment + Service
└── audit-service.yml      # Deployment + Service
```

## Prerequisites

- Kubernetes cluster (local: `minikube`, `kind`, or `k3d`)
- NGINX Ingress Controller
- PostgreSQL, Kafka, Redis available in-cluster or as external services

## Deploy

```bash
# 1. Create namespace
kubectl apply -f namespace.yml

# 2. Create ConfigMap and Secrets (edit secret.yml first with real values)
kubectl apply -f configmap.yml
kubectl apply -f secret.yml

# 3. Deploy all services
kubectl apply -f api-gateway.yml \
              -f auth-service.yml \
              -f payment-service.yml \
              -f wallet-service.yml \
              -f fraud-service.yml \
              -f notification-service.yml \
              -f audit-service.yml

# 4. Watch rollout
kubectl rollout status deployment -n finflow --timeout=120s

# 5. Check all pods
kubectl get pods -n finflow
```

## Resource Requests/Limits Summary

| Service | Memory Request | Memory Limit | CPU Request | CPU Limit | Min Replicas | Max Replicas |
|---|---|---|---|---|---|---|
| api-gateway | 256Mi | 512Mi | 100m | 500m | 2 | 6 |
| auth-service | 256Mi | 512Mi | 100m | 500m | 2 | 2 |
| payment-service | 384Mi | 768Mi | 200m | 1000m | 2 | 10 |
| wallet-service | 256Mi | 512Mi | 150m | 800m | 2 | 8 |
| fraud-service | 256Mi | 512Mi | 200m | 1000m | 2 | 6 |
| notification-service | 192Mi | 384Mi | 100m | 500m | 2 | 2 |
| audit-service | 192Mi | 384Mi | 100m | 500m | 2 | 2 |

## Health Checks

All services expose Spring Boot Actuator health probes:
- **Readiness**: `/actuator/health/readiness` — gates traffic routing
- **Liveness**: `/actuator/health/liveness` — triggers pod restart on failure

## Graceful Shutdown

All services have `terminationGracePeriodSeconds: 40` (> Spring's 30s shutdown timeout). Kubernetes sends `SIGTERM`, Spring drains in-flight requests within 30s, then exits cleanly.
