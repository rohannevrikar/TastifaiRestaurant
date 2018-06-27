package tastifai.restaurant.Adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rohannevrikar.restaurant.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import tastifai.restaurant.Activities.MainActivity;
import tastifai.restaurant.Models.Order;
import tastifai.restaurant.Models.TimePOJO;

import static tastifai.restaurant.Activities.LoginActivity.serviceMediaPlayer;
import static tastifai.restaurant.Activities.MainActivity.deliveryCharge;
import static tastifai.restaurant.Activities.MainActivity.mediaPlayer;
import static tastifai.restaurant.Activities.MainActivity.totalPrice;

import static tastifai.restaurant.Activities.MainActivity.guid;
import static tastifai.restaurant.Fragments.CurrentOrder.v;
import static tastifai.restaurant.Activities.MainActivity.currentCount;
import static tastifai.restaurant.Activities.MainActivity.deliveryCount;
import static tastifai.restaurant.Activities.MainActivity.progressCount;
import static tastifai.restaurant.Activities.MainActivity.restaurantId;
import static tastifai.restaurant.Activities.MainActivity.tabLayout;
import static tastifai.restaurant.Activities.MainActivity.viewPager;
import static tastifai.restaurant.Services.CheckNewOrdersService.isMediaPlayerRunning;

/**
 * Created by Rohan Nevrikar on 01-02-2018.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private final Context mContext;
    private View view;
    private final LayoutInflater layoutInflater;
    private static final String TAG = "message";
    private ArrayList<Order> orderList;
    private RecyclerView mRecyclerView;
    public static TimePOJO timings = null;
    private int layout;

    public OrderAdapter(Context mContext, ArrayList<Order> orderList, int layout) {
        Log.d(TAG, "OrderAdapter: " + orderList.size());
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
        this.orderList = orderList;

        this.layout = layout;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layout == R.layout.activity_currentorder) {
            view = layoutInflater.inflate(R.layout.order_row, parent, false);

            Log.d(TAG, "onCreateViewHolder: View is order_row");
        } else if (layout == R.layout.activity_inprogress) {
            view = layoutInflater.inflate(R.layout.progress_row, parent, false);
            Log.d(TAG, "onCreateViewHolder: View is inprogress_row");

        } else {
            view = layoutInflater.inflate(R.layout.delivery_row, parent, false);
            Log.d(TAG, "onCreateViewHolder: View is delivery_row");

        }


        Log.d(TAG, "onCreateViewHolder: " + view.getResources().getResourceEntryName(layout) + " " + view.getResources().getResourceEntryName(R.layout.activity_currentorder));


        return new ViewHolder(view, mContext, orderList);

    }

    //18:28:06.392
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        int serialNumber = position + 1;

        String string = "\u20B9";
        byte[] utf8 = null;
        try {
            utf8 = string.getBytes("UTF-8");
            string = new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //final boolean isExpanded = position==mExpandedPosition;
        holder.customer.setText(orderList.get(position).getCustomerName());
        holder.price.setText(string + "" + String.format("%.2f", orderList.get(position).getTotalUser()));
        holder.itemNumber.setText(serialNumber + ". ");
        holder.dateTime.setText(orderList.get(position).getDateTime());
        Log.d(TAG, "onBindViewHolder: View pager value" + viewPager.getCurrentItem());
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        holder.itemRecyclerView.setLayoutManager(layoutManager);
        CustomListAdapter adapter = new CustomListAdapter(mContext, orderList.get(position).getItemList());
        holder.itemRecyclerView.setAdapter(adapter);
        holder.instructions.setText(orderList.get(position).getinstruction());


//        timings = new TimePOJO();
//        if(layout == R.layout.activity_currentorder){
//            timings.setReceivedAt("04:20PM");
//        }
//        else if(layout == R.layout.activity_inprogress){
//            timings.setReceivedAt("04:20PM");
//            timings.setAcceptedAt("04:22PM");
//
//        }
//        else if(layout == R.layout.activity_delivery)
//        {
//            timings.setReceivedAt("04:20PM");
//            timings.setAcceptedAt("04:22PM");
//            timings.setDispatchedAt("04:55PM");
//        }

        holder.dynamicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (layout == R.layout.activity_currentorder) {
                    v.cancel();
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    //mContext.stopService(new Intent(mContext, MediaPlayerService.class));
                    Log.d(TAG, "onClick: Accept clicked ");
                    guid = orderList.get(position).getGuid();

                    String URL = "http://foodspecwebapi.us-east-1.elasticbeanstalk.com/api/FoodSpec/PostAcceptOrders/" + restaurantId + "/" + guid;
                    CallAPI api = new CallAPI();
                    api.execute(URL);
                    Toast.makeText(mContext, "Accepting order, please wait a moment...", Toast.LENGTH_LONG).show();

//                    currentCount = currentCount - 1;
//                    progressCount = progressCount + 1;
                    ((MainActivity) mContext).adapter.changeFragmentTitle(0, "ORDER(" + currentCount + ")");
                    ((MainActivity) mContext).adapter.changeFragmentTitle(1, "PROGRESS(" + progressCount + ")");

                    tabLayout.setupWithViewPager(viewPager);


//                    MainActivity.progressOrders.add(orderList.get(position));
//                    MainActivity.progressCount = MainActivity.progressOrders.size();
//                    orderList.remove(position);
                    MainActivity.currentCount = orderList.size();
//                     mRecyclerView.getAdapter().notifyDataSetChanged();
                    //((MainActivity)mContext).updateViewPager();
                    ((MainActivity) mContext).orderCount();
                    notifyDataSetChanged();


                } else if (layout == R.layout.activity_inprogress) {
                    Log.d(TAG, "onClick: Progress" + orderList.size() + " " + position);
                    guid = orderList.get(position).getGuid();
                    String URL = "http://foodspecwebapi.us-east-1.elasticbeanstalk.com/api/FoodSpec/PostStartDeliveryOrders/" + restaurantId + "/" + guid;
                    CallAPI api = new CallAPI();
                    api.execute(URL);
                    Toast.makeText(mContext, "Starting delivery, please wait a moment...", Toast.LENGTH_LONG).show();

//                    progressCount = progressCount - 1;
//                    deliveryCount = deliveryCount + 1;
                    ((MainActivity) mContext).adapter.changeFragmentTitle(1, "PROGRESS(" + progressCount + ")");
                    ((MainActivity) mContext).adapter.changeFragmentTitle(2, "DELIVERY(" + deliveryCount + ")");

                    tabLayout.setupWithViewPager(viewPager);

//                    MainActivity.deliveryOrders.add(orderList.get(position));
//                    MainActivity.deliveryCount = MainActivity.deliveryOrders.size();
//                    orderList.remove(position);
//                    Log.d(TAG, "onClick: Start delivery clicked");

                    MainActivity.progressCount = orderList.size();
                    //mRecyclerView.getAdapter().notifyDataSetChanged();
                    //((MainActivity)mContext).updateViewPager();

                    ((MainActivity) mContext).orderCount();
                    notifyDataSetChanged();


                } else if (layout == R.layout.activity_delivery) {

                    Log.d(TAG, "onClick: Delivered clicked" + orderList.size());
                    guid = orderList.get(position).getGuid();
                    Log.d(TAG, "onClick: guid: " + guid);
                    String URL = "http://foodspecwebapi.us-east-1.elasticbeanstalk.com/api/FoodSpec/PostDeliveredOrder/" + restaurantId + "/" + guid + "/";
                    CallAPI api = new CallAPI();
                    api.execute(URL);
                    //orderList.remove(position);
                    Toast.makeText(mContext, "Changing delivery status, please wait a moment...", Toast.LENGTH_LONG).show();

                    MainActivity.deliveryCount = orderList.size();
                    //mRecyclerView.getAdapter().notifyDataSetChanged();
                    // ((MainActivity)mContext).updateViewPager();

                    ((MainActivity) mContext).orderCount();


                    ((MainActivity) mContext).adapter.changeFragmentTitle(2, "DELIVERY(" + deliveryCount + ")");
                    tabLayout.setupWithViewPager(viewPager);


                    ((MainActivity) mContext).orderCount();
                    notifyDataSetChanged();

                }

            }
        });

//
        holder.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalPrice = orderList.get(position).getTotalUser();
                guid = orderList.get(position).getGuid();
                Log.d(TAG, "onClick: guid: " + guid);
                ((MainActivity) mContext).orderDetailsFragment(layout, orderList.get(position).isNavigationAvailable(), orderList.get(position).getCustomerName(), orderList.get(position).getItemList(), orderList.get(position).getDeliveryAddress(), orderList.get(position).getContactNumber(), orderList.get(position).getUserLat(), orderList.get(position).getUserLng(), orderList.get(position).getDiscount());

            }
        });


        //setListViewHeightBasedOnItems(holder.orderListView);

//        holder.expandView.setVisibility(isExpanded?View.VISIBLE:View.GONE);
//        holder.expandView.setActivated(isExpanded);
//        if (isExpanded)
//            previousExpandedPosition = position;
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mExpandedPosition = isExpanded ? -1:position;
//                notifyItemChanged(previousExpandedPosition);
//                notifyItemChanged(position);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + orderList.size());
        return orderList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView customer;
        public TextView price;
        public final ArrayList<Order> orderList;
        private View expandView;
        private RecyclerView itemRecyclerView;
        private Button btnDetails;
        private Button dynamicButton;
        public TextView dateTime;
        private TextView itemNumber;
        private TextView instructions;

        public final Context mContext;

        public ViewHolder(View itemView, Context mContext, ArrayList<Order> orderList) {
            super(itemView);
            this.mContext = mContext;
            this.orderList = orderList;
            customer = (TextView) itemView.findViewById(R.id.txtCustomer);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.orderRecyclerView);
            dynamicButton = itemView.findViewById(R.id.dynamicButton);
            btnDetails = itemView.findViewById(R.id.detailsButton);
            price = itemView.findViewById(R.id.totalPrice);
            dateTime = itemView.findViewById(R.id.dateTime);
            itemNumber = itemView.findViewById(R.id.itemNumber);
            instructions = itemView.findViewById(R.id.instructions);

        }


    }

    private class CallAPI extends AsyncTask<Object, String, String> {
        StringBuilder builder = new StringBuilder();
        String text;
        String dateTime, deliverAt, itemName, itemPrice, quantity;
        ArrayList<String> guidList = new ArrayList<>();


        @Override
        protected String doInBackground(Object... objects) {

            try {
                URL url = new URL((String) objects[0]);
                Log.d(TAG, "doInBackground: " + url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                //connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setReadTimeout(7000);
                connection.setConnectTimeout(7000);
                connection.connect();
//                InputStream istream = connection.getInputStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    Log.d("OrderAdapter", "doInBackground: " + line);
//                    builder.append(line);
//
//
//
//                }
                int responseCode = connection.getResponseCode();
                Log.d("OrderAdapter", "Response Code: " + responseCode);
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    Log.d("OrderAdapter", "doInBackground: " + responseCode + " " + builder.toString());
//                    String[] myArray = builder.toString().split(",");
//                    Log.d("OrderAdapter", "onPostExecute: " + myArray[0]);
//                    return builder.toString();
//                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
    }
}