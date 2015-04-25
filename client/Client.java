package pkg.client;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.market.api.IPO;
import pkg.order.OrderType;
import pkg.trader.Trader;

public class Client {
	public static void main(String[] args) {
		Market m = new Market("NASDAQ");
		IPO.enterNewStock(m, "SBUX", "Starbucks Corp.", 92.86);
		IPO.enterNewStock(m, "TWTR", "Twitter Inc.", 47.88);
		IPO.enterNewStock(m, "VSLR", "Vivint Solar", 16.44);
		IPO.enterNewStock(m, "GILD", "Gilead Sciences", 93.33);

		m.printStocks();

		Market m1 = new Market("Nikkei");
		IPO.enterNewStock(m1, "BABA", "Alibaba", 84.88);
		IPO.enterNewStock(m1, "BDU", "Baidu", 253.66);

		m1.printStocks();

		// Create 10 traders
		Trader trader1 = new Trader("Neda", 200000.00);
		Trader trader2 = new Trader("Scott", 100000.00);
		Trader trader3 = new Trader("Luke", 100000.00);
		Trader trader4 = new Trader("Thomas", 100000.00);
		Trader trader5 = new Trader("Sritika", 100000.00);
		Trader trader6 = new Trader("Meg", 100000.00);
		Trader trader7 = new Trader("Jen", 100000.00);
		Trader trader8 = new Trader("Emory", 100000.00);
		Trader trader9 = new Trader("Justin", 100000.00);
		Trader trader10 = new Trader("Zach", 100000.00);
		Trader trader11 = new Trader("Matt", 100000.00);
		Trader trader12 = new Trader("Angela", 100000.00);
		Trader trader13 = new Trader("Hamza", 100000.00);
		Trader trader14 = new Trader("Ethan", 100000.00);

		// Two traders with market orders
		Trader trader15 = new Trader("T1", 300000.00);
		Trader trader16 = new Trader("T2", 300000.00);

		try {
			trader1.buyFromBank(m, "SBUX", 1600);
			trader2.buyFromBank(m, "SBUX", 300);
			trader3.buyFromBank(m, "SBUX", 300);
			trader4.buyFromBank(m, "SBUX", 300);
			trader5.buyFromBank(m, "SBUX", 600);
			trader6.buyFromBank(m, "SBUX", 700);
			trader7.buyFromBank(m, "SBUX", 500);
			trader15.buyFromBank(m, "SBUX", 1500);
			// Exception
			trader8.buyFromBank(m, "SBUX", 5000);
		} catch (StockMarketExpection e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		trader1.printTrader();
		trader2.printTrader();
		trader3.printTrader();
		trader4.printTrader();
		trader5.printTrader();
		trader6.printTrader();
		trader7.printTrader();
		trader8.printTrader();
		trader15.printTrader();

		// Place sell orders
		try {
			trader1.placeNewOrder(m, "SBUX", 100, 97.0, OrderType.SELL);
			trader2.placeNewOrder(m, "SBUX", 300, 97.5, OrderType.SELL);
			trader3.placeNewOrder(m, "SBUX", 300, 98.0, OrderType.SELL);
			trader4.placeNewOrder(m, "SBUX", 300, 98.5, OrderType.SELL);
			trader5.placeNewOrder(m, "SBUX", 500, 99.0, OrderType.SELL);
			trader6.placeNewOrder(m, "SBUX", 700, 99.5, OrderType.SELL);
			trader7.placeNewOrder(m, "SBUX", 500, 100.0, OrderType.SELL);
			trader15.placeNewMarketOrder(m, "SBUX", 1500, 0, OrderType.SELL);

		} catch (StockMarketExpection e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Printing after the sell orders are placed");
		System.out.println("&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^");
		trader1.printTrader();
		trader2.printTrader();
		trader3.printTrader();
		trader4.printTrader();
		trader5.printTrader();
		trader6.printTrader();
		trader7.printTrader();
		trader15.printTrader();

		// Place buy orders
		try {
			trader8.placeNewOrder(m, "SBUX", 200, 101.0, OrderType.BUY);
			trader9.placeNewOrder(m, "SBUX", 300, 100.5, OrderType.BUY);
			trader10.placeNewOrder(m, "SBUX", 400, 100.0, OrderType.BUY);
			trader11.placeNewOrder(m, "SBUX", 500, 99.5, OrderType.BUY);
			trader12.placeNewOrder(m, "SBUX", 900, 99.0, OrderType.BUY);
			trader13.placeNewOrder(m, "SBUX", 1000, 98.5, OrderType.BUY);
			trader14.placeNewOrder(m, "SBUX", 900, 98.0, OrderType.BUY);
			trader16.placeNewMarketOrder(m, "SBUX", 700, 0, OrderType.BUY);
		} catch (StockMarketExpection e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Printing after the buy orders are placed");
		System.out.println("&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^");
		trader8.printTrader();
		trader9.printTrader();
		trader10.printTrader();
		trader11.printTrader();
		trader12.printTrader();
		trader13.printTrader();
		trader14.printTrader();
		trader16.printTrader();

		m.triggerTrade();

		System.out.println("Printing after the tradings are done");
		System.out.println("&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^");
		trader1.printTrader();
		trader2.printTrader();
		trader3.printTrader();
		trader4.printTrader();
		trader5.printTrader();
		trader6.printTrader();
		trader7.printTrader();
		trader8.printTrader();
		trader9.printTrader();
		trader10.printTrader();
		trader11.printTrader();
		trader12.printTrader();
		trader13.printTrader();
		trader14.printTrader();
		trader15.printTrader();
		trader16.printTrader();

		m.printHistoryFor("SBUX");

	}
}
