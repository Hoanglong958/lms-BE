package com.ra.base_spring_boot.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {

    public static Pageable createPageable(int page, int size, String sort, String defaultField) {
        Sort sortObj = parseSort(sort, defaultField);
        return PageRequest.of(page, size, sortObj);
    }

    /**
     * Chuyển đổi chuỗi sort thành đối tượng Sort của Spring Data
     */
    public static Sort parseSort(String sort, String defaultField) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, defaultField);
        }

        String[] parts = sort.split(",");
        if (parts.length == 2) {
            try {
                Sort.Direction direction = Sort.Direction.fromString(parts[1]);
                return Sort.by(direction, parts[0]);
            } catch (IllegalArgumentException e) {
                return Sort.by(Sort.Direction.DESC, parts[0]);
            }
        }
        
        return Sort.by(Sort.Direction.DESC, defaultField);
    }
}
