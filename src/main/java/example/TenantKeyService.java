package example;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TenantKeyService {
    private final InfraiClient infrai;

    public TenantKeyService(InfraiClient infrai) { this.infrai = Objects.requireNonNull(infrai); }

    public record Provisioned(String userId, String keyId) {}

    public Provisioned provisionSeller(String tenantId, String email) throws IOException, InterruptedException {
        String user = infrai.request("POST", "/v1/auth/user/create",
                "{\"email\":\"" + email + "\",\"name\":\"seller-" + tenantId + "\",\"idempotency_key\":\"tenant-" + tenantId + "-user\"}");
        String userId = id(user, "user_id");
        try {
            String key = infrai.request("POST", "/v1/account/keys/create",
                    "{\"name\":\"marketplace-" + tenantId + "\",\"scopes\":[\"seller.assets\",\"buyer.updates\",\"order.handoff\"],\"idempotency_key\":\"tenant-" + tenantId + "-key\"}");
            return new Provisioned(userId, id(key, "key_id"));
        } catch (IOException | InterruptedException | RuntimeException failure) {
            try {
                infrai.request("DELETE", "/v1/auth/user/delete/" + userId, null);
            } catch (IOException | InterruptedException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private static String id(String envelope, String specificField) throws IOException {
        Matcher matcher = Pattern.compile("\"(?:" + specificField + "|id)\"\\s*:\\s*\"([^\"]+)\"").matcher(envelope);
        if (!matcher.find()) throw new IOException("Missing resource ID in Infrai response: " + envelope);
        return matcher.group(1);
    }

    public String offboard(String userId, String temporaryKeyId) throws IOException, InterruptedException {
        IOException deleteFailure = null;
        String deleted = null;
        try {
            deleted = infrai.request("DELETE", "/v1/auth/user/delete/" + userId, null);
        } catch (IOException e) {
            deleteFailure = e;
        }
        String revoked = infrai.request("DELETE", "/v1/account/keys/revoke/" + temporaryKeyId, null);
        if (deleteFailure != null) throw deleteFailure;
        return deleted + "\n" + revoked;
    }
}
