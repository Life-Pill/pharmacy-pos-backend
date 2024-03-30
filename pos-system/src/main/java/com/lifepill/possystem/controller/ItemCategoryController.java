package com.lifepill.possystem.controller;

import com.lifepill.possystem.dto.ItemCategoryDTO;
import com.lifepill.possystem.service.ItemService;
import com.lifepill.possystem.util.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lifepill/v1/item-Category")
@CrossOrigin
public class ItemCategoryController {

    @Autowired
    private ItemService itemService;
    @PostMapping(path = "/save")
    public ResponseEntity<StandardResponse> saveCategory(@RequestBody ItemCategoryDTO categoryDTO) {

        String message = itemService.saveCategory(categoryDTO);
        return new ResponseEntity<>(
                new StandardResponse(201, "Success", message),
                HttpStatus.CREATED);
    }

//    @GetMapping("/category/all")
//    public ResponseEntity<StandardResponse> getAllCategories() {
//        List<ItemCategoryDTO> categories = itemService.getAllCategories();
//        return new ResponseEntity<>(
//                new StandardResponse(200, "Success", categories),
//                HttpStatus.OK);
//    }

    @GetMapping("/category/all")
    public ResponseEntity<StandardResponse> getAllCategories() {
        List<ItemCategoryDTO> categories = itemService.getAllCategories();
        return new ResponseEntity<>(
                new StandardResponse(200, "Success", categories),
                HttpStatus.OK);
    }



}
