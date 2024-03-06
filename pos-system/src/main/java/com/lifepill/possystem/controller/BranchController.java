package com.lifepill.possystem.controller;

import com.lifepill.possystem.dto.BranchDTO;
import com.lifepill.possystem.dto.requestDTO.BranchUpdateDTO;
import com.lifepill.possystem.service.BranchService;
import com.lifepill.possystem.util.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("lifepill/v1/branch")
@CrossOrigin
public class BranchController {

    @Autowired
    private BranchService branchService;

    @PostMapping("/save")
    public String saveBranch(@RequestBody BranchDTO branchDTO) {
        branchService.saveBranch(branchDTO);
        return "saved";
    }

    @GetMapping(path = "/get-all-branches")
    public ResponseEntity<StandardResponse> getAllBranches() {
        List<BranchDTO> allBranches = branchService.getAllBranches();
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(201, "SUCCESS", allBranches),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/get-by-id", params = "id")
    public BranchDTO getBranchById(@RequestParam(value = "id") int branchId) {
        BranchDTO branchDTO = branchService.getBranchById(branchId);
        return branchDTO;
    }

    @DeleteMapping(path = "/delete-branch/{id}")
    public String deleteBranch(@PathVariable(value = "id") int branchId) {
        String deleted = branchService.deleteBranch(branchId);
        return deleted;
    }

    @PutMapping("/update")
    public String updateBranch(@RequestBody BranchUpdateDTO branchUpdateDTO){
        String message = branchService.updateBranch(branchUpdateDTO);
        return message;
    }
}
