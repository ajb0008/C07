package pkg.market;

import java.util.ArrayList;
import java.util.HashMap;

import pkg.exception.StockMarketExpection;
import pkg.order.Order;
import pkg.order.OrderBook;
import pkg.stock.Stock;

public class Market {
	String marketName;
	HashMap<String, Stock> stockList;
	MarketHistory marketHistory;
	OrderBook orderBook;

	public Market(String name) {
		this.marketName = name;
		stockList = new HashMap<String, Stock>();
		marketHistory = new MarketHistory(this);
		orderBook = new OrderBook(this);
	}

	public void addStock(Stock stock) throws StockMarketExpection {
		if (stock.getPrice() < 0.0) {
			throw new StockMarketExpection("Stock has a negative price ("
					+ stock.getSymbol() + ", " + stock.getPrice() + ")");
		}
		if (stockList.containsKey(stock.getSymbol())) {
			throw new StockMarketExpection(
					"Tried to enter a stock that is already present ("
							+ stock.getSymbol() + ")");
		}
		stockList.put(stock.getSymbol(), stock);
		marketHistory
				.startHistoryWithPrice(stock.getSymbol(), stock.getPrice());
	}

	public Stock getStockForSymbol(String symbol) {
		if (stockList.containsKey(symbol)) {
			return stockList.get(symbol);
		}
		return null;
	}

	public Stock removeStockFromStockList(String symbol)
			throws StockMarketExpection {
		if (getStockForSymbol(symbol) == null) {
			throw new StockMarketExpection("Stock not present (" + symbol + ")");
		}
		return stockList.remove(symbol);
	}

	public void updateStockPrice(String symbol, double newPrice)
			throws StockMarketExpection {

		if (getStockForSymbol(symbol) == null) {
			throw new StockMarketExpection("Stock not present (" + symbol + ")");
		}
		if (newPrice < 0.0) {
			throw new StockMarketExpection(
					"Stock price cannot be set to a negative value (" + symbol
							+ ", " + getStockForSymbol(symbol).getPrice()
							+ ") -> " + newPrice + " X Not Allowed ");
		}

		Stock s = removeStockFromStockList(symbol);
		s.setPrice(newPrice);
		addStock(s);
	}

	public void printStocks() {
		System.out.println(marketName);
		for (String stockSymbol : stockList.keySet()) {
			System.out.print("(" + stockSymbol + ", "
					+ stockList.get(stockSymbol).getPrice() + ") ");
		}
		System.out.println();
	}

	public void printHistoryFor(String symbol) {
		if (getStockForSymbol(symbol) != null) {
			ArrayList<Double> priceList = getMarketHistory()
					.getPriceFor(symbol);
			System.out.println("Stock Name: " + symbol + " in Market: "
					+ marketName);
			for (int i = priceList.size() - 1; i >= 0; i--) {
				System.out.print(priceList.get(i));
				if (i != 0)
					System.out.print(" - ");
			}
			System.out.println();
		}
	}

	public void addOrder(Order order) {
		orderBook.addToOrderBook(order);
	}

	public MarketHistory getMarketHistory() {
		return marketHistory;
	}

	public void setMarketHistory(MarketHistory marketHistory) {
		this.marketHistory = marketHistory;
	}

	public void triggerTrade() {
		orderBook.trade();
	}

}
