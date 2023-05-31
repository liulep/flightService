package com.yue.spring.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yue.spring.Exception.YueException;
import com.yue.spring.client.CouponClient;
import com.yue.spring.client.RouteClient;
import com.yue.spring.client.StockClient;
import com.yue.spring.client.UserClient;
import com.yue.spring.mq.config.RabbitMQConfig;
import com.yue.spring.handler.utils.OauthUtils;
import com.yue.spring.handler.utils.RedissonLockUtil;
import com.yue.spring.pojo.DO.Coupon;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import com.yue.spring.pojo.DTO.CouponDTO;
import com.yue.spring.pojo.DTO.OrderDTO;
import com.yue.spring.pojo.PO.CouponFlowPO;
import com.yue.spring.pojo.PO.OrderInfoPO;
import com.yue.spring.pojo.PO.OrderPO;
import com.yue.spring.pojo.VO.OrderInfoVo;
import com.yue.spring.pojo.VO.OrderVo;
import com.yue.spring.pojo.VO.RouteEn;
import com.yue.spring.service.OrderInfoService;
import com.yue.spring.service.OrderService;
import com.yue.spring.utils.SequenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("order")
@SuppressWarnings("all")
public class OrderController implements ApplicationContextAware {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private SequenceUtils sequenceUtils;

    @Autowired
    private StockClient stockClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RouteClient routeClient;

    private final static String INIT_STOCK = "line_stock:";

    private final static String ORDER_INFO = "order_num_user:";

    private final static String ORDER_USER = "order_user_list:";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CouponClient couponClient;

    private ApplicationContext context;


    //创建订单
    @PostMapping("/create/order")
    public String book(@RequestBody @Valid OrderDTO planeOrder) {
        // fromTime 第一趟航班开始时间
        // toTime 第二趟航班开始时间 如果只有一趟航班则toTime不传
        // lineIds: 航班Ids
        // userRealtionIds: 联系人Ids
        boolean res = false;
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/stock.lua")));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String[] date = new String[]{format.format(planeOrder.getFromTime()), planeOrder.getToTime() == null ? null : format.format(planeOrder.getToTime())};
        //判断当前用户是否还有其他订单还未付款
        Object o = redisTemplate.opsForValue().get(ORDER_USER + OauthUtils.getCurrentUser().getUserId());
        if (o != null)
            throw new YueException("您当前还有其他订单暂未付款");
        String sequence = sequenceUtils.getSequence();
        RedissonLockUtil.lock(sequence, 3);
        try {
            res = RedissonLockUtil.tryLock(sequence, TimeUnit.SECONDS, 5, 10);
            if (res) {
                int num = planeOrder.getUserRelationIds().split(",").length;
                //获取航班剩余座位数
                int i = 0;
                for (String lineId : planeOrder.getLineIds().split(",")) {
                    String s = date[i];
                    if (s == null) s = date[0];
                    List<String> keys = Arrays.asList(INIT_STOCK + s + ":" + lineId);
                    Long result = (Long) redisTemplate.execute(redisScript, keys, num);
                    if (result == -1) {
                        throw new YueException("该航班座位数已售空！");
                    } else if (result == 0) {
                        throw new YueException("剩余座位数不足！");
                    } else if (result == -2) {
                        throw new YueException("该航班没有机票出售！");
                    }
                    i++;
                }
                System.out.println(sequence);
                planeOrder.setId(Long.parseLong(sequence.toString()));
                planeOrder.setUserId(Integer.parseInt(OauthUtils.getCurrentUser().getUserId()));
                //将创建订单信息加入队列中
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "boot.order", planeOrder, RabbitMQConfig.messagePostProcessor);
                /**
                 * 0： 订单取消
                 * 1： 订单等待付款中
                 * 2：订单预定完成
                 */
                return sequence;
            } else throw new YueException("当前人数访问过多，请稍后再试");
        } catch (Exception e) {
            throw new YueException(e.getMessage());
        } finally {
            if (res)
                RedissonLockUtil.unlock(sequence);
        }
    }

    //支付订单
    @PostMapping("/pay")
    public String pay(@RequestBody OrderDTO order) {
        OrderDTO planeOrder = (OrderDTO) redisTemplate.opsForValue().get(ORDER_INFO + order.getId());
        Order byId = orderService.getById(order.getId());
        if (byId != null) {
            if (byId.getStatus() == 2)
                throw new YueException("该订单已被支付");
            else if (byId.getStatus() == 0)
                throw new YueException("该订单已被取消");
        }
        //订单没有过期的情况
        if (planeOrder != null) {
            //插入主订单
            orderService.save(Order.builder()
                    .id(planeOrder.getId())
                    .status(2)
                    .userId(planeOrder.getUserId())
                    .totalPrice(order.getPrice())
                    .type(planeOrder.getType())
                    .departureDate(planeOrder.getDepartureDate())
                    .createTime(planeOrder.getCreateTime())
                    .build());
            //插入子订单
            List<OrderInfo> orderInfos = new ArrayList<>();
            Date[] time = new Date[]{planeOrder.getFromTime(), ObjectUtils.isEmpty(planeOrder.getToTime()) ? null : planeOrder.getToTime()};
            int i = 0;
            for (String lineId : planeOrder.getLineIds().split(",")) {
                for (String userRelationId : planeOrder.getUserRelationIds().split(",")) {
                    OrderInfo build = OrderInfo.builder()
                            .orderId(planeOrder.getId())
                            .lineId(Integer.valueOf(lineId))
                            .userRelationId(Integer.valueOf(userRelationId))
                            .departureDate(time[i])
                            .build();
                    orderInfos.add(build);
                }
                i++;
            }
            orderInfoService.saveBatch(orderInfos);
            planeOrder.setStatus(2);
            orderService.returnStock(planeOrder);
           if(ObjectUtils.isNotEmpty(order.getCouponId())){
               orderService.useCoupon(order.getCouponId());
           }
            rabbitTemplate.convertAndSend("article_exchange","article_key", Integral.builder()
                    .userId(planeOrder.getUserId())
                    .integral(new Double(order.getPrice()/100).longValue())
                    .createTime(new Date())
                    .build());
            return "支付成功";
        } else
            throw new YueException("该订单已失效，请稍后再试");
    }

    //订单退款
    @PostMapping("/refund")
    public String refund(@RequestBody OrderDTO order) throws ParseException {
        //order.id 订单Id
        //根据订单号获取航班Ids
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = sdf.parse(sdf.format(new Date()));
        Date time = format.parse(format.format(new Date()));
        Order byId = orderService.getById(order.getId());
        List<OrderInfo> orderInfos = orderInfoService.lambdaQuery()
                .eq(OrderInfo::getOrderId, order.getId())
                .list();
        double price = byId.getTotalPrice();
        long l = 0;
        Set<Integer> userRelationIds = new HashSet<>();
        Set<Integer> lineIds = new HashSet<>();
        for (int i = 0; i < orderInfos.size(); i++) {
            OrderInfo orderInfo = orderInfos.get(i);
            RouteEn routeEn = routeClient.routeById(orderInfo.getLineId());
            Date parse = format.parse(routeEn.getStartTime());
            l += parse.getTime() - time.getTime();
            if (i == 0) {
                order.setFromTime(orderInfo.getDepartureDate());
            } else order.setToTime(orderInfo.getDepartureDate());
            userRelationIds.add(orderInfo.getUserRelationId());
            lineIds.add(orderInfo.getLineId());
        }
        if (l > 0) {
            int hour = (int) l / (1000 * 60 * 60);
            if (hour <= 2) {
                price = price * 0.2;
            } else if (hour <= 22) {
                price = price * 0.1;
            } else if (hour >= 24) {
                price = price * 0.05;
            }
        } else {
            price = price * 0.5;
        }
        byId.setStatus(4);
        byId.setTotalPrice(price);
        byId.setCreateTime(new Date(System.currentTimeMillis()));
        orderService.updateById(byId);
        //返回库存
        order.setStatus(2);
        StringBuffer lines = new StringBuffer();
        StringBuffer userRelations = new StringBuffer();
        lineIds.forEach(lineId -> lines.append(lineId + ","));
        userRelationIds.forEach(userRelationId -> userRelations.append(userRelationId + ","));
        order.setUserRelationIds(userRelations.substring(userRelations.length() - 1, userRelations.length()));
        order.setLineIds(lines.substring(lines.length() - 1, lines.length()));
        orderService.returnStock(order);
        rabbitTemplate.convertAndSend("article_exchange","article_key", Integral.builder()
                .userId(byId.getUserId())
                .integral(new Double(price/100).longValue()*-1)
                .createTime(new Date())
                .build());
        return "订单退款成功";
    }

    //订单取消
    @PostMapping("/cancel")
    public String cancel(@RequestBody OrderDTO order) {
        OrderDTO planeOrder = (OrderDTO) redisTemplate.opsForValue().get(ORDER_INFO + order.getId());
        if (null == planeOrder)
            throw new YueException("订单已取消");
        Order item = new Order();
        BeanUtils.copyProperties(planeOrder, item);
        item.setStatus(0);
        orderService.save(item);
        List<OrderInfo> orderInfos = new ArrayList<>();
        Date[] time = new Date[]{planeOrder.getFromTime(), ObjectUtils.isEmpty(planeOrder.getToTime()) ? null : planeOrder.getToTime()};
        int i = 0;
        for (String lineId : planeOrder.getLineIds().split(",")) {
            for (String userRelationId : planeOrder.getUserRelationIds().split(",")) {
                OrderInfo build = OrderInfo.builder()
                        .orderId(item.getId())
                        .lineId(Integer.valueOf(lineId))
                        .userRelationId(Integer.valueOf(userRelationId))
                        .departureDate(time[i])
                        .build();
                orderInfos.add(build);
            }
            i++;
        }
        orderInfoService.saveBatch(orderInfos);
        orderService.returnStock(planeOrder);
        return "订单取消成功";
    }

    //获取订单信息
    @GetMapping("/user_order/info")
    @Transactional
    public Map<String, Object> orderInfo(@RequestParam(value = "status",defaultValue = "3") Integer status,
                                         @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage,
                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        /**
         * 0 -> 订单取消
         * 1 -> 订单等待支付
         * 2 -> 订单完成
         */

        List<Order> orders = new ArrayList<>();
        Integer pages = 0;
        List<OrderVo> orderVos = new ArrayList<>();
        List<OrderInfoVo> orderInfoVos = new ArrayList<>();
        Object o = null;
        if (status == 1) { //订单等待支付
            o = redisTemplate.opsForValue().get(ORDER_USER + OauthUtils.getCurrentUser().getUserId());
            if (o == null) {
                Map<String, Object> data = new HashMap<String, Object>() {{
                    put("pages", 0);
                    put("data", orderVos);
                }};
                return data;
            }
            ;
            orders.add((Order) o);
            pages = orders.size();
        } else if (status == 0 || status == 2 || status == 4) {
            Page<Order> orderIpage = orderService.lambdaQuery()
                    .eq(Order::getUserId, OauthUtils.getCurrentUser().getUserId())
                    .eq(Order::getStatus, status)
                    .orderByDesc(Order::getCreateTime)
                    .page(new Page<>(currentPage, pageSize));
            orders = orderIpage.getRecords();
            pages = (int) orderIpage.getPages();
        } else if (status == 3) {
            Page<Order> orderIpage = orderService.lambdaQuery()
                    .eq(Order::getUserId, OauthUtils.getCurrentUser().getUserId())
                    .in(Order::getStatus, Arrays.asList(0, 2, 4))
                    .orderByDesc(Order::getCreateTime)
                    .page(new Page<>(currentPage, pageSize));
            o = redisTemplate.opsForValue().get(ORDER_USER + OauthUtils.getCurrentUser().getUserId());
            if (o != null && currentPage == 1) {
                orders.add((Order) o);
            }
            orders.addAll(orderIpage.getRecords());
            pages = (int) orderIpage.getPages();
        }
        Object finalO = o;
        orders.forEach(order -> {
            //查询子订单
            List<OrderInfoPO> orderInfos = new ArrayList<>();
            try (Cursor<OrderInfo> cursor = orderInfoService.selectOrderById(order.getId())) {
                cursor.forEach(item -> {
                    OrderInfoPO orderInfoPO = OrderInfoPO.builder()
                            .relation(userClient.getRelationById(item.getUserRelationId()))
                            .route(routeClient.routeById(item.getLineId()))
                            .build();
                    BeanUtils.copyProperties(item, orderInfoPO);
                    orderInfos.add(orderInfoPO);
                });
                if (orderInfos.size() == 0) {
                    orderRedis(orderInfos, finalO);
                }
                OrderPO build = OrderPO.builder()
                        .orderInfos(orderInfos)
                        .build();
                BeanUtils.copyProperties(order, build);
                OrderInfoVo orderInfoVo = OrderInfoVo.builder()
                        .orderPO(build)
                        .build();
                orderInfoVos.add(orderInfoVo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Integer finalPages = pages;
        Map<String, Object> data = new HashMap<String, Object>() {{
            put("pages", finalPages);
            put("data", orderInfoVos);
        }};
        return data;
    }

    //从redis中查询订单
    public void orderRedis(List<OrderInfoPO> orderInfos, Object o) {
        OrderDTO orderDTO = (OrderDTO) o;
        Order orderRedis = (Order) orderDTO;
        //获取子订单
        Date[] date = new Date[]{orderDTO.getFromTime(), ObjectUtils.isEmpty(orderDTO.getToTime()) ? null : orderDTO.getToTime()};
        int i = 0;
        for (String lineId : orderDTO.getLineIds().split(",")) {
            Date time = date[i];
            for (String userRelationId : orderDTO.getUserRelationIds().split(",")) {
                OrderInfoPO build = OrderInfoPO.builder()
                        .relation(userClient.getRelationById(Integer.valueOf(userRelationId)))
                        .route(routeClient.routeById(Integer.valueOf(lineId)))
                        .build();
                build.setOrderId(orderDTO.getId());
                build.setLineId(Integer.valueOf(lineId));
                build.setUserRelationId(Integer.valueOf(userRelationId));
                build.setDepartureDate(time);
                orderInfos.add(build);
            }
            i++;
        }
    }

    //获取订单详细信息
    @GetMapping("/info")
    @Transactional
    public OrderInfoVo info(OrderDTO order) {
        //查询父订单
        Order byId = orderService.getById(order.getId());
        List<OrderInfoPO> orderInfos = new ArrayList<>();
        OrderDTO orderDTO = (OrderDTO) redisTemplate.opsForValue().get(ORDER_INFO + order.getId());
        if (byId != null) {
            //查询子订单
            try (Cursor<OrderInfo> cursor = orderInfoService.selectOrderById(byId.getId())) {
                cursor.forEach(item -> {
                    OrderInfoPO orderInfoPO = OrderInfoPO.builder()
                            .relation(userClient.getRelationById(item.getUserRelationId()))
                            .route(routeClient.routeById(item.getLineId()))
                            .build();
                    BeanUtils.copyProperties(item, orderInfoPO);
                    orderInfos.add(orderInfoPO);
                });
                OrderPO build = OrderPO.builder()
                        .orderInfos(orderInfos)
                        .build();
                BeanUtils.copyProperties(byId, build);
                return OrderInfoVo.builder()
                        .orderPO(build)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (orderDTO != null) {
            //从redis查找订单
            //获取父订单
            Order orderRedis = (Order) orderDTO;
            //获取子订单
            Date[] date = new Date[]{orderDTO.getFromTime(), ObjectUtils.isEmpty(orderDTO.getToTime()) ? null : orderDTO.getToTime()};
            int i = 0;
            for (String lineId : orderDTO.getLineIds().split(",")) {
                Date time = date[i];
                for (String userRelationId : orderDTO.getUserRelationIds().split(",")) {
                    OrderInfoPO build = OrderInfoPO.builder()
                            .relation(userClient.getRelationById(Integer.valueOf(userRelationId)))
                            .route(routeClient.routeById(Integer.valueOf(lineId)))
                            .build();
                    build.setOrderId(orderDTO.getId());
                    build.setLineId(Integer.valueOf(lineId));
                    build.setUserRelationId(Integer.valueOf(userRelationId));
                    build.setDepartureDate(time);
                    orderInfos.add(build);
                }
                i++;
            }
            OrderPO build = OrderPO.builder()
                    .orderInfos(orderInfos)
                    .build();
            BeanUtils.copyProperties(orderRedis, build);
            return OrderInfoVo.builder()
                    .orderPO(build)
                    .timeOut(orderDTO.getTimeout())
                    .build();
        }
        throw new YueException("没有该订单信息！");
    }

    //机票改签
    @PostMapping("/change")
    public Object change(@RequestBody OrderDTO order) {
        /**
         * formTime:第一趟航班
         * toTime:第二趟航班
         * userRelationIds:乘坐人Id
         * LineIds：航班Id
         * type:类型
         * id:需要改签的订单号
         */
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/stock.lua")));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Order byId = orderService.getById(order.getId());
        if (ObjectUtils.isEmpty(byId))
            throw new YueException("该订单不存在");
        else if (byId.getStatus() == 4)
            throw new YueException("该订单已退款");
        else if (byId.getStatus() == 0 || byId.getStatus() == 1)
            throw new YueException("机票购买之后才能改签哦");
        //获取子订单
        List<OrderInfo> list = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderId, byId.getId()).list();
        Date[] date = new Date[]{order.getFromTime(), ObjectUtils.isEmpty(order.getToTime()) ? null : order.getToTime()};
        int i=0;
        for(OrderInfo orderInfo:list){
            if(new Date().after(orderInfo.getDepartureDate())){
                throw new YueException("飞机已经起飞了哦，不能改签了");
            }
        }
        String sequence = sequenceUtils.getSequence();
        //添加父订单
        Order buildOrder = Order.builder()
                .id(Long.parseLong(sequence))
                .status(2)
                .userId(Integer.parseInt(OauthUtils.getCurrentUser().getUserId()))
                .departureDate(byId.getDepartureDate())
                .type(order.getType())
                .createTime(new Date())
                .build();
        //添加子订单
        Double totalPrice=0.0;
        List<OrderInfo> orderInfos=new ArrayList<>();
        for(String lineId:order.getLineIds().split(",")){
            for(String userRelationId:order.getUserRelationIds().split(",")){
                List<String> keys = Arrays.asList(INIT_STOCK + format.format(date[i]) + ":" + lineId);
                System.out.println(keys.get(0));
                Long result = (Long) redisTemplate.execute(redisScript, keys, 1);
                if (result == -1) {
                    throw new YueException("该航班座位数已售空！");
                } else if (result == 0) {
                    throw new YueException("剩余座位数不足！");
                } else if (result == -2) {
                    throw new YueException("该航班没有机票出售！");
                }else {
                    OrderInfo build = OrderInfo.builder()
                            .orderId(buildOrder.getId())
                            .lineId(Integer.valueOf(lineId))
                            .userRelationId(Integer.valueOf(userRelationId))
                            .departureDate(date[i])
                            .build();
                    orderInfos.add(build);
                    RouteEn routeEn = routeClient.routeById(Integer.parseInt(lineId));
                    totalPrice+=routeEn.getPrice();
                }
            }
            i++;
        }
        buildOrder.setTotalPrice(totalPrice);
        orderService.save(buildOrder);
        orderInfoService.saveBatch(orderInfos);
        //删除以前的订单
        orderService.removeById(byId.getId());
        //归还库存
        order.setStatus(0);
        orderService.returnStock(order);
        return "改签成功";
    }

    //订单删除
    @PostMapping("/delete")
    public String delete(@RequestBody OrderDTO order){
        //根据订单Id删除订单
        orderService.removeById(order.getId());
        return "订单删除成功！";
    }

    //查询即将需要出行的订单
    @GetMapping("/nearest_order")
    @Transactional
    public OrderInfoVo get(){
        //获取订单号
        Order order = orderInfoService.getNearestFlight(Integer.valueOf(OauthUtils.getCurrentUser().getUserId()));
        List<OrderInfoPO> orderInfos = new ArrayList<>();
        if(ObjectUtils.isEmpty(order))
            throw new YueException("最近无出行航班哦");
        //判断是否是往返
        if(order.getType()!=2){
            //获取订单详情
            try (Cursor<OrderInfo> cursor = orderInfoService.selectOrderById(Long.valueOf(order.getId()))) {
                cursor.forEach(item -> {
                    OrderInfoPO orderInfoPO = OrderInfoPO.builder()
                            .route(routeClient.routeById(item.getLineId()))
                            .build();
                    BeanUtils.copyProperties(item, orderInfoPO);
                    orderInfos.add(orderInfoPO);
                });
                OrderPO build = OrderPO.builder()
                        .orderInfos(orderInfos)
                        .build();
                BeanUtils.copyProperties(order, build);
                return OrderInfoVo.builder()
                        .orderPO(build)
                        .dayNum(DateUtil.between(new Date(), orderInfos.get(0).getDepartureDate(), DateUnit.DAY))
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //获取订单详情
            List<OrderInfo> list = orderInfoService.lambdaQuery()
                    .eq(OrderInfo::getOrderId, order.getId())
                    .orderByAsc(OrderInfo::getDepartureDate).list();
            OrderInfoPO orderInfoPO = OrderInfoPO.builder()
                    .route(routeClient.routeById(list.get(0).getLineId()))
                    .build();
            BeanUtils.copyProperties(list.get(0), orderInfoPO);
            orderInfos.add(orderInfoPO);
            OrderPO build = OrderPO.builder()
                    .orderInfos(orderInfos)
                    .build();
            BeanUtils.copyProperties(order, build);
            return OrderInfoVo.builder()
                    .orderPO(build)
                    .dayNum(DateUtil.between(new Date(),build.getOrderInfos().get(0).getDepartureDate(), DateUnit.DAY,false))
                    .build();
        }
        return null;
    }

    //计算价格
    @GetMapping("/calculatePrice")
    public Map<String,Object> price(OrderDTO order){
        /**
         * couponId:优惠卷ID
         * id:订单Id
         */
        OrderDTO orderDTO = (OrderDTO) redisTemplate.opsForValue().get(ORDER_INFO + order.getId());
        if(ObjectUtils.isEmpty(orderDTO))
            throw new YueException("暂无此订单哦");
        //获取优惠卷信息
        CouponFlowPO couponFlowPO = couponClient.couponInfo(order.getCouponId());
        if(ObjectUtils.isEmpty(couponFlowPO))
            throw new YueException("您没有该优惠卷哦");
        Coupon coupon = couponFlowPO.getCoupon();
        Double totalPrice= orderDTO.getTotalPrice();
        if(totalPrice<coupon.getFullMoney())
            throw new YueException("还没到满减的金额");
        Double price=totalPrice-coupon.getMinusMoney();
        Map<String,Object> map=new HashMap<String,Object>(){{
            put("originalPrice",totalPrice);
            put("fullReduction",coupon.getMinusMoney());
            put("actualPrice",price);
        }};
        return map;
    }

    //健康检测
    @GetMapping("/health")
    public String health() {
        return "success";
    }

    //服务停止
    @PostMapping("/shutdown")
    public void shutdown() {
        ConfigurableApplicationContext run = (ConfigurableApplicationContext) context;
        run.close();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
