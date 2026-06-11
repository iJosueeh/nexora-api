package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import com.nexora.core.domain.user.aggregates.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends DomainModel {
    private Post post;
    private User autor;
    private Comment parent;
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    private String content;

    public static Comment create(Post post, User autor, String content) {
        validateContent(content);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAutor(autor);
        comment.setContent(content.trim());
        return comment;
    }

    public static Comment createReply(Post post, User autor, String content, Comment parent) {
        validateContent(content);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAutor(autor);
        comment.setContent(content.trim());
        comment.setParent(parent);
        return comment;
    }

    public void updateContent(String newContent) {
        validateContent(newContent);
        this.content = newContent.trim();
    }

    public boolean isReply() {
        return this.parent != null;
    }

    public boolean isAuthor(User user) {
        if (user == null || user.getId() == null || this.autor == null) {
            return false;
        }
        return user.getId().equals(this.autor.getId());
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
    }
}
