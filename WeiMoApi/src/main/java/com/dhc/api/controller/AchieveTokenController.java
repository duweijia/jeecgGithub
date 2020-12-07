package com.dhc.api.controller;

import java.util.Map;
import javax.annotation.Resource;
import org.springframework.ui.Model;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import com.dhc.api.service.order.SyncOrderService;
import com.dhc.api.service.deliver.DeliverGoodService;
import com.dhc.api.service.achieve.AchieveTokenService;
import com.dhc.api.service.refresh.RefreshTokenService;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/token")
public class AchieveTokenController {
	
	@Resource(name = "achieveTokenService")
	private AchieveTokenService achieveTokenService;
	
	@Resource(name="refreshTokenService")
	private RefreshTokenService refreshTokenService ;

	@Resource(name = "syncOrderService")
	private SyncOrderService syncOrderService;

	@Resource(name = "deliverGoodService")
	private DeliverGoodService deliverGoodService;

	@RequestMapping("/achieveToken")
	public String achieveToken(HttpServletRequest request, Model model) {
		// 获取平台code
		String code = request.getParameter("code");
		Map<String, Object> map = achieveTokenService.achieveToken(code);
		model.addAttribute("respMsg", map.get("respMsg"));
		return "home";
	}
	
	@RequestMapping("/refreshToken")
	public void refreshToken() {
		refreshTokenService.refreshAccessToken();
	}

	@RequestMapping("/syncOrderList")
	public void syncOrderList() {
		syncOrderService.syncOrderList();
		
		syncOrderService.getOrderPaymentInfo();
	}

	@RequestMapping("/sendGoods")
	public void sendGoods() {
		deliverGoodService.sendGoods();
	}
}
