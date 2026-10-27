package com.minegocio.mi_negocio.util;

import com.minegocio.mi_negocio.dtos.store.StoreMemberResponse;
import com.minegocio.mi_negocio.dtos.store.StoreResponse;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.entity.StoreUser;
import com.minegocio.mi_negocio.entity.User;

public final class StoreMapper {

    private StoreMapper() {}

    // Requiere ownerUser cargado (join fetch) o una transaccion abierta
    public static StoreResponse toResponse(Store s) {
        User owner = s.getOwnerUser();
        return new StoreResponse(
                s.getId(), s.getName(), s.getAddress(), s.getPhone(), s.getLogoUrl(), s.getStatus(),
                owner.getId(), owner.getFullName(), owner.getEmail(), s.getCreatedAt());
    }

    // Requiere user cargado (join fetch) o una transaccion abierta
    public static StoreMemberResponse toMember(StoreUser su) {
        User u = su.getUser();
        return new StoreMemberResponse(
                u.getId(), u.getFullName(), u.getEmail(), su.getRole(),
                u.isActive(), su.isActive(), su.getCreatedAt());
    }
}
