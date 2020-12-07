package com.dhc.api.service.achieve;

import java.util.Map;
import java.util.HashMap;
import com.dhc.api.Constant;
import org.apache.log4j.Logger;
import javax.annotation.Resource;
import com.dhc.api.utils.DateUtil;
import com.dhc.api.utils.ParseContent;
import com.dhc.api.utils.RequestUtil;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


@Service("achieveTokenService")
public class AchieveTokenServiceImpl implements AchieveTokenService{
	
	@Resource(name = "jdbcTemplate")
	private NamedParameterJdbcTemplate jdbcTemplate;

	private static final Logger log = Logger.getLogger(AchieveTokenServiceImpl.class);

	// 通过code获取access_token 和 refresf_token
	public Map<String,Object> achieveToken(String code) {
		log.info("achieve accessToken and refresfToken is start ...and code=" + code);
		Map<String, Object> param = new HashMap<String, Object>();
		StringBuffer str = new StringBuffer();
		// 通过code获取access_token
		str.append(Constant.WEIMO_URL).append("token?code=").append(code).append("&grant_type=authorization_code")
				.append("&client_id=").append(Constant.CLIENT_ID).append("&client_secret=")
				.append(Constant.CLIENT_SECRET).append("&redirect_uri=").append(Constant.REDIRECT_URI);

		log.info("URI:" + str.toString());

		String result = "";// 微盟返回结果
		try {
			result = RequestUtil.doPost(str.toString(), null, null, ContentType.APPLICATION_FORM_URLENCODED);
			log.info("result:" + result);
			Map<String, Object> map = new HashMap<String, Object>();
			// 解析报文参数
			map = ParseContent.analysisResult(result);

			if ("0000".equals(map.get("respCode"))) {
				// 获取access_token过期时间
				String expireTime = DateUtil.timePastSecond((String) map.get("expiresIn"));

				log.info("expireTime:" + expireTime);
				// 保存access_token、refresh_token
				StringBuffer sql = new StringBuffer();
				sql.append("insert into WM_TOKEN(ID,ACCESS_TOKEN,REFRESH_TOKEN,TOKEN_EXPIRE_TIME,CREATE_TIME)")
						.append(" values(seq_wm_token_id.nextval,'" + map.get("accessToken") + "','"
								+ map.get("refreshToken") + "',to_date('" + expireTime
								+ "','yyyy-MM-dd hh24:mi:ss'),sysdate)");

				log.info("sql:" + sql.toString());

				jdbcTemplate.update(sql.toString(), new HashMap<String, Object>());

				param.put("respMsg", Constant.RESP_INFO_SUCCESS);
			} else {
				param.put("respMsg", map.get("respMsg"));
			}
		} catch (Exception e) {
			param.put("respMsg", Constant.RESP_MSG_UNKNOWERR);
			log.info("error message:" + e.getMessage());
		}
		return param;
	}
}
