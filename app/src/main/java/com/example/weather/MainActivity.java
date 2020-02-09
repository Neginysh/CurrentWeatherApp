package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.DataModel.WeatherDataModel;
import com.example.weather.ForecastDataModel.FWeatherDataModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText city;
    private String cityName;
    private FloatingActionButton fab;
    private TextView cityTV;
    private TextView temp;
    private TextView sunrise;
    private TextView sunset;
    private TextView wind;
    private TextView date;
    private TextView condition;
    private ImageView background, conditionIcon;
    private int count = 0;
    private static final String API_KEY = "&appid=" + "23a88ebdb1f682b7385e1cc56dff2f21";
    public static final String URL = "https://api.openweathermap.org/data/2.5/weather?q=";
    public static final String datePattern = "EEEE=dd-MM-yyyy";


    //public static final String FORCAST_URL = "https://api.openweathermap.org/data/2.5/forecast/hourly?q=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        getWeatherFromServer("Las Vegas");

    }

    private void bindViews() {

        city = findViewById(R.id.city);
        fab = findViewById(R.id.fab);
        cityTV = findViewById(R.id.city_name);
        temp = findViewById(R.id.temp);
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);
        wind = findViewById(R.id.wind);
        date = findViewById(R.id.date);
        condition = findViewById(R.id.condition);
        background = findViewById(R.id.background);
        conditionIcon = findViewById(R.id.conditionIcon);


        city.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                i2 = city.getText().toString().trim().length();
               // fab.setImageResource(i2 == 0 ? R.drawable.ref : R.drawable.ref);
                count = i2;

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            if (count == 0) {
                Toast.makeText(this, "No city entered", Toast.LENGTH_SHORT).show();
            } else {
                cityName = city.getText().toString();
                city.setText("");
                getWeatherFromServer(cityName);
            }

        }

    }

    private void getWeatherFromServer(String cityName) {
        String url = URL + cityName + API_KEY;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                // parseResponse(responseString);
                parseResponseByGSON(responseString);
                //getWeatherWithForecast(responseString);
            }
        });


    }


    private void parseResponseByGSON(String responseString) {
        Gson gson = new Gson();
        WeatherDataModel initModel = gson.fromJson(responseString, WeatherDataModel.class);

        double tempKelvin = initModel.getMain().getTemp();
        int cel = getCelcius(tempKelvin);
        temp.setText(cel + "" + (char) 0x00B0 + "C");
        cityTV.setText(initModel.getName());
        sunrise.setText(getFormattedTime(initModel.getSys().getSunrise()));
        sunset.setText(getFormattedTime(initModel.getSys().getSunset()));
        wind.setText(initModel.getWind().getSpeed() + " m/s");


        String conditionStr = initModel.getWeather().get(0).getMain();
        condition.setText(conditionStr);
        setIcon(conditionStr);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        String formattedDate = sdf.format(calendar.getTime());
        date.setText(formattedDate);
    }

    private void setIcon(String conditionStr) {
        if (conditionStr.toLowerCase().contains("sun")) {
            conditionIcon.setImageResource(R.drawable.sunny);
            background.setImageResource(R.drawable.sunny_background);
            date.setTextColor(Color.WHITE);

        } else if (conditionStr.toLowerCase().contains("cloud")) {
            conditionIcon.setImageResource(R.drawable.cloud);
            background.setImageResource(R.drawable.cloudy_background);
            date.setTextColor(Color.BLACK);

        } else if (conditionStr.toLowerCase().contains("rain") || conditionStr.toLowerCase().contains("drizzle")) {
            conditionIcon.setImageResource(R.drawable.rain);
            background.setImageResource(R.drawable.cloudy_background);
            date.setTextColor(Color.BLACK);


        } else if (conditionStr.toLowerCase().contains("mist") || conditionStr.toLowerCase().contains("fog") || conditionStr.toLowerCase().contains("dust")) {
            conditionIcon.setImageResource(R.drawable.mist);
            background.setImageResource(R.drawable.night_background);
            temp.setTextColor(Color.WHITE);
            date.setTextColor(Color.WHITE);

        } else if (conditionStr.toLowerCase().contains("clear")) {
            conditionIcon.setImageResource(R.drawable.clear);
            background.setImageResource(R.drawable.sunny_background);
            date.setTextColor(Color.WHITE);

        } else if (conditionStr.toLowerCase().contains("snow")) {
            conditionIcon.setImageResource(R.drawable.snow);
            background.setImageResource(R.drawable.cloudy_background);
            date.setTextColor(Color.BLACK);


        }
    }


    private int getCelcius(double tempKelvin) {
        return (int) (tempKelvin - 273.15);
    }

    private String getFormattedTime(int unixSeconds) {
        Date date = new Date(unixSeconds * 1000L); // convert seconds to milliseconds
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // the format of your date
        return dateFormat.format(date);
    }


    /*private void parseResponse(String responseString) {
        try {
            JSONObject allObj = new JSONObject(responseString);
            String weatherStr = allObj.getString("weather");
            JSONArray weatherJsonArray = new JSONArray(weatherStr);

            List wList = new ArrayList();
            for (int i = 0; i < weatherJsonArray.length(); i++) {
                JSONObject obj = weatherJsonArray.getJSONObject(i);
                String id = obj.getString("id");
                String main = obj.getString("main");
                String description = obj.getString("description");
                String icon = obj.getString("icon");

                wList.add(id);
                wList.add(main);
                wList.add(description);
                wList.add(icon);

            }

            String main = allObj.getString("main");
            JSONObject mainObj = new JSONObject(main);
            String temp = mainObj.getString("temp");

            double tempInKelvin = Double.valueOf(temp);




        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/


      /*  private void getWeatherWithForecast(String cityName) {
        String url = FORCAST_URL + cityName + ",DE" + API_KEY;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                ParseResponseWithForcast(responseString);
            }
        });
    }

    private void ParseResponseWithForcast(String responseString) {

        Gson gson = new Gson();
        FWeatherDataModel initModel = gson.fromJson(responseString, FWeatherDataModel.class);
        List times = new ArrayList();
        for (int i=0; i<initModel.getList().size(); i++){
            int time = initModel.getList().get(i).getDt();
            times.add(time);
        }
        Toast.makeText(this, "ParseResponseWithForcast: " + times , Toast.LENGTH_SHORT).show();

        Log.d("timesss", "ParseResponseWithForcast: " + times);


    }*/
}
