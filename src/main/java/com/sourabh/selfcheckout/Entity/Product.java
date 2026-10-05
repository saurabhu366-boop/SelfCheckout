package com.sourabh.selfcheckout.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Column(name = "name")
    private String name;

    @Column(name = "mrp")
    private BigDecimal mrp;

    @Column(name = "size")
    private String size;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Transient
    private int stockQuantity;

    /**
     * Temporary compatibility accessor for code that still expects the old price field.
     * Remove when cart and response code use mrp directly.
     */
    public double getPrice() {
        return mrp == null ? 0.0 : mrp.doubleValue();
    }
}
