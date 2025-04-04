package org.galliumpowered.pagination;

public interface PaginationListAudience {
    /**
     * Send a pagination list to this audience
     *
     * @param paginationList The pagination list to send
     */
    void sendPaginationList(PaginationList paginationList);

    /**
     * The maximum lines this pagination list audience can display
     * in a content box when sent a {@link PaginationList}
     *
     * <p>This does not include the header and footer which will be
     * displayed as well. Most likely, this will be the client's maximum
     * chat lines minus 2</p>
     *
     * @return Max lines
     */
    int getMaxPaginationLines();

    /**
     * Get what is identifying this audience for pagination
     *
     * @return Pagination identifier
     */
    String getPaginationIdentifier();
}
