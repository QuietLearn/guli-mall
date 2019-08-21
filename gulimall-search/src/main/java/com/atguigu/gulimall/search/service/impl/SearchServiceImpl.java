package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.to.es.EsSkuVo;
import com.atguigu.gulimall.search.service.SearchService;
import com.atguigu.gulimall.search.vo.*;
import com.google.common.collect.Lists;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.Aggregation;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import springfox.documentation.spring.web.json.Json;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    /**
     * 去es检索商品
     * @param params  前端传来的查询条件
     */
    @Override
    public SearchResponse search(SearchParam params) {

        //0、根据前端传来的条件生成一个DSL语句
        String searchDSL = buildSearchDSL(params);
        //1、创建一个检索动作
        Search search = new Search.Builder(searchDSL)
                .addIndex(Constant.ES_GULIMALL_INDEX)
                .addType(Constant.ES_SPU_TYPE)
                .build();


        SearchResult searchResult = null;
        try {
            //2、执行检索获取到检索的数据，返回的这个就是kibana里面搜索出来的整个json大对象
            searchResult = jestClient.execute(search);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //3、将检索出来的searchResult，提取前端需要用的数据模型，组装交给前端
        SearchResponse searchResponse = buildResult(searchResult);

        //4、封装前端传来的页码，每页大小
        searchResponse.setPageNum(params.getPageNum());
        searchResponse.setPageSize(params.getPageSize());

        return searchResponse;
    }

    private SearchResponse buildResult(SearchResult searchResult) {
        //searchResult 就是es返回的查询到的结果
        log.info("接受到的SearchResult：{}",searchResult);
        SearchResponse response = new SearchResponse();

        //1、从返回的searchResult抽取所有查询到的商品数据；
        //1.1）、获取所有查询到的记录
//        todo 获取命中记录
        List<SearchResult.Hit<EsSkuVo, Void>> hits = searchResult.getHits(EsSkuVo.class);
        List<EsSkuVo> esSkuVoList = Lists.newLinkedList();
        //1.2）、遍历查询到的结果
        hits.forEach(hit->{
            EsSkuVo esSkuVo = hit.source;
            Map<String, List<String>> highlight = hit.highlight;
            //将商品的名字重新改为高亮的名字
            if (highlight!=null){
                esSkuVo.setSkuTitle(highlight.get("skuTitle").get(0));
            }
            esSkuVoList.add(esSkuVo);
        });

        response.setProducts(esSkuVoList);
        //2、封装分页的总记录树
        response.setTotal(searchResult.getTotal());

        //所有的聚合结果
        MetricAggregation aggregations = searchResult.getAggregations();
        //3、设置当前查到的结果所涉及到的所有属性关系
//        todo 请注意，这边是得到聚合的结果，而不是设置聚合条件DSL进行聚合，只是得到结果而已
        //response.setAttrs();
        ChildrenAggregation attr_agg = aggregations.getChildrenAggregation("attr_agg");

        TermsAggregation attrId_agg = attr_agg.getTermsAggregation("attrId_agg");

        List<SearchResponseAttrVo> attrs = new ArrayList<>();
        //获取到attrId_agg的Buck就能知道有多少个attrId
        List<TermsAggregation.Entry> attrIdAggBuckets = attrId_agg.getBuckets();
        //循环attrId的聚合，并通过获取attrname和value子聚合获取attrID--对应的attrname和attrValues，并将检索结果的聚合结果封装
        attrIdAggBuckets.forEach(attrIdAggBucket->{
            //获取attrId
            Long attrId = Long.valueOf(attrIdAggBucket.getKey());
            //获取attrName子聚合并获取name值
            TermsAggregation attrName_agg = attrIdAggBucket.getTermsAggregation("attrName_agg");
            String attrName = attrName_agg.getBuckets().get(0).getKey();

            //获取attrValue子聚合并获取value值
            TermsAggregation attrValue_agg = attrIdAggBucket.getTermsAggregation("attrValue_agg");
            List<TermsAggregation.Entry> attrValueBuckets = attrValue_agg.getBuckets();
            //属性的值
            List<String> attrValueList = new ArrayList<>();
            attrValueBuckets.forEach(attrValueBucket->{
                String attrValue = attrValueBucket.getKey();
                attrValueList.add(attrValue);
            });
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            searchResponseAttrVo.setName(attrName);
            searchResponseAttrVo.setProductAttributeId(attrId);
            searchResponseAttrVo.setValue(attrValueList);
            attrs.add(searchResponseAttrVo);
        });

        response.setAttrs(attrs);
        //4、设置当前查到的结果所涉及到的所有品牌关系
        /*SearchResponseAttrVo brand = new SearchResponseAttrVo();
        response.setBrand(brand);*/
        List<BrandVo> brandVoList = getBrandVoList(aggregations);
        response.setBrand(brandVoList);

        //5、设置当前查到的结果所涉及到的所有分类关系
      /*  SearchResponseAttrVo catelog = new SearchResponseAttrVo();
        response.setCatelog(catelog);*/
        List<CategoryVo> categoryVoList = getCategoryVoList(aggregations);
        response.setCatelog(categoryVoList);
        return response;
    }

    private List<BrandVo> getBrandVoList(MetricAggregation aggregations){
        TermsAggregation brandId_agg = aggregations.getTermsAggregation("brandId_agg");
        List<TermsAggregation.Entry> brandIdBuckets = brandId_agg.getBuckets();

        List<BrandVo> brandVoList = Lists.newArrayList();
        brandIdBuckets.forEach(brandIdBucket->{
            BrandVo brandVo = new BrandVo();
            Long brandId = Long.valueOf(brandIdBucket.getKey());
//            todo 聚合的聚合条件不是term 都是terms
            TermsAggregation brandName_agg = brandIdBucket.getTermsAggregation("brand_name");
            String brandName = brandName_agg.getBuckets().get(0).getKey();
//            String brandName = brandName_agg.getBuckets().get(0).getKey();
            TermsAggregation brandLogo_agg = brandIdBucket.getTermsAggregation("brand_logo");
            String brandLogo = brandLogo_agg.getBuckets().get(0).getKey();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setLogo(brandLogo);
            brandVoList.add(brandVo);
            //todo 老师的品牌存储方案  何必多次一举，本来springmvc就自动集成jackson把响应转为json'的
            /*List<String> brands = Lists.newArrayList();
            Map<String,Object> b = new HashMap<>();
            b.put("id",brandId);
            b.put("name",brandName);
            brands.add(JSON.toJSONString(b));*/
        });




        return brandVoList;
    }

    private List<CategoryVo> getCategoryVoList(MetricAggregation aggregations){
        TermsAggregation catIdAgg = aggregations.getTermsAggregation("catId_agg");
        List<TermsAggregation.Entry> catIdBuckets = catIdAgg.getBuckets();

        List<CategoryVo> categoryVoList = Lists.newArrayList();
        catIdBuckets.forEach(catIdBucket->{
            CategoryVo categoryVo = new CategoryVo();
            Long catId = Long.valueOf(catIdBucket.getKey());
//            todo 聚合的聚合条件不是term 都是terms
            TermsAggregation categoryName_agg = catIdBucket.getTermsAggregation("cat_name");
            String CategoryName = categoryName_agg.getBuckets().get(0).getKey();
//            String CategoryName = CategoryName_agg.getBuckets().get(0).getKey();
            categoryVo.setCategoryId(catId);
            categoryVo.setCategoryName(CategoryName);
            categoryVoList.add(categoryVo);
        });
        return categoryVoList;
    }

    private String buildSearchDSL(SearchParam params) {

        //0、先构建获取一个SearchSourceBuilder，辅助我们得到DSL语句
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //1、查询&过滤
        BoolQueryBuilder bool = new BoolQueryBuilder();
        //1）、构造match条件
        if(StringUtils.isNotBlank(params.getKeyword())){
            MatchQueryBuilder match = new MatchQueryBuilder("skuTitle",params.getKeyword());
            bool.must(match);
        } else{

        }
//        todo 用或者做拼接条件 关键字可以作分类查询？ 根据关键字分类也可以查询出结果
        //2）、构造过滤条件
        //2.1）、按照品牌过滤
        Long[] brandIds = params.getBrand();
        if(brandIds!=null && brandIds.length>0){
            TermsQueryBuilder brandTermsQb = new TermsQueryBuilder("brandId", brandIds);
            bool.filter(brandTermsQb);
        }

        //2.2）、按照分类id过滤
        Long[] catelog3Ids = params.getCatelog3();
        if(catelog3Ids !=null && catelog3Ids.length>0){
            TermsQueryBuilder categoryTermsQb = new TermsQueryBuilder("productCategoryId", catelog3Ids);
            bool.filter(categoryTermsQb);
        }

        //2.3）、按照价格区间过滤
        Integer priceFrom = params.getPriceFrom();
        Integer priceTo = params.getPriceTo();
        if(priceFrom !=null || priceTo !=null){
            RangeQueryBuilder priceRangeQb = new RangeQueryBuilder("price");
            if(priceFrom !=null){
                priceRangeQb.gte(priceFrom);
            }
            if(priceTo !=null){
                priceRangeQb.lte(priceTo);
            }
            bool.filter(priceRangeQb);
        }

        String[] props = params.getProps();


        if (props!=null&&props.length>0){
            //2:win10-android-
            //3:4g
            //4:5.5   格式：  属性id：属性值1-属性值2
            for (String prop : props) {
                String[] attrIdOrValue = prop.split(":");
                if(attrIdOrValue!=null && attrIdOrValue.length == 2) {
                    String attrId = attrIdOrValue[0];
                    String attValuesStr = attrIdOrValue[1];
                    String[] attrValues = attValuesStr.split("-");

                    BoolQueryBuilder attrNestedBoolqb = new BoolQueryBuilder();
                    TermQueryBuilder attrIdTermQb = new TermQueryBuilder("attrValueList.attrId", attrId);
                    attrNestedBoolqb.must(attrIdTermQb);

                    //属性值 terms即可以 任意满足其中1个条件即可，是in，，将满足里面任意一个条件的所有数据 集合起来
                    TermsQueryBuilder attrValuesTermQb = new TermsQueryBuilder("attrValueList.attrValue", attrValues);
                    attrNestedBoolqb.must(attrValuesTermQb);

                    // ScoreMode 如何将多个子命中率得分聚合到单个父级得分中。
                    NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrValueList", attrNestedBoolqb, ScoreMode.None);

                    bool.filter(nestedQueryBuilder);
                }
            }
        }


        builder.query(bool);// this.queryBuilder = query;  return this;
        //2、分页  2  1:0,2  2: 2,2  3:4,2
        builder.from((params.getPageNum()-1)*params.getPageSize());
        builder.size(params.getPageSize());

        //3、高亮
        if(StringUtils.isNotBlank(params.getKeyword())){
            //前端传递了按关键字的查询条件
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            try {
                byte[] encode = Base64.getEncoder().encode("<b style='color:red'>".getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            highlightBuilder.preTags("<b style='color:red'>")
                    .postTags("</b>")
                    .field("skuTitle");
            builder.highlighter(highlightBuilder);
        }

        //4、排序 0:asc 1:asc 因为jd也是单字段排序所以就1个就行了  0：综合排序  1：销量  2：价格
        String order = params.getOrder();
        if(StringUtils.isNotBlank(order)){
            String[] orderStr = order.split(":");
            //验证传递的参数
            if(orderStr!=null && orderStr.length == 2){
                //解析升降序规则
                SortOrder sortOrder = orderStr[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
                if(orderStr[0].equals("0")){
                    builder.sort("_score", sortOrder);
                }
                if(orderStr[0].equals("1")){
                    builder.sort("sale",sortOrder);
                }
                if(orderStr[0].equals("2")){
                    builder.sort("price",sortOrder);
                }
            }
        }

        //5、聚合
        //5.1）、属性嵌套大聚合
        NestedAggregationBuilder attrAggg = new NestedAggregationBuilder("attr_agg","attrValueList");

        //嵌套大聚合里面的子聚合
        TermsAggregationBuilder attrId_agg = new TermsAggregationBuilder("attrId_agg", ValueType.LONG);
        attrId_agg.field("attrValueList.attrId");

        //子聚合里面的子聚合

        TermsAggregationBuilder attrName_agg = new TermsAggregationBuilder("attrName_agg", ValueType.STRING);
        TermsAggregationBuilder attrValue_agg = new TermsAggregationBuilder("attrValue_agg", ValueType.STRING);

        attrName_agg.field("attrValueList.attrName");
        attrValue_agg.field("attrValueList.attrValue");

        //subAggregation子聚合
        attrId_agg.subAggregation(attrName_agg);
        attrId_agg.subAggregation(attrValue_agg);

        attrAggg.subAggregation(attrId_agg);
        builder.aggregation(attrAggg);

        //5.2）、品牌嵌套大聚合
        //品牌id聚合
        TermsAggregationBuilder brandId_agg = new TermsAggregationBuilder("brandId_agg",ValueType.LONG);
        brandId_agg.field("brandId");
        TermsAggregationBuilder brandName_agg = new TermsAggregationBuilder("brand_name", ValueType.STRING);
        brandName_agg.field("brandName");

        TermsAggregationBuilder brandLogo_agg = new TermsAggregationBuilder("brand_logo", ValueType.STRING);
        brandLogo_agg.field("brandLogo");

        brandId_agg.subAggregation(brandName_agg);
        brandId_agg.subAggregation(brandLogo_agg);

        TermsAggregationBuilder catId_agg = new TermsAggregationBuilder("catId_agg", ValueType.LONG);
        catId_agg.field("productCategoryId");
        TermsAggregationBuilder catName_agg = new TermsAggregationBuilder("cat_name", ValueType.STRING);
        catName_agg.field("productCategoryName");
        catId_agg.subAggregation(catName_agg);


        builder.aggregation(brandId_agg);
        builder.aggregation(catId_agg);
        return builder.toString();
    }

    public static void main(String[] args) {
       /* byte[] encode =null;
        try {
            byte[] bytes = "<b style='color:red'>".getBytes("UTF-8");
            for (byte aByte : bytes) {
                System.out.print(aByte+" ");
            }
            System.out.println();
            encode = Base64.getEncoder().encode(bytes);
            for (byte bByte : encode) {
                System.out.print(bByte+" ");
            }
            System.out.println();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        byte[] decode = Base64.getDecoder().decode(encode);
        for (byte b : decode) {
            System.out.print(b+" ");
        }

        System.out.println();
        System.out.println(decode);
        String s = new String(decode);
        System.out.println(s);*/

        SearchServiceImpl searchService = new SearchServiceImpl();
        SearchParam searchParam = new SearchParam();
        //2:win10-android-
        //3:4g
        //4:5.5   格式：
        String props[] = {"31:2-3","34:5"};
        searchParam.setProps(props);
        searchParam.setBrand(new Long[]{1l,3l,4l});
        searchParam.setKeyword("手机");

        String s = searchService.buildSearchDSL(searchParam);
        System.out.println(s);

    }
}
