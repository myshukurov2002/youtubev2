package com.example.controller;

import com.example.dto.CategoryDTO;
import com.example.enums.Language;
import com.example.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody CategoryDTO category){
        CategoryDTO response = categoryService.add(category);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@RequestBody CategoryDTO category,
                                 @PathVariable("id") Integer id){
        categoryService.update(id, category);
        return ResponseEntity.ok(true);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id){
        String  response = categoryService.delete(id);
        if(response.length()>0){
            return ResponseEntity.ok("Category Deleted");
        }
        return ResponseEntity.badRequest().body("Category not found");
    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public List<CategoryDTO> all(){
        return categoryService.getAll();
    }
    @GetMapping("/open/get-by-id/{id}")
    public CategoryDTO getById(@PathVariable Integer id,
                                 @RequestParam(defaultValue = "en") Language lang){
        return categoryService.getById(id, lang);
    }

}

