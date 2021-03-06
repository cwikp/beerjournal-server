package com.beerjournal.breweriana.category;

import com.beerjournal.breweriana.category.persistence.Category;
import com.beerjournal.breweriana.category.persistence.CategoryRepository;
import com.beerjournal.infrastructure.error.BeerJournalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.beerjournal.infrastructure.error.ErrorInfo.CATEGORY_NOT_FOUND;

@Service
@RequiredArgsConstructor
class CategoryService {

    private final CategoryRepository categoryRepository;

    CategoryDto getCategoryByName(String name) {
        Category category = categoryRepository.findOneByName(name)
                .orElseThrow(() -> new BeerJournalException(CATEGORY_NOT_FOUND));

        return CategoryDto.of(category);
    }

    Set<String> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }

}
