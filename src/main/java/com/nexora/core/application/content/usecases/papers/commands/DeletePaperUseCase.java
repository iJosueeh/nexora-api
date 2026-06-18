package com.nexora.core.application.content.usecases.papers.commands;

import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeletePaperUseCase {

    private final PaperRepository paperRepository;

    public boolean execute(UUID paperId, UUID currentUserId) {
        var paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));

        if (!paper.getAuthorId().equals(currentUserId)) {
            throw new SecurityException("No tienes permiso para eliminar este recurso");
        }

        paperRepository.deleteById(paperId);
        return true;
    }
}
