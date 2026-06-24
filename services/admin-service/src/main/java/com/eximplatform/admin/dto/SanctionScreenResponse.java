package com.eximplatform.admin.dto;

import java.util.List;

/** Result of screening a name: whether it hit the list and the matching entries. */
public record SanctionScreenResponse(String query, boolean hit, int count, List<SanctionMatch> matches) {

    public static SanctionScreenResponse of(String query, List<SanctionMatch> matches) {
        return new SanctionScreenResponse(query, !matches.isEmpty(), matches.size(), matches);
    }
}
