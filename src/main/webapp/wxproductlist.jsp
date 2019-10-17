<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>微信js支付测试</title>
</head>
<body>
	<div class="wrap">
		<!-- 顶部栏 -->
		<header class="top-bar pr">
			<a href="#" class="back pa"></a>
		</header>
		<a href="javascript:void(0)" onclick="buy()">0.01元,点击购买</a>
	</div>
	<script src="/js/jquery-1.8.3.min.js"></script>
	<script>
	
	function buy() {
		
		var data = '';

		$
				.ajax({
					async : true,
					cache : true,
					url : '../../wx/api/wx_prepay',
					type : 'post',
					data : data,
					dataType : 'json',
					success : function(data, response) {
						if (null != data) {
							appId = data.appId;
							timeStamp = data.timeStamp;
							nonceStr = data.nonceStr;
							pg = data.pg;
							signType = data.signType;
							paySign = data.paySign;
							if (typeof WeixinJSBridge == "undefined") {
								if (document.addEventListener) {
									document.addEventListener(
											'WeixinJSBridgeReady',
											onBridgeReady, false);
								} else if (document.attachEvent) {
									document.attachEvent(
											'WeixinJSBridgeReady',
											onBridgeReady);
									document.attachEvent(
											'onWeixinJSBridgeReady',
											onBridgeReady);
								}
							} else {
								WeixinJSBridge
										.invoke(
												'getBrandWCPayRequest',
												{
													"appId" : appId, //公众号名称，由商户传入     
													"timeStamp" : timeStamp, //时间戳，自1970年以来的秒数     
													"nonceStr" : nonceStr, //随机串     
													"package" : "prepay_id="
															+ pg,
													"signType" : signType, //微信签名方式:     
													"paySign" : paySign
												//微信签名 
												},

												function(res) {
													if (res.err_msg == "get_brand_wcpay_request:ok") {
														alert("支付成功");
													}
													if (res.err_msg == "get_brand_wcpay_request:cancel") {
														alert("交易取消");
													}
													if (res.err_msg == "get_brand_wcpay_request:fail") {
														alert("支付失败");
													}
												});
							}
						}else{
							alert("登录超时,请重新登录!");
						}

					},
					error : function(XMLHttpRequest, textStatus) {
						alert("系统繁忙,请稍候再试!");
					}

				});

	}
	
	</script>
</body>
</html>