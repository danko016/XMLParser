package com.example.dev.xmlparser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView list;
    private Spinner spinner;

    String rss1 = "Sky News";
    String rss2 = "New York Times";

    String urlRss1 = "http://feeds.skynews.com/feeds/rss/technology.xml";
    String urlRss2 = "http://rss.nytimes.com/services/xml/rss/nyt/Europe.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (ListView) findViewById(R.id.LVListView);
        spinner = (Spinner) findViewById(R.id.Spinner);

        //set spinner adapter
        final String[] items = new String[]{"Choose RSS", rss1, rss2};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getSelectedItem().toString();
                if (selected.equals(rss1)) {
                    Log.d("tag", "load rss1");
                    Parse parse1 = new Parse(getApplicationContext(), urlRss1);
                    parse1.execute("");

                } else if (selected.equals(rss2)) {
                    Log.d("tag", "load rss2");
                    Parse parse1 = new Parse(getApplicationContext(), urlRss2);
                    parse1.execute("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    //inner class for downloading xml async
    class Parse extends AsyncTask<String, String, String> {

        private Context context;

        Parse(Context context, String rss) {
            this.http_rss = rss;
            this.context = context;
        }

        private String http_rss = "";

        private ArrayList<String> title;
        private ArrayList<String> url;
        private ArrayList<String> date;
        private ArrayList<String> description;
        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            Log.d("tag", "preExec");

            //loading while fetching data
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getResources().getText(R.string.loading));
            progressDialog.setCancelable(false);
            Log.d("tag", "preExec1");
            progressDialog.show();
            Log.d("tag", "preExec2");

            title = new ArrayList<>();
            url = new ArrayList<>();
            description = new ArrayList<>();
            date = new ArrayList<>();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                //Jsoup parsing XML
                Document document = Jsoup.connect(http_rss)
                        .userAgent("")
                        .timeout(9999 * 9999).get();

                Elements elements = document.getElementsByTag("item");

                //fetching data by xml element - depend on rss.xml website
                for (Element element : elements) {
                    String title_parsed = element.getElementsByTag("title").first().text();
                    String url_parsed = element.getElementsByTag("guid").first().text();
                    String date_parsed = element.getElementsByTag("pubDate").first().text();
                    String description_parsed = element.getElementsByTag("description").first().text();

                    title.add(title_parsed);
                    url.add(url_parsed);
                    date.add(date_parsed);
                    description.add(description_parsed);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (title.size() == 0) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }

            //setting adapter for each item in listView
            String[] list_title = title.toArray(new String[title.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.textview_item, list_title);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    //Open dialog in webView to choose to open site from browser
                    String article = date.get(position) + "<br><br>" + description.get(position);
                    AlertDialog.Builder details = new AlertDialog.Builder(MainActivity.this);
                    details.setTitle(title.get(position));

                    WebView webView = new WebView(context);
                    webView.loadData(article, "text/html; charset=UTF-8", "UTF-8");

                    details.setView(webView);
                    details.setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    details.setPositiveButton(getResources().getText(R.string.open), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url.get(position)));
                            startActivity(intent);
                        }
                    });
                    details.show();

                }
            });
        }
    }
}
