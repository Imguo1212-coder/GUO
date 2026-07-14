package com.test.guo.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.guo.common.exception.BusinessException;
import com.test.guo.common.exception.ErrorCode;
import com.test.guo.common.result.Result;
import com.test.guo.user.dto.DepartmentDTO;
import com.test.guo.user.dto.UserCreateRequest;
import com.test.guo.user.dto.UserUpdateRequest;
import com.test.guo.user.entity.User;
import com.test.guo.user.feign.DeptClient;
import com.test.guo.user.mapper.UserMapper;
import com.test.guo.user.vo.UserVO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final DeptClient deptClient;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserMapper userMapper, DeptClient deptClient) {
        this.userMapper = userMapper;
        this.deptClient = deptClient;
    }

    public User create(UserCreateRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setEmail(request.getEmail());
        user.setDepartmentId(request.getDepartmentId());
        user.setCreateTime(LocalDateTime.now());

        int rows = userMapper.insert(user);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.USER_OPERATION_FAILED, "用户新增失败");
        }
        return user;
    }
    public void delete(Long id) {
        int rows = userMapper.deleteById(id);

        if (rows == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在，删除失败");
        }
    }

    public void update(Long id, UserUpdateRequest request) {
        User user = new User();
        user.setId(id);
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setEmail(request.getEmail());
        user.setDepartmentId(request.getDepartmentId());

        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在，修改失败");
        }
    }

    public UserVO getByIdWithDeptName(Long id) {
        User user = userMapper.selectById(id);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toUserVO(user);
    }

    public IPage<UserVO> pageWithDeptName(String name, long page, long size) {
        // 创建分页对象
        Page<User> userPage = new Page<>(page, size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(User::getName, name);
        }

        IPage<User> resultPage = userMapper.selectPage(userPage, wrapper);  // wrapper：有 name 就按名字模糊查
        // 分页查数据库
        List<Long> deptIds = resultPage.getRecords().stream()
                .map(User::getDepartmentId)  // 取出每个用户的部门 id
                .filter(Objects::nonNull)  // 没部门的不用
                .distinct()   // wrapper：有 name 就按名字模糊查
                .collect(Collectors.toList());

        Map<Long, DepartmentDTO> deptMap = loadDeptMap(deptIds);  //有 id → Feign listByIds 一次.成功：List 转成 Map;失败：空 Map + WARN（降级）
                                                                  // 没 id → 直接空 Map，连 Feign 都不用打
        Page<UserVO> voPage = new Page<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal()
        );
        List<UserVO> voList = new ArrayList<>();
        for (User user : resultPage.getRecords()) {
            voList.add(toUserVO(user, deptMap));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> list(String name) {
        if (name == null || name.trim().isEmpty()) {
            return userMapper.selectList(null);
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getName, name);
        return userMapper.selectList(wrapper);
    }
    private Map<Long, DepartmentDTO> loadDeptMap(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Result<List<DepartmentDTO>> response = deptClient.listByIds(deptIds);
            if (response == null || response.getData() == null) {
                log.warn("批量查询部门返回为空, deptIds={}", deptIds);
                return Collections.emptyMap();
            }
            Map<Long, DepartmentDTO> map = new HashMap<>();
            for (DepartmentDTO dept : response.getData()) {
                if (dept != null && dept.getId() != null) {
                    map.put(dept.getId(), dept);
                }
            }
            return map;
        } catch (Exception e) {
            log.warn("批量查询部门失败，已降级为空部门名, deptIds={}, reason={}",
                    deptIds, e.toString());
            return Collections.emptyMap();
        }
    }

    private UserVO toUserVO(User user) {
        Map<Long, DepartmentDTO> deptMap = Collections.emptyMap();
        if (user.getDepartmentId() != null) {
            try {
                Result<DepartmentDTO> response = deptClient.getById(user.getDepartmentId());
                if (response != null && response.getData() != null) {
                    DepartmentDTO dept = response.getData();
                    deptMap = Map.of(dept.getId(), dept);
                }
            } catch (Exception e) {
                log.warn("查询部门失败，已降级, deptId={}, reason={}",
                        user.getDepartmentId(), e.toString());
            }
        }
        return toUserVO(user, deptMap);
    }

    private UserVO toUserVO(User user, Map<Long, DepartmentDTO> deptMap) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setAge(user.getAge());
        vo.setEmail(user.getEmail());
        vo.setCreateTime(user.getCreateTime());
        vo.setDepartmentId(user.getDepartmentId());

        if (user.getDepartmentId() != null && deptMap != null) {
            DepartmentDTO dept = deptMap.get(user.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getName());
                vo.setDepartmentDescription(dept.getDescription());
            }
        }
        return vo;
    }
}
