package ru.rutmiit.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rutmiit.models.entities.Subject;
import ru.rutmiit.repositories.SubjectRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(String id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));
    }

    public Subject createSubject(String name) {
        if (subjectRepository.existsByName(name)) {
            throw new IllegalArgumentException("Предмет с таким названием уже существует");
        }

        Subject subject = Subject.builder()
                .name(name)
                .build();

        return subjectRepository.save(subject);
    }

    public void deleteSubject(String id) {
        Subject subject = getSubjectById(id);
        subjectRepository.delete(subject);
    }
}