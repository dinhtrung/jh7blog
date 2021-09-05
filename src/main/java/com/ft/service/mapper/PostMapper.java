package com.ft.service.mapper;

import com.ft.domain.*;
import com.ft.service.dto.PostDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Post} and its DTO {@link PostDTO}.
 */
@Mapper(componentModel = "spring", uses = { CategoryMapper.class })
public interface PostMapper extends EntityMapper<PostDTO, Post> {
    @Mapping(target = "category", source = "category", qualifiedByName = "name")
    PostDTO toDto(Post s);
}
