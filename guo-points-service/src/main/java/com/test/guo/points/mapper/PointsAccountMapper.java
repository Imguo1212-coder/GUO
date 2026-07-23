package com.test.guo.points.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface PointsAccountMapper {

    @Insert("""
        INSERT INTO user_points_account(
            user_id,
            balance,
            update_time
        )
        VALUES(
            #{userId},
            #{amount},
            NOW()
        )
        ON DUPLICATE KEY UPDATE
            balance = balance + #{amount},
            update_time = NOW()
        """)
    int addPoints(
            @Param("userId") Long userId,
            @Param("amount") Integer amount
    );
}
