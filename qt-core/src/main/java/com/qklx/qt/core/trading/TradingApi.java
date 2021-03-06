/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Gareth Jon Lynch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.qklx.qt.core.trading;

import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.core.config.AccountConfig;
import com.qklx.qt.core.config.KlineConfig;
import com.qklx.qt.core.config.MarketConfig;
import com.qklx.qt.core.response.Kline;
import com.qklx.qt.core.response.OrdersDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * BX-bot's Trading API.
 * </p>
 * <p>
 * This is what Trading Strategies use to trade.
 * </p>
 * <p>
 * Exchange Adapters provide their own implementation of the API for the exchange they wish to integrate with.
 * </p>
 * <p>
 * This version of the Trading API only supports <a href="http://www.investopedia.com/terms/l/limitorder.asp">limit orders</a>
 * traded at the <a href="http://www.investopedia.com/terms/s/spotprice.asp">spot price</a>.
 * It does not support futures or margin trading.
 * </p>
 *
 * @author gazbert
 * @since 1.0
 */
public interface TradingApi {

    /**
     * Returns the current version of the API.
     *
     * @return the API version.
     * @since 1.0
     */
    default String getVersion() {
        return "1.1";
    }

    /**
     * Returns the API implementation name.
     *
     * @return the API implementation name.
     * @since 1.0
     */
    String getImplName();

    /**
     * 获取市场上买卖订单
     *
     * @param config
     * @return
     */
    MarketOrder getMarketOrders(MarketConfig config, String size);

    /**
     * 查询为完成的订单
     *
     * @param config
     * @return
     * @throws ExchangeNetworkException
     * @throws TradingApiException
     */
    List<OpenOrder> getOpenOrders(MarketConfig config, AccountConfig accountConfig, String size) throws ExchangeNetworkException, TradingApiException;

    /**
     * Places an order on the exchange.
     *
     * @param marketId  the id of the market.
     * @param orderType Value must be {@link OrderType#BUY} or {@link OrderType#SELL}.
     * @param quantity  amount of units you are buying/selling in this order.
     * @param price     the price per unit you are buying/selling at.
     * @return the id of the order.
     * @throws ExchangeNetworkException if a network error occurred trying to connect to the exchange. This is
     *                                  implementation specific for each Exchange Adapter - see the documentation for the
     *                                  adapter you are using. You could retry the API call, or exit from your Trading Strategy
     *                                  and let the Trading Engine execute your Trading Strategy at the next trade cycle.
     * @throws TradingApiException      if the API call failed for any reason other than a network error. This means something
     *                                  bad as happened; you would probably want to wrap this exception in a
     *                                  StrategyException and let the Trading Engine shutdown the bot immediately
     *                                  to prevent unexpected losses.
     * @since 1.0
     */
    Long createOrder(String marketId, String accountId, com.qklx.qt.core.enums.OrderType orderType, BigDecimal quantity, BigDecimal price)
            throws ExchangeNetworkException, TradingApiException;

    /**
     * Cancels your existing order on the exchange.
     *
     * @param orderId  your order Id.
     * @param marketId the id of the market the order was placed on, e.g. btc_usd
     * @return true if order cancelled ok, false otherwise.
     * @throws ExchangeNetworkException if a network error occurred trying to connect to the exchange. This is
     *                                  implementation specific for each Exchange Adapter - see the documentation for the
     *                                  adapter you are using. You could retry the API call, or exit from your Trading Strategy
     *                                  and let the Trading Engine execute your Trading Strategy at the next trade cycle.
     * @throws TradingApiException      if the API call failed for any reason other than a network error. This means something
     *                                  bad as happened; you would probably want to wrap this exception in a
     *                                  StrategyException and let the Trading Engine shutdown the bot immediately
     *                                  to prevent unexpected losses.
     * @since 1.0
     */
    boolean cancelOrder(String orderId, String marketId) throws ExchangeNetworkException, TradingApiException;


    /**
     * 获取订单详情
     * @param orderId
     * @return
     */
    OrdersDetail orderDetail(Long orderId);
    /**
     * Fetches the latest price for a given market.
     * This is usually in BTC for altcoin markets and USD for BTC/USD markets - see the Exchange Adapter documentation.
     *
     * @param marketId the id of the market.
     * @return the latest market price.
     * @throws ExchangeNetworkException if a network error occurred trying to connect to the exchange. This is
     *                                  implementation specific for each Exchange Adapter - see the documentation for the
     *                                  adapter you are using. You could retry the API call, or exit from your Trading Strategy
     *                                  and let the Trading Engine execute your Trading Strategy at the next trade cycle.
     * @throws TradingApiException      if the API call failed for any reason other than a network error. This means something
     *                                  bad as happened; you would probably want to wrap this exception in a
     *                                  StrategyException and let the Trading Engine shutdown the bot immediately
     *                                  to prevent unexpected losses.
     * @since 1.0
     */
    BigDecimal getLatestMarketPrice(String marketId) throws ExchangeNetworkException, TradingApiException;

    /**
     * 获取账户的余额信息
     *
     * @since 1.0
     */
    BalanceInfo getBalanceInfo(String accountId, RedisUtil redisUtil) throws ExchangeNetworkException, TradingApiException;

    /**
     * Returns the exchange BUY order fee for a given market id.
     * The returned value is the % of the BUY order that the exchange uses to calculate its fee as
     * a {@link BigDecimal}. If the fee is 0.33%, then the {@link BigDecimal} value returned is
     * 0.0033.
     *
     * @param marketId the id of the market.
     * @return the % of the BUY order that the exchange uses to calculate its fee as a {@link BigDecimal}.
     * @throws ExchangeNetworkException if a network error occurred trying to connect to the exchange. This is
     *                                  implementation specific for each Exchange Adapter - see the documentation for the
     *                                  adapter you are using. You could retry the API call, or exit from your Trading Strategy
     *                                  and let the Trading Engine execute your Trading Strategy at the next trade cycle.
     * @throws TradingApiException      if the API call failed for any reason other than a network error. This means something
     *                                  bad as happened; you would probably want to wrap this exception in a
     *                                  StrategyException and let the Trading Engine shutdown the bot immediately
     *                                  to prevent unexpected losses.
     * @since 1.0
     */
    BigDecimal getPercentageOfBuyOrderTakenForExchangeFee(String marketId) throws TradingApiException, ExchangeNetworkException;

    /**
     * Returns the exchange SELL order fee for a given market id.
     * The returned value is the % of the SELL order that the exchange uses to calculate its fee as a
     * {@link BigDecimal}. If the fee is 0.33%, then the {@link BigDecimal} value returned is
     * 0.0033.
     *
     * @param marketId the id of the market.
     * @return the % of the SELL order that the exchange uses to calculate its fee as a {@link BigDecimal}.
     * @throws ExchangeNetworkException if a network error occurred trying to connect to the exchange. This is
     *                                  implementation specific for each Exchange Adapter - see the documentation for the
     *                                  adapter you are using. You could retry the API call, or exit from your Trading Strategy
     *                                  and let the Trading Engine execute your Trading Strategy at the next trade cycle.
     * @throws TradingApiException      if the API call failed for any reason other than a network error. This means something
     *                                  bad as happened; you would probably want to wrap this exception in a
     *                                  StrategyException and let the Trading Engine shutdown the bot immediately
     *                                  to prevent unexpected losses.
     * @since 1.0
     */
    BigDecimal getPercentageOfSellOrderTakenForExchangeFee(String marketId) throws TradingApiException, ExchangeNetworkException;

    /**
     * Returns the exchange Ticker a given market id.
     * <p>
     * Not all exchangeAdapter provide the information returned in the Ticker methods - you'll need to check the relevant
     * Exchange Adapter code/Javadoc and online Exchange API documentation.
     * <p>
     * If the exchange does not provide the information, a null value is returned.
     *
     * @param marketId the id of the market.
     * @return the exchange Ticker for a given market.
     * @throws ExchangeNetworkException if a network error occurred trying to connect to the exchange. This is
     *                                  implementation specific for each Exchange Adapter - see the documentation for the
     *                                  adapter you are using. You could retry the API call, or exit from your Trading Strategy
     *                                  and let the Trading Engine execute your Trading Strategy at the next trade cycle.
     * @throws TradingApiException      if the API call failed for any reason other than a network error. This means something
     *                                  bad as happened; you would probably want to wrap this exception in a
     *                                  StrategyException and let the Trading Engine shutdown the bot immediately
     *                                  to prevent unexpected losses.
     * @since 1.1
     */
    default Ticker getTicker(String marketId) throws TradingApiException, ExchangeNetworkException {

        return new Ticker() {
            @Override
            public BigDecimal getLast() {
                return null;
            }

            @Override
            public BigDecimal getBid() {
                return null;
            }

            @Override
            public BigDecimal getAsk() {
                return null;
            }

            @Override
            public BigDecimal getLow() {
                return null;
            }

            @Override
            public BigDecimal getHigh() {
                return null;
            }

            @Override
            public BigDecimal getOpen() {
                return null;
            }

            @Override
            public BigDecimal getVolume() {
                return null;
            }

            @Override
            public BigDecimal getVwap() {
                return null;
            }

            @Override
            public Long getTimestamp() {
                return null;
            }
        };
    }

    /**
     * symbol	string	true	NA	交易对	btcusdt, ethbtc...
     * period	string	true	NA	返回数据时间粒度，也就是每根蜡烛的时间区间	1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
     * size	integer	false	150	返回 K 线数据条数	[1, 2000]
     *
     * @return
     */
    List<Kline> getKline(MarketConfig marketConfig, KlineConfig klineConfig);
}
