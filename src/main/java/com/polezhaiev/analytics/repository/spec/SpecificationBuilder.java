package com.polezhaiev.analytics.repository.spec;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T, D> {
    Specification<T> build(D searchParameters);
}
