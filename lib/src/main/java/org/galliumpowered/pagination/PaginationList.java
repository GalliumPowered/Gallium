package org.galliumpowered.pagination;

import net.kyori.adventure.text.Component;

import java.util.List;

public interface PaginationList {
    /**
     * Get the title of this pagination list
     *
     * @return Page title
     */
    Component getTitle();

    /**
     * Get the contents of this pagination list
     *
     * @return Page contents
     */
    List<Component> getContents();

    /**
     * Get the padding of this pagination list
     *
     * @return Page padding
     */
    Component getPadding();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private Component title;
        private List<Component> contents;
        private Component padding = Component.text("=");

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder contents(List<Component> contents) {
            this.contents = contents;
            return this;
        }

        public Builder padding(Component padding) {
            this.padding = padding;
            return this;
        }

        public PaginationList build() {
            return new PaginationList() {
                @Override
                public Component getTitle() {
                    return title;
                }

                @Override
                public List<Component> getContents() {
                    return contents;
                }

                @Override
                public Component getPadding() {
                    return padding;
                }
            };
        }
    }
}
