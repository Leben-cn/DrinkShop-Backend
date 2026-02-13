package com.leben.drinkshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddressRequest {

    // 修改地址时需要传 ID，新增时不需要
    private Long id;

    private String contactName;

    private String contactPhone;

    private String addressPoi;

    private String addressDetail;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Boolean isDefault = false;
}