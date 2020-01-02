package com.spikingacacia.spikyletabuyer.orders;

import com.spikingacacia.spikyletabuyer.database.BOrders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.spikingacacia.spikyletabuyer.LoginA.bOrdersList;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class BOOrderC
{
    public final List<OrderItem> ITEMS = new ArrayList<OrderItem>();
    public final Map<String, OrderItem> ITEM_MAP = new HashMap<String, OrderItem>();

    public BOOrderC(int whichOrder)
    {
        List<String> order_numbers=new ArrayList<>();
        Iterator iterator= bOrdersList.entrySet().iterator();
        while (iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, BOrders>set=(LinkedHashMap.Entry<Integer, BOrders>) iterator.next();
            BOrders bOrders=set.getValue();
            //int id=bOrders.getId();
            //int userId=bOrders.getUserId();
            //int itemId=bOrders.getItemId();
            int orderNumber=bOrders.getOrderNumber();
            int orderStatus=bOrders.getOrderStatus();
            //String orderName=bOrders.getOrderName();
            //double price=bOrders.getPrice();
            //String username=bOrders.getUsername();
            //int tableNumber=bOrders.getTableNumber();
            String dateAdded=bOrders.getDateAdded();
            //String dateChanged=bOrders.getDateChanged();
            String[] date_pieces=dateAdded.split(" ");
            //show only the finished orders
            if( (whichOrder==5) && (orderStatus!=5) )
                continue;
            //show current orders
            if( (whichOrder==0) && (orderStatus==5) )
                continue;
            String unique_name=date_pieces[0]+":"+orderNumber+":"+orderStatus;
            order_numbers.add(unique_name);
        }

        int position=0;
        Set<String> unique=new HashSet<>(order_numbers);
        List<String> order_counts=new ArrayList<>(unique);
        Iterator<String> iterator_2=order_counts.iterator();
        while(iterator_2.hasNext())
        {
            String unique_name=iterator_2.next();
            //items
            int id=0;
            int userId=0;
            int itemId=0;
            int orderNumber=0;
            int orderStatus=0;
            String orderName="";
            double price=0.0;
            String restaurant="";
            int tableNumber=0;
            String dateAdded="";
            //String dateChanged="";


            Iterator iterator_3= bOrdersList.entrySet().iterator();
            while (iterator_3.hasNext())
            {
                LinkedHashMap.Entry<Integer, BOrders>set=(LinkedHashMap.Entry<Integer, BOrders>) iterator_3.next();
                BOrders bOrders=set.getValue();
                orderStatus=bOrders.getOrderStatus();
                if( (whichOrder==5) && (orderStatus!=5) )
                    continue;
                //show current orders
                if( (whichOrder==0) && (orderStatus==5) )
                    continue;
                orderNumber=bOrders.getOrderNumber();
                dateAdded=bOrders.getDateAdded();
                String[] date_pieces=dateAdded.split(" ");
                String unique_name_2=date_pieces[0]+":"+orderNumber+":"+orderStatus;
                if(unique_name_2.contentEquals(unique_name))
                {
                    id=bOrders.getId();
                    //userId=bOrders.getUserId();
                    itemId=bOrders.getItemId();

                    orderName=bOrders.getOrderName();
                    price=bOrders.getPrice();
                    restaurant=bOrders.getRestaurantName();
                    tableNumber=bOrders.getTableNumber();
                    //dateChanged=bOrders.getDateChanged();
                   break;
                }

            }
            addItem(CreateItem(position+1,id,itemId,orderNumber,orderStatus,orderName,price,restaurant,tableNumber,dateAdded));
            position+=1;

        }
    }

    private  void addItem(OrderItem item)
    {
        ITEMS.add(item);
        ITEM_MAP.put(item.position, item);
    }

    public  OrderItem CreateItem(int position, int id, int itemId, int orderNumber, int orderStatus, String orderName, double price, String username, int tableNumber, String dateAdded)
    {
        return new OrderItem(String.valueOf(position), id, itemId, orderNumber, orderStatus, orderName, price, username, tableNumber, dateAdded);
    }

    public class OrderItem
    {
        public String position;
        public int id;
        //public int userId;
        public int itemId;
        public int orderNumber;
        public int orderStatus;
        public String orderName;
        public double price;
        public String restaurantName;
        public int tableNumber;
        public String dateAdded;
        //public String dateChanged;

        public OrderItem(String position, int id, int itemId, int orderNumber, int orderStatus, String orderName, double price, String restaurantName, int tableNumber, String dateAdded)
        {
            this.position = position;
            this.id = id;
            //this.userId = userId;
            this.itemId = itemId;
            this.orderNumber = orderNumber;
            this.orderStatus = orderStatus;
            this.orderName = orderName;
            this.price = price;
            this.restaurantName=restaurantName;
            this.tableNumber=tableNumber;
            this.dateAdded = dateAdded;
            //this.dateChanged = dateChanged;
        }

        @Override
        public String toString()
        {
            return restaurantName;
        }
    }
}
