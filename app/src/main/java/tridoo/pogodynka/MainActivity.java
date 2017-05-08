package tridoo.pogodynka;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends Activity implements LocationListener {
    private TextView tMiasto;
    private TextView tWspolrzedne;
    private TextView tCisnienie;
    private TextView tWilgotnosc;
    private TextView tTemperatura;
    private TextView tWiatr;
    private TextView tDeszcz;
    private TextView tOpis;


    private double szerokosc;
    private double dlugosc;
    private String miasto=null;
    private static final String URL="http://api.openweathermap.org/data/2.5/weather";
    private static final String KEY="790929e6ef22001b70cdb4022712d1b8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tMiasto = (TextView) findViewById(R.id.txt_miasto);
        tWspolrzedne= (TextView) findViewById(R.id.txt_wspolrzedne);
        tCisnienie =(TextView) findViewById(R.id.txt_cisnienie);
        tWilgotnosc= (TextView) findViewById(R.id.txt_wilgotnosc);
        tTemperatura= (TextView) findViewById(R.id.txt_temp);
        tWiatr= (TextView) findViewById(R.id.txt_wiatr);
        tDeszcz= (TextView) findViewById(R.id.txt_deszcz);
        tOpis= (TextView) findViewById(R.id.txt_opis);

        Button btnPobierz = (Button) this.findViewById(R.id.btn);
        btnPobierz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (miasto == null || szerokosc == 0 && dlugosc == 0) {
                    Random randomGenerator = new Random();
                    szerokosc=randomGenerator.nextDouble()*100;
                    dlugosc=randomGenerator.nextDouble()*100;
                }
                pobierzDane(szerokosc,dlugosc);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        szerokosc= location.getLatitude();
        dlugosc = location.getLongitude();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }

    @Override
    public void onProviderEnabled(String provider) {    }

    @Override
    public void onProviderDisabled(String provider) {    }


    private void pobierzDane(double szerokosc, double dlugosc) {
        String pWspolrzedne="?lat="+szerokosc+"&lon="+dlugosc ;
        String pKey="&appid="+KEY;
        String pUrl = URL + pWspolrzedne + pKey;
        TestAsyncTask testAsyncTask = new TestAsyncTask(MainActivity.this, pUrl);
        testAsyncTask.execute();
    }


    private class TestAsyncTask extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private String mUrl;

        public TestAsyncTask(Context context, String url) {
            mContext = context;
            mUrl = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return getJSON(mUrl);
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            wyswietlDane(strings);
        }


        public String getJSON(String url) {
            HttpURLConnection c = null;
            try {
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.connect();
                int status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        return sb.toString();
                }

            } catch (Exception ex) {
                return ex.toString();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        //disconnect error
                    }
                }
            }
            return null;
        }

        private void wyswietlDane(String pString) {
            try {
                JSONObject reader = new JSONObject(pString);
                String pWspolrzedne = reader.getString("coord");
                String pPogoda = reader.getString("weather");

                //JSONArray pJSONArray=new JSONArray(pPogoda);//.getString("description");
                String pOpis=new JSONArray(pPogoda).getJSONObject(0).getString("main");
                pOpis+=" "+new JSONArray(pPogoda).getJSONObject(0).getString("description");

                String pMain = reader.getString("main");
                String pWiatr = reader.getString("wind");
                //String pChmury = reader.getString("clouds");
                String pMiasto = reader.getString("name");
                String pDeszcz=null;

                int pTemperatura=new JSONObject(pMain).getInt("temp")-273;
                int pCisnienie =new JSONObject(pMain).getInt("pressure");
                int pwilgotnosc=new JSONObject(pMain).getInt("humidity");

                try {
                    pDeszcz =reader.getString("rain");
                }
                catch (JSONException e){
                }

                miasto=pMiasto;
                tMiasto.setText("Miasto: "+pMiasto);
                tWspolrzedne.setText("współrzędne: "+pWspolrzedne);
                tCisnienie.setText("Cisnienie: "+pCisnienie);
                tWilgotnosc.setText("Wilgotnosc: "+pwilgotnosc);
                tTemperatura.setText("temperatura: "+ pTemperatura);
                tWiatr.setText("Wiatr:" + pWiatr);
                tDeszcz.setText(pDeszcz);
                tOpis.setText("Opis: "+pOpis);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}
