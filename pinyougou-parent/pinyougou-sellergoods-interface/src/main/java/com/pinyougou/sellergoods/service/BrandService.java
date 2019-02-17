package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import com.pinyougou.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {

    public List<TbBrand> findAll();

    /**
     * 品牌分页查询
     * @param page 当前页
     * @param size 每页显示记录数
     * @return
     */
    public PageResult findPage(TbBrand brand,int page,int size);

    /**
     * 添加
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 差一个
     * @param id
     * @return
     */
    public TbBrand findOne(long id);

    /**
     * 修改
     * @param brand
     */
    void update(TbBrand brand);


    void delete(long[] ids);

    /**
     * 返回下拉列表
     * @return
     */
    public List<Map> selectOptionList();
}
