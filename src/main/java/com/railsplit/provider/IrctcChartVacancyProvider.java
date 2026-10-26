
package com.railsplit.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

@Component
public class IrctcChartVacancyProvider implements ChartVacancyProvider {

    private static final String CHART_URL =
            "https://www.irctc.co.in/online-charts/api/coachComposition";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public IrctcChartVacancyProvider(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode getCoachComposition(
            String trainNo,
            String boardingStation,
            String remoteStation,
            String trainSourceStation,
            String journeyDate,
            String travelClass,
            String coach
    ) {
        Map<String, String> requestBody = Map.of(
                "trainNo", trainNo,
                "boardingStation", boardingStation,
                "remoteStation", remoteStation,
                "trainSourceStation", trainSourceStation,
                "jDate", journeyDate,
                "cls", travelClass,
                "coach", coach
        );

        String responseBody = restClient.post()
                .uri(CHART_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "IRCTC returned a response that is not valid JSON.", e);
        }
    }
}
