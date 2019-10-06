package com.atguigu.gulimall.order.controller;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.Order;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.order.OrderEntityVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {


    @Autowired
    OrderService orderService;


    //以后只要登陆了都会带上请求头
    @ApiOperation("订单确认信息")
    @GetMapping("/confirm")
    public Resp<OrderConfirmVo> orderConfirm(HttpServletRequest request){

        String authorization = request.getHeader("Authorization");
        Map<String, Object> body = GuliJwtUtils.getJwtBody(authorization);
        //userId
        long id = Long.parseLong(body.get("id").toString());

        System.out.println(request);
        OrderConfirmVo confirmVo =  orderService.confirmOrderData(id);

        return Resp.ok(confirmVo);
    }

    @PostMapping("/submit")
    public Resp<Object> submitOrder(@RequestBody OrderSubmitVo vo,
                                    HttpServletRequest request){
        Long userId = getCurrentUserId(request);

        Resp<Object> resp = orderService.submitOrder(vo, userId);
        Object data = resp.getData();
        if(data instanceof OrderEntityVo){
            //订单成功了...
            //生成一个支付页，等待支付
        }
        return resp;
    }


    private Long getCurrentUserId(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        Map<String, Object> body = GuliJwtUtils.getJwtBody(authorization);
        long id = Long.parseLong(body.get("id").toString());
        return id;
    }

//    /**
//     * 创建订单
//     * @return
//     */
//    @GetMapping("/create")
//    public Order createOrder(){
//
//       Order order =  orderService.createOrder();
//
//       return order;
//    }
}
