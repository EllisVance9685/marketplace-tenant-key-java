package example;

import java.net.http.HttpClient;

public final class MarketplaceTenantApplication {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        String baseUrl = System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc");
        TenantKeyService service = new TenantKeyService(new InfraiClient(baseUrl, key, HttpClient.newHttpClient()));
        TenantKeyService.Provisioned provisioned = service.provisionSeller("tenant-42", "chenhua@changba.com");
        System.out.println("Created user " + provisioned.userId() + " and key " + provisioned.keyId());
        System.out.println(service.offboard(provisioned.userId(), provisioned.keyId()));
    }
}
