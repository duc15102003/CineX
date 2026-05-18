package com.cinex.common.service;

import com.cinex.common.entity.BaseEntity;
import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * [Template Method Pattern] Class CRUD dùng chung cho tất cả module.
 * Subclass chỉ cần override getRepository() để trả repository riêng.
 * Các method findById, findAll, save, softDelete đã viết sẵn — DRY principle.
 */
public abstract class BaseService<E extends BaseEntity, ID> {

    protected abstract JpaRepository<E, ID> getRepository();

    @Transactional(readOnly = true)
    public E findById(ID id) {
        return getRepository().findById(id)
                .filter(entity -> !"DELETED".equals(entity.getStorageState()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<E> findAll(Pageable pageable) {
        Page<E> page = getRepository().findAll(pageable);
        return PageResponse.from(page);
    }

    @Transactional
    public E save(E entity) {
        return getRepository().save(entity);
    }

    @Transactional
    public void softDelete(ID id) {
        E entity = findById(id);
        entity.setStorageState("DELETED");
        getRepository().save(entity);
    }
}
