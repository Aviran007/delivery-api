package com.dropit.delivery.api.domain.repository;

import java.util.Collection;
import java.util.Optional;

public interface BaseRepository<T, ID> {
	T save(T entity);
	Optional<T> findById(ID id);
	Collection<T> findAll();
	void delete(ID id);
}

