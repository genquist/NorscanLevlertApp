package com.norscan.levlertapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.norscan.levlertapp.utils.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class deviceList extends Fragment
{
    boolean retrieve = false;
    private String deviceBaseUrl = "https://www.levlertsecure.com:3001/monitored_devices.json?";
    private String readingTimeUrl = "https://www.levlertsecure.com:3001/readings.json?&order=0&limit=1";
    String authToken;
    JsonHelper helper;
    ListView listView;
    CustomAdapter adapter;
    device[] deviceObjectList;
    List<String> deviceList =  new ArrayList<String>();
    List<String> deviceIdList =  new ArrayList<String>();
    List<String> timeList =  new ArrayList<String>();
    List<String> statusList =  new ArrayList<String>();
    int deviceCounter = 999999;

    public deviceList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_device_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getDevices();
    }

    public void getDevices()
    {
        if(!retrieve)
        {
            helper = new JsonHelper(getActivity());
            helper.trustEveryone();
            SharedPreferences sp;
            sp = getActivity().getSharedPreferences("LoginActivity", 0);
            authToken = sp.getString("authToken", null);

            if (deviceObjectList != null)
            {
                deviceList.clear();
                deviceIdList.clear();
                deviceObjectList = null;
            }
            DeviceParser devices = new DeviceParser();
            devices.execute();
        }
        else
        {
            listView = (ListView) getActivity().findViewById(R.id.device_listView);

            adapter = new CustomAdapter(getActivity(), R.layout.device_list_item, deviceObjectList);
            listView.setAdapter(adapter);

            AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView parent, View v, int position, long id)
                {
                    Intent i = new Intent(getActivity(), detailsActivity.class);
                    i.putExtra("deviceName", deviceObjectList[position].name.toString());
                    i.putExtra("deviceTime", deviceObjectList[position].readingTime.toString());
                    startActivity(i);
                }
            };

            listView.setOnItemClickListener(mMessageClickedHandler);

        }
    }
    public class DeviceParser extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            int responseCode = 0;
            try
            {
                String deviceData;
                URL url = new URL(deviceBaseUrl);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Authorization", authToken);
                conn.connect();
                responseCode = conn.getResponseCode();
                InputStream stream = conn.getInputStream();

                deviceData = helper.convertStreamToString(stream);

                stream.close();

                JSONObject reader = new JSONObject(deviceData);
                JSONArray data = reader.getJSONArray("data");

                deviceCounter = data.length();

                for(int i = 0; i < data.length(); i++)
                {
                    JSONObject deviceObject = data.getJSONObject(i);

                    JSONObject currentDevice = deviceObject.getJSONObject("monitored_device");

                    deviceList.add(currentDevice.getString("description"));
                    deviceIdList.add(currentDevice.getString("id"));

                    timeParser tp = new timeParser();
                    tp.execute(i);
                }
                retrieve = true;

            } catch (Exception ex)
            {
                Log.e("Something went wrong " + String.valueOf(responseCode), ex.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            MenuItem refreshItem = ((MainActivity)getActivity()).getMenu().findItem(R.id.action_refresh);
            refreshItem.setActionView(null);
            try
            {
                deviceObjectList = new device[deviceList.size()];

                for(int i = 0; i < deviceList.size(); i++)
                {
                    device test = new device(deviceList.get(i),"0");
                    deviceObjectList[i] = test;
                }

                listView = (ListView) getActivity().findViewById(R.id.device_listView);
                final String[] fdeviceList = new String[deviceList.size()];
                deviceList.toArray(fdeviceList);

                adapter = new CustomAdapter(getActivity(), R.layout.device_list_item, deviceObjectList);
                listView.setAdapter(adapter);

                AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener()
                {
                    public void onItemClick(AdapterView parent, View v, int position, long id)
                    {
                        Intent i = new Intent(getActivity(), detailsActivity.class);
                        i.putExtra("deviceName", deviceObjectList[position].name.toString());
                        i.putExtra("deviceTime", deviceObjectList[position].readingTime.toString());
                        startActivity(i);
                    }
                };

                listView.setOnItemClickListener(mMessageClickedHandler);
            }
            catch (Exception e)
            {
                Toast.makeText(getActivity(), "Retrieve Device List Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public class timeParser extends AsyncTask<Integer, Void, Void>
    {
        int position = 0;
        String reading = "";
        @Override
        protected Void doInBackground(Integer... voids)
        {
            try
            {
                position = voids[0];
                readingTimeUrl += "&monitored_device_id=" + deviceIdList.get(position);
                URL url1 = new URL(readingTimeUrl);

                HttpsURLConnection conn1 = (HttpsURLConnection) url1.openConnection();
                conn1.setReadTimeout(10000);
                conn1.setConnectTimeout(15000);
                conn1.setRequestMethod("GET");
                conn1.setDoInput(true);
                conn1.setRequestProperty("Authorization", authToken);
                conn1.connect();
                InputStream stream1 = conn1.getInputStream();
                JSONObject reader1 = new JSONObject(helper.convertStreamToString(stream1));

                JSONArray data1 = reader1.getJSONArray("data");
                JSONObject dataObj1 = data1.getJSONObject(0);
                JSONObject reading1 = dataObj1.getJSONObject("reading");

                reading = reading1.getString("datetime_utc");
                deviceObjectList[position].readingTime = reading1.getString("datetime_utc");
                //timeList.add(reading1.getString("datetime_utc"));

                stream1.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            updateView(position, reading);
        }
    }

    public class CustomAdapter extends ArrayAdapter<device>
    {
        private LayoutInflater inflater=null;
        public CustomAdapter(Context context, int resource, device[] objects)
        {
            super(context, resource, objects);
            inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private class deviceHolder
        {
            public TextView deviceName;
            public TextView lastContactTime;
            public ImageView statusImg;
            public ImageView deviceImg;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            device obj = getItem(position);
            String name = obj.name;
            String readingTime = obj.readingTime;

            Date date = new Date(Integer.valueOf(readingTime) * 1000L);
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
            String formatted = format.format(date);

            deviceHolder holder=new deviceHolder();
            View rowView;
            rowView = inflater.inflate(R.layout.device_list_item, null);
            holder.deviceName = (TextView) rowView.findViewById(R.id.deviceName_list_item);
            holder.lastContactTime = (TextView) rowView.findViewById(R.id.lastContactTime_list_item);
            holder.statusImg = (ImageView) rowView.findViewById(R.id.statusImg);

            holder.deviceName.setText(name);
            holder.lastContactTime.setText(formatted);
            holder.statusImg.setImageResource(R.drawable.ic_action_accept);
            return rowView;
        }
    }

    class device
    {
        String name;
        String readingTime;

        public device (String name, String readingTime) {
            this.name = name;
            this.readingTime = readingTime;
        }
    }

    private void updateView(int index, String time)
    {
        View v = listView.getChildAt(index - listView.getFirstVisiblePosition());

        if(v == null)
        {
            return;
        }

        TextView someText = (TextView) v.findViewById(R.id.lastContactTime_list_item);
        Date date = new Date(Integer.valueOf(time) * 1000L);
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
        String formatted = format.format(date);

        someText.setText(formatted);
    }
}
