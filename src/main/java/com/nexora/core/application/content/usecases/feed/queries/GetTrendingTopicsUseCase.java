package com.nexora.core.application.content.usecases.feed.queries;

import com.nexora.core.infrastructure.persistence.content.adapters.FeedQueryJdbcAdapter;
import com.nexora.core.application.content.dto.TrendingTopicView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTrendingTopicsUseCase {

    private final FeedQueryJdbcAdapter feedQueryJdbcAdapter;

    public List<TrendingTopicView> execute(int limit) {
        return feedQueryJdbcAdapter.queryTrendingTopics(limit);
    }
}
