
package com.railsplit.provider;

import com.fasterxml.jackson.databind.JsonNode;

public interface ChartVacancyProvider {

    JsonNode getCoachComposition(
            String trainNo,
            String boardingStation,
            String remoteStation,
            String trainSourceStation,
            String journeyDate,
            String travelClass,
            String coach
    );
}
