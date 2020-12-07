package com.dhc.api.task;

import org.apache.log4j.Logger;
import javax.annotation.Resource;
import com.dhc.api.utils.DateUtil;
import org.springframework.stereotype.Component;
import com.dhc.api.service.order.SyncOrderService;
import com.dhc.api.service.deliver.DeliverGoodService;
import com.dhc.api.service.refresh.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class RefreshTokenTask {
	
	private static final Logger log = Logger.getLogger(RefreshTokenTask.class);
	
	@Resource(name="refreshTokenService")
	private RefreshTokenService refreshTokenService ;
	
	@Resource(name="syncOrderService")
	private SyncOrderService syncOrderService ;
	
	
	@Resource(name="deliverGoodService")
	private DeliverGoodService deliverGoodService ;
	
	// 刷新access_token
	@Scheduled(cron = "0 0 */1 * * ?")
	public void refreshToken() {
		refreshTokenService.refreshAccessToken();
	}

	// 同步weimo订单
	@Scheduled(cron = "0 0 10,13,16,22 * * ?")
	public void syncOrder() {
		log.info("syncOrderList start: " + DateUtil.formatNow("yyyy-MM-dd HH:mm:ss"));
		syncOrderService.syncOrderList();

		syncOrderService.getOrderPaymentInfo();
		log.info("sync paymentInfo end: " + DateUtil.formatNow("yyyy-MM-dd HH:mm:ss"));
	}
	
	// 订单发货
	@Scheduled(cron = "0 0 */2 * * ?")
	public void sendGoods() {
		deliverGoodService.sendGoods();
	}
}
