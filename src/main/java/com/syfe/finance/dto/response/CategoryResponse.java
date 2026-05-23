package com.syfe.finance.dto.response;

import com.syfe.finance.entity.Category.CategoryType;

public class CategoryResponse {

    private String name;
    private CategoryType type;
    private boolean isCustom;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public boolean getIsCustom() { return isCustom; }
    public void setIsCustom(boolean isCustom) { this.isCustom = isCustom; }
}
