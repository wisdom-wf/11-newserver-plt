package com.elderlycare.controller.ess;

import com.elderlycare.common.Result;
import com.elderlycare.dto.ess.ContractQueryDTO;
import com.elderlycare.service.ess.ContractService;
import com.elderlycare.vo.ess.ContractVO;
import com.elderlycare.vo.ess.SignUrlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public Result<List<ContractVO>> getContractList(ContractQueryDTO query) {
        List<ContractVO> list = contractService.getContractList(query);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ContractVO> getContractDetail(@PathVariable String id) {
        return Result.success(new ContractVO());
    }

    @GetMapping("/order/{orderId}")
    public Result<ContractVO> getContractByOrderId(@PathVariable String orderId) {
        ContractVO contract = contractService.getContractByOrderId(orderId);
        return Result.success(contract);
    }

    @GetMapping("/{id}/sign-url")
    public Result<SignUrlVO> getSignUrl(@PathVariable String id,
                                        @RequestParam(defaultValue = "0") Integer approverType) {
        SignUrlVO signUrl = contractService.getSignUrl(id, approverType);
        return Result.success(signUrl);
    }

    @GetMapping("/{id}/status")
    public Result<String> getContractStatus(@PathVariable String id) {
        String status = contractService.getContractStatus(id);
        return Result.success(status);
    }

    @GetMapping("/{id}/download")
    public Result<String> downloadContract(@PathVariable String id) {
        String downloadUrl = contractService.downloadContract(id);
        return Result.success(downloadUrl);
    }

    @PostMapping("/callback")
    public Result<Void> handleCallback(@RequestBody Object callbackData) {
        contractService.handleCallback(callbackData);
        return Result.success();
    }
}