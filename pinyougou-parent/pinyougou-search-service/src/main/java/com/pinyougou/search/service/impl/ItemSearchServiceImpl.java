package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)//推荐时间是放在服务端
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {

        Map map = new HashMap();
        /*Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows",page.getContent());*/
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));


        //1.高亮显示列表
        Map itemList = searchList(searchMap);
        map.putAll(itemList);
        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //3.查询品牌规格列表
        String category= (String) searchMap.get("category");
        if (!category.equals("")){
            Map list = searchBrandAndSpecList(category);
            map.putAll(list );
        }else {
            if (categoryList.size()>0){
                Map list = searchBrandAndSpecList(categoryList.get(0));
                map.putAll(list );
            }
        }

        return map;
    }

    /**
     * 查询列表方法
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map=new HashMap();

        //高亮显示构建
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions heighlightOptions = new HighlightOptions().addField("item_title");
        //构建高亮对象前缀后缀
        heighlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        heighlightOptions.setSimplePostfix("</em>");//后缀
        //为查询对象设置高亮选项
        query.setHighlightOptions(heighlightOptions);

        //1.1关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按照商品分类筛选过滤
        if (!"".equals(searchMap.get("category"))) {//如果用户选择了子分类
            FilterQuery filterQuery =new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3按照品牌分类筛选过滤
        if (!"".equals(searchMap.get("brand"))) {//如果用户选择了子分类
            FilterQuery filterQuery =new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4按规格过滤
        if (searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");

            for (String key : specMap.keySet()) {
                FilterQuery filterQuery =new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5按照价钱分级过滤
        if (!"".equals(searchMap.get("price"))){
            String[] prices = searchMap.get("price").toString().split("-");
            if (!prices[0].equals("0")){//如果最低价格不等于0
                FilterQuery filterQuery =new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!prices[1].equals("*")){//如果最大价格不是*
                FilterQuery filterQuery =new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页
        Integer pageNo= (Integer) searchMap.get("pageNo");
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize= (Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=30;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //排序
        //1.7排序
        String sortValue= (String) searchMap.get("sort");//ASC  DESC
        String sortField= (String) searchMap.get("sortField");//排序字段
        if(sortValue!=null && !sortValue.equals("")){
            if(sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
                query.addSort(sort);
            }
        }

        //********获取高亮结果集*******要在这之前做过滤查询
        //返回高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮入口集合
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表(获取值取决于高亮域的个数)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
            /*for (HighlightEntry.Highlight highlight : highlightList) {
                List<String> sns = highlight.getSnipplets();//为什么不直接获取，而是一个集合，因为每个域可能存在多值情况/multiValue

            }*/
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                TbItem item = entry.getEntity();
                //保证只有一个值时可用
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }

        }
        map.put("rows", page.getContent());
        map.put("totalPages",page.getTotalPages());
        map.put("total",page.getTotalElements() );
        return map;
    }

    /**
     * 查询分组商品分类列表
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap){
        List<String> list = new ArrayList();
        Query query =new SimpleQuery("*:*");
        //关键字查询  where
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//group by
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());//将分组结果添加到返回值中
        }
        return list;
    }
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 查询品牌和规格列表
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //根据商品名称获得模板ID
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId!=null){
            //根据模板Id获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            //根据模板Id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
