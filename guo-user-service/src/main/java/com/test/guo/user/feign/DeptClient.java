package com.test.guo.user.feign;

import com.test.guo.common.result.Result;
import com.test.guo.user.dto.DepartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "dept-service",fallback = DeptClientFallback.class)
public interface DeptClient {

    @GetMapping("/departments/{id}")
    Result<DepartmentDTO> getById(
            @PathVariable("id") Long id);

    @GetMapping("/departments/batch")
    Result<List<DepartmentDTO>> listByIds
            (@RequestParam("ids") List<Long> ids);
}
