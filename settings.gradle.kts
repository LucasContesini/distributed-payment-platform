rootProject.name = "distributed-payment-platform"

include(
    "shared-libs:common-domain",
    "shared-libs:common-events",
    "api-gateway",
    "auth-service",
    "payment-service",
    "wallet-service",
    "fraud-service",
    "notification-service",
    "audit-service"
)
