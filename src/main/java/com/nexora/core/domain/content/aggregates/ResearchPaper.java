package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchPaper extends DomainModel {
    private String slug;
    private String title;
    private String summary;
    private String faculty;
    private int views;
    private UUID authorId;
    private String pdfUrl;

    public static ResearchPaper create(String slug, String title, String summary, String faculty, UUID authorId, String pdfUrl) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("Author ID cannot be null");
        }

        ResearchPaper paper = new ResearchPaper();
        paper.setSlug(slug != null ? slug.trim() : null);
        paper.setTitle(title.trim());
        paper.setSummary(summary);
        paper.setFaculty(faculty);
        paper.setViews(0);
        paper.setAuthorId(authorId);
        paper.setPdfUrl(pdfUrl);
        return paper;
    }

    public void incrementViews() {
        this.views++;
    }
}
