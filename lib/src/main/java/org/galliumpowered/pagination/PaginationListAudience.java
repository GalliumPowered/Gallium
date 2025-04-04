package org.galliumpowered.pagination;

public interface PaginationListAudience {
    /**
     * Send a pagination list to this audience
     *
     * @param paginationList The pagination list to send
     */
    void sendPaginationList(PaginationList paginationList);

    /**
     * The maximum lines this pagination list audience recieves
     *
     * @return Max lines
     */
    int getMaxChatLines();

    /**
     * Get what is identifying this audience for pagination
     *
     * @return Pagination identifier
     */
    String getPaginationIdentifier();
}
