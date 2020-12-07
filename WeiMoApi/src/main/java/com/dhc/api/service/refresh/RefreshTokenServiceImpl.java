package com.dhc.api.service.refresh;

import java.util.Map;
import java.util.HashMap;
import com.dhc.api.Constant;
import org.apache.log4j.Logger;
import javax.annotation.Resource;
import com.dhc.api.utils.DateUtil;
import com.dhc.api.utils.RequestUtil;
import com.dhc.api.utils.ParseContent;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


@Service("refreshTokenService")
public class RefreshTokenServiceImpl implements RefreshTokenService{
	
	@Resource(name = "jdbcTemplate")
	private NamedParameterJdbcTemplate jdbcTemplate;

	private static final Logger log = Logger.getLogger(RefreshTokenServiceImpl.class);

	//通过refresh_token刷新access_token
	public String refreshAccessToken() {
		log.info("refresh access token start...");

		String refreshToken = "";// 查询refresh_token
		String sql = "SELECT ID,REFRESH_TOKEN FROM WM_TOKEN WHERE ID = (SELECT MAX(ID) FROM WM_TOKEN)";
		Map<String, Object> data = jdbcTemplate.queryForMap(sql, new HashMap<String, Object>());
		refreshToken = (String) data.get("REFRESH_TOKEN");
		log.info("id:" + data.get("ID") + ",refreshToken:" + refreshToken);
		
		StringBuffer str = new StringBuffer();
		str.append(Constant.WEIMO_URL).append("token?grant_type=refresh_token").append("&client_id=")
				.append(Constant.CLIENT_ID).append("&client_secret=").append(Constant.CLIENT_SECRET)
				.append("&refresh_token=").append(refreshToken);

		String accessToken = "";// access_token
		String result = "";// 微盟返回结果
		try {
			result = RequestUtil.doPost(str.toString(), null, null, ContentType.APPLICATION_FORM_URLENCODED);
			log.info("result:" + result);
			Map<String, Object> map = new HashMap<String, Object>();
			// 解析报文参数
			map = ParseContent.analysisResult(result);
			if ("0000".equals(map.get("respCode"))) {
				accessToken = (String) map.get("accessToken");
				// 更新token有效期时间
				String expireTime = DateUtil.timePastSecond((String) map.get("expiresIn"));
				// 更新access_token
				StringBuffer buffer = new StringBuffer();
				buffer.append("UPDATE WM_TOKEN SET ACCESS_TOKEN = '" + map.get("accessToken")
						+ "',TOKEN_EXPIRE_TIME = to_date('" + expireTime + "','yyyy-MM-dd hh24:mi:ss') WHERE ID = "
						+ data.get("ID") + "");

				jdbcTemplate.update(buffer.toString(), new HashMap<String, Object>());
				log.info("update access_token is success");
			} else {
				log.info("update access_token is fail and reason is:" + map.get("respMsg"));
			}
		} catch (Exception e) {
			log.info("update access_token is fail...");
			log.info("error message:" + e.getMessage());
		}
		return accessToken;
	}
}
