package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.BuyOrder;
import pkg.order.Order;
import pkg.order.OrderType;
import pkg.order.SellOrder;
import pkg.stock.Stock;
import pkg.util.OrderUtility;

public class Trader {
  
    String name;
    
    double cashInHand;
    
    ArrayList<Order> position;
    
    ArrayList<Order> ordersPlaced;

    public Trader(String name, double cashInHand) {
        super();
        this.name = name;
        this.cashInHand = cashInHand;
        this.position = new ArrayList<Order>();
        this.ordersPlaced = new ArrayList<Order>();
    }

    public void buyFromBank(Market m, String symbol, int volume)
            throws StockMarketExpection {
        Stock stock = m.getStockForSymbol(symbol);
        Order order = new BuyOrder(symbol, volume, stock.getPrice(), this);
        if (cashInHand >= order.getTotalPrice()) {
            cashInHand -= order.getTotalPrice();
            position.add(order);
        } else {
            throw new StockMarketExpection("Cannot place order for stock: "
                    + symbol + " since there is not enough money. Trader: "
                    + name);
        }
    }

    public void placeNewOrder(Market m, String symbol, int volume,
            double price, OrderType orderType) throws StockMarketExpection {
        Order order = createOrder(symbol, volume, price, orderType, false);
        m.addOrder(order);
        ordersPlaced.add(order);

    }

    public void placeNewMarketOrder(Market m, String symbol, int volume,
            double price, OrderType orderType) throws StockMarketExpection {
        Order order = createOrder(symbol, volume, price, orderType, true);
        m.addOrder(order);
        ordersPlaced.add(order);
    }

    private Order createOrder(String symbol, int volume, double price,
            OrderType orderType, boolean isMarketOrder)
            throws StockMarketExpection {
        Order order = null;
        if (orderType == OrderType.BUY) {
            if (isMarketOrder) {
                order = new BuyOrder(symbol, volume, true, this);
            } else {
                order = new BuyOrder(symbol, volume, price, this);
            }
            if (cashInHand < order.getTotalPrice()) {
                throw new StockMarketExpection("Cannot place order for stock: "
                        + symbol + " since there is not enough money. Trader: "
                        + name);
            }
        } else {
            if (isMarketOrder) {
                order = new SellOrder(symbol, volume, true, this);
            } else {
                order = new SellOrder(symbol, volume, price, this);
            }
            if (OrderUtility.ownedQuantity(position, symbol) < volume) {
                throw new StockMarketExpection(
                        "Cannot place order for stock: "
                                + symbol
                                + " since Trader does not own the correct quantity of stocks. Trader: "
                                + name);
            }
        }
        if (OrderUtility.isAlreadyPresent(ordersPlaced, order)) {
            throw new StockMarketExpection(
                    "Cannot place order for stock: "
                            + symbol
                            + " since an order for that stock has already been placed. Trader: "
                            + name);
        }
        return order;
    }

    public void tradePerformed(Order o, double matchPrice)
            throws StockMarketExpection {
        if (o.getClass() == BuyOrder.class) {
            if (cashInHand <= matchPrice * o.getSize()) {
                throw new StockMarketExpection("");
            }
            cashInHand -= matchPrice * o.getSize();
            position.add(o);
            ordersPlaced.remove(o);
        } else {
            ordersPlaced.remove(o);
            Order extracted = OrderUtility.findAndExtractOrder(position,
                    o.getStockSymbol());
            extracted.setSize(extracted.getSize() - o.getSize());
            if (extracted.getSize() > 0) {
                position.add(extracted);
            }
            cashInHand += o.getSize() * matchPrice;

        }
    }

    public void printTrader() {
        System.out.println("Trader Name: " + name);
        System.out.println("=====================");
        System.out.println("Cash: " + cashInHand);
        System.out.println("Stocks Owned: ");
        for (Order o : position) {
            o.printStockNameInOrder();
        }
        System.out.println("Stocks Desired: ");
        for (Order o : ordersPlaced) {
            o.printOrder();
        }
        System.out.println("+++++++++++++++++++++");
        System.out.println("+++++++++++++++++++++");
    }

    public ArrayList<Order> getOrdersPlaced() {
        return ordersPlaced;
    }

    public ArrayList<Order> getPosition() {
        return position;
    }
}
