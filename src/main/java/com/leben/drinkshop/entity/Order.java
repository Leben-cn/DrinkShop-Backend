package com.leben.drinkshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号 */
    @Column(unique = true, nullable = false)
    private String orderNo;

    /** 用户ID (对应你的App用户) */
    private Long userId;

    /** 订单状态 */
    private Integer status; // 0:待支付, 1:待制作...

    // --- 店铺快照 ---
    private Long shopId;
    private String shopName;
    private String shopLogo;

    // --- 金额明细 (使用 BigDecimal) ---
    private BigDecimal payAmount;       // 实付
    private BigDecimal goodsTotalPrice; // 商品总价
    private BigDecimal packingFee;      // 打包费
    private BigDecimal deliveryFee;     // 配送费
    private BigDecimal discountAmount;  // 优惠

    // --- 收货信息快照 ---
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /** 更新时间 */
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "is_commented")
    private Boolean isCommented=false;

    // --- 关联关系 ---

    // 一对多关联：一个订单对应多个商品详情
    // cascade = CascadeType.ALL 表示保存订单时，会自动保存里面的 items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;
}