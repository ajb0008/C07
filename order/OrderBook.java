package pkg.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.market.api.PriceSetter;

public class OrderBook {
    Market m;
    HashMap<String, ArrayList<Order>> buyOrders;
    HashMap<String, ArrayList<Order>> sellOrders;
    PriceSetter priceSetter;

    public OrderBook(Market m) {
        this(m, new PriceSetter());
    }

    public OrderBook(Market m, PriceSetter ps) {
        this.m = m;
        buyOrders = new HashMap<String, ArrayList<Order>>();
        sellOrders = new HashMap<String, ArrayList<Order>>();
        priceSetter = ps;
        ps.registerObserver(m.getMarketHistory());
    }

    public void addToOrderBook(Order order) {
        ArrayList<Order> orders = null;
        if (order.getClass() == BuyOrder.class) {
            
          buyOrders = addBuyOrder(order, buyOrders);
           
        } else if (order.getClass() == SellOrder.class) {
          sellOrders = addsellOrder(order, sellOrders);
        }
        orders.add(order);
    }

    public void trade() {
        for (String stockSymbol : buyOrders.keySet()) {
            ArrayList<Order> buyOrdersForSymbol = buyOrders.get(stockSymbol);
            ArrayList<Order> sellOrdersForSymbol = sellOrders.get(stockSymbol);

            double orderPrice = findMatchingPrice(buyOrdersForSymbol,
                    sellOrdersForSymbol);
            int orderSize = getOrderSize(buyOrdersForSymbol,
                    sellOrdersForSymbol, orderPrice);

            m.getMarketHistory().setSubject(priceSetter);
            priceSetter.setNewPrice(m, stockSymbol, orderPrice);

            try {
                processBuyOrders(buyOrdersForSymbol, orderPrice, orderSize);
                processSellOrders(sellOrdersForSymbol, orderPrice, orderSize);
            } catch (StockMarketExpection e) {
                e.printStackTrace();
            }
        }
    }

    public int getOrderSize(ArrayList<Order> buyOrdersForSymbol,
            ArrayList<Order> sellOrdersForSymbol, double orderPrice) {
        HashSet<Order> validBuyOrders = new HashSet<Order>(buyOrdersForSymbol);
        HashSet<Order> validSellOrders = new HashSet<Order>(sellOrdersForSymbol);
        validateOrders(validBuyOrders, validSellOrders, null, null, orderPrice);
        int orderSize = Math.min(getTotalSize(validBuyOrders),
                getTotalSize(validSellOrders));
        return orderSize;
    }

    public double findMatchingPrice(ArrayList<Order> buyOrdersForSymbol,
            ArrayList<Order> sellOrdersForSymbol) {
        TreeMap<Double, CumulativeOrder> cumulativeBuyOrders = new TreeMap<Double, OrderBook.CumulativeOrder>();
        TreeMap<Double, CumulativeOrder> cumulativeSellOrders = new TreeMap<Double, OrderBook.CumulativeOrder>();
        HashSet<Order> unboughtOrders = new HashSet<Order>();
        HashSet<Order> unsoldOrders = new HashSet<Order>();

        associateOrdersByPrice(buyOrdersForSymbol, cumulativeBuyOrders,
                unboughtOrders);

        associateOrdersByPrice(sellOrdersForSymbol, cumulativeSellOrders,
                unsoldOrders);

        int orderSize = 0;
        double orderPrice = 0;
        for (CumulativeOrder cumulativeSellOrder : cumulativeSellOrders
                .values()) {
            unboughtOrders.clear();
            for (CumulativeOrder cumulativeBuyOrder : cumulativeBuyOrders
                    .values()) {
                double newPrice = cumulativeSellOrder.getCumulativePrice();
                int newSize = calcNewSize(unboughtOrders, unsoldOrders,
                        cumulativeBuyOrder, cumulativeSellOrder, newPrice);
                if (newSize >= orderSize) {

                    orderSize = newSize;
                    orderPrice = newPrice;
                    unboughtOrders.addAll(cumulativeBuyOrder.getOrders());
                    unsoldOrders.addAll(cumulativeSellOrder.getOrders());
                } else if (newPrice <= orderPrice) {

                }
            }
        }
        return orderPrice;
    }

    /**
     * Find orders in the ArrayList for which the price is less than or equal to
     * the order price. Remove those orders. Notify the traders.
     */
    public void processSellOrders(ArrayList<Order> sellOrdersForSymbol,
            double orderPrice, int orderSize) throws StockMarketExpection {
        ArrayList<Order> sellOrdersCompleted = new ArrayList<Order>();
        int size = 0;
        for (Order order : sellOrdersForSymbol) {
            if (order.isMarketOrder() || order.getPrice() <= orderPrice) {
                if (order.getSize() + size <= orderSize) {
                    order.getTrader().tradePerformed(order, orderPrice);
                    sellOrdersCompleted.add(order);
                } else {
                    order.setSize(orderSize - size);
                    order.getTrader().tradePerformed(order, orderPrice);
                    sellOrdersCompleted.add(order);
                }
            }
        }
        for (Order order : sellOrdersCompleted) {
            sellOrdersForSymbol.remove(order);
        }
    }

    /**
     * Find orders in the ArrayList for which the price is greater than or equal
     * to the order price. Remove those orders. Notify the traders.
     */
    public void processBuyOrders(ArrayList<Order> buyOrdersForSymbol,
            double orderPrice, int orderSize) throws StockMarketExpection {
        ArrayList<Order> buyOrdersCompleted = new ArrayList<Order>();
        int size = 0;
        for (Order order : buyOrdersForSymbol) {
            if (order.isMarketOrder() || order.getPrice() >= orderPrice) {
                if (order.getSize() + size <= orderSize) {
                    order.getTrader().tradePerformed(order, orderPrice);
                    buyOrdersCompleted.add(order);
                } else {
                    order.setSize(orderSize - size);
                }
            }
        }

        for (Order order : buyOrdersCompleted) {
            buyOrdersForSymbol.remove(order);
        }
    }

    /**
     * Calculates new volume of all unprocessed orders if the sets are merged.
     */
    public int calcNewSize(HashSet<Order> unboughtOrders,
            HashSet<Order> unsoldOrders, CumulativeOrder cumulativeBuyOrder,
            CumulativeOrder cumulativeSellOrder, double matchingPrice) {
        validateOrders(unboughtOrders, unsoldOrders, cumulativeBuyOrder,
                cumulativeSellOrder, matchingPrice);
        int unboughtSize = getTotalSize(unboughtOrders);
        int unsoldSize = getTotalSize(unsoldOrders);
        return Math.min(unboughtSize, unsoldSize);
    }

    /**
     * Calculates new volume of all unprocessed orders if the sets are merged.
      */
    private void validateOrders(HashSet<Order> unboughtOrders,
            HashSet<Order> unsoldOrders, CumulativeOrder cumulativeBuyOrder,
            CumulativeOrder cumulativeSellOrder, double matchingPrice) {
        HashSet<Order> validBuyOrders = new HashSet<Order>();
        HashSet<Order> validSellOrders = new HashSet<Order>();
      	validBuyOrders = addValidBuyOrders(unboughtOrders, validBuyOrders);
        validSellOrders = addValidSellOrders(unsoldOrders, validSellOrders);

        if (cumulativeBuyOrder != null
                && cumulativeBuyOrder.getCumulativePrice() >= matchingPrice) {
            validBuyOrders.addAll(cumulativeBuyOrder.getOrders());
        }
        if (cumulativeSellOrder != null
                && cumulativeSellOrder.getCumulativePrice() <= matchingPrice) {
            validSellOrders.addAll(cumulativeSellOrder.getOrders());
        }

        unboughtOrders.clear();
        unboughtOrders.addAll(validBuyOrders);
        unsoldOrders.clear();
        unsoldOrders.addAll(validSellOrders);
    }

    /**
     * Calculates the total volume of the set of orders.
     */
    public int getTotalSize(HashSet<Order> orders) {
        int totalSize = 0;
        for (Order o : orders) {
            totalSize += o.getSize();
        }
        return totalSize;
    }

    public void associateOrdersByPrice(ArrayList<Order> allOrders,
            TreeMap<Double, CumulativeOrder> cumulativeOrders,
            HashSet<Order> pendingOrders) {
        for (Order order : allOrders) {
            if (order.isMarketOrder()) {
                continue;
            }

            CumulativeOrder cumulativeOrder = cumulativeOrders.get(order
                    .getPrice());
            if (cumulativeOrder == null) {
                cumulativeOrder = new CumulativeOrder(order.getPrice());
                cumulativeOrders.put(order.getPrice(), cumulativeOrder);
            }

            cumulativeOrder.addOrder(order);
        }
    }
  
  public HashSet<Order> addValidBuyOrders(HashSet<Order> unboughtOrders, HashSet<Order> validBuyOrders) {
	for (Order o : unboughtOrders) {
            if (o.isMarketOrder() || o.getPrice() >= matchingPrice) {
                validBuyOrders.add(o);
            }
        } 
    	return validBuyOrders;
  }
  
   HashSet<Order> addValidSellOrders(HashSet<Order> unsoldOrders, HashSet<Order> validSellOrders);
          for (Order o : unsoldOrders) {
            if (o.isMarketOrder() || o.getPrice() <= matchingPrice) {
                validSellOrders.add(o);
            }
          }
  	return validSellOrders;
  }
 

   HashMap<String, ArrayList<Order>> addBuyOrder(Order order,  HashMap<String, ArrayList<Order>> buyOrders) {
          ArrayList<Order> orders = orders = buyOrders.get(order.getStockSymbol());
          if (orders == null) {
               	orders = new ArrayList<Order>();
               	buyOrders.put(order.getStockSymbol(), orders);
          }
    	  return buyOrders;
  }

   HashMap<String, ArrayList<Order>> addSellOrder(Order order,  HashMap<String, ArrayList<Order>> sellOrders) {
          ArrayList<Order> orders = sellOrders.get(order.getStockSymbol());
            if (orders == null) {
                orders = new ArrayList<Order>();
                sellOrders.put(order.getStockSymbol(), orders);
            }
    	  return sellOrders;
  }
    

    public class CumulativeOrder {
        private double cumulativePrice;
        private ArrayList<Order> orders;

        public CumulativeOrder(double cumulativePrice) {
            setCumulativePrice(cumulativePrice);
            orders = new ArrayList<Order>();
        }

        public void addOrder(Order order) {
            if (order.getPrice() != cumulativePrice) {
                throw new IllegalArgumentException("Prices don't match!");
            }
            if (orders.contains(order)) {
                System.err.println("Tried to add duplicate order for price"
                        + cumulativePrice);
                return;
            }
            orders.add(order);
        }

        public ArrayList<Order> getOrders() {
            return orders;
        }

        public double getCumulativePrice() {
            return cumulativePrice;
        }

        public void setCumulativePrice(double cumulativePrice) {
            this.cumulativePrice = cumulativePrice;
        }
    }
}
