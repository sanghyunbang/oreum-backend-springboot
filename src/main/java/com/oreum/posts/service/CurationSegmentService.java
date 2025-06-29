package com.oreum.posts.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oreum.posts.dao.CurationSegmentRepository;
import com.oreum.posts.dto.CurationSegmentDoc;

@Service
public class CurationSegmentService {

    private final CurationSegmentRepository repository;

    public CurationSegmentService(CurationSegmentRepository repository) {
        this.repository = repository;
    }

    public List<CurationSegmentDoc> searchByKeyword(String keyword) {
        return repository.searchByKeyword(keyword);
    }
}

