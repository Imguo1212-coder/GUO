package com.test.guo.dept.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.test.guo.dept.entity.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentCacheService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentCacheService.class);
    private static final String KEY_PREFIX = "dept:id:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public DepartmentCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private String key(Long id) {
        return KEY_PREFIX + id;
    }

    public Department get(Long id) {
        if (id == null) {
            return null;
        }
        try {
            String json = stringRedisTemplate.opsForValue().get(key(id));
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, Department.class);
        } catch (Exception e) {
            // 缓存坏了就删掉，回源数据库，避免接口 500
            log.warn("读取部门缓存失败，将回源数据库, id={}, reason={}", id, e.toString());
            delete(id);
            return null;
        }
    }

    public void put(Department department) {
        if (department == null || department.getId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(department);
            stringRedisTemplate.opsForValue().set(key(department.getId()), json, TTL);
        } catch (Exception e) {
            log.warn("写入部门缓存失败, id={}, reason={}", department.getId(), e.toString());
        }
    }

    public void delete(Long id) {
        if (id == null) {
            return;
        }
        stringRedisTemplate.delete(key(id));
    }

    public Map<Long, Department> getAll(List<Long> ids) {
        Map<Long, Department> hitMap = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return hitMap;
        }
        for (Long id : ids) {
            Department cached = get(id);
            if (cached != null) {
                hitMap.put(id, cached);
            }
        }
        return hitMap;
    }

    public void putAll(List<Department> departments) {
        if (departments == null || departments.isEmpty()) {
            return;
        }
        for (Department department : departments) {
            put(department);
        }
    }

    public List<Long> missIds(List<Long> ids, Map<Long, Department> hitMap) {
        List<Long> miss = new ArrayList<>();
        if (ids == null) {
            return miss;
        }
        for (Long id : ids) {
            if (id != null && !hitMap.containsKey(id)) {
                miss.add(id);
            }
        }
        return miss;
    }
}
