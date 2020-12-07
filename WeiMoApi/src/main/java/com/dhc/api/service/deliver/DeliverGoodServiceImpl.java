package com.dhc.api.service.deliver;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import com.dhc.api.Constant;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import javax.annotation.Resource;
import com.dhc.api.utils.DateUtil;
import com.dhc.api.utils.HttpClient;
import org.springframework.stereotype.Service;
import com.alibaba.druid.support.json.JSONUtils;
import com.dhc.api.service.refresh.RefreshTokenService;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Service("deliverGoodService")
public class DeliverGoodServiceImpl implements DeliverGoodService {

	@Resource(name = "jdbcTemplate")
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Resource(name = "refreshTokenService")
	private RefreshTokenService refreshTokenService;

	private static final Logger log = Logger.getLogger(DeliverGoodServiceImpl.class);

	public void sendGoods() {
		log.info("send order job is start...and startTime: " + DateUtil.formatNow("yyyy-MM-dd HH:mm:ss"));

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

		StringBuffer url = new StringBuffer();
		url.append(Constant.SEND_GOODS).append("?accesstoken=").append(accessToken);

		// log.info("URI:" + url.toString());

		// 查询待发货订单
		String waitSql = "SELECT OUT_ORDER_NO,COURIER_NUMBER FROM WM_OUT_ORDERM WHERE COURIER_NUMBER IS NOT NULL AND ORDER_STATUS in ('20','30') AND SYNC_FLAG = '2'";

		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(waitSql, new HashMap<String, Object>());
		for (Map<String, Object> order : queryForList) {
			log.info("orderNo:" + order.get("OUT_ORDER_NO") + " ,deliveryNo:" + order.get("COURIER_NUMBER"));

			// 组装参数
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("orderNo", order.get("OUT_ORDER_NO"));
			map.put("isNeedLogistics", true);
			map.put("deliveryNo", order.get("COURIER_NUMBER"));
			map.put("deliveryCompanyCode", "jd");
			map.put("deliveryCompanyName", "京东物流");

			// log.info("request parameters{}:" + JSONUtils.toJSONString(map));

			// 调用微盟API
			String result = "";
			try {
				HttpClient httpClient = new HttpClient(url.toString(), Constant.REQUEST_METHOD, Constant.CHARSET, 30,
						30, null);
				result = httpClient.send(JSONUtils.toJSONString(map));
			} catch (IOException e) {
				e.printStackTrace();
				log.info("request WeiMo api is fail...and outOrderNo=" + order.get("OUT_ORDER_NO"));
				continue;
			}
			log.info("result:" + result);

			// 解析
			JSONObject resultJson = JSONObject.fromObject(result);

			JSONObject code = JSONObject.fromObject(resultJson.getString("code"));
			String errCode = code.getString("errcode");
			String errMsg = code.getString("errmsg");

			log.info("errCode:" + errCode + " ,errMsg:" + errMsg);
			
			// 更新WM_OUT_ORDERM
			StringBuffer buffer = new StringBuffer();
			if ("0".equals(errCode)) {
				buffer.append("UPDATE WM_OUT_ORDERM SET SYNC_FLAG = '0',UPDATE_DATE = SYSDATE,REMARKS = '" + errMsg
						+ "' WHERE OUT_ORDER_NO = '" + order.get("OUT_ORDER_NO") + "'");
			} else {
				log.info(order.get("OUT_ORDER_NO") + " delivery is fail and msg:" + errMsg);

				buffer.append("UPDATE WM_OUT_ORDERM SET SYNC_FLAG = '3',REMARKS = '" + errMsg
						+ "',UPDATE_DATE = SYSDATE WHERE OUT_ORDER_NO = '" + order.get("OUT_ORDER_NO") + "'");
			}

			jdbcTemplate.update(buffer.toString(), new HashMap<String, Object>());
		}
		log.info("send order job is end...and endTime: " + DateUtil.formatNow("yyyy-MM-dd HH:mm:ss"));
	}
}
