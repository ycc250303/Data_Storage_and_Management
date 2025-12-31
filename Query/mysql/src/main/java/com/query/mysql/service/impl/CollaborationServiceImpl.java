package com.query.mysql.service.impl;

import com.query.mysql.mapper.MoviesMapper;
import com.query.mysql.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CollaborationServiceImpl implements CollaborationService {

    @Autowired
    private MoviesMapper moviesMapper;

    @Override
    public List<Map<String, Object>> getActorCollaboration(int limit) {
        return moviesMapper.getActorCollaboration(limit);
    }

    @Override
    public List<Map<String, Object>> getActorCollaborationFast(int limit) {
        return moviesMapper.getActorCollaborationFast(limit);
    }

    @Override
    public List<Map<String, Object>> getDirectorActorCollaboration(int limit) {
        return moviesMapper.getDirectorActorCollaboration(limit);
    }

    @Override
    public List<Map<String, Object>> getDirectorActorCollaborationFast(int limit) {
        return moviesMapper.getDirectorActorCollaborationFast(limit);
    }

    @Override
    public List<Map<String, Object>> getActorCollaborationReviews(int limit) {
        return moviesMapper.getActorCollaborationReviews(limit);
    }

    @Override
    public List<Map<String, Object>> getActorCollaborationReviewsFast(int limit) {
        return moviesMapper.getActorCollaborationReviewsFast(limit);
    }

    @Override
    public List<Map<String, Object>> getActorCollaborationByGenre(String genre, int limit) {
        return moviesMapper.getActorCollaborationByGenre(genre, limit);
    }

    @Override
    public List<Map<String, Object>> getActorCollaborationByGenreFast(String genre, int limit) {
        return moviesMapper.getActorCollaborationByGenreFast(genre, limit);
    }
}

