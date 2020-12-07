package com.dhc.api.utils;

import java.util.Map;
import java.util.HashMap;
import com.dhc.api.Constant;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * 组装微盟授权参数
 * @author wj.du
 * @date   2020.08.17
 */
public class ParseContent {
	
	private static final Logger log = Logger.getLogger(ParseContent.class);
	
	public static Map<String, Object> analysisResult(String result) {
		// 定义返回参数
		Map<String, Object> map = new HashMap<String, Object>();

		JSONObject json = (JSONObject) JSONObject.parse(result);
		// accessToken 和 refreshToken
		String accessToken = json.getString("access_token");
		String refreshToken = json.getString("refresh_token");
		// access_token过期时间
		String expiresIn = json.getString("expires_in");

		// 失败-沒有公共返回參數
		String error = json.getString("error");
		String description = json.getString("error_description");

		String errCode = json.getString("errcode");
		String errMsg = json.getString("errmsg");
		
		if (StringUtils.isNotBlank(accessToken) && StringUtils.isNotBlank(refreshToken)) {
			map.put("respCode", Constant.RESP_CODE_SUCCESS);
			map.put("respMsg", Constant.RESP_INFO_SUCCESS);
			map.put("accessToken", accessToken);
			map.put("refreshToken", refreshToken);
			map.put("expiresIn", expiresIn);
		} else {
			log.info("request token is fail...");
			map.put("respCode", Constant.RESP_CODE_OTHER);
			if (StringUtils.isNotBlank(error)) {
				log.info("error:" + error + " ,and error_description:" + description);
				map.put("respMsg", description);
			} else {
				log.info("errCode:" + errCode + " ,and errMsg:" + errMsg);
				map.put("respMsg", errMsg);
			}
		}
		return map;
	}
}
