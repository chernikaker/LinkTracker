package backend.academy.scrapper.client.bot;

import backend.academy.dto.LinkUpdate;
import org.springframework.web.client.RestClient;

public class BotClientImpl implements BotClient {

    private final static String DEFAULT_URL = "http://localhost:8080";
    private final RestClient restClient;

    public BotClientImpl(String baseUrl) {
        restClient = RestClient.builder()
                .baseUrl(baseUrl == null ? DEFAULT_URL : baseUrl)
                .build();
    }

    public BotClientImpl() {
        this(null);
    }

    @Override
    public void sendUpdates(LinkUpdate update) {
        restClient.post().uri("/updates").body(update).retrieve().toBodilessEntity();
    }
}
