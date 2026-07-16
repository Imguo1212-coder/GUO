package com.test.guo.user.feign;

import com.test.guo.common.result.Result;
import com.test.guo.user.dto.DepartmentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class DeptClientFallback implements DeptClient {
    private static final Logger log = LoggerFactory.getLogger(DeptClientFallback.class);
    @Override
    public Result<DepartmentDTO>getById(Long id){
        log.warn("Sentinel fallback:单查部门失败，deptId={}",id);
        return Result.success(null);
    }
    @Override
    public Result<List<DepartmentDTO>>listByIds(List<Long> ids){
        log.warn("Sentinel fallback:批量查部门失败，deptId={}",ids);
        return Result.success(Collections.emptyList());
}
}
