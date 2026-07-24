package com.test.guo.behavior.repository;

import com.test.guo.behavior.document.UserBehaviorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserBehaviorRepository extends MongoRepository<UserBehaviorDocument, String> {
    List<UserBehaviorDocument> findByTargetUserIdOrderByOccurredAtDesc(Long targetUserId);
}
