package com.lichenglin.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.lichenglin.common.valid.AddGroup;
import com.lichenglin.common.valid.ListValue;
import com.lichenglin.common.valid.UpdateGroup;
import com.lichenglin.common.valid.UpdateStatusGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
@Data
@TableName("pms_brand")
@AllArgsConstructor
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "修改必须指定品牌ID",groups = {UpdateGroup.class})
	@Null(message = "新增不能指定品牌ID",groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "brand name doesn't be null",groups = {AddGroup.class,UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "logo must be a validate URL address",groups = {AddGroup.class,UpdateGroup.class})
	@NotEmpty(groups = {AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {UpdateStatusGroup.class,AddGroup.class})
	@ListValue(values = {0,1},groups = {AddGroup.class, UpdateStatusGroup.class},message = "The specified value must be submitted")
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "^[a-zA-Z]$",message = "first letter must be letter",groups = {AddGroup.class,UpdateGroup.class})
	@NotEmpty(message = "firstLetter doesn't be null",groups = {AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0,message = "sort must be great than 0",groups = {AddGroup.class,UpdateGroup.class})
	@NotNull(message = "sort doesn't be null",groups = {AddGroup.class})
	private Integer sort;

}
