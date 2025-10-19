package com.example.eveant.category;

public class CategoryRejectionRequest {
    private int replacementCategoryId;
    public CategoryRejectionRequest(int replacementCategoryId) {
        this.replacementCategoryId = replacementCategoryId;
    }
    public int getReplacementCategoryId() { return replacementCategoryId; }
}
