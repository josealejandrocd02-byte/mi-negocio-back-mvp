package com.minegocio.mi_negocio.dtos.store;

import java.util.List;

public class StoreDetailResponse {

    private final StoreResponse store;
    private final List<StoreMemberResponse> members;

    public StoreDetailResponse(StoreResponse store, List<StoreMemberResponse> members) {
        this.store = store;
        this.members = members;
    }

    public StoreResponse getStore() { return store; }
    public List<StoreMemberResponse> getMembers() { return members; }
}
