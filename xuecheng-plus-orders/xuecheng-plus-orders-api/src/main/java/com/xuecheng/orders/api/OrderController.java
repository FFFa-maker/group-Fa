package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {
    @Autowired
    OrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @ApiOperation("生成支付二维码")
    @ResponseBody
    @PostMapping("/generatepaycode")
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = user.getId();
        PayRecordDto payRecordDto = orderService.createOrder(userId, addOrderDto);
        return payRecordDto;
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException, AlipayApiException {
        //第三方支付平台下单
        //查询支付记录
        XcPayRecord payRecord = orderService.getPayRecordByPayno(payNo);
        if(payRecord == null){
            XueChengPlusException.cast("支付记录不存在");
        }
        String status = payRecord.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("无需重复支付");
        }

        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.URL,
                APP_ID,
                APP_PRIVATE_KEY,
                AlipayConfig.FORMAT,
                AlipayConfig.CHARSET,
                ALIPAY_PUBLIC_KEY,
                AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://dv2x2b.natappfree.cc/orders/receivenotify");
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+payNo+"\"," +
                "    \"total_amount\":"+payRecord.getTotalPrice()+"," +
                "    \"subject\":\""+payRecord.getOrderName()+"\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }"
        );
        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo) throws IOException {
        //查询支付结果
        PayRecordDto payRecordDto = orderService.queryPayResult(payNo);
        return payRecordDto;
    }

    @PostMapping("/receivenotify")
    public void paynotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, AlipayApiException, IOException {
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if(verify_result) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
            if (trade_status.equals("TRADE_SUCCESS")) {//交易成功
                //更新支付记录表的支付状态以及订单表状态为成功
                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setApp_id(APP_ID);
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTotal_amount(total_amount);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTrade_status(trade_status);
                orderService.saveAliPayStatus(payStatusDto);
            }
            response.getWriter().write("success");
        }else{
            response.getWriter().write("fail");
        }

    }

}
