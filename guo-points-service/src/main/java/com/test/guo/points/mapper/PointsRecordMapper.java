package com.test.guo.points.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;


public interface PointsRecordMapper {

    @Insert("""
        INSERT IGNORE INTO user_points_record(   //IGNORE：遇到唯一键重复时，不抛出异常，忽略本次插入
               event_id,
               user_id,
               change_amount,
               reason,
               create_time
         )
         VALUES (
            #{eventId},
            #{userId},
            #{changeAmount},
            #{reason},
            NOW()
         )
         """)
    int insertRecord(
            @Param("eventId") String eventId,
            @Param("userId") Long userId,
            @Param("changeAmount") Integer changeAmount,
            @Param("reason") String reason

    );
}
