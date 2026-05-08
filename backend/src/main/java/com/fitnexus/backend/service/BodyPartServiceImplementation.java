package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.BodyPart;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.BodyPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BodyPartServiceImplementation implements BodyPartService {
    private final BodyPartRepository bodyPartRepository;

    @Override
    public List<BodyPart> getAll() {
        return bodyPartRepository.findAllByOrderByUpdatedAtDescIdDesc();
    }

    @Override
    public BodyPart getById(Long id) {
        return bodyPartRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Body part not found"));
    }

    @Override
    public BodyPart create(BodyPart request) {
        BodyPart entity = new BodyPart();
        copyFields(request, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return bodyPartRepository.save(entity);
    }

    @Override
    public BodyPart update(Long id, BodyPart request) {
        BodyPart entity = getById(id);
        copyFields(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        return bodyPartRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        if (!bodyPartRepository.existsById(id)) {
            throw new InvalidOperationException("Body part not found");
        }
        bodyPartRepository.deleteById(id);
    }

    private void copyFields(BodyPart source, BodyPart target) {
        if (source == null || source.getName() == null || source.getName().isBlank()) {
            throw new IllegalArgumentException("Body part name is required");
        }
        String name = source.getName().trim();
        Long targetId = target.getId();
        if (targetId == null ? bodyPartRepository.existsByNameIgnoreCase(name) : bodyPartRepository.existsByNameIgnoreCaseAndIdNot(name, targetId)) {
            throw new IllegalArgumentException("Body part name already exists");
        }

        target.setName(name);
        target.setDescription(source.getDescription());
        target.setStatus(normalizeStatus(source.getStatus()));
    }

    private String normalizeStatus(String value) {
        return value == null || value.isBlank() ? "ACTIVE" : value.trim().toUpperCase();
    }
}
