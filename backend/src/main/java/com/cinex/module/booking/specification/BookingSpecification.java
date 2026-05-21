package com.cinex.module.booking.specification;

import com.cinex.module.booking.dto.BookingFilter;
import com.cinex.module.booking.entity.Booking;
import com.cinex.module.booking.entity.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

public class BookingSpecification {

    private BookingSpecification() {}

    public static Specification<Booking> fromFilter(BookingFilter filter, Long userId) {
        Specification<Booking> spec = Specification.where(hasUser(userId));

        if (!Boolean.TRUE.equals(filter.getIncludeDeleted())) {
            spec = spec.and(notDeleted());
        }
        if (filter.getStatus() != null) {
            spec = spec.and(hasStatus(filter.getStatus()));
        }
        return spec;
    }

    public static Specification<Booking> hasUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> notDeleted() {
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("storageState")),
                        cb.notEqual(root.get("storageState"), "DELETED")
                );
    }
}
