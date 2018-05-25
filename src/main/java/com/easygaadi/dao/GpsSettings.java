package com.easygaadi.dao;

import com.easygaadi.util.PreCondition;
import org.springframework.data.annotation.Id;

/**
 * @author Petri Kainulainen
 */
public final class GpsSettings {

    static final int MAX_LENGTH_DESCRIPTION = 500;
    static final int MAX_LENGTH_TITLE = 100;

    @Id
    private String id;

    private String description;

    private String title;

    public GpsSettings() {}

    private GpsSettings(Builder builder) {
        this.description = builder.description;
        this.title = builder.title;
    }

    static Builder getBuilder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void update(String title, String description) {
        checkTitleAndDescription(title, description);

        this.title = title;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format(
                "Todo[id=%s, description=%s, title=%s]",
                this.id,
                this.description,
                this.title
        );
    }

    /**
     * We don't have to use the builder pattern here because the constructed class has only two String fields.
     * However, I use the builder pattern in this example because it makes the code a bit easier to read.
     */
    static class Builder {

        private String description;

        private String title;

        private Builder() {}

        Builder description(String description) {
            this.description = description;
            return this;
        }

        Builder title(String title) {
            this.title = title;
            return this;
        }

        GpsSettings build() {
            GpsSettings build = new GpsSettings(this);

            build.checkTitleAndDescription(build.getTitle(), build.getDescription());

            return build;
        }
    }

    private void checkTitleAndDescription(String title, String description) {
        PreCondition.notNull(title, "Title cannot be null");
        PreCondition.notEmpty(title, "Title cannot be empty");
        PreCondition.isTrue(title.length() <= MAX_LENGTH_TITLE,
                "Title cannot be longer than %d characters",
                MAX_LENGTH_TITLE
        );

        if (description != null) {
            PreCondition.isTrue(description.length() <= MAX_LENGTH_DESCRIPTION,
                    "Description cannot be longer than %d characters",
                    MAX_LENGTH_DESCRIPTION
            );
        }
    }
}
