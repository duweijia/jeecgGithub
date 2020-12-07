package com.dhc.api.service.order;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Calendar;
import com.dhc.api.Constant;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import javax.annotation.Resource;
import com.dhc.api.utils.DateUtil;
import java.text.SimpleDateFormat;
import com.dhc.api.utils.AmountUtil;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import com.alibaba.druid.support.json.JSONUtils;
import org.springframework.web.client.RestTemplate;
import com.dhc.api.service.refresh.RefreshTokenService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Service("syncOrderService")
public class SyncOrderServiceImpl implements SyncOrderService {

	@Resource(name = "jdbcTemplate")
	private NamedParameterJdbcTemplate jdbcTemplate;
	@Resource(name = "refreshTokenService")
	private RefreshTokenService refreshTokenService;
	
	public static final int PAGE_SIZE = 10;

	private static final Logger log = Logger.getLogger(SyncOrderServiceImpl.class);

	@Transactional
	public void syncOrderList() {
		log.info("query order list start...");
		// 获取access_token令牌
		String accessToken = getAccessToken();
		// 获取订单列表URI
		StringBuffer url = new StringBuffer();
		url.append(Constant.QUERY_ORDER_LIST).append("?accesstoken=").append(accessToken);

		int pageNum = 0;// 当前页码
		while ((getOrdersList(++pageNum, PAGE_SIZE, url.toString())) == PAGE_SIZE) {

		}
	}

	private int getOrdersList(int pageNum, int pageSize, String url) {
		log.info("pageNum:" + pageNum + " ,pageSize:" + pageSize);
		int orderCount = 0;
		// 订单日期
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String orderDate = dateFormat.format(calendar.getTime());
		// 组装订单参数
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("createStartTime", orderDate + " 00:00:00");
		param.put("createEndTime", DateUtil.formatNow("yyyy-MM-dd") + " 23:59:59");
		param.put("orderStatuses", new int[] { 1 });// 0-待支付,1-待发货,2-已发货,3-已完成,4-已取消

		map.put("queryParameter", param);

		log.info("request parameters{}:" + JSONUtils.toJSONString(map));
		// 调用微盟API
		RestTemplate restTemplate = new RestTemplate();
		// 设置请求头为JSON格式
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<String>(JSONUtils.toJSONString(map), headers);
		String result = restTemplate.postForObject(url, request, String.class);

		//log.info("result:" + result);
		// 解析
		JSONObject resultJson = JSONObject.fromObject(result);

		JSONObject code = JSONObject.fromObject(resultJson.getString("code"));
		String errCode = code.getString("errcode");
		String errMsg = code.getString("errmsg");

		log.info("errCode:" + errCode + " ,errMsg:" + errMsg);
		if ("0".equals(errCode)) {
			log.info("Query WeiMo OrderList is success ...");
			JSONObject data = JSONObject.fromObject(resultJson.getString("data"));

			String totalCount = data.getString("totalCount");
			JSONArray pageArray = data.getJSONArray("pageList");

			log.info("totalCount:" + totalCount);

			Map<String, Object> hashMap = new HashMap<String, Object>();
			if (pageArray != null && pageArray.size() > 0) {
				for (Object obj : pageArray) {
					orderCount++;
					// 获取WeiMo订单
					JSONObject order = JSONObject.fromObject(obj);

					JSONObject deliveryDetail = JSONObject.fromObject(order.getString("deliveryDetail"));
					JSONObject logistics = JSONObject.fromObject(deliveryDetail.getString("logisticsDeliveryDetail"));
					// 收货地址
					String adddress = new StringBuffer().append(logistics.getString("receiverProvince"))
							.append(logistics.getString("receiverCity")).append(logistics.getString("receiverCounty"))
							.append(logistics.getString("receiverArea")).append(logistics.getString("receiverAddress"))
							.toString();

					log.info("orderNo:" + order.getString("orderNo"));
					int count = jdbcTemplate
							.queryForInt("SELECT COUNT(OUT_ORDER_NO) FROM WM_OUT_ORDERM WHERE OUT_ORDER_NO = '"
									+ order.getString("orderNo") + "'", hashMap);
					if (count < 1) {
						String inOrderNo = jdbcTemplate.queryForObject(
								"SELECT TO_CHAR(SEQ_OUT_ORDER.NEXTVAL,'00000000') FROM DUAL", hashMap, String.class);
						inOrderNo = "OUT" + inOrderNo.trim();
						
						// 订单插入WM_OUT_ORDERM
						StringBuffer insertWmOrder = new StringBuffer();

						insertWmOrder.append("INSERT INTO WM_OUT_ORDERM ( ")
								.append("OUT_ORDER_NO, IN_ORDER_NO, ORDER_DATE, PAY_DATE, ")
								.append("ORDER_AMT, DELIVERY_AMT, RECEIVER_NAME, RECEIVER_TEL, ")
								.append("RECEIVER_ADD, ORDER_TYPE, ORDER_STATUS, INSERT_DATE, UPDATE_DATE, SYNC_FLAG")
								.append(") values ( ").append("'" + order.getString("orderNo") + "',")
								.append("'" + inOrderNo + "',")
								.append("to_date('" + DateUtil.stampToDate(Long.valueOf(order.getString("createTime")))
										+ "','yyyy-mm-dd hh24:mi:ss'),")
								.append("to_date('" + DateUtil.stampToDate(Long.valueOf(order.getString("paymentTime")))
										+ "','yyyy-mm-dd hh24:mi:ss'),")
								.append("'" + order.getString("paymentAmount") + "','"
										+ order.getString("deliveryPaymentAmount") + "','"
										+ order.getString("receiverName") + "','" + order.getString("receiverMobile")
										+ "','" + adddress + "','01','10',sysdate,sysdate,'1')");
						jdbcTemplate.update(insertWmOrder.toString(), hashMap);

						// 商品列表
						JSONArray itemArr = order.getJSONArray("itemList");
						if (itemArr != null && itemArr.size() > 0) {
							for (Object item : itemArr) {
								JSONObject good = JSONObject.fromObject(item);
								log.info("skuCode:" + good.getString("skuCode") + " ,skuNum:"
										+ good.getString("skuNum") + " ,skuAmount:" + good.getString("skuAmount"));
								
								StringBuffer goodDetail = new StringBuffer();
								goodDetail.append("INSERT INTO WM_OUT_ORDERDT ( ")
										.append("OUT_ORDER_NO, IN_ORDER_NO, GOODS_CODE, GOODS_CNT,GOODS_PRICE, GOODS_AMT, PAYMENT_AMOUNT) ")
										.append("values ( ").append("'" + order.getString("orderNo") + "',")
										.append("'" + inOrderNo + "',")
										.append("'" + good.getString("skuCode") + "',")
										.append("'" + good.getString("skuNum") + "',")
										.append("'" + good.getString("price") + "',")
										.append("'" + AmountUtil.mul(Double.valueOf(good.getString("skuNum")),
												Double.valueOf(good.getString("price"))) + "',")
										.append("'" + good.getString("paymentAmount") + "'").append(")");
								jdbcTemplate.update(goodDetail.toString(), hashMap);
							}
						}
					}
				}
			}
		} else {
			log.info("Query WeiMo orderList is fail and errMsg:" + errMsg);
		}
		return orderCount;
	}
	
	@Transactional
	public void getOrderPaymentInfo(){
		log.info("query order paymentInfo start . . .");
		// 获取access_token
		String accessToken = getAccessToken();
		// 订单详情URI
		StringBuffer url = new StringBuffer();
		url.append(Constant.QUERY_ORDER_DETAIL).append("?accesstoken=").append(accessToken);
		// 查询订单
		String sql = "SELECT OUT_ORDER_NO FROM WM_OUT_ORDERM WHERE (PAYMENT_NUMBER IS NULL OR THIRD_PAYMENT_NUMBER IS NULL) AND ORDER_AMT <> 0 ";

		List<String> orderNoList = jdbcTemplate.queryForList(sql, new HashMap<String, Object>(), String.class);
		for (String orderNo : orderNoList) {
			log.info("orderNo=" + orderNo);
			// 组装订单参数
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("orderNo", orderNo);

			// 调用微盟API
			RestTemplate restTemplate = new RestTemplate();
			// 设置请求头为JSON格式
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(JSONUtils.toJSONString(map), headers);
			String result = restTemplate.postForObject(url.toString(), request, String.class);

			log.info("result:" + result);

			// 解析
			JSONObject resultJson = JSONObject.fromObject(result);

			JSONObject code = JSONObject.fromObject(resultJson.getString("code"));
			String errCode = code.getString("errcode");
			String errMsg = code.getString("errmsg");

			log.info("errCode:" + errCode + " ,errMsg:" + errMsg);
			if ("0".equals(errCode)) {
				JSONObject data = JSONObject.fromObject(resultJson.getString("data"));
				// 支付详情
				JSONObject paymentInfo = data.getJSONObject("paymentInfo");
				// 支付单号
				String tradeId = paymentInfo.getString("tradeId");
				// 第三方支付单号
				String channelTrxNo = paymentInfo.getString("channelTrxNo");

				jdbcTemplate.update(
						"UPDATE WM_OUT_ORDERM SET PAYMENT_NUMBER = '" + tradeId + "',THIRD_PAYMENT_NUMBER = '"
								+ channelTrxNo + "' WHERE OUT_ORDER_NO = '" + orderNo + "'",
						new HashMap<String, Object>());
			} else {
				log.info("query order paymentInfo is fail and msg=" + errMsg);
			}
		}
		
	}
	
	private String getAccessToken() {
		// 查询access_token令牌
		String sql = "SELECT ACCESS_TOKEN,TOKEN_EXPIRE_TIME FROM WM_TOKEN WHERE ID = (SELECT MAX(ID) FROM WM_TOKEN)";

		Map<String, Object> data = jdbcTemplate.queryForMap(sql, new HashMap<String, Object>());
		String accessToken = (String) data.get("ACCESS_TOKEN");
		Date expireTime = (Date) data.get("TOKEN_EXPIRE_TIME");

		log.info("expire_time:" + expireTime + " ,current_time:" + DateUtil.formatNow("yyyy-MM-dd HH:mm:ss"));

		// 若access_token已过期,主动刷新access_token
		if (expireTime.before(new Date())) {
			accessToken = refreshTokenService.refreshAccessToken();
		}
		return accessToken;
	}
}
