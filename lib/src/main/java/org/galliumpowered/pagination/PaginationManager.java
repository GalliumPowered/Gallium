package org.galliumpowered.pagination;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaginationManager {
    private final Map<String, Page> pages = new HashMap<>();

    public void nextPage(PaginationListAudience audience) {
        Page page = pages.get(audience.getPaginationIdentifier());

        int maxIndex = page.paginationList.getContents().size();
        int newIndex = Math.min(page.index + audience.getMaxPaginationLines(), maxIndex);

        page.index = newIndex;

        List<Component> contents = page.paginationList.getContents().subList(newIndex,
                Math.min(newIndex + audience.getMaxPaginationLines(), maxIndex));

        audience.sendPaginationList(PaginationList.builder()
                .title(page.paginationList.getTitle())
                .padding(page.paginationList.getPadding())
                .contents(contents)
                .build());
    }

    public void previousPage(PaginationListAudience audience) {
        Page page = pages.get(audience.getPaginationIdentifier());

        int newIndex = Math.max(page.index - audience.getMaxPaginationLines(), 0);

        List<Component> contents = page.paginationList.getContents().subList(newIndex,
                Math.min(newIndex + audience.getMaxPaginationLines(), page.paginationList.getContents().size()));

        page.index = newIndex;

        audience.sendPaginationList(PaginationList.builder()
                .title(page.paginationList.getTitle())
                .padding(page.paginationList.getPadding())
                .contents(contents)
                .build());
    }

    public void submit(PaginationListAudience audience, PaginationList paginationList) {
        pages.put(audience.getPaginationIdentifier(), new Page(paginationList, 0));
    }

    // page tracker
    static class Page {
        private final PaginationList paginationList;
        private int index;

        public Page(PaginationList paginationList, int index) {
            this.paginationList = paginationList;
            this.index = index;
        }
    }
}
