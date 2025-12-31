package com.query.mysql.service;

import java.util.List;
import java.util.Map;

public interface CollaborationService {
    List<Map<String, Object>> getActorCollaboration(int limit);
    List<Map<String, Object>> getActorCollaborationFast(int limit);
    
    List<Map<String, Object>> getDirectorActorCollaboration(int limit);
    List<Map<String, Object>> getDirectorActorCollaborationFast(int limit);
    
    List<Map<String, Object>> getActorCollaborationReviews(int limit);
    List<Map<String, Object>> getActorCollaborationReviewsFast(int limit);
    
    List<Map<String, Object>> getActorCollaborationByGenre(String genre, int limit);
    List<Map<String, Object>> getActorCollaborationByGenreFast(String genre, int limit);
}

