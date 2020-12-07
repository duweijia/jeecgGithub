package com.dhc.api;

/**
 * Title:常量
 * Description:常量
 * Copyright: Copyright (c)2020
 * @author dwj
 */
public class Constant {
	
	// 字符编码
	public static final String CHARSET = "UTF-8";
	// 请求方式
	public static final String REQUEST_METHOD = "POST";
	// client_id
	public static final String CLIENT_ID = "1582974BBEB88F5C12ED87F54AA5FDD0";

	// client_secret
	public static final String CLIENT_SECRET = "3EE852CC9912DAE85BD4276840F45C22";

	// WEIMO_URL
	public static final String WEIMO_URL = "https://dopen.weimob.com/fuwu/b/oauth2/";

	// redirect_uri 用于接收平台code
	public static final String REDIRECT_URI = "http://jd.dhc.net.cn/Weimob/requestAccessToken";

	// 查询订单列表
	public static final String QUERY_ORDER_LIST = "https://dopen.weimob.com/api/1_0/ec/order/queryOrderList";
	
	// 查询订单详情
	public static final String QUERY_ORDER_DETAIL = "https://dopen.weimob.com/api/1_0/ec/order/queryOrderDetail";

	// 发货
	public static final String SEND_GOODS = "https://dopen.weimob.com/api/1_0/ec/order/deliveryOrder";

	// 公共返回码
	public static final String RESP_CODE_SUCCESS = "0000";

	public static final String RESP_INFO_SUCCESS = "SUCCESS";

	public static final String RESP_CODE_OTHER = "1001";

	public static final String RESP_INFO_OTHER = "其他错误";

	public static final String RESP_CODE_UNKNOWERR = "9999";

	public static final String RESP_MSG_UNKNOWERR = "系统异常";
}
