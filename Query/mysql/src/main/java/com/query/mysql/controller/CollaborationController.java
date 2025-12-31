package com.query.mysql.controller;

import com.query.mysql.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mysql/stats")
public class CollaborationController {

    @Autowired
    private CollaborationService collaborationService;

    /**
     * 多表查询：演员合作统计
     */
    @GetMapping("/actor-collaboration")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaboration(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getActorCollaboration(limit));
    }

    /**
     * 宽表查询：演员合作统计
     */
    @GetMapping("/actor-collaboration/fast")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborationFast(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getActorCollaborationFast(limit));
    }

    /**
     * 多表查询：导演-演员合作统计
     */
    @GetMapping("/director-actor")
    public ResponseEntity<List<Map<String, Object>>> getDirectorActorCollaboration(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getDirectorActorCollaboration(limit));
    }

    /**
     * 宽表查询：导演-演员合作统计
     */
    @GetMapping("/director-actor/fast")
    public ResponseEntity<List<Map<String, Object>>> getDirectorActorCollaborationFast(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getDirectorActorCollaborationFast(limit));
    }

    /**
     * 多表查询：按评分/评论数排名的演员合作
     */
    @GetMapping("/actor-collaboration-reviews")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborationReviews(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getActorCollaborationReviews(limit));
    }

    /**
     * 宽表查询：按评分/评论数排名的演员合作
     */
    @GetMapping("/actor-collaboration-reviews/fast")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborationReviewsFast(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getActorCollaborationReviewsFast(limit));
    }

    /**
     * 多表查询：特定类型的演员合作
     */
    @GetMapping("/actor-collaboration-by-genre")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborationByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getActorCollaborationByGenre(genre, limit));
    }

    /**
     * 宽表查询：特定类型的演员合作
     */
    @GetMapping("/actor-collaboration-by-genre/fast")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborationByGenreFast(
            @RequestParam String genre,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(collaborationService.getActorCollaborationByGenreFast(genre, limit));
    }
}

