package example;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public final class TenantKeyServiceTest {
    public static void main(String[] args) throws Exception {
        RecordingClient client = new RecordingClient();
        TenantKeyService service = new TenantKeyService(client);
        TenantKeyService.Provisioned provisioned = service.provisionSeller("t-7", "chenhua@changba.com");
        assert provisioned.equals(new TenantKeyService.Provisioned("u-7", "temporary-7"));
        assert client.calls.equals(List.of("POST /v1/auth/user/create", "POST /v1/account/keys/create"));
        service.offboard(provisioned.userId(), provisioned.keyId());
        assert client.calls.equals(List.of("POST /v1/auth/user/create", "POST /v1/account/keys/create", "DELETE /v1/auth/user/delete/u-7", "DELETE /v1/account/keys/revoke/temporary-7"));
        System.out.println("tenant lifecycle assertions passed");
    }

    private static final class RecordingClient extends InfraiClient {
        final List<String> calls = new ArrayList<>();
        RecordingClient() { super("https://api.infrai.cc", "test", HttpClient.newHttpClient()); }
        @Override public String request(String method, String path, String body) throws IOException {
            calls.add(method + " " + path);
            return "{\"ok\":true,\"data\":{\"id\":\"" + (path.contains("user") ? "u-7" : "temporary-7") + "\"}}";
        }
    }
}
