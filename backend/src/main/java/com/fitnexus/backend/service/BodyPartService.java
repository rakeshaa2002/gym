package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.BodyPart;

import java.util.List;

public interface BodyPartService {
    List<BodyPart> getAll();

    BodyPart getById(Long id);

    BodyPart create(BodyPart request);

    BodyPart update(Long id, BodyPart request);

    void delete(Long id);
}
