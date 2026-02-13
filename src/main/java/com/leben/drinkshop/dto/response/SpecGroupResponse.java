package com.leben.drinkshop.dto.response;
import lombok.Data;
import java.util.List;

@Data
public class SpecGroupResponse {
    private Long id;
    private String groupName;      // "温度"
    private Boolean isMultiple;    // 是否多选
    private List<SpecOptionResponse> options; // 该组下的选项
}